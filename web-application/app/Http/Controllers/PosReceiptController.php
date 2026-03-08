<?php

namespace App\Http\Controllers;

use App\Models\SaleSession;
use App\Models\ReceiptProfile;
use App\Models\SystemSetting;
use App\Models\WebSale;
use App\Services\PosAuthManager;
use App\Services\PosSaleSessionCartService;
use App\Services\PosSaleSessionService;
use App\Services\PosSessionService;
use App\Services\WebMoneyService;
use Illuminate\Contracts\View\View;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;
use RuntimeException;

class PosReceiptController extends Controller
{
    private const PAPER_58 = '58mm';
    private const PAPER_80 = '80mm';
    private const PAPER_A4 = 'a4';

    /** @var array<int, string> */
    private const PAPER_OPTIONS = [
        self::PAPER_58,
        self::PAPER_80,
        self::PAPER_A4,
    ];

    public function index(
        Request $request,
        PosAuthManager $authManager,
        PosSessionService $posSessionService,
        PosSaleSessionService $saleSessionService,
        PosSaleSessionCartService $saleSessionCartService,
        WebMoneyService $moneyService,
    ): View {
        $mobileUser = $authManager->resolveAuthenticatedUser($request);
        $company = $authManager->resolveAuthenticatedCompany($request);

        abort_if($mobileUser === null || $company === null, 401);

        $activePosSession = $posSessionService->getActiveSessionFromRequest($request, $company);
        $heldSales = [];

        if ($activePosSession !== null) {
            $heldSales = $saleSessionService
                ->listHeldByPosSession($activePosSession->id)
                ->map(function (SaleSession $session) use ($saleSessionCartService, $moneyService): array {
                    $cartItems = $saleSessionCartService->get($session);
                    $itemCount = 0;
                    $totalKurus = 0;

                    foreach ($cartItems as $item) {
                        $itemCount += (int) ($item['quantity'] ?? 0);
                        $totalKurus += (int) ($item['lineTotalKurus'] ?? 0);
                    }

                    return [
                        'id' => $session->id,
                        'label' => $session->source_label,
                        'itemCount' => $itemCount,
                        'totalKurus' => $totalKurus,
                        'totalAmount' => $moneyService->formatKurus($totalKurus),
                        'updatedAt' => optional($session->updated_at)->diffForHumans(),
                    ];
                })
                ->values()
                ->all();
        }

        $hasWebSalePayments = Schema::hasTable('web_sale_payments');
        $completedSalesQuery = WebSale::query()
            ->where('company_id', $company->id)
            ->with(['register:id,name'])
            ->latest('completed_at')
            ->limit(30);

        $completedSales = $completedSalesQuery->get();
        $paymentMethodMap = $hasWebSalePayments
            ? $this->resolvePaymentMethodMap(
                $completedSales
                    ->pluck('id')
                    ->map(fn ($id): int => (int) $id)
                    ->all()
            )
            : [];

        $completedSales = $completedSales
            ->map(function (WebSale $sale) use ($moneyService, $hasWebSalePayments, $paymentMethodMap): array {
                $paymentMethod = $hasWebSalePayments
                    ? (string) ($paymentMethodMap[(int) $sale->id] ?? 'cash')
                    : 'cash';

                return [
                    'id' => $sale->id,
                    'registerName' => $sale->register?->name ?? '-',
                    'itemCount' => (int) $sale->total_items,
                    'totalAmount' => $moneyService->formatKurus((int) $sale->total_amount_kurus),
                    'completedAt' => optional($sale->completed_at)->diffForHumans(),
                    'paymentMethod' => $paymentMethod,
                    'paymentMethodLabel' => $this->formatPaymentMethod($paymentMethod),
                ];
            })
            ->all();

        return view('pos.receipts', [
            'mobileUser' => $mobileUser,
            'company' => $company,
            'activePosSessionId' => $activePosSession?->id,
            'heldSales' => $heldSales,
            'completedSales' => $completedSales,
        ]);
    }

