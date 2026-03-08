<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\LicenseRequestWorkflowService;
use App\Services\MobileUserTokenManager;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class CreateLicenseRequestController extends Controller
{
    public function __invoke(
        Request $request,
        MobileUserTokenManager $tokenManager,
        LicenseRequestWorkflowService $workflow,
    ): JsonResponse {
        $mobileUser = $tokenManager->findActiveUserByToken($request->bearerToken());

        $payload = $request->validate([
            'requesterName' => ['nullable', 'string', 'max:255'],
            'requesterEmail' => ['nullable', 'email', 'max:255'],
            'requesterPhone' => ['nullable', 'string', 'max:64'],
            'companyCode' => ['nullable', 'string', 'max:64'],
            'requestedPackageCode' => ['required', 'string', 'in:SILVER,GOLD,PRO'],
            'bankReferenceNote' => ['nullable', 'string', 'max:255'],
        ]);

        $requesterName = trim((string) ($payload['requesterName'] ?? ''));
        $requesterEmail = trim((string) ($payload['requesterEmail'] ?? ''));

        if ($mobileUser !== null) {
            if ($requesterName === '') {
                $requesterName = $mobileUser->name;
            }
            if ($requesterEmail === '') {
                $requesterEmail = $mobileUser->email;
            }
        }

        if ($requesterName === '' || $requesterEmail === '') {
            return response()->json([
                'message' => 'Talep oluşturmak için ad soyad ve e-posta gereklidir.',
            ], 422);
        }

        $licenseRequest = $workflow->createRequest(
            requesterName: $requesterName,
            requesterEmail: $requesterEmail,
            requesterPhone: $payload['requesterPhone'] ?? null,
            requestedPackageCode: $payload['requestedPackageCode'],
            bankReferenceNote: $payload['bankReferenceNote'] ?? null,
            companyCode: $payload['companyCode'] ?? null,
            mobileUser: $mobileUser,
        );

        return response()->json([
            'requestId' => $licenseRequest->id,
            'status' => $licenseRequest->status,
            'requestedPackageCode' => $licenseRequest->requested_package_code,
            'bankTransfer' => config('license.bank_transfer'),
            'createdAt' => $licenseRequest->created_at?->valueOf(),
        ], 201);
    }
}
