<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\MobileUser;
use App\Services\MobileUserTokenManager;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class LoginMobileUserController extends Controller
{
    public function __invoke(Request $request, MobileUserTokenManager $tokenManager): JsonResponse
    {
        $payload = $request->validate([
            'email' => ['required', 'email', 'max:255'],
            'password' => ['required', 'string', 'min:8'],
            'deviceUid' => ['nullable', 'string', 'max:128'],
            'deviceName' => ['nullable', 'string', 'max:255'],
        ]);

        $mobileUser = MobileUser::query()
            ->where('email', mb_strtolower(trim($payload['email']), 'UTF-8'))
            ->first();

        if ($mobileUser === null || ! $mobileUser->verifyPassword($payload['password'])) {
            return response()->json(['message' => 'E-posta veya şifre hatalı'], 422);
        }

        if (! $mobileUser->isActive()) {
            return response()->json(['message' => 'Kullanıcı pasif durumda'], 403);
        }

        $token = $tokenManager->issue(
            $mobileUser,
            $payload['deviceUid'] ?? null,
            $payload['deviceName'] ?? null
        );

        return response()->json([
            'user' => [
                'id' => $mobileUser->id,
                'name' => $mobileUser->name,
                'email' => $mobileUser->email,
            ],
            'accessToken' => $token,
        ]);
    }
}
