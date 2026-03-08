<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\MobileUser;
use App\Services\MobileUserTokenManager;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class RegisterMobileUserController extends Controller
{
    public function __invoke(Request $request, MobileUserTokenManager $tokenManager): JsonResponse
    {
        $payload = $request->validate([
            'name' => ['required', 'string', 'max:255'],
            'email' => ['required', 'email', 'max:255', 'unique:mobile_users,email'],
            'password' => ['required', 'string', 'min:8'],
            'deviceUid' => ['nullable', 'string', 'max:128'],
            'deviceName' => ['nullable', 'string', 'max:255'],
        ]);

        $mobileUser = MobileUser::query()->create([
            'name' => trim($payload['name']),
            'email' => mb_strtolower(trim($payload['email']), 'UTF-8'),
            'password' => $payload['password'],
            'status' => 'active',
        ]);

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
        ], 201);
    }
}
