<?php

namespace App\Services;

use App\Models\Device;
use Illuminate\Support\Str;

class DeviceTokenManager
{
    public function issue(Device $device): string
    {
        $token = Str::random(80);

        $device->forceFill([
            'activation_token_hash' => hash('sha256', $token),
        ])->save();

        return $token;
    }

    public function findActiveDeviceByToken(?string $token): ?Device
    {
        if (blank($token)) {
            return null;
        }

        return Device::query()
            ->with('company')
            ->where('activation_token_hash', hash('sha256', $token))
            ->where('is_active', true)
            ->first();
    }
}
