<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Company;
use App\Models\Device;
use App\Models\DeviceCompanyHistory;
use App\Services\DeviceTokenManager;
use App\Services\InactiveCompanyCleanupService;
use App\Services\MobileUserTokenManager;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Str;

class DeviceActivationController extends Controller
{
    public function __invoke(
        Request $request,
        DeviceTokenManager $tokenManager,
        MobileUserTokenManager $mobileUserTokenManager,
        InactiveCompanyCleanupService $cleanupService
    ): JsonResponse {
        $cleanupService->runIfEnabled();

        $payload = $request->validate([
            'companyCode' => ['nullable', 'string', 'max:64', 'required_without:companyName'],
            'companyName' => ['nullable', 'string', 'max:255', 'required_without:companyCode'],
            'forceNewCompany' => ['nullable', 'boolean'],
            'deviceUid' => ['required', 'string', 'max:128'],
            'deviceName' => ['required', 'string', 'max:255'],
            'appVersion' => ['nullable', 'string', 'max:32'],
        ]);

        $mobileUser = $mobileUserTokenManager->findActiveUserByToken($request->bearerToken());
        $companyCode = trim((string) ($payload['companyCode'] ?? ''));
        $companyName = trim((string) ($payload['companyName'] ?? ''));
        $forceNewCompany = (bool) ($payload['forceNewCompany'] ?? false);
        $device = Device::query()
            ->with('company')
            ->where('device_uid', $payload['deviceUid'])
            ->first();

        if ($companyCode !== '') {
            $company = Company::query()
                ->where('company_code', $companyCode)
                ->first();

            if ($company === null) {
                return response()->json(['message' => 'Firma kodu bulunamadı'], 422);
            }
        } else {
            $normalizedCompanyName = Str::squish($companyName);
            $existingDeviceCompany = $device?->company;

            if ($existingDeviceCompany !== null) {
                $matchesExistingCompany = mb_strtolower($existingDeviceCompany->name, 'UTF-8') === mb_strtolower($normalizedCompanyName, 'UTF-8');
                if (! $matchesExistingCompany && ! $forceNewCompany) {
                    return response()->json([
                        'message' => "Bu cihaz daha önce {$existingDeviceCompany->name} firması ile aktive edildi",
                        'errorCode' => 'DEVICE_ALREADY_BOUND',
                        'existingCompanyName' => $existingDeviceCompany->name,
                        'existingCompanyCode' => $existingDeviceCompany->company_code,
                    ], 409);
                }

                if ($matchesExistingCompany) {
                    $company = $existingDeviceCompany;
                    if ($mobileUser !== null && $company->owner_mobile_user_id === null) {
                        $company->forceFill([
                            'owner_mobile_user_id' => $mobileUser->id,
                            'created_via' => 'registered_user',
                        ])->save();
                    }
                } else {
                    $company = Company::query()->create([
                        'name' => $normalizedCompanyName,
                        'owner_mobile_user_id' => $mobileUser?->id,
                        'created_via' => $mobileUser !== null ? 'registered_user' : 'guest',
                        'status' => 'active',
                    ]);
                }
            } else {
                $company = null;

                if ($mobileUser !== null) {
                    $company = Company::query()
                        ->where('owner_mobile_user_id', $mobileUser->id)
                        ->whereRaw('LOWER(name) = ?', [mb_strtolower($normalizedCompanyName, 'UTF-8')])
                        ->first();

                    if ($company === null) {
                        $company = Company::query()
                            ->whereNull('owner_mobile_user_id')
                            ->whereRaw('LOWER(name) = ?', [mb_strtolower($normalizedCompanyName, 'UTF-8')])
                            ->first();

                        if ($company !== null) {
                            $company->forceFill([
                                'owner_mobile_user_id' => $mobileUser->id,
                                'created_via' => 'registered_user',
                            ])->save();
                        }
                    }
                }

                $company ??= Company::query()->create([
                    'name' => $normalizedCompanyName,
                    'owner_mobile_user_id' => $mobileUser?->id,
                    'created_via' => $mobileUser !== null ? 'registered_user' : 'guest',
                    'status' => 'active',
                ]);
            }
        }

        if (! $company->isActive()) {
            return response()->json(['message' => 'Firma pasif durumda'], 403);
        }

        $activeOtherDeviceExists = Device::query()
            ->where('company_id', $company->id)
            ->where('is_active', true)
            ->where('device_uid', '!=', $payload['deviceUid'])
            ->exists();

        if ($activeOtherDeviceExists) {
            return response()->json(['message' => 'Bu firma için başka bir cihaz zaten aktif'], 409);
        }

        $device = $device ?? Device::query()->firstOrNew([
            'device_uid' => $payload['deviceUid'],
        ]);

        if ($device->exists && $device->company_id !== null && $device->company_id !== $company->id && ! $forceNewCompany) {
            return response()->json([
                'message' => "Bu cihaz daha önce {$device->company?->name} firması ile aktive edildi",
                'errorCode' => 'DEVICE_ALREADY_BOUND',
                'existingCompanyName' => $device->company?->name,
                'existingCompanyCode' => $device->company?->company_code,
            ], 409);
        }

        $device->fill([
            'company_id' => $company->id,
            'device_name' => $payload['deviceName'],
            'platform' => 'android',
            'is_active' => true,
            'last_seen_at' => now(),
        ])->save();

        $token = $tokenManager->issue($device->refresh());

        DeviceCompanyHistory::query()->updateOrCreate(
            [
                'device_uid' => $payload['deviceUid'],
                'company_id' => $company->id,
            ],
            [
                'first_seen_at' => now(),
                'last_seen_at' => now(),
                'activation_source' => $mobileUser !== null ? 'registered_user' : 'guest',
            ]
        );

        return response()->json([
            'companyId' => $company->id,
            'companyName' => $company->name,
            'companyCode' => $company->company_code,
            'createdVia' => $company->created_via,
            'deviceId' => $device->id,
            'activationToken' => $token,
            'activatedAt' => now()->valueOf(),
        ]);
    }
}
