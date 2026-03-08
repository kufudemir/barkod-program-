<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\MobileUserTokenManager;
use Carbon\Carbon;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class SyncMobileUserConsentController extends Controller
{
    public function __invoke(Request $request, MobileUserTokenManager $tokenManager): JsonResponse
    {
        $mobileUser = $tokenManager->findActiveUserByToken($request->bearerToken());

        if ($mobileUser === null) {
            return response()->json(['message' => 'Geçersiz oturum'], 401);
        }

        $payload = $request->validate([
            'version' => ['required', 'string', 'max:32'],
            'acceptedAt' => ['required', 'integer', 'min:1'],
        ]);

        $acceptedAtMillis = (int) $payload['acceptedAt'];
        $acceptedAt = Carbon::createFromTimestampUTC((int) floor($acceptedAtMillis / 1000))
            ->setTimezone(config('app.timezone'));

        $mobileUser->forceFill([
            'consent_version' => trim($payload['version']),
            'consent_accepted_at' => $acceptedAt,
        ])->save();

        return response()->json([
            'message' => 'Onay bilgisi güncellendi',
        ]);
    }
}
