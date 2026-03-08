<?php

namespace App\Services;

class WebMoneyService
{
    public function parseTlInputToKurus(string $input): ?int
    {
        $normalized = $this->normalizeTlInput($input);

        if ($normalized === '') {
            return null;
        }

        if (! is_numeric($normalized)) {
            return null;
        }

        return (int) round(((float) $normalized) * 100, 0, PHP_ROUND_HALF_UP);
    }

    public function formatKurus(int $kurus): string
    {
        return number_format($kurus / 100, 2, ',', '.') . ' TL';
    }

    public function roundUpToWholeTl(int $priceKurus): int
    {
        $remainder = $priceKurus % 100;

        return $remainder === 0 ? $priceKurus : $priceKurus + (100 - $remainder);
    }

    public function applyPercentDiscount(int $priceKurus, float $percent): int
    {
        $percent = abs($percent);
        $discounted = (int) round($priceKurus * (1 - ($percent / 100)), 0, PHP_ROUND_HALF_UP);

        return $this->roundUpToWholeTl(max(1, $discounted));
    }

    public function applyFixedDiscount(int $priceKurus, int $discountKurus): int
    {
        return $this->roundUpToWholeTl(max(1, $priceKurus - max(0, $discountKurus)));
    }

    private function normalizeTlInput(string $input): string
    {
        $cleaned = trim($input);
        $cleaned = str_ireplace(['TL', '₺'], '', $cleaned);
        $cleaned = str_replace(["\xc2\xa0", ' '], '', $cleaned);

        if ($cleaned === '') {
            return '';
        }

        $numeric = preg_replace('/[^0-9,\.\-]/', '', $cleaned) ?? '';

        if ($numeric === '') {
            return '';
        }

        if (substr_count($numeric, '-') > 1 || (str_contains($numeric, '-') && ! str_starts_with($numeric, '-'))) {
            return '';
        }

        if (str_contains($numeric, ',') && str_contains($numeric, '.')) {
            return str_replace(',', '.', str_replace('.', '', $numeric));
        }

        if (substr_count($numeric, '.') > 1) {
            $lastDot = strrpos($numeric, '.');
            $rebuilt = '';
            foreach (str_split($numeric) as $index => $character) {
                if ($character !== '.' || $index === $lastDot) {
                    $rebuilt .= $character;
                }
            }
            $numeric = $rebuilt;
        }

        return str_replace(',', '.', $numeric);
    }
}