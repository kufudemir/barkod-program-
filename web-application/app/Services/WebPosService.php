<?php

namespace App\Services;

use App\Models\Company;
use App\Models\CompanyProductOffer;
use App\Models\WebSale;
use Illuminate\Support\Arr;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;
use RuntimeException;

class WebPosService
{
    public function __construct(private readonly WebMoneyService $moneyService)
    {
    }

    public function addBarcode(int $companyId, array $cartItems, string $barcode): array
    {
        $barcode = trim($barcode);

        $offer = CompanyProductOffer::query()
            ->with(['globalProduct'])
            ->where('company_id', $companyId)
            ->where('barcode', $barcode)
            ->where('is_active', true)
            ->first();

        if (! $offer) {
            throw new RuntimeException('Bu barkod için seçilen firmada aktif fiyat kaydı bulunamadı.');
        }

        $existingIndex = $this->findCartIndex($cartItems, $barcode);
        if ($existingIndex !== null) {
            $cartItems[$existingIndex]['quantity']++;
            return $this->recalculateCart($cartItems);
        }

        $cartItems[] = [
            'barcode' => $barcode,
            'productName' => $offer->globalProduct?->canonical_name ?? $barcode,
            'groupName' => $offer->group_name ?: $offer->globalProduct?->group_name,
            'quantity' => 1,
            'baseSalePriceKurus' => (int) $offer->sale_price_kurus,
            'salePriceKurus' => (int) $offer->sale_price_kurus,
            'costPriceKurus' => (int) $offer->cost_price_kurus,
        ];

        return $this->recalculateCart($cartItems);
    }

    public function increment(array $cartItems, string $barcode): array
    {
        return $this->mutate($cartItems, $barcode, function (array $item): array {
            $item['quantity']++;
            return $item;
        });
    }

    public function decrement(array $cartItems, string $barcode): array
    {
        return $this->mutate($cartItems, $barcode, function (array $item): ?array {
            $item['quantity']--;
            return $item['quantity'] > 0 ? $item : null;
        });
    }

    public function remove(array $cartItems, string $barcode): array
    {
        return $this->mutate($cartItems, $barcode, fn (): ?array => null);
    }

    public function setQuantity(array $cartItems, string $barcode, int $quantity): array
    {
        return $this->mutate($cartItems, $barcode, function (array $item) use ($quantity): ?array {
            return $quantity > 0 ? array_replace($item, ['quantity' => $quantity]) : null;
        });
    }

    public function setCustomPrice(array $cartItems, string $barcode, int $priceKurus): array
    {
        return $this->mutate($cartItems, $barcode, function (array $item) use ($priceKurus): array {
            $item['salePriceKurus'] = $this->moneyService->roundUpToWholeTl($priceKurus);
            return $item;
        });
    }

    public function applyPercentDiscount(array $cartItems, string $barcode, float $percent): array
    {
        return $this->mutate($cartItems, $barcode, function (array $item) use ($percent): array {
            $item['salePriceKurus'] = $this->moneyService->applyPercentDiscount((int) $item['salePriceKurus'], $percent);
            return $item;
        });
    }

    public function applyFixedDiscount(array $cartItems, string $barcode, int $discountKurus): array
    {
        return $this->mutate($cartItems, $barcode, function (array $item) use ($discountKurus): array {
            $item['salePriceKurus'] = $this->moneyService->applyFixedDiscount((int) $item['salePriceKurus'], $discountKurus);
            return $item;
        });
    }

    public function resetPrice(array $cartItems, string $barcode): array
    {
        return $this->mutate($cartItems, $barcode, function (array $item): array {
            $item['salePriceKurus'] = (int) $item['baseSalePriceKurus'];
            return $item;
        });
    }

    public function recalculateCart(array $cartItems): array
    {
        return array_values(array_map(function (array $item): array {
            $item['quantity'] = max(1, (int) ($item['quantity'] ?? 1));
            $item['baseSalePriceKurus'] = (int) ($item['baseSalePriceKurus'] ?? 0);
            $item['salePriceKurus'] = (int) ($item['salePriceKurus'] ?? $item['baseSalePriceKurus']);
            $item['costPriceKurus'] = (int) ($item['costPriceKurus'] ?? 0);
            $item['lineTotalKurus'] = $item['salePriceKurus'] * $item['quantity'];
            $item['lineProfitKurus'] = ($item['salePriceKurus'] - $item['costPriceKurus']) * $item['quantity'];
            $item['hasCustomPrice'] = $item['salePriceKurus'] !== $item['baseSalePriceKurus'];
            return $item;
        }, $cartItems));
    }