    public function show(
        Request $request,
        WebSale $webSale,
        PosAuthManager $authManager,
        WebMoneyService $moneyService,
    ): View {
        $mobileUser = $authManager->resolveAuthenticatedUser($request);
        $company = $authManager->resolveAuthenticatedCompany($request);

        abort_if($mobileUser === null || $company === null, 401);
        abort_if((int) $webSale->company_id !== (int) $company->id, 404);

        $receiptProfile = $this->loadReceiptProfile(
            companyId: (int) $webSale->company_id,
            branchId: (int) ($webSale->branch_id ?? 0),
        );
        $print = $this->resolvePrintOptions($request, $receiptProfile);
        $basePath = route('pos.receipts.show', ['webSale' => $webSale->id], false);

        return view('pos.receipt-show', [
            'mobileUser' => $mobileUser,
            'company' => $company,
            'companyProfile' => $this->loadCompanyProfile((int) $company->id, (string) $company->name),
            'receiptProfile' => $receiptProfile,
            'viewerLabel' => (string) $mobileUser->email,
            'backUrl' => route('pos.receipts.index', [], false),
            'sale' => $this->buildSalePayload($webSale, $moneyService),
            'print' => array_merge($print, [
                'isPublic' => false,
                'previewUrls' => [
                    self::PAPER_58 => $this->buildUrl($basePath, ['paper' => self::PAPER_58]),
                    self::PAPER_80 => $this->buildUrl($basePath, ['paper' => self::PAPER_80]),
                    self::PAPER_A4 => $this->buildUrl($basePath, ['paper' => self::PAPER_A4]),
                ],
                'printUrl' => $this->buildUrl($basePath, [
                    'paper' => $print['paper'],
                    'output' => 'print',
                    'autoprint' => 1,
                ]),
                'pdfUrl' => $this->buildUrl($basePath, [
                    'paper' => $print['paper'],
                    'output' => 'pdf',
                    'autoprint' => 1,
                ]),
            ]),
        ]);
    }

    public function detailsJson(
        Request $request,
        WebSale $webSale,
        PosAuthManager $authManager,
        WebMoneyService $moneyService,
    ): JsonResponse {
        $mobileUser = $authManager->resolveAuthenticatedUser($request);
        $company = $authManager->resolveAuthenticatedCompany($request);

        if ($mobileUser === null || $company === null) {
            return response()->json([
                'ok' => false,
                'message' => 'POS oturumu bulunamadi.',
            ], 401);
        }

        if ((int) $webSale->company_id !== (int) $company->id) {
            return response()->json([
                'ok' => false,
                'message' => 'Satis kaydina erisim izni yok.',
            ], 404);
        }

        $salePayload = $this->buildSalePayload($webSale, $moneyService);
        $salePayload['itemsEditable'] = $webSale->items
            ->map(function ($item): array {
                return [
                    'id' => (int) $item->id,
                    'barcode' => (string) $item->barcode,
                    'name' => (string) $item->product_name_snapshot,
                    'quantity' => (int) $item->quantity,
                    'unitSalePriceKurus' => (int) $item->unit_sale_price_kurus_snapshot,
                    'unitSalePrice' => number_format(((int) $item->unit_sale_price_kurus_snapshot) / 100, 2, ',', '.'),
                ];
            })
            ->all();

        $basePath = route('pos.receipts.show', ['webSale' => $webSale->id], false);

        return response()->json([
            'ok' => true,
            'data' => [
                'sale' => $salePayload,
                'print' => [
                    'paper58PrintUrl' => $this->buildUrl($basePath, [
                        'paper' => self::PAPER_58,
                        'output' => 'print',
                        'autoprint' => 1,
                    ]),
                    'paper80PrintUrl' => $this->buildUrl($basePath, [
                        'paper' => self::PAPER_80,
                        'output' => 'print',
                        'autoprint' => 1,
                    ]),
                    'a4PdfUrl' => $this->buildUrl($basePath, [
                        'paper' => self::PAPER_A4,
                        'output' => 'pdf',
                        'autoprint' => 1,
                    ]),
                ],
            ],
        ]);
    }

