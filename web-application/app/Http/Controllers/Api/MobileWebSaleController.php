<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Company;
use App\Models\Device;
use App\Models\MobileUser;
use App\Models\PosSession;
use App\Models\ReceiptProfile;
use App\Models\SaleSession;
use App\Models\SyncEventDedup;
use App\Models\WebSale;
use App\Models\WebSaleItem;
use App\Services\MobileUserTokenManager;
use App\Services\PosSaleSessionCartService;
use App\Services\WebPosService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\URL;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;
use RuntimeException;

class MobileWebSaleController extends Controller
{
    public function active(
        Request $request,
        MobileUserTokenManager $tokenManager,
        PosSaleSessionCartService $cartService,
        WebPosService $webPosService,
    ): JsonResponse {
        try {
            $context = $this->resolveContext(
                request: $request,
                tokenManager: $tokenManager,
                requireActivePosSession: false,
            );
        } catch (RuntimeException $exception) {
            return $this->errorResponse($exception->getMessage(), 401);
        }

        $company = $context['company'];
        $posSession = $context['posSession'];

        if (! $posSession instanceof PosSession) {
            return response()->json([
                'ok' => true,
                'data' => [
                    'hasActiveSession' => false,
                    'message' => 'Aktif web POS oturumu bulunamadi.',
                ],
            ]);
        }

        $saleSession = $this->resolveOrCreateDeviceSaleSession(
            posSession: $posSession,
            deviceUid: $context['deviceUid'],
            deviceName: $context['deviceName'],
            mobileUserId: $context['mobileUser']->id,
        );

        $cartItems = $webPosService->recalculateCart($cartService->get($saleSession));
        $summary = $webPosService->summarize($cartItems);

        return response()->json([
            'ok' => true,
            'data' => $this->buildSessionPayload(
                company: $company,
                posSession: $posSession,
                saleSession: $saleSession,
                cartItems: $cartItems,
                summary: $summary,
            ),
        ]);
    }

    public function scan(
        Request $request,
        MobileUserTokenManager $tokenManager,
        PosSaleSessionCartService $cartService,
        WebPosService $webPosService,
    ): JsonResponse {
        $payload = $request->validate([
            'barcode' => ['required', 'string', 'max:64'],
        ]);

        return $this->mutateCart(
            request: $request,
            tokenManager: $tokenManager,
            cartService: $cartService,
            webPosService: $webPosService,
            successMessage: 'Urun sepete eklendi.',
            mutation: function (Company $company, array $cartItems) use ($webPosService, $payload): array {
                return $webPosService->addBarcode($company->id, $cartItems, (string) $payload['barcode']);
            },
        );
    }

    public function increment(
        Request $request,
        MobileUserTokenManager $tokenManager,
        PosSaleSessionCartService $cartService,
        WebPosService $webPosService,
    ): JsonResponse {
        $payload = $request->validate([
            'barcode' => ['required', 'string', 'max:64'],
        ]);

        return $this->mutateCart(
            request: $request,
            tokenManager: $tokenManager,
            cartService: $cartService,
            webPosService: $webPosService,
            successMessage: 'Adet arttirildi.',
            mutation: fn (Company $company, array $cartItems): array => $webPosService->increment($cartItems, (string) $payload['barcode']),
        );
    }

    public function decrement(
        Request $request,
        MobileUserTokenManager $tokenManager,
        PosSaleSessionCartService $cartService,
        WebPosService $webPosService,
    ): JsonResponse {
        $payload = $request->validate([
            'barcode' => ['required', 'string', 'max:64'],
        ]);

        return $this->mutateCart(
            request: $request,
            tokenManager: $tokenManager,
            cartService: $cartService,
            webPosService: $webPosService,
            successMessage: 'Adet guncellendi.',
            mutation: fn (Company $company, array $cartItems): array => $webPosService->decrement($cartItems, (string) $payload['barcode']),
        );
    }

