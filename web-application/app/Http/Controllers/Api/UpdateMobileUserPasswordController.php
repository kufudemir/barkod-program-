<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\MobileUserTokenManager;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class UpdateMobileUserPasswordController extends Controller
{
    public function __invoke(Request $request, MobileUserTokenManager $tokenManager): JsonResponse
    {
        $mobileUser = $tokenManager->findActiveUserByToken($request->bearerToken());

        if ($mobileUser === null) {
            return response()->json(['message' => 'Geçersiz oturum'], 401);
        }

        $payload = $request->validate([
            'currentPassword' => ['required', 'string', 'min:8'],
            'newPassword' => ['required', 'string', 'min:8'],
        ]);

        if (! $mobileUser->verifyPassword($payload['currentPassword'])) {
            return response()->json(['message' => 'Mevcut şifre hatalı'], 422);
        }

        $mobileUser->forceFill([
            'password' => $payload['newPassword'],
        ])->save();

        return response()->json([
            'message' => 'Şifre güncellendi',
        ]);
    }
}