    public function updateJson(
        Request $request,
        WebSale $webSale,
        PosAuthManager $authManager,
        WebMoneyService $moneyService,
    ): JsonResponse {
        $permissionError = $this->requireEditPastSalesPermission($request, $authManager);
        if ($permissionError instanceof JsonResponse) {
            return $permissionError;
        }

        $company = $authManager->resolveAuthenticatedCompany($request);
        if ($company === null) {
            return response()->json([
                'ok' => false,
                'message' => 'POS oturumu bulunamadi.',
            ], 401);
        }

        if ((int) $webSale->company_id !== (int) $company->id) {
            return response()->json([
                'ok' => false,
                'message' => 'Satis kaydina erisim izni yok.',
            ], 404);
        }

        $payload = $request->validate([
            'payment_method' => ['nullable', 'in:cash,card,other'],
            'items' => ['required', 'array', 'min:1'],
            'items.*.id' => ['required', 'integer', 'min:1'],
            'items.*.quantity' => ['required', 'integer', 'min:1', 'max:100000'],
            'items.*.unit_sale_price_kurus' => ['required', 'integer', 'min:1', 'max:999999999'],
        ]);

        $webSale->loadMissing('items');
        $existingItems = $webSale->items->keyBy('id');
        $incomingItems = collect($payload['items'] ?? []);

        if ($incomingItems->count() !== $existingItems->count()) {
            return response()->json([
                'ok' => false,
                'message' => 'Satis satirlari eksik veya hatali gonderildi.',
            ], 422);
        }

        $paymentMethod = (string) ($payload['payment_method'] ?? $this->resolvePaymentMethodForSale((int) $webSale->id));
        if (!in_array($paymentMethod, ['cash', 'card', 'other'], true)) {
            $paymentMethod = 'cash';
        }

        try {
            DB::transaction(function () use ($incomingItems, $existingItems, $webSale, $paymentMethod): void {
                $totalItems = 0;
                $totalAmountKurus = 0;
                $totalCostKurus = 0;
                $totalProfitKurus = 0;

                foreach ($incomingItems as $incomingItem) {
                    $itemId = (int) ($incomingItem['id'] ?? 0);
                    $saleItem = $existingItems->get($itemId);
                    if ($saleItem === null) {
                        throw new RuntimeException('Satir kaydi bulunamadi.');
                    }

                    $quantity = max(1, (int) ($incomingItem['quantity'] ?? 1));
                    $unitSalePriceKurus = max(1, (int) ($incomingItem['unit_sale_price_kurus'] ?? 1));
                    $unitCostKurus = (int) $saleItem->unit_cost_price_kurus_snapshot;
                    $lineTotalKurus = $quantity * $unitSalePriceKurus;
                    $lineProfitKurus = ($unitSalePriceKurus - $unitCostKurus) * $quantity;

                    $saleItem->update([
                        'quantity' => $quantity,
                        'unit_sale_price_kurus_snapshot' => $unitSalePriceKurus,
                        'line_total_kurus' => $lineTotalKurus,
                        'line_profit_kurus' => $lineProfitKurus,
                    ]);

                    $totalItems += $quantity;
                    $totalAmountKurus += $lineTotalKurus;
                    $totalCostKurus += $unitCostKurus * $quantity;
                    $totalProfitKurus += $lineProfitKurus;
                }

                $webSale->update([
                    'total_items' => $totalItems,
                    'total_amount_kurus' => $totalAmountKurus,
                    'total_cost_kurus' => $totalCostKurus,
                    'profit_kurus' => $totalProfitKurus,
                ]);

                if (Schema::hasTable('web_sale_payments')) {
                    $payment = DB::table('web_sale_payments')
                        ->where('web_sale_id', $webSale->id)
                        ->orderBy('id')
                        ->first();

                    if ($payment === null) {
                        DB::table('web_sale_payments')->insert([
                            'web_sale_id' => $webSale->id,
                            'method' => $paymentMethod,
                            'amount_kurus' => $totalAmountKurus,
                            'created_at' => now(),
                            'updated_at' => now(),
                        ]);
                    } else {
                        DB::table('web_sale_payments')
                            ->where('id', $payment->id)
                            ->update([
                            'method' => $paymentMethod,
                            'amount_kurus' => $totalAmountKurus,
                            'updated_at' => now(),
                        ]);

                        DB::table('web_sale_payments')
                            ->where('web_sale_id', $webSale->id)
                            ->where('id', '!=', $payment->id)
                            ->delete();
                    }
                }
            });
        } catch (RuntimeException $exception) {
            return response()->json([
                'ok' => false,
                'message' => $exception->getMessage(),
            ], 422);
        }

        $webSale->refresh();
        $webSale->loadMissing('items', 'register:id,name', 'branch:id,name');
        $salePayload = $this->buildSalePayload($webSale, $moneyService);

        return response()->json([
            'ok' => true,
            'message' => 'Satis fis kaydi guncellendi.',
            'data' => [
                'sale' => $salePayload,
            ],
        ]);
    }