    public function summarize(array $cartItems): array
    {
        $recalculated = $this->recalculateCart($cartItems);

        return [
            'items' => array_sum(array_map(fn (array $item): int => (int) $item['quantity'], $recalculated)),
            'totalAmountKurus' => array_sum(array_map(fn (array $item): int => (int) $item['lineTotalKurus'], $recalculated)),
            'totalCostKurus' => array_sum(array_map(fn (array $item): int => (int) $item['costPriceKurus'] * (int) $item['quantity'], $recalculated)),
            'profitKurus' => array_sum(array_map(fn (array $item): int => (int) $item['lineProfitKurus'], $recalculated)),
        ];
    }

    public function completeSale(
        Company $company,
        array $cartItems,
        ?int $adminUserId = null,
        ?int $mobileUserId = null,
        ?int $branchId = null,
        ?int $registerId = null,
        ?int $posSessionId = null,
        string $paymentMethod = 'cash',
    ): WebSale
    {
        $cartItems = $this->recalculateCart($cartItems);

        if ($cartItems === []) {
            throw new RuntimeException('Satış tamamlamak için sepette ürün olmalıdır.');
        }

        $summary = $this->summarize($cartItems);

        return DB::transaction(function () use (
            $company,
            $cartItems,
            $summary,
            $adminUserId,
            $mobileUserId,
            $branchId,
            $registerId,
            $posSessionId,
            $paymentMethod,
        ): WebSale {
            $sale = WebSale::query()->create([
                'company_id' => $company->id,
                'branch_id' => $branchId,
                'register_id' => $registerId,
                'pos_session_id' => $posSessionId,
                'created_by_user_id' => $adminUserId,
                'created_by_mobile_user_id' => $mobileUserId,
                'total_items' => $summary['items'],
                'total_amount_kurus' => $summary['totalAmountKurus'],
                'total_cost_kurus' => $summary['totalCostKurus'],
                'profit_kurus' => $summary['profitKurus'],
                'completed_at' => now(),
            ]);

            foreach ($cartItems as $item) {
                $sale->items()->create([
                    'barcode' => Arr::get($item, 'barcode'),
                    'product_name_snapshot' => Arr::get($item, 'productName'),
                    'group_name_snapshot' => Arr::get($item, 'groupName'),
                    'quantity' => (int) Arr::get($item, 'quantity', 1),
                    'unit_sale_price_kurus_snapshot' => (int) Arr::get($item, 'salePriceKurus', 0),
                    'unit_cost_price_kurus_snapshot' => (int) Arr::get($item, 'costPriceKurus', 0),
                    'line_total_kurus' => (int) Arr::get($item, 'lineTotalKurus', 0),
                    'line_profit_kurus' => (int) Arr::get($item, 'lineProfitKurus', 0),
                ]);
            }

            if (Schema::hasTable('web_sale_payments')) {
                $sale->payments()->create([
                    'method' => $paymentMethod,
                    'amount_kurus' => (int) $summary['totalAmountKurus'],
                ]);
            }

            $relations = ['company', 'items'];
            if (Schema::hasTable('web_sale_payments')) {
                $relations[] = 'payments';
            }

            return $sale->load($relations);
        });
    }

    private function mutate(array $cartItems, string $barcode, callable $callback): array
    {
        $index = $this->findCartIndex($cartItems, $barcode);
        if ($index === null) {
            throw new RuntimeException('İlgili ürün sepette bulunamadı.');
        }

        $updated = $callback($cartItems[$index]);
        if ($updated === null) {
            unset($cartItems[$index]);
            return $this->recalculateCart(array_values($cartItems));
        }

        $cartItems[$index] = $updated;
        return $this->recalculateCart($cartItems);
    }

    private function findCartIndex(array $cartItems, string $barcode): ?int
    {
        foreach ($cartItems as $index => $item) {
            if (($item['barcode'] ?? null) === $barcode) {
                return $index;
            }
        }

        return null;
    }
}