    public function remove(
        Request $request,
        MobileUserTokenManager $tokenManager,
        PosSaleSessionCartService $cartService,
        WebPosService $webPosService,
    ): JsonResponse {
        $payload = $request->validate([
            'barcode' => ['required', 'string', 'max:64'],
        ]);

        return $this->mutateCart(
            request: $request,
            tokenManager: $tokenManager,
            cartService: $cartService,
            webPosService: $webPosService,
            successMessage: 'Satir sepetten kaldirildi.',
            mutation: fn (Company $company, array $cartItems): array => $webPosService->remove($cartItems, (string) $payload['barcode']),
        );
    }

    public function setCustomPrice(
        Request $request,
        MobileUserTokenManager $tokenManager,
        PosSaleSessionCartService $cartService,
        WebPosService $webPosService,
    ): JsonResponse {
        $payload = $request->validate([
            'barcode' => ['required', 'string', 'max:64'],
            'salePriceKurus' => ['required', 'integer', 'min:1'],
        ]);

        return $this->mutateCart(
            request: $request,
            tokenManager: $tokenManager,
            cartService: $cartService,
            webPosService: $webPosService,
            successMessage: 'Satir fiyati guncellendi.',
            mutation: fn (Company $company, array $cartItems): array => $webPosService->setCustomPrice(
                $cartItems,
                (string) $payload['barcode'],
                (int) $payload['salePriceKurus'],
            ),
        );
    }

    public function applyPercentDiscount(
        Request $request,
        MobileUserTokenManager $tokenManager,
        PosSaleSessionCartService $cartService,
        WebPosService $webPosService,
    ): JsonResponse {
        $payload = $request->validate([
            'barcode' => ['required', 'string', 'max:64'],
            'percent' => ['required', 'numeric', 'min:0.01', 'max:99.99'],
        ]);

        return $this->mutateCart(
            request: $request,
            tokenManager: $tokenManager,
            cartService: $cartService,
            webPosService: $webPosService,
            successMessage: 'Yuzde indirim uygulandi.',
            mutation: fn (Company $company, array $cartItems): array => $webPosService->applyPercentDiscount(
                $cartItems,
                (string) $payload['barcode'],
                (float) $payload['percent'],
            ),
        );
    }

    public function applyFixedDiscount(
        Request $request,
        MobileUserTokenManager $tokenManager,
        PosSaleSessionCartService $cartService,
        WebPosService $webPosService,
    ): JsonResponse {
        $payload = $request->validate([
            'barcode' => ['required', 'string', 'max:64'],
            'discountKurus' => ['required', 'integer', 'min:1'],
        ]);

        return $this->mutateCart(
            request: $request,
            tokenManager: $tokenManager,
            cartService: $cartService,
            webPosService: $webPosService,
            successMessage: 'Sabit indirim uygulandi.',
            mutation: fn (Company $company, array $cartItems): array => $webPosService->applyFixedDiscount(
                $cartItems,
                (string) $payload['barcode'],
                (int) $payload['discountKurus'],
            ),
        );
    }

    public function resetPrice(
        Request $request,
        MobileUserTokenManager $tokenManager,
        PosSaleSessionCartService $cartService,
        WebPosService $webPosService,
    ): JsonResponse {
        $payload = $request->validate([
            'barcode' => ['required', 'string', 'max:64'],
        ]);

        return $this->mutateCart(
            request: $request,
            tokenManager: $tokenManager,
            cartService: $cartService,
            webPosService: $webPosService,
            successMessage: 'Satir fiyati liste fiyatina sifirlandi.',
            mutation: fn (Company $company, array $cartItems): array => $webPosService->resetPrice($cartItems, (string) $payload['barcode']),
        );
    }