    public function deleteJson(
        Request $request,
        WebSale $webSale,
        PosAuthManager $authManager,
    ): JsonResponse {
        $permissionError = $this->requireEditPastSalesPermission($request, $authManager);
        if ($permissionError instanceof JsonResponse) {
            return $permissionError;
        }

        $company = $authManager->resolveAuthenticatedCompany($request);
        if ($company === null) {
            return response()->json([
                'ok' => false,
                'message' => 'POS oturumu bulunamadi.',
            ], 401);
        }

        if ((int) $webSale->company_id !== (int) $company->id) {
            return response()->json([
                'ok' => false,
                'message' => 'Satis kaydina erisim izni yok.',
            ], 404);
        }

        $deletedId = (int) $webSale->id;
        $registerId = (int) ($webSale->register_id ?? 0);

        DB::transaction(function () use ($webSale): void {
            $webSale->delete();
        });

        return response()->json([
            'ok' => true,
            'message' => 'Satis kaydi silindi.',
            'data' => [
                'deletedSaleId' => $deletedId,
                'registerId' => $registerId,
            ],
        ]);
    }

    public function publicShow(
        Request $request,
        WebSale $webSale,
        WebMoneyService $moneyService,
    ): View {
        $webSale->loadMissing('company:id,name,company_code');
        $receiptProfile = $this->loadReceiptProfile(
            companyId: (int) $webSale->company_id,
            branchId: (int) ($webSale->branch_id ?? 0),
        );
        $print = $this->resolvePrintOptions($request, $receiptProfile);

        return view('pos.receipt-show', [
            'mobileUser' => null,
            'company' => $webSale->company,
            'companyProfile' => $this->loadCompanyProfile((int) $webSale->company_id, (string) ($webSale->company?->name ?? '')),
            'receiptProfile' => $receiptProfile,
            'viewerLabel' => 'Mobil yazdirma baglantisi',
            'backUrl' => null,
            'sale' => $this->buildSalePayload($webSale, $moneyService),
            'print' => array_merge($print, [
                'isPublic' => true,
                'previewUrls' => [],
                'printUrl' => null,
                'pdfUrl' => null,
            ]),
        ]);
    }

    public function resumeHeld(
        Request $request,
        SaleSession $saleSession,
        PosAuthManager $authManager,
        PosSessionService $posSessionService,
        PosSaleSessionService $saleSessionService,
    ): RedirectResponse {
        $company = $authManager->resolveAuthenticatedCompany($request);
        if ($company === null) {
            return redirect()->route('pos.login');
        }

        $activePosSession = $posSessionService->getActiveSessionFromRequest($request, $company);
        if ($activePosSession === null) {
            return back()->with('error', 'Geri acmak icin aktif POS oturumu gereklidir.');
        }

        try {
            $saleSessionService->resumeHeldTab($request, $activePosSession, $saleSession->id);
        } catch (RuntimeException $exception) {
            return back()->with('error', $exception->getMessage());
        }

        return redirect()->route('pos.home')->with('success', 'Bekleyen satis geri acildi.');
    }

    public function discardHeld(
        Request $request,
        SaleSession $saleSession,
        PosAuthManager $authManager,
        PosSessionService $posSessionService,
        PosSaleSessionService $saleSessionService,
    ): RedirectResponse {
        $company = $authManager->resolveAuthenticatedCompany($request);
        if ($company === null) {
            return redirect()->route('pos.login');
        }

        $activePosSession = $posSessionService->getActiveSessionFromRequest($request, $company);
        if ($activePosSession === null) {
            return back()->with('error', 'Bekleyen satis silmek icin aktif POS oturumu gereklidir.');
        }

        try {
            $saleSessionService->discardHeldTab($activePosSession, $saleSession->id);
        } catch (RuntimeException $exception) {
            return back()->with('error', $exception->getMessage());
        }

        return back()->with('success', 'Bekleyen satis silindi.');
    }

