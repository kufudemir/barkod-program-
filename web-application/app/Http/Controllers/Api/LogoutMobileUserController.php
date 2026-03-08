<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\MobileUserTokenManager;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class LogoutMobileUserController extends Controller
{
    public function __invoke(Request $request, MobileUserTokenManager $tokenManager): JsonResponse
    {
        $tokenManager->revokeCurrent($request->bearerToken());

        return response()->json([
            'message' => 'Oturum kapatildi',
        ]);
    }
}
