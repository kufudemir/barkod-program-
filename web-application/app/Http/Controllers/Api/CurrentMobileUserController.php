<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\MobileUserTokenManager;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class CurrentMobileUserController extends Controller
{
    public function __invoke(Request $request, MobileUserTokenManager $tokenManager): JsonResponse
    {
        $mobileUser = $tokenManager->findActiveUserByToken($request->bearerToken());

        if ($mobileUser === null) {
            return response()->json(['message' => 'Geçersiz oturum'], 401);
        }

        return response()->json([
            'user' => [
                'id' => $mobileUser->id,
                'name' => $mobileUser->name,
                'email' => $mobileUser->email,
                'consentVersion' => $mobileUser->consent_version,
                'consentAcceptedAt' => $mobileUser->consent_accepted_at?->valueOf(),
            ],
        ]);
    }
}
