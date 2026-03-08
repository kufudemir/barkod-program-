<?php

namespace App\Services;

use App\Models\SaleSession;
use App\Models\SaleSessionItem;
use Illuminate\Support\Facades\DB;

class PosSaleSessionCartService
{
    /**
     * @return array<int, array<string, mixed>>
     */
    public function get(SaleSession $saleSession): array
    {
        return SaleSessionItem::query()
            ->where('sale_session_id', $saleSession->id)
            ->orderBy('id')
            ->get()
            ->map(fn (SaleSessionItem $item): array => [
                'barcode' => $item->barcode,
                'productName' => $item->product_name_snapshot,
                'groupName' => $item->group_name_snapshot,
                'quantity' => (int) $item->quantity,
                'baseSalePriceKurus' => (int) $item->base_sale_price_kurus,
                'salePriceKurus' => (int) $item->applied_sale_price_kurus,
                'costPriceKurus' => (int) $item->cost_price_kurus,
                'lineTotalKurus' => (int) $item->line_total_kurus,
                'lineProfitKurus' => (int) $item->line_profit_kurus,
            ])
            ->all();
    }

    /**
     * @param array<int, array<string, mixed>> $cartItems
     */
    public function replace(SaleSession $saleSession, array $cartItems): void
    {
        DB::transaction(function () use ($saleSession, $cartItems): void {
            $byBarcode = [];
            foreach ($cartItems as $item) {
                $barcode = trim((string) ($item['barcode'] ?? ''));
                if ($barcode === '') {
                    continue;
                }

                $byBarcode[$barcode] = $item;
            }

            if ($byBarcode === []) {
                SaleSessionItem::query()
                    ->where('sale_session_id', $saleSession->id)
                    ->delete();

                $saleSession->touch();

                return;
            }

            foreach ($byBarcode as $barcode => $item) {
                $base = (int) ($item['baseSalePriceKurus'] ?? 0);
                $applied = (int) ($item['salePriceKurus'] ?? $base);
                $mode = $this->resolvePricingMode($base, $applied);

                SaleSessionItem::query()->updateOrCreate(
                    [
                        'sale_session_id' => $saleSession->id,
                        'barcode' => $barcode,
                    ],
                    [
                        'product_name_snapshot' => (string) ($item['productName'] ?? $barcode),
                        'group_name_snapshot' => $item['groupName'] ?? null,
                        'quantity' => max(1, (int) ($item['quantity'] ?? 1)),
                        'base_sale_price_kurus' => $base,
                        'applied_sale_price_kurus' => $applied,
                        'cost_price_kurus' => (int) ($item['costPriceKurus'] ?? 0),
                        'pricing_mode' => $mode,
                        'pricing_meta_json' => null,
                        'line_total_kurus' => (int) ($item['lineTotalKurus'] ?? 0),
                        'line_profit_kurus' => (int) ($item['lineProfitKurus'] ?? 0),
                    ],
                );
            }

            SaleSessionItem::query()
                ->where('sale_session_id', $saleSession->id)
                ->whereNotIn('barcode', array_keys($byBarcode))
                ->delete();

            $saleSession->touch();
        });
    }

    public function clear(SaleSession $saleSession): void
    {
        SaleSessionItem::query()
            ->where('sale_session_id', $saleSession->id)
            ->delete();

        $saleSession->touch();
    }

    private function resolvePricingMode(int $basePriceKurus, int $appliedPriceKurus): string
    {
        if ($appliedPriceKurus === $basePriceKurus) {
            return 'list';
        }

        return $appliedPriceKurus < $basePriceKurus ? 'fixed_discount' : 'custom';
    }
}