    public function complete(
        Request $request,
        MobileUserTokenManager $tokenManager,
        PosSaleSessionCartService $cartService,
        WebPosService $webPosService,
    ): JsonResponse {
        try {
            $context = $this->resolveContext($request, $tokenManager, true);
        } catch (RuntimeException $exception) {
            $status = str_contains(mb_strtolower($exception->getMessage(), 'UTF-8'), 'oturum') ? 401 : 422;
            return $this->errorResponse($exception->getMessage(), $status);
        }

        $company = $context['company'];
        $mobileUser = $context['mobileUser'];
        $posSession = $context['posSession'];
        $saleSession = $context['saleSession'];
        $cartItems = $cartService->get($saleSession);

        if ($cartItems === []) {
            return $this->errorResponse('Satis tamamlamak icin sepet bos olmamalidir.', 422);
        }

        try {
            $sale = $webPosService->completeSale(
                $company,
                $cartItems,
                null,
                $mobileUser->id,
                $posSession->register?->branch_id,
                $posSession->register_id,
                $posSession->id,
            );
        } catch (RuntimeException $exception) {
            return $this->errorResponse($exception->getMessage(), 422);
        }

        $saleSession->update(['status' => 'completed']);
        $nextSaleSession = SaleSession::query()->create([
            'pos_session_id' => $posSession->id,
            'source_device_uid' => $saleSession->source_device_uid,
            'source_label' => $saleSession->source_label,
            'created_by_mobile_user_id' => $mobileUser->id,
            'status' => 'active',
        ]);

        $emptyCart = [];
        $summary = $webPosService->summarize($emptyCart);

        $posSession->update(['last_activity_at' => now()]);

        return response()->json([
            'ok' => true,
            'message' => 'Satis tamamlandi. Satis no: #' . $sale->id,
            'data' => array_merge(
                $this->buildSessionPayload(
                    company: $company,
                    posSession: $posSession,
                    saleSession: $nextSaleSession,
                    cartItems: $emptyCart,
                    summary: $summary,
                ),
                [
                    'sale' => [
                        'id' => $sale->id,
                        'totalAmountKurus' => (int) $sale->total_amount_kurus,
                        'totalAmount' => $this->formatKurus((int) $sale->total_amount_kurus),
                        'totalItems' => (int) $sale->total_items,
                        'completedAt' => optional($sale->completed_at)->valueOf(),
                    ],
                ],
            ),
        ]);
    }

    public function print(
        Request $request,
        MobileUserTokenManager $tokenManager,
    ): JsonResponse
    {
        try {
            $context = $this->resolveContext(
                request: $request,
                tokenManager: $tokenManager,
                requireActivePosSession: false,
            );
        } catch (RuntimeException $exception) {
            return $this->errorResponse($exception->getMessage(), 401);
        }

        $payload = $request->validate([
            'saleId' => ['nullable', 'integer', 'min:1'],
            'paper' => ['nullable', 'in:58mm,80mm,a4'],
            'output' => ['nullable', 'in:print,pdf'],
        ]);

        $company = $context['company'];
        $posSession = $context['posSession'];

        $saleQuery = WebSale::query()->where('company_id', $company->id);
        if (!empty($payload['saleId'])) {
            $saleQuery->where('id', (int) $payload['saleId']);
        } elseif ($posSession instanceof PosSession) {
            $saleQuery->where('register_id', $posSession->register_id);
        }

        $sale = $saleQuery->latest('completed_at')->first();
        if (! $sale instanceof WebSale) {
            return response()->json([
                'ok' => true,
                'message' => 'Yazdirma icin tamamlanan satis bulunamadi.',
                'data' => [
                    'printReady' => false,
                ],
            ]);
        }

        $defaultPaper = $this->resolveReceiptProfilePaperSize(
            companyId: (int) $company->id,
            branchId: (int) ($sale->branch_id ?? 0),
        );
        $paper = (string) ($payload['paper'] ?? $defaultPaper);
        if (!in_array($paper, ['58mm', '80mm', 'a4'], true)) {
            $paper = $defaultPaper;
        }
        $output = (string) ($payload['output'] ?? 'print');
        $expiresAt = now()->addMinutes(15);

        $previewUrl = URL::temporarySignedRoute('pos.receipts.public', $expiresAt, [
            'webSale' => $sale->id,
            'paper' => $paper,
            'output' => $output,
            'autoprint' => 0,
            'source' => 'mobile',
        ]);

        $printUrl = URL::temporarySignedRoute('pos.receipts.public', $expiresAt, [
            'webSale' => $sale->id,
            'paper' => $paper,
            'output' => $output,
            'autoprint' => 1,
            'source' => 'mobile',
        ]);

        $pdfUrl = URL::temporarySignedRoute('pos.receipts.public', $expiresAt, [
            'webSale' => $sale->id,
            'paper' => 'a4',
            'output' => 'pdf',
            'autoprint' => 1,
            'source' => 'mobile',
        ]);

        return response()->json([
            'ok' => true,
            'message' => 'Yazdirma baglantisi hazirlandi. Tarayicida yazdirabilir veya PDF kaydedebilirsiniz.',
            'data' => [
                'printReady' => true,
                'saleId' => (int) $sale->id,
                'previewUrl' => $previewUrl,
                'printUrl' => $printUrl,
                'pdfUrl' => $pdfUrl,
                'expiresAt' => $expiresAt->valueOf(),
            ],
        ]);
    }

