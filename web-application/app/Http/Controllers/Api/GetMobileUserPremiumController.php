<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\MobileUserTokenManager;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class GetMobileUserPremiumController extends Controller
{
    public function __invoke(Request $request, MobileUserTokenManager $tokenManager): JsonResponse
    {
        $mobileUser = $tokenManager->findActiveUserByToken($request->bearerToken());

        if ($mobileUser === null) {
            return response()->json(['message' => 'Gecersiz oturum'], 401);
        }

        $canonicalTier = match (strtoupper((string) $mobileUser->premium_tier)) {
            'PRO' => 'SILVER',
            'SILVER', 'GOLD', 'FREE' => strtoupper((string) $mobileUser->premium_tier),
            default => 'FREE',
        };

        // Legacy tier keeps old Android clients on FREE/PRO without parser failures.
        $legacyTier = $canonicalTier === 'FREE' ? 'FREE' : 'PRO';

        return response()->json([
            'premium' => [
                'tier' => $legacyTier,
                'canonicalTier' => $canonicalTier,
                'source' => $mobileUser->premium_source,
                'activatedAt' => $mobileUser->premium_activated_at?->valueOf(),
                'expiresAt' => $mobileUser->premium_expires_at?->valueOf(),
                'licenseCodeMasked' => $mobileUser->premium_license_mask,
            ],
        ]);
    }
}
