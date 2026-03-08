<?php

namespace App\Services;

use App\Models\MobileUser;
use App\Models\MobileUserAccessToken;
use Illuminate\Support\Str;

class MobileUserTokenManager
{
    public function issue(MobileUser $mobileUser, ?string $deviceUid = null, ?string $deviceName = null): string
    {
        $plainToken = Str::random(64);

        MobileUserAccessToken::query()->create([
            'mobile_user_id' => $mobileUser->id,
            'token_hash' => hash('sha256', $plainToken),
            'device_uid' => $deviceUid,
            'device_name' => $deviceName,
            'last_used_at' => now(),
        ]);

        $mobileUser->forceFill([
            'last_login_at' => now(),
        ])->save();

        return $plainToken;
    }

    public function findActiveUserByToken(?string $plainToken): ?MobileUser
    {
        if (blank($plainToken)) {
            return null;
        }

        $token = MobileUserAccessToken::query()
            ->with('mobileUser')
            ->where('token_hash', hash('sha256', $plainToken))
            ->first();

        if ($token === null || $token->mobileUser === null || ! $token->mobileUser->isActive()) {
            return null;
        }

        $token->forceFill([
            'last_used_at' => now(),
        ])->save();

        return $token->mobileUser;
    }

    public function revokeAllForUser(MobileUser $mobileUser): void
    {
        $mobileUser->accessTokens()->delete();
    }

    public function revokeCurrent(?string $plainToken): void
    {
        if (blank($plainToken)) {
            return;
        }

        MobileUserAccessToken::query()
            ->where('token_hash', hash('sha256', $plainToken))
            ->delete();
    }
}
