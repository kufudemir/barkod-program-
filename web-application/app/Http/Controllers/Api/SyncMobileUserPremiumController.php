<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\MobileUserTokenManager;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Carbon;

class SyncMobileUserPremiumController extends Controller
{
    public function __invoke(Request $request, MobileUserTokenManager $tokenManager): JsonResponse
    {
        $mobileUser = $tokenManager->findActiveUserByToken($request->bearerToken());

        if ($mobileUser === null) {
            return response()->json(['message' => 'Geçersiz oturum'], 401);
        }

        $payload = $request->validate([
            'tier' => ['required', 'string', 'in:FREE,PRO,SILVER,GOLD'],
            'source' => ['required', 'string', 'in:NONE,TRIAL,LICENSE_CODE,GOOGLE_PLAY'],
            'activatedAt' => ['nullable', 'integer'],
            'expiresAt' => ['nullable', 'integer'],
            'licenseCodeMasked' => ['nullable', 'string', 'max:255'],
        ]);

        $incomingTier = strtoupper((string) $payload['tier']);
        $normalizedTier = match ($incomingTier) {
            'PRO' => 'SILVER',
            'FREE', 'SILVER', 'GOLD' => $incomingTier,
            default => 'FREE',
        };

        $mobileUser->forceFill([
            'premium_tier' => $normalizedTier,
            'premium_source' => $payload['source'],
            'premium_activated_at' => isset($payload['activatedAt']) ? Carbon::createFromTimestampUTC((int) floor($payload['activatedAt'] / 1000))->setTimezone(config('app.timezone')) : null,
            'premium_expires_at' => isset($payload['expiresAt']) ? Carbon::createFromTimestampUTC((int) floor($payload['expiresAt'] / 1000))->setTimezone(config('app.timezone')) : null,
            'premium_license_mask' => $payload['licenseCodeMasked'] ?? null,
        ])->save();

        return response()->json([
            'message' => 'Premium durumu senkronize edildi',
        ]);
    }
}