    public function publishLocalSale(
        Request $request,
        MobileUserTokenManager $tokenManager,
    ): JsonResponse {
        try {
            $context = $this->resolveContext(
                request: $request,
                tokenManager: $tokenManager,
                requireActivePosSession: false,
            );
        } catch (RuntimeException $exception) {
            return $this->errorResponse($exception->getMessage(), 401);
        }

        $payload = $request->validate([
            'localSaleId' => ['required', 'integer', 'min:1'],
            'createdAt' => ['required', 'integer', 'min:1'],
            'totalItems' => ['required', 'integer', 'min:1'],
            'totalAmountKurus' => ['required', 'integer', 'min:1'],
            'totalCostKurus' => ['required', 'integer', 'min:0'],
            'profitKurus' => ['required', 'integer'],
            'paymentMethod' => ['nullable', 'in:cash,card,other'],
            'items' => ['required', 'array', 'min:1'],
            'items.*.barcode' => ['required', 'string', 'max:64'],
            'items.*.productName' => ['required', 'string', 'max:255'],
            'items.*.quantity' => ['required', 'integer', 'min:1'],
            'items.*.unitSalePriceKurus' => ['required', 'integer', 'min:1'],
            'items.*.unitCostPriceKurus' => ['required', 'integer', 'min:0'],
            'items.*.lineTotalKurus' => ['required', 'integer', 'min:1'],
            'items.*.lineProfitKurus' => ['nullable', 'integer'],
        ]);

        $company = $context['company'];
        $mobileUser = $context['mobileUser'];
        $deviceUid = (string) ($context['deviceUid'] ?? '');
        $paymentMethod = (string) ($payload['paymentMethod'] ?? 'cash');
        if (!in_array($paymentMethod, ['cash', 'card', 'other'], true)) {
            $paymentMethod = 'cash';
        }

        $createdAtRaw = (int) $payload['createdAt'];
        $completedAt = $createdAtRaw > 0
            ? now()->setTimestamp((int) floor($createdAtRaw / 1000))
            : now();

        $existingSale = WebSale::query()
            ->where('company_id', $company->id)
            ->where('created_by_mobile_user_id', $mobileUser->id)
            ->where('completed_at', $completedAt)
            ->where('total_items', (int) $payload['totalItems'])
            ->where('total_amount_kurus', (int) $payload['totalAmountKurus'])
            ->first();

        if ($existingSale instanceof WebSale) {
            return response()->json([
                'ok' => true,
                'message' => 'Satis daha once buluta aktarildi.',
                'data' => [
                    'saleId' => (int) $existingSale->id,
                    'alreadySynced' => true,
                ],
            ]);
        }

        $device = Device::query()
            ->where('company_id', $company->id)
            ->where('device_uid', $deviceUid)
            ->first();

        $eventUuid = 'mobile-sale-' . (int) $payload['localSaleId'];
        if ($device instanceof Device && Schema::hasTable('sync_event_dedups')) {
            $alreadyProcessed = SyncEventDedup::query()
                ->where('device_id', $device->id)
                ->where('event_uuid', $eventUuid)
                ->exists();

            if ($alreadyProcessed) {
                return response()->json([
                    'ok' => true,
                    'message' => 'Satis daha once buluta aktarildi.',
                    'data' => [
                        'saleId' => null,
                        'alreadySynced' => true,
                    ],
                ]);
            }
        }

        $sale = DB::transaction(function () use ($payload, $company, $mobileUser, $completedAt, $paymentMethod, $device, $eventUuid): WebSale {
            $sale = WebSale::query()->create([
                'company_id' => $company->id,
                'created_by_mobile_user_id' => $mobileUser->id,
                'total_items' => (int) $payload['totalItems'],
                'total_amount_kurus' => (int) $payload['totalAmountKurus'],
                'total_cost_kurus' => (int) $payload['totalCostKurus'],
                'profit_kurus' => (int) $payload['profitKurus'],
                'completed_at' => $completedAt,
            ]);

            foreach ((array) $payload['items'] as $item) {
                $quantity = max(1, (int) ($item['quantity'] ?? 1));
                $unitSale = max(1, (int) ($item['unitSalePriceKurus'] ?? 1));
                $unitCost = max(0, (int) ($item['unitCostPriceKurus'] ?? 0));
                $lineTotal = max(1, (int) ($item['lineTotalKurus'] ?? ($quantity * $unitSale)));
                $lineProfit = array_key_exists('lineProfitKurus', $item)
                    ? (int) ($item['lineProfitKurus'] ?? 0)
                    : ($lineTotal - ($quantity * $unitCost));

                WebSaleItem::query()->create([
                    'web_sale_id' => $sale->id,
                    'barcode' => (string) $item['barcode'],
                    'product_name_snapshot' => (string) $item['productName'],
                    'group_name_snapshot' => null,
                    'quantity' => $quantity,
                    'unit_sale_price_kurus_snapshot' => $unitSale,
                    'unit_cost_price_kurus_snapshot' => $unitCost,
                    'line_total_kurus' => $lineTotal,
                    'line_profit_kurus' => $lineProfit,
                ]);
            }

            if (Schema::hasTable('web_sale_payments')) {
                DB::table('web_sale_payments')->insert([
                    'web_sale_id' => $sale->id,
                    'method' => $paymentMethod,
                    'amount_kurus' => (int) $payload['totalAmountKurus'],
                    'created_at' => now(),
                    'updated_at' => now(),
                ]);
            }

            if ($device instanceof Device && Schema::hasTable('sync_event_dedups')) {
                SyncEventDedup::query()->create([
                    'device_id' => $device->id,
                    'event_uuid' => $eventUuid,
                    'processed_at' => now(),
                ]);
            }

            return $sale;
        });

        return response()->json([
            'ok' => true,
            'message' => 'Satis buluta aktarildi.',
            'data' => [
                'saleId' => (int) $sale->id,
                'alreadySynced' => false,
            ],
        ]);
    }

