<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\MobileUser;
use App\Models\MobileUserPasswordReset;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Carbon;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Mail;
use Throwable;

class RequestMobileUserPasswordResetController extends Controller
{
    public function __invoke(Request $request): JsonResponse
    {
        $payload = $request->validate([
            'email' => ['required', 'email'],
        ]);

        /** @var MobileUser|null $mobileUser */
        $mobileUser = MobileUser::query()
            ->where('email', $payload['email'])
            ->where('status', 'active')
            ->first();

        if ($mobileUser === null) {
            return response()->json([
                'message' => 'Eğer bu e-posta ile kayıtlı bir hesap varsa şifre sıfırlama kodu gönderildi.',
            ]);
        }

        MobileUserPasswordReset::query()
            ->where('mobile_user_id', $mobileUser->id)
            ->delete();

        $code = (string) random_int(100000, 999999);
        $now = now();
        $expiresAt = $now->copy()->addMinutes(15);

        MobileUserPasswordReset::query()->create([
            'mobile_user_id' => $mobileUser->id,
            'email' => $mobileUser->email,
            'code_hash' => Hash::make($code),
            'expires_at' => $expiresAt,
            'requested_at' => $now,
        ]);

        try {
            Mail::raw(
                "Merhaba {$mobileUser->name},\n\nŞifre sıfırlama kodunuz: {$code}\nBu kod 15 dakika boyunca geçerlidir.\n\nEğer bu işlemi siz yapmadıysanız bu mesajı dikkate almayın.",
                function ($message) use ($mobileUser): void {
                    $message->to($mobileUser->email, $mobileUser->name)
                        ->subject('barkod.space şifre sıfırlama kodu');
                }
            );
        } catch (Throwable $throwable) {
            Log::error('Mobil şifre sıfırlama maili gönderilemedi.', [
                'mobile_user_id' => $mobileUser->id,
                'email' => $mobileUser->email,
                'error' => $throwable->getMessage(),
            ]);

            return response()->json([
                'message' => 'Mail gönderilemedi. Sunucu mail ayarlarınızı kontrol edin.',
            ], 500);
        }

        return response()->json([
            'message' => 'Eğer bu e-posta ile kayıtlı bir hesap varsa şifre sıfırlama kodu gönderildi.',
            'expiresAt' => Carbon::parse($expiresAt)->valueOf(),
        ]);
    }
}