    private function buildSalePayload(WebSale $webSale, WebMoneyService $moneyService): array
    {
        $hasWebSalePayments = Schema::hasTable('web_sale_payments');
        $webSale->loadMissing(['items', 'register:id,name', 'branch:id,name']);

        $items = $webSale->items->map(fn ($item): array => [
            'barcode' => $item->barcode,
            'name' => $item->product_name_snapshot,
            'quantity' => (int) $item->quantity,
            'unitPrice' => $moneyService->formatKurus((int) $item->unit_sale_price_kurus_snapshot),
            'lineTotal' => $moneyService->formatKurus((int) $item->line_total_kurus),
        ])->all();

        $payments = $hasWebSalePayments
            ? $this->resolvePaymentsPayloadForSale((int) $webSale->id, $moneyService)
            : [[
                'method' => 'cash',
                'methodLabel' => 'Nakit',
                'amount' => $moneyService->formatKurus((int) $webSale->total_amount_kurus),
            ]];

        if ($payments === []) {
            $payments = [[
                'method' => 'cash',
                'methodLabel' => 'Nakit',
                'amount' => $moneyService->formatKurus((int) $webSale->total_amount_kurus),
            ]];
        }

        return [
            'id' => $webSale->id,
            'branchName' => $webSale->branch?->name ?? '-',
            'registerName' => $webSale->register?->name ?? '-',
            'completedAt' => optional($webSale->completed_at)->format('d.m.Y H:i:s'),
            'itemCount' => (int) $webSale->total_items,
            'totalAmount' => $moneyService->formatKurus((int) $webSale->total_amount_kurus),
            'items' => $items,
            'payments' => $payments,
        ];
    }

    private function requireEditPastSalesPermission(
        Request $request,
        PosAuthManager $authManager,
    ): ?JsonResponse {
        $summary = $authManager->resolveRoleSummary($request);
        $permissions = is_array($summary['permissions'] ?? null) ? $summary['permissions'] : [];
        if ((bool) ($permissions['canEditPastSales'] ?? false)) {
            return null;
        }

        return response()->json([
            'ok' => false,
            'message' => 'Gecmis satis kayitlarini duzenlemek icin yetkiniz yok.',
        ], 403);
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

            $method = (string) ($row->method ?? 'cash');
            $map[$saleId] = in_array($method, ['cash', 'card', 'other'], true) ? $method : 'cash';
        }

