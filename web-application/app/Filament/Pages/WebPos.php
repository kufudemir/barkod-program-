<?php

namespace App\Filament\Pages;

use App\Models\Company;
use App\Models\WebSale;
use App\Services\WebMoneyService;
use App\Services\WebPosService;
use BackedEnum;
use Filament\Notifications\Notification;
use Filament\Pages\Page;
use Illuminate\Support\Facades\Auth;
use RuntimeException;
use UnitEnum;

class WebPos extends Page
{
    protected static string|BackedEnum|null $navigationIcon = 'heroicon-o-shopping-cart';
    protected static ?string $navigationLabel = 'Web POS';
    protected static string|UnitEnum|null $navigationGroup = 'Web POS';
    protected static ?int $navigationSort = 10;
    protected string $view = 'filament.pages.web-pos';

    public static function shouldRegisterNavigation(): bool
    {
        return false;
    }

    public ?int $selectedCompanyId = null;
    public string $barcodeInput = '';
    public array $cartItems = [];
    public array $customPriceInputs = [];
    public array $percentInputs = [];
    public array $fixedDiscountInputs = [];
    public array $companies = [];
    public array $recentSales = [];

    public function mount(): void
    {
        $this->loadCompanies();

        if ($this->selectedCompanyId === null && $this->companies !== []) {
            $this->selectedCompanyId = (int) $this->companies[0]['id'];
        }

        $storedCompanyId = session()->get('web_pos.selected_company_id');
        if ($storedCompanyId && collect($this->companies)->contains(fn (array $company): bool => (int) $company['id'] === (int) $storedCompanyId)) {
            $this->selectedCompanyId = (int) $storedCompanyId;
        }

        $this->loadCompanyState();
    }

    public function getTitle(): string
    {
        return 'Web POS';
    }

    public function getHeading(): string
    {
        return 'Web POS';
    }

    public function updatedSelectedCompanyId(): void
    {
        if ($this->selectedCompanyId !== null) {
            session()->put('web_pos.selected_company_id', $this->selectedCompanyId);
        }

        $this->loadCompanyState();
    }

    public function scanBarcode(WebPosService $webPosService): void
    {
        if (! $this->selectedCompanyId) {
            $this->danger('Önce firma seçin.');
            return;
        }

        $barcode = trim($this->barcodeInput);
        if ($barcode === '') {
            $this->danger('Önce barkod girin.');
            return;
        }

        try {
            $this->cartItems = $webPosService->addBarcode($this->selectedCompanyId, $this->cartItems, $barcode);
            $this->barcodeInput = '';
            $this->syncCartState();
            $this->success('Ürün sepete eklendi.');
        } catch (RuntimeException $exception) {
            $this->danger($exception->getMessage());
        }
    }

    public function incrementQuantity(string $barcode, WebPosService $webPosService): void
    {
        $this->runCartMutation(fn () => $webPosService->increment($this->cartItems, $barcode));
    }

    public function decrementQuantity(string $barcode, WebPosService $webPosService): void
    {
        $this->runCartMutation(fn () => $webPosService->decrement($this->cartItems, $barcode));
    }

    public function removeItem(string $barcode, WebPosService $webPosService): void
    {
        $this->runCartMutation(fn () => $webPosService->remove($this->cartItems, $barcode), 'Ürün sepetten çıkarıldı.');
    }

    public function applyCustomPrice(string $barcode, WebPosService $webPosService, WebMoneyService $moneyService): void
    {
        $value = $this->customPriceInputs[$barcode] ?? '';
        $priceKurus = $moneyService->parseTlInputToKurus((string) $value);
        if ($priceKurus === null || $priceKurus <= 0) {
            $this->danger('Geçerli satış fiyatı girin.');
            return;
        }

        $this->runCartMutation(fn () => $webPosService->setCustomPrice($this->cartItems, $barcode, $priceKurus), 'Özel fiyat uygulandı.');
    }

    public function applyPercentDiscount(string $barcode, WebPosService $webPosService): void
    {
        $value = str_replace(',', '.', trim((string) ($this->percentInputs[$barcode] ?? '')));
        if (! is_numeric($value) || (float) $value <= 0) {
            $this->danger('Geçerli indirim yüzdesi girin.');
            return;
        }

        $this->runCartMutation(fn () => $webPosService->applyPercentDiscount($this->cartItems, $barcode, (float) $value), 'Yüzde indirimi uygulandı.');
    }