    private function mutateCart(
        Request $request,
        MobileUserTokenManager $tokenManager,
        PosSaleSessionCartService $cartService,
        WebPosService $webPosService,
        string $successMessage,
        callable $mutation,
    ): JsonResponse {
        try {
            $context = $this->resolveContext($request, $tokenManager, true);
        } catch (RuntimeException $exception) {
            $status = str_contains(mb_strtolower($exception->getMessage(), 'UTF-8'), 'oturum') ? 401 : 422;
            return $this->errorResponse($exception->getMessage(), $status);
        }

        try {
            $updatedCart = $mutation($context['company'], $cartService->get($context['saleSession']));
            $cartService->replace($context['saleSession'], $updatedCart);
        } catch (RuntimeException $exception) {
            return $this->errorResponse($exception->getMessage(), 422);
        }

        $cartItems = $webPosService->recalculateCart($cartService->get($context['saleSession']));
        $summary = $webPosService->summarize($cartItems);
        $context['posSession']->update(['last_activity_at' => now()]);

        return response()->json([
            'ok' => true,
            'message' => $successMessage,
            'data' => $this->buildSessionPayload(
                company: $context['company'],
                posSession: $context['posSession'],
                saleSession: $context['saleSession'],
                cartItems: $cartItems,
                summary: $summary,
            ),
        ]);
    }

