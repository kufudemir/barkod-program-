<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\MobileUser;
use App\Models\MobileUserPasswordReset;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;

class ConfirmMobileUserPasswordResetController extends Controller
{
    public function __invoke(Request $request): JsonResponse
    {
        $payload = $request->validate([
            'email' => ['required', 'email'],
            'code' => ['required', 'digits:6'],
            'newPassword' => ['required', 'string', 'min:8'],
        ]);

        /** @var MobileUser|null $mobileUser */
        $mobileUser = MobileUser::query()
            ->where('email', $payload['email'])
            ->where('status', 'active')
            ->first();

        if ($mobileUser === null) {
            return response()->json(['message' => 'Kod veya e-posta geçersiz'], 422);
        }

        /** @var MobileUserPasswordReset|null $passwordReset */
        $passwordReset = MobileUserPasswordReset::query()
            ->where('mobile_user_id', $mobileUser->id)
            ->where('email', $mobileUser->email)
            ->latest('requested_at')
            ->first();

        if ($passwordReset === null || ! $passwordReset->isUsable()) {
            return response()->json(['message' => 'Kodun süresi dolmuş veya kullanılmış'], 422);
        }

        if (! Hash::check($payload['code'], $passwordReset->code_hash)) {
            return response()->json(['message' => 'Kod veya e-posta geçersiz'], 422);
        }

        $mobileUser->forceFill([
            'password' => $payload['newPassword'],
        ])->save();

        $passwordReset->forceFill([
            'used_at' => now(),
        ])->save();

        return response()->json([
            'message' => 'Şifreniz güncellendi. Yeni şifrenizle giriş yapabilirsiniz.',
        ]);
    }
}