    public function applyFixedDiscount(string $barcode, WebPosService $webPosService, WebMoneyService $moneyService): void
    {
        $value = $this->fixedDiscountInputs[$barcode] ?? '';
        $discountKurus = $moneyService->parseTlInputToKurus((string) $value);
        if ($discountKurus === null || $discountKurus <= 0) {
            $this->danger('Geçerli TL indirimi girin.');
            return;
        }

        $this->runCartMutation(fn () => $webPosService->applyFixedDiscount($this->cartItems, $barcode, $discountKurus), 'TL indirimi uygulandı.');
    }

    public function resetItemPrice(string $barcode, WebPosService $webPosService): void
    {
        $this->runCartMutation(fn () => $webPosService->resetPrice($this->cartItems, $barcode), 'Liste fiyatına dönüldü.');
    }

    public function completeSale(WebPosService $webPosService): void
    {
        if (! $this->selectedCompanyId) {
            $this->danger('Önce firma seçin.');
            return;
        }

        $company = Company::query()->find($this->selectedCompanyId);
        if (! $company) {
            $this->danger('Seçili firma bulunamadı.');
            return;
        }

        try {
            $sale = $webPosService->completeSale($company, $this->cartItems, Auth::id());
            $this->cartItems = [];
            $this->customPriceInputs = [];
            $this->percentInputs = [];
            $this->fixedDiscountInputs = [];
            $this->syncCartState();
            $this->loadRecentSales();
            $this->success('Satış tamamlandı. Satış no: #' . $sale->id);
        } catch (RuntimeException $exception) {
            $this->danger($exception->getMessage());
        }
    }

    public function getCartSummaryProperty(): array
    {
        return app(WebPosService::class)->summarize($this->cartItems);
    }

    public function formatMoney(int $kurus): string
    {
        return app(WebMoneyService::class)->formatKurus($kurus);
    }

    private function loadCompanies(): void
    {
        $this->companies = Company::query()
            ->where('status', 'active')
            ->orderBy('name')
            ->get(['id', 'name', 'company_code'])
            ->map(fn (Company $company): array => [
                'id' => $company->id,
                'name' => $company->name,
                'companyCode' => $company->company_code,
            ])
            ->all();
    }

    private function loadCompanyState(): void
    {
        $this->cartItems = $this->loadCartFromSession();
        $this->loadRecentSales();
        $this->syncInputMaps();
    }

    private function loadCartFromSession(): array
    {
        if (! $this->selectedCompanyId) {
            return [];
        }

        return session()->get($this->cartSessionKey(), []);
    }

    private function syncCartState(): void
    {
        if ($this->selectedCompanyId) {
            session()->put($this->cartSessionKey(), $this->cartItems);
        }

        $this->syncInputMaps();
    }

    private function syncInputMaps(): void
    {
        $this->customPriceInputs = [];
        $this->percentInputs = [];
        $this->fixedDiscountInputs = [];

        foreach ($this->cartItems as $item) {
            $barcode = (string) $item['barcode'];
            $this->customPriceInputs[$barcode] = app(WebMoneyService::class)->formatKurus((int) $item['salePriceKurus']);
            $this->percentInputs[$barcode] = '';
            $this->fixedDiscountInputs[$barcode] = '';
        }
    }

    private function loadRecentSales(): void
    {
        if (! $this->selectedCompanyId) {
            $this->recentSales = [];
            return;
        }

        $moneyService = app(WebMoneyService::class);

        $this->recentSales = WebSale::query()
            ->with(['items'])
            ->where('company_id', $this->selectedCompanyId)
            ->latest('completed_at')
            ->limit(8)
            ->get()
            ->map(function (WebSale $sale) use ($moneyService): array {
                return [
                    'id' => $sale->id,
                    'completedAt' => optional($sale->completed_at)->diffForHumans(),
                    'items' => $sale->total_items,
                    'total' => $moneyService->formatKurus((int) $sale->total_amount_kurus),
                    'profit' => $moneyService->formatKurus((int) $sale->profit_kurus),
                    'lines' => $sale->items->map(fn ($item): string => sprintf('%s x%d', $item->product_name_snapshot, $item->quantity))->all(),
                ];
            })
            ->all();
    }

    private function cartSessionKey(): string
    {
        return 'web_pos.cart.' . $this->selectedCompanyId;
    }

    private function runCartMutation(callable $callback, string $successMessage = 'Sepet güncellendi.'): void
    {
        try {
            $this->cartItems = $callback();
            $this->syncCartState();
            $this->success($successMessage);
        } catch (RuntimeException $exception) {
            $this->danger($exception->getMessage());
        }
    }

    private function success(string $message): void
    {
        Notification::make()->title($message)->success()->send();
    }

    private function danger(string $message): void
    {
        Notification::make()->title($message)->danger()->send();
    }
}