    private function resolveContext(
        Request $request,
        MobileUserTokenManager $tokenManager,
        bool $requireActivePosSession,
    ): array {
        $mobileUser = $tokenManager->findActiveUserByToken($request->bearerToken());
        if (! $mobileUser instanceof MobileUser) {
            throw new RuntimeException('Gecersiz oturum.');
        }

        $payload = $request->validate([
            'companyCode' => ['required', 'string', 'max:64'],
            'deviceUid' => ['required', 'string', 'max:160'],
            'deviceName' => ['nullable', 'string', 'max:120'],
        ]);

        $company = Company::query()
            ->where('company_code', trim((string) $payload['companyCode']))
            ->where('status', 'active')
            ->where(function ($query) use ($mobileUser): void {
                $query
                    ->where('owner_mobile_user_id', $mobileUser->id)
                    ->orWhereHas('staffRoles', function ($staffQuery) use ($mobileUser): void {
                        $staffQuery
                            ->where('mobile_user_id', $mobileUser->id)
                            ->where('status', 'active')
                            ->whereIn('role', ['manager', 'cashier']);
                    });
            })
            ->first();

        if (! $company instanceof Company) {
            throw new RuntimeException('Firma bulunamadi.');
        }

        $posSession = PosSession::query()
            ->with(['register.branch'])
            ->where('status', 'active')
            ->whereHas('register.branch', function ($query) use ($company): void {
                $query->where('company_id', $company->id);
            })
            ->orderByDesc('last_activity_at')
            ->orderByDesc('opened_at')
            ->first();

        if (! $requireActivePosSession || $posSession instanceof PosSession) {
            $saleSession = $posSession instanceof PosSession
                ? $this->resolveOrCreateDeviceSaleSession(
                    posSession: $posSession,
                    deviceUid: trim((string) $payload['deviceUid']),
                    deviceName: trim((string) ($payload['deviceName'] ?? '')),
                    mobileUserId: $mobileUser->id,
                )
                : null;

            return [
                'mobileUser' => $mobileUser,
                'company' => $company,
                'posSession' => $posSession,
                'saleSession' => $saleSession,
                'deviceUid' => trim((string) $payload['deviceUid']),
                'deviceName' => trim((string) ($payload['deviceName'] ?? '')),
            ];
        }

        throw new RuntimeException('Aktif web POS oturumu bulunamadi.');
    }

    private function resolveOrCreateDeviceSaleSession(
        PosSession $posSession,
        string $deviceUid,
        string $deviceName,
        int $mobileUserId,
    ): SaleSession {
        $session = SaleSession::query()
            ->where('pos_session_id', $posSession->id)
            ->where('status', 'active')
            ->where('source_device_uid', $deviceUid)
            ->orderByDesc('id')
            ->first();

        if ($session instanceof SaleSession) {
            return $session;
        }

        $label = $deviceName !== ''
            ? 'Telefon - ' . mb_substr($deviceName, 0, 24, 'UTF-8')
            : 'Telefon - ' . mb_substr($deviceUid, -6, null, 'UTF-8');

        return SaleSession::query()->create([
            'pos_session_id' => $posSession->id,
            'source_device_uid' => $deviceUid,
            'source_label' => $label,
            'created_by_mobile_user_id' => $mobileUserId,
            'status' => 'active',
        ]);
    }

    /**
     * @param array<int, array<string, mixed>> $cartItems
     * @param array<string, mixed> $summary
     * @return array<string, mixed>
     */
    private function buildSessionPayload(
        Company $company,
        PosSession $posSession,
        SaleSession $saleSession,
        array $cartItems,
        array $summary,
    ): array {
        $register = $posSession->register;
        $branch = $register?->branch;
        $totalAmountKurus = (int) ($summary['totalAmountKurus'] ?? 0);
        $recentSales = $this->buildRecentSales((int) $company->id, 10);
        $lastSale = $recentSales[0] ?? null;

        return [
            'hasActiveSession' => true,
            'company' => [
                'companyId' => $company->id,
                'companyCode' => $company->company_code,
                'companyName' => $company->name,
            ],
            'posSession' => [
                'id' => $posSession->id,
                'branchId' => $branch?->id,
                'branchName' => $branch?->name,
                'registerId' => $register?->id,
                'registerName' => $register?->name,
            ],
            'saleSession' => [
                'id' => $saleSession->id,
                'label' => $saleSession->source_label,
                'sourceDeviceUid' => $saleSession->source_device_uid,
            ],
            'summary' => [
                'itemCount' => (int) ($summary['items'] ?? 0),
                'totalAmountKurus' => $totalAmountKurus,
                'totalAmount' => $this->formatKurus($totalAmountKurus),
                'canCheckout' => $cartItems !== [],
            ],
            'lastSale' => $lastSale,
            'recentSales' => $recentSales,
            'cartItems' => array_map(function (array $item): array {
                $lineTotalKurus = (int) ($item['lineTotalKurus'] ?? 0);
                $baseSalePriceKurus = (int) ($item['baseSalePriceKurus'] ?? 0);
                $salePriceKurus = (int) ($item['salePriceKurus'] ?? $baseSalePriceKurus);

                return [
                    'barcode' => (string) ($item['barcode'] ?? ''),
                    'productName' => (string) ($item['productName'] ?? ''),
                    'quantity' => (int) ($item['quantity'] ?? 1),
                    'baseSalePriceKurus' => $baseSalePriceKurus,
                    'salePriceKurus' => $salePriceKurus,
                    'lineTotalKurus' => $lineTotalKurus,
                    'lineTotal' => $this->formatKurus($lineTotalKurus),
                    'hasCustomPrice' => $salePriceKurus !== $baseSalePriceKurus,
                ];
            }, $cartItems),
        ];
    }

