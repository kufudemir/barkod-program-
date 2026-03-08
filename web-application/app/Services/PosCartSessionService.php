<?php

namespace App\Services;

class PosCartSessionService
{
    public function get(int $companyId, ?int $posSessionId = null): array
    {
        if ($companyId <= 0) {
            return [];
        }

        $value = session()->get($this->sessionKey($companyId, $posSessionId), []);

        return is_array($value) ? $value : [];
    }

    public function put(int $companyId, array $cartItems, ?int $posSessionId = null): void
    {
        if ($companyId <= 0) {
            return;
        }

        session()->put($this->sessionKey($companyId, $posSessionId), $cartItems);
    }

    public function clear(int $companyId, ?int $posSessionId = null): void
    {
        if ($companyId <= 0) {
            return;
        }

        session()->forget($this->sessionKey($companyId, $posSessionId));
    }

    private function sessionKey(int $companyId, ?int $posSessionId = null): string
    {
        $suffix = $posSessionId !== null && $posSessionId > 0 ? 'session.' . $posSessionId : 'session.default';

        return 'pos.cart.' . $companyId . '.' . $suffix;
    }
}