        return $map;
    }

    private function resolvePaymentMethodForSale(int $saleId): string
    {
        if ($saleId <= 0 || !Schema::hasTable('web_sale_payments')) {
            return 'cash';
        }

        $row = DB::table('web_sale_payments')
            ->select('method')
            ->where('web_sale_id', $saleId)
            ->orderBy('id')
            ->first();

        $method = (string) ($row->method ?? 'cash');

        return in_array($method, ['cash', 'card', 'other'], true) ? $method : 'cash';
    }

    /**
     * @return array<int, array<string, string>>
     */
    private function resolvePaymentsPayloadForSale(int $saleId, WebMoneyService $moneyService): array
    {
        if ($saleId <= 0 || !Schema::hasTable('web_sale_payments')) {
            return [];
        }

        $rows = DB::table('web_sale_payments')
            ->select('method', 'amount_kurus')
            ->where('web_sale_id', $saleId)
            ->orderBy('id')
            ->get();

        if ($rows->isEmpty()) {
            return [];
        }

        return $rows->map(function ($payment) use ($moneyService): array {
            $method = (string) ($payment->method ?? 'cash');
            if (!in_array($method, ['cash', 'card', 'other'], true)) {
                $method = 'cash';
            }

            return [
                'method' => $method,
                'methodLabel' => $this->formatPaymentMethod($method),
                'amount' => $moneyService->formatKurus((int) ($payment->amount_kurus ?? 0)),
            ];
        })->all();
    }

    private function loadReceiptProfile(int $companyId, int $branchId): array
    {
        $defaultProfile = [
            'name' => 'Varsayilan Fis Profili',
            'paperSize' => '80mm',
            'printMode' => 'browser',
            'headerLines' => ['', ''],
            'footerLines' => ['', ''],
            'visibleFields' => [
                'showCompany' => true,
                'showTax' => true,
                'showPayment' => true,
                'showBarcode' => true,
                'showDate' => true,
                'showRegister' => true,
            ],
        ];

        if (!Schema::hasTable('receipt_profiles')) {
            return $defaultProfile;
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

        if (!$profile instanceof ReceiptProfile) {
            return $defaultProfile;
        }

        $headerLines = array_values(
            array_map(
                fn ($line): string => trim((string) $line),
                is_array($profile->header_json) ? $profile->header_json : [],
            ),
        );
        $footerLines = array_values(
            array_map(
                fn ($line): string => trim((string) $line),
                is_array($profile->footer_json) ? $profile->footer_json : [],
            ),
        );
        $visibleRaw = is_array($profile->visible_fields_json) ? $profile->visible_fields_json : [];

        return [
            'name' => (string) ($profile->name ?: $defaultProfile['name']),
            'paperSize' => in_array((string) $profile->paper_size, self::PAPER_OPTIONS, true)
                ? (string) $profile->paper_size
                : $defaultProfile['paperSize'],
            'printMode' => in_array((string) $profile->print_mode, ['browser', 'pdf'], true)
                ? (string) $profile->print_mode
                : $defaultProfile['printMode'],
            'headerLines' => [
                (string) ($headerLines[0] ?? ''),
                (string) ($headerLines[1] ?? ''),
            ],
            'footerLines' => [
                (string) ($footerLines[0] ?? ''),
                (string) ($footerLines[1] ?? ''),
            ],
            'visibleFields' => [
                'showCompany' => (bool) ($visibleRaw['showCompany'] ?? $defaultProfile['visibleFields']['showCompany']),
                'showTax' => (bool) ($visibleRaw['showTax'] ?? $defaultProfile['visibleFields']['showTax']),
                'showPayment' => (bool) ($visibleRaw['showPayment'] ?? $defaultProfile['visibleFields']['showPayment']),
                'showBarcode' => (bool) ($visibleRaw['showBarcode'] ?? $defaultProfile['visibleFields']['showBarcode']),
                'showDate' => (bool) ($visibleRaw['showDate'] ?? $defaultProfile['visibleFields']['showDate']),
                'showRegister' => (bool) ($visibleRaw['showRegister'] ?? $defaultProfile['visibleFields']['showRegister']),
            ],
        ];
    }

    private function loadCompanyProfile(int $companyId, string $fallbackCompanyName): array
    {
        $raw = (string) (SystemSetting::query()->where('key', 'company_profile.' . $companyId)->value('value') ?? '');
        $decoded = json_decode($raw, true);
        $data = is_array($decoded) ? $decoded : [];

        return [
            'companyTitle' => trim((string) ($data['companyTitle'] ?? '')) ?: $fallbackCompanyName,
            'taxNumber' => trim((string) ($data['taxNumber'] ?? '')),
            'taxOffice' => trim((string) ($data['taxOffice'] ?? '')),
        ];
    }

    /**
     * @return array{
     *     paper: string,
     *     paperLabel: string,
     *     output: string,
     *     autoPrint: bool,
     *     isThermal: bool,
     *     isA4: bool
     * }
     */
    private function resolvePrintOptions(Request $request, array $receiptProfile = []): array
    {
        $defaultPaper = strtolower((string) ($receiptProfile['paperSize'] ?? self::PAPER_80));
        if (! in_array($defaultPaper, self::PAPER_OPTIONS, true)) {
            $defaultPaper = self::PAPER_80;
        }

        $defaultOutput = strtolower((string) (($receiptProfile['printMode'] ?? 'browser') === 'pdf' ? 'pdf' : 'print'));
        if (! in_array($defaultOutput, ['print', 'pdf'], true)) {
            $defaultOutput = 'print';
        }

        $paper = strtolower((string) $request->query('paper', $defaultPaper));
        if (! in_array($paper, self::PAPER_OPTIONS, true)) {
            $paper = $defaultPaper;
        }

        $output = strtolower((string) $request->query('output', $defaultOutput));
        if (! in_array($output, ['print', 'pdf'], true)) {
            $output = $defaultOutput;
        }

        $autoPrintRaw = strtolower((string) $request->query('autoprint', '0'));
        $autoPrint = in_array($autoPrintRaw, ['1', 'true', 'yes'], true);

        return [
            'paper' => $paper,
            'paperLabel' => $this->formatPaperLabel($paper),
            'output' => $output,
            'autoPrint' => $autoPrint,
            'isThermal' => in_array($paper, [self::PAPER_58, self::PAPER_80], true),
            'isA4' => $paper === self::PAPER_A4,
        ];
    }

    private function buildUrl(string $path, array $query = []): string
    {
        if ($query === []) {
            return $path;
        }

        return $path . '?' . http_build_query($query);
    }

    private function formatPaperLabel(string $paper): string
    {
        return match ($paper) {
            self::PAPER_58 => '58mm',
            self::PAPER_A4 => 'A4',
            default => '80mm',
        };
    }

    private function formatPaymentMethod(string $method): string
    {
        return match ($method) {
            'card' => 'Kart',
            'other' => 'Diger',
            default => 'Nakit',
        };
    }
}