    /**
     * @return array<int, array<string, mixed>>
     */
    private function buildRecentSales(int $companyId, int $limit): array
    {
        $rows = WebSale::query()
            ->where('company_id', $companyId)
            ->with(['register:id,name'])
            ->latest('completed_at')
            ->limit(max(1, $limit))
            ->get();

        $paymentMethodMap = $this->resolvePaymentMethodMap(
            $rows
                ->pluck('id')
                ->map(fn ($id): int => (int) $id)
                ->all()
        );

        return $rows->map(function (WebSale $sale) use ($paymentMethodMap): array {
            $paymentMethod = (string) ($paymentMethodMap[(int) $sale->id] ?? 'cash');
            if (!in_array($paymentMethod, ['cash', 'card', 'other'], true)) {
                $paymentMethod = 'cash';
            }

            return [
                'saleId' => (int) $sale->id,
                'totalItems' => (int) $sale->total_items,
                'totalAmountKurus' => (int) $sale->total_amount_kurus,
                'totalAmount' => $this->formatKurus((int) $sale->total_amount_kurus),
                'completedAtEpochMs' => optional($sale->completed_at)?->valueOf(),
                'completedAtLabel' => optional($sale->completed_at)->diffForHumans(),
                'registerName' => $sale->register?->name ?? 'Mobil POS',
                'paymentMethod' => $paymentMethod,
            ];
        })->all();
    }

    /**
     * @param array<int, int> $saleIds
     * @return array<int, string>
     */
    private function resolvePaymentMethodMap(array $saleIds): array
    {
        if ($saleIds === [] || !Schema::hasTable('web_sale_payments')) {
            return [];
        }

        $rows = DB::table('web_sale_payments')
            ->select('web_sale_id', 'method')
            ->whereIn('web_sale_id', $saleIds)
            ->orderBy('web_sale_id')
            ->orderBy('id')
            ->get();

        $map = [];
        foreach ($rows as $row) {
            $saleId = (int) ($row->web_sale_id ?? 0);
            if ($saleId <= 0 || array_key_exists($saleId, $map)) {
                continue;
            }
            $map[$saleId] = (string) ($row->method ?? 'cash');
        }

        return $map;
    }

    private function resolveReceiptProfilePaperSize(int $companyId, int $branchId): string
    {
        if (!Schema::hasTable('receipt_profiles')) {
            return '80mm';
        }

        $profile = ReceiptProfile::query()
            ->where('company_id', $companyId)
            ->where('branch_id', $branchId)
            ->where('is_default', true)
            ->latest('id')
            ->first();

        if (!$profile instanceof ReceiptProfile) {
            $profile = ReceiptProfile::query()
                ->where('company_id', $companyId)
                ->whereNull('branch_id')
                ->where('is_default', true)
                ->latest('id')
                ->first();
        }

        $paperSize = strtolower((string) ($profile?->paper_size ?? '80mm'));

        return in_array($paperSize, ['58mm', '80mm', 'a4'], true) ? $paperSize : '80mm';
    }

    private function formatKurus(int $kurus): string
    {
        return number_format($kurus / 100, 2, ',', '.') . ' TL';
    }

    private function errorResponse(string $message, int $status): JsonResponse
    {
        return response()->json([
            'ok' => false,
            'message' => $message,
        ], $status);
    }
}
