<?php

namespace App\Http\Controllers;

use App\Models\CompanyLicense;
use App\Models\ReceiptProfile;
use App\Models\SaleSession;
use App\Models\SystemSetting;
use App\Models\WebSale;
use App\Services\PosAuthManager;
use App\Services\PosRoleService;
use App\Services\PosSaleSessionCartService;
use App\Services\PosSaleSessionService;
use App\Services\PosSessionService;
use App\Services\WebMoneyService;
use App\Services\WebPosService;
use Illuminate\Contracts\View\View;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;
use Illuminate\Validation\Rule;
use RuntimeException;

class PosShellController extends Controller
{
    public function __invoke(
        Request $request,
        PosAuthManager $authManager,
        PosSaleSessionService $saleSessionService,
        PosSaleSessionCartService $saleSessionCartService,
        PosSessionService $posSessionService,
        WebPosService $webPosService,
        WebMoneyService $moneyService,
        PosRoleService $roleService,
    ): View {
        $mobileUser = $authManager->resolveAuthenticatedUser($request);
        $company = $authManager->resolveAuthenticatedCompany($request);

        abort_if($mobileUser === null || $company === null, 401);
        $roleSummary = $authManager->resolveRoleSummary($request);
        $permissions = $roleSummary['permissions'] ?? $roleService->permissionsForRole(null);

        $context = $posSessionService->resolveContext($request, $company, $mobileUser);
        $selectedBranch = $context['selectedBranch'];
        $selectedRegister = $context['selectedRegister'];
        $activePosSession = $context['activePosSession'];
        $saleSessionContext = null;

        if ($activePosSession !== null) {
            $saleSessionContext = $saleSessionService->resolveContext(
                request: $request,
                posSession: $activePosSession,
                mobileUserId: $mobileUser->id,
            );
        } else {
            $saleSessionService->clearSelection($request);
        }

        $todaySales = WebSale::query()
            ->where('company_id', $company->id)
            ->where(function ($query) use ($selectedRegister): void {
                $query
                    ->where('register_id', $selectedRegister->id)
                    ->orWhereNull('register_id');
            })
            ->whereDate('completed_at', today())
            ->get();

        $latestLicense = CompanyLicense::query()
            ->with('package')
            ->where('company_id', $company->id)
            ->latest('id')
            ->first();

        $hasWebSalePayments = Schema::hasTable('web_sale_payments');

        $recentSalesQuery = WebSale::query()
            ->where('company_id', $company->id)
            ->where(function ($query) use ($selectedRegister): void {
                $query
                    ->where('register_id', $selectedRegister->id)
                    ->orWhereNull('register_id');
            })
            ->with(['register:id,name'])
            ->latest('completed_at')
            ->limit(8);

        $recentSalesRows = $recentSalesQuery->get();
        $paymentMethodMap = $hasWebSalePayments
            ? $this->resolvePaymentMethodMap(
                $recentSalesRows
                    ->pluck('id')
                    ->map(fn ($id): int => (int) $id)
                    ->all()
            )
            : [];

        $recentSales = $recentSalesRows
            ->map(function (WebSale $sale) use ($hasWebSalePayments, $paymentMethodMap): array {
                $paymentMethod = $hasWebSalePayments
                    ? (string) ($paymentMethodMap[(int) $sale->id] ?? 'cash')
                    : 'cash';

                return [
                    'id' => $sale->id,
                    'items' => (int) $sale->total_items,
                    'totalAmount' => number_format(((int) $sale->total_amount_kurus) / 100, 2, ',', '.') . ' TL',
                    'completedAt' => optional($sale->completed_at)->diffForHumans(),
                    'registerName' => $sale->register?->name ?? 'Mobil POS',
                    'paymentMethod' => $paymentMethod,
                    'paymentMethodLabel' => $this->formatPaymentMethod($paymentMethod),
                ];
            })
            ->all();

        $cartItems = $activePosSession !== null && $saleSessionContext !== null
            ? $webPosService->recalculateCart($saleSessionCartService->get($saleSessionContext['activeSession']))
            : [];
        $cartSummary = $webPosService->summarize($cartItems);

        $ownedCompanies = $authManager->listOwnedCompaniesByEmail((string) $mobileUser->email);

        $registerOptionsByBranch = [];
        foreach ($context['branches'] as $branchModel) {
            $registerOptionsByBranch[(string) $branchModel->id] = $branchModel->registers()
                ->where('status', 'active')
                ->orderBy('name')
                ->get(['id', 'name', 'code'])
                ->map(fn ($register): array => [
                    'id' => $register->id,
                    'name' => $register->name,
                    'code' => $register->code,
                ])
                ->all();
        }

        return view('pos.shell', [
            'mobileUser' => $mobileUser,
            'company' => $company,
            'ownedCompanies' => $ownedCompanies,
            'roleSummary' => $roleSummary,
            'rolePermissions' => $permissions,
            'latestLicense' => $latestLicense,
            'cartItems' => $cartItems,
            'cartSummary' => [
                'itemCount' => (int) ($cartSummary['items'] ?? 0),
                'totalAmount' => $moneyService->formatKurus((int) ($cartSummary['totalAmountKurus'] ?? 0)),
            ],
            'todaySummary' => [
                'saleCount' => $todaySales->count(),
                'itemCount' => (int) $todaySales->sum('total_items'),
                'totalAmount' => number_format(((int) $todaySales->sum('total_amount_kurus')) / 100, 2, ',', '.') . ' TL',
            ],
            'recentSales' => $recentSales,
            'barcodePrefill' => old('barcode', ''),
            'posContext' => [
                'branches' => $context['branches']->map(fn ($branch): array => [
                    'id' => $branch->id,
                    'name' => $branch->name,
                    'code' => $branch->code,
                ])->all(),
                'registers' => $context['registers']->map(fn ($register): array => [
                    'id' => $register->id,
                    'name' => $register->name,
                    'code' => $register->code,
                ])->all(),
                'registerOptionsByBranch' => $registerOptionsByBranch,
                'selectedBranchId' => $selectedBranch->id,
                'selectedRegisterId' => $selectedRegister->id,
                'activeSession' => [
                    'id' => $activePosSession?->id,
                    'status' => $activePosSession?->status,
                    'openedAt' => optional($activePosSession?->opened_at)->format('d.m.Y H:i'),
                ],
            ],
            'saleSessionContext' => [
                'sessions' => $saleSessionContext !== null
                    ? $saleSessionContext['sessions']->map(fn ($session): array => [
                        'id' => $session->id,
                        'label' => $session->source_label,
                        'sourceDeviceUid' => $session->source_device_uid,
                        'status' => $session->status,
                    ])->all()
                    : [],
                'heldSessions' => $saleSessionContext !== null
                    ? $this->serializeHeldSessions($saleSessionContext['activeSession'])
                    : [],
                'activeSessionId' => $saleSessionContext['activeSession']->id ?? null,
                'activeLabel' => $saleSessionContext['activeSession']->source_label ?? null,
            ],
            'companyProfile' => $this->loadCompanyProfile($company),
            'receiptProfile' => $this->loadReceiptProfile($company->id, $selectedBranch->id),
        ]);
    }

    public function switchCompany(
        Request $request,
        PosAuthManager $authManager,
    ): RedirectResponse {
        $payload = $request->validate([
            'company_code' => ['required', 'string', 'max:64'],
        ]);

        try {
            $authManager->switchCompany($request, $payload['company_code']);
        } catch (RuntimeException $exception) {
            return back()->with('error', $exception->getMessage());
        }

        return redirect()->route('pos.home')->with('success', 'Firma degistirildi.');
    }

    public function switchContext(
        Request $request,
        PosAuthManager $authManager,
        PosSessionService $posSessionService,
        PosSaleSessionService $saleSessionService,
    ): RedirectResponse|JsonResponse {
        $permissionError = $this->requirePermission(
            $request,
            $authManager,
            'canManagePosContext',
            'Sube/kasa ayari yapmak icin yetkiniz yok.',
        );
        if ($permissionError instanceof RedirectResponse || $permissionError instanceof JsonResponse) {
            return $permissionError;
        }

        $payload = $request->validate([
            'branch_id' => ['required', 'integer', 'min:1'],
            'register_id' => ['required', 'integer', 'min:1'],
        ]);

        $mobileUser = $authManager->resolveAuthenticatedUser($request);
        $company = $authManager->resolveAuthenticatedCompany($request);

        if ($mobileUser === null || $company === null) {
            return redirect()->route('pos.login');
        }

        try {
            $result = $posSessionService->switchContext(
                $request,
                $company,
                $mobileUser,
                (int) $payload['branch_id'],
                (int) $payload['register_id'],
            );
        } catch (RuntimeException $exception) {
            return back()->with('error', $exception->getMessage());
        }

        $saleSessionService->clearSelection($request);

        return back()->with('success', 'Sube ve kasa guncellendi. POS oturumu hazir.');
    }

    public function openSession(
        Request $request,
        PosAuthManager $authManager,
        PosSessionService $posSessionService,
        PosSaleSessionService $saleSessionService,
    ): RedirectResponse|JsonResponse {
        $permissionError = $this->requirePermission(
            $request,
            $authManager,
            'canManagePosContext',
            'POS oturumu acmak icin yetkiniz yok.',
        );
        if ($permissionError instanceof RedirectResponse || $permissionError instanceof JsonResponse) {
            return $permissionError;
        }

        $mobileUser = $authManager->resolveAuthenticatedUser($request);
        $company = $authManager->resolveAuthenticatedCompany($request);

        if ($mobileUser === null || $company === null) {
            return redirect()->route('pos.login');
        }

        try {
            $session = $posSessionService->openCurrentSession($request, $company, $mobileUser);
        } catch (RuntimeException $exception) {
            return back()->with('error', $exception->getMessage());
        }

        $saleSessionService->clearSelection($request);

        return back()->with('success', 'POS oturumu acildi (#' . $session->id . ').');
    }

    public function closeSession(
        Request $request,
        PosAuthManager $authManager,
        PosSessionService $posSessionService,
        PosSaleSessionService $saleSessionService,
    ): RedirectResponse|JsonResponse {
        $permissionError = $this->requirePermission(
            $request,
            $authManager,
            'canManagePosContext',
            'POS oturumu kapatmak icin yetkiniz yok.',
        );
        if ($permissionError instanceof RedirectResponse || $permissionError instanceof JsonResponse) {
            return $permissionError;
        }

        $company = $authManager->resolveAuthenticatedCompany($request);
        if ($company === null) {
            return redirect()->route('pos.login');
        }

        $closed = $posSessionService->closeCurrentSession($request, $company);

        if ($closed === null) {
            return back()->with('error', 'Kapatilacak aktif POS oturumu bulunamadi.');
        }

        $saleSessionService->clearSelection($request);

        return back()->with('success', 'POS oturumu kapatildi (#' . $closed->id . ').');
    }

    public function createSaleSession(
        Request $request,
        PosAuthManager $authManager,
        PosSessionService $posSessionService,
        PosSaleSessionService $saleSessionService,
        PosSaleSessionCartService $saleSessionCartService,
        WebPosService $webPosService,
    ): RedirectResponse|JsonResponse {
        $mobileUser = $authManager->resolveAuthenticatedUser($request);
        $company = $authManager->resolveAuthenticatedCompany($request);

        if ($mobileUser === null || $company === null) {
            return $this->unauthenticatedResponse($request);
        }

        $activePosSession = $posSessionService->getActiveSessionFromRequest($request, $company);
        if ($activePosSession === null) {
            return $this->errorResponse($request, 'Yeni satis sekmesi acmak icin aktif POS oturumu gereklidir.');
        }

        $session = $saleSessionService->createManualTab($request, $activePosSession, $mobileUser->id);

        if ($this->isAsyncRequest($request)) {
            return $this->saleSessionSuccessResponse(
                request: $request,
                saleSession: $session,
                saleSessionCartService: $saleSessionCartService,
                webPosService: $webPosService,
                message: 'Yeni satis sekmesi acildi.',
            );
        }

        return back()->with('success', 'Yeni satis sekmesi acildi.');
    }

    public function switchSaleSession(
        Request $request,
        PosAuthManager $authManager,
        PosSessionService $posSessionService,
        PosSaleSessionService $saleSessionService,
        PosSaleSessionCartService $saleSessionCartService,
        WebPosService $webPosService,
    ): RedirectResponse|JsonResponse {
        $payload = $request->validate([
            'sale_session_id' => ['required', 'integer', 'min:1'],
        ]);

        $company = $authManager->resolveAuthenticatedCompany($request);
        if ($company === null) {
            return $this->unauthenticatedResponse($request);
        }

        $activePosSession = $posSessionService->getActiveSessionFromRequest($request, $company);
        if ($activePosSession === null) {
            return $this->errorResponse($request, 'Aktif POS oturumu bulunamadigi icin sekme degistirilemedi.');
        }

        try {
            $session = $saleSessionService->switchTab($request, $activePosSession, (int) $payload['sale_session_id']);
        } catch (RuntimeException $exception) {
            return $this->errorResponse($request, $exception->getMessage());
        }

        if ($this->isAsyncRequest($request)) {
            return $this->saleSessionSuccessResponse(
                request: $request,
                saleSession: $session,
                saleSessionCartService: $saleSessionCartService,
                webPosService: $webPosService,
                message: 'Satis sekmesi degistirildi.',
            );
        }

        return back()->with('success', 'Satis sekmesi degistirildi.');
    }

    public function closeSaleSession(
        Request $request,
        PosAuthManager $authManager,
        PosSessionService $posSessionService,
        PosSaleSessionService $saleSessionService,
        PosSaleSessionCartService $saleSessionCartService,
        WebPosService $webPosService,
    ): RedirectResponse|JsonResponse {
        $payload = $request->validate([
            'sale_session_id' => ['required', 'integer', 'min:1'],
        ]);

        $mobileUser = $authManager->resolveAuthenticatedUser($request);
        $company = $authManager->resolveAuthenticatedCompany($request);
        if ($mobileUser === null || $company === null) {
            return $this->unauthenticatedResponse($request);
        }

        $activePosSession = $posSessionService->getActiveSessionFromRequest($request, $company);
        if ($activePosSession === null) {
            return $this->errorResponse($request, 'Aktif POS oturumu bulunamadigi icin sekme kapatilamadi.');
        }

        try {
            $nextSession = $saleSessionService->closeTab(
                request: $request,
                posSession: $activePosSession,
                saleSessionId: (int) $payload['sale_session_id'],
                mobileUserId: $mobileUser->id,
            );
        } catch (RuntimeException $exception) {
            return $this->errorResponse($request, $exception->getMessage());
        }

        if ($this->isAsyncRequest($request)) {
            return $this->saleSessionSuccessResponse(
                request: $request,
                saleSession: $nextSession,
                saleSessionCartService: $saleSessionCartService,
                webPosService: $webPosService,
                message: 'Satis sekmesi kapatildi. Aktif sekme: ' . $nextSession->source_label . '.',
            );
        }

        return back()->with('success', 'Satis sekmesi kapatildi. Aktif sekme: ' . $nextSession->source_label . '.');
    }

    public function updateCompanyProfile(
        Request $request,
        PosAuthManager $authManager,
    ): RedirectResponse|JsonResponse {
        $permissionError = $this->requirePermission(
            $request,
            $authManager,
            'canManageCompanyProfile',
            'Firma bilgilerini guncellemek icin yetkiniz yok.',
        );
        if ($permissionError instanceof RedirectResponse || $permissionError instanceof JsonResponse) {
            return $permissionError;
        }

        $company = $authManager->resolveAuthenticatedCompany($request);
        if ($company === null) {
            return $this->unauthenticatedResponse($request);
        }

        $payload = $request->validate([
            'company_title' => ['nullable', 'string', 'max:160'],
            'contact_name' => ['nullable', 'string', 'max:120'],
            'contact_phone' => ['nullable', 'string', 'max:32'],
            'contact_email' => ['nullable', 'email', 'max:120'],
            'tax_number' => ['nullable', 'string', 'max:32'],
            'tax_office' => ['nullable', 'string', 'max:120'],
        ]);

        $profile = [
            'companyTitle' => trim((string) ($payload['company_title'] ?? '')),
            'contactName' => trim((string) ($payload['contact_name'] ?? '')),
            'contactPhone' => trim((string) ($payload['contact_phone'] ?? '')),
            'contactEmail' => trim((string) ($payload['contact_email'] ?? '')),
            'taxNumber' => trim((string) ($payload['tax_number'] ?? '')),
            'taxOffice' => trim((string) ($payload['tax_office'] ?? '')),
        ];

        SystemSetting::query()->updateOrCreate(
            ['key' => $this->companyProfileKey($company->id)],
            ['value' => json_encode($profile, JSON_UNESCAPED_UNICODE)],
        );

        if ($this->isAsyncRequest($request)) {
            return response()->json([
                'ok' => true,
                'message' => 'Firma bilgileri guncellendi.',
                'data' => [
                    'companyProfile' => $this->loadCompanyProfile($company),
                ],
            ]);
        }

        return back()->with('success', 'Firma bilgileri guncellendi.');
    }

    public function updateReceiptProfile(
        Request $request,
        PosAuthManager $authManager,
    ): RedirectResponse|JsonResponse {
        $permissionError = $this->requirePermission(
            $request,
            $authManager,
            'canManageReceiptProfile',
            'Fis ayarlarini degistirmek icin yetkiniz yok.',
        );
        if ($permissionError instanceof RedirectResponse || $permissionError instanceof JsonResponse) {
            return $permissionError;
        }

        $company = $authManager->resolveAuthenticatedCompany($request);
        if ($company === null) {
            return $this->unauthenticatedResponse($request);
        }

        if (!Schema::hasTable('receipt_profiles')) {
            return $this->errorResponse(
                $request,
                'Fis profili tablosu hazir degil. Lutfen setup ekranindan migration adimini calistirin.',
            );
        }

        $payload = $request->validate([
            'branch_id' => ['required', 'integer', 'min:1'],
            'profile_name' => ['nullable', 'string', 'max:120'],
            'paper_size' => ['required', Rule::in(['58mm', '80mm', 'a4'])],
            'print_mode' => ['required', Rule::in(['browser', 'pdf'])],
            'header_line_1' => ['nullable', 'string', 'max:160'],
            'header_line_2' => ['nullable', 'string', 'max:160'],
            'footer_line_1' => ['nullable', 'string', 'max:160'],
            'footer_line_2' => ['nullable', 'string', 'max:160'],
            'show_company' => ['sometimes', 'string', Rule::in(['1'])],
            'show_tax' => ['sometimes', 'string', Rule::in(['1'])],
            'show_payment' => ['sometimes', 'string', Rule::in(['1'])],
            'show_barcode' => ['sometimes', 'string', Rule::in(['1'])],
            'show_date' => ['sometimes', 'string', Rule::in(['1'])],
            'show_register' => ['sometimes', 'string', Rule::in(['1'])],
        ]);

        $profileName = trim((string) ($payload['profile_name'] ?? ''));
        $branchId = (int) $payload['branch_id'];
        $branchExists = $company->branches()
            ->where('id', $branchId)
            ->where('status', 'active')
            ->exists();
        if (!$branchExists) {
            return $this->errorResponse($request, 'Secili sube bulunamadi veya pasif durumda.');
        }
        $visibleFields = [
            'showCompany' => array_key_exists('show_company', $payload),
            'showTax' => array_key_exists('show_tax', $payload),
            'showPayment' => array_key_exists('show_payment', $payload),
            'showBarcode' => array_key_exists('show_barcode', $payload),
            'showDate' => array_key_exists('show_date', $payload),
            'showRegister' => array_key_exists('show_register', $payload),
        ];
        $headerLines = [
            trim((string) ($payload['header_line_1'] ?? '')),
            trim((string) ($payload['header_line_2'] ?? '')),
        ];
        $footerLines = [
            trim((string) ($payload['footer_line_1'] ?? '')),
            trim((string) ($payload['footer_line_2'] ?? '')),
        ];

        ReceiptProfile::query()
            ->where('company_id', $company->id)
            ->where('branch_id', $branchId)
            ->update(['is_default' => false]);

        ReceiptProfile::query()->updateOrCreate(
            [
                'company_id' => $company->id,
                'branch_id' => $branchId,
                'is_default' => true,
            ],
            [
                'name' => $profileName !== '' ? $profileName : 'Varsayilan Fis Profili',
                'paper_size' => (string) $payload['paper_size'],
                'print_mode' => (string) $payload['print_mode'],
                'header_json' => $headerLines,
                'footer_json' => $footerLines,
                'visible_fields_json' => $visibleFields,
                'field_order_json' => ['header', 'meta', 'items', 'total', 'payments', 'footer'],
            ],
        );

        $savedProfile = $this->loadReceiptProfile($company->id, $branchId);

        if ($this->isAsyncRequest($request)) {
            return response()->json([
                'ok' => true,
                'message' => 'Fis ayarlari guncellendi.',
                'data' => [
                    'receiptProfile' => $savedProfile,
                ],
            ]);
        }

        return back()->with('success', 'Fis ayarlari guncellendi.');
    }

    private function unauthenticatedResponse(Request $request): RedirectResponse|JsonResponse
    {
        if ($this->isAsyncRequest($request)) {
            return response()->json([
                'ok' => false,
                'message' => 'POS oturumu bulunamadi.',
                'redirect' => route('pos.login', [], false),
            ], 401);
        }

        return redirect()->route('pos.login');
    }

    private function errorResponse(Request $request, string $message): RedirectResponse|JsonResponse
    {
        if ($this->isAsyncRequest($request)) {
            return response()->json([
                'ok' => false,
                'message' => $message,
            ], 422);
        }

        return back()->with('error', $message);
    }

    private function forbiddenResponse(Request $request, string $message): RedirectResponse|JsonResponse
    {
        if ($this->isAsyncRequest($request)) {
            return response()->json([
                'ok' => false,
                'message' => $message,
            ], 403);
        }

        return back()->with('error', $message);
    }

    private function requirePermission(
        Request $request,
        PosAuthManager $authManager,
        string $permissionKey,
        string $message,
    ): RedirectResponse|JsonResponse|null {
        $summary = $authManager->resolveRoleSummary($request);
        $permissions = is_array($summary['permissions'] ?? null) ? $summary['permissions'] : [];
        if (!((bool) ($permissions[$permissionKey] ?? false))) {
            return $this->forbiddenResponse($request, $message);
        }

        return null;
    }

    private function saleSessionSuccessResponse(
        Request $request,
        SaleSession $saleSession,
        PosSaleSessionCartService $saleSessionCartService,
        WebPosService $webPosService,
        string $message,
    ): JsonResponse {
        $cartItems = $webPosService->recalculateCart($saleSessionCartService->get($saleSession));
        $summary = $webPosService->summarize($cartItems);

        return response()->json([
            'ok' => true,
            'message' => $message,
            'data' => [
                'cartItems' => $this->serializeCartItems($cartItems),
                'summary' => [
                    'itemCount' => (int) ($summary['items'] ?? 0),
                    'totalAmountKurus' => (int) ($summary['totalAmountKurus'] ?? 0),
                    'totalAmount' => $this->formatKurus((int) ($summary['totalAmountKurus'] ?? 0)),
                ],
                'canCheckout' => $cartItems !== [],
                'activeSaleSession' => [
                    'id' => $saleSession->id,
                    'label' => $saleSession->source_label,
                ],
                'saleSessions' => $this->serializeSaleSessions($saleSession),
                'heldSessions' => $this->serializeHeldSessions($saleSession),
            ],
        ]);
    }

    /**
     * @param array<int, array<string, mixed>> $cartItems
     * @return array<int, array<string, mixed>>
     */
    private function serializeCartItems(array $cartItems): array
    {
        return array_map(function (array $item): array {
            $barcode = (string) ($item['barcode'] ?? '');
            $lineTotalKurus = (int) ($item['lineTotalKurus'] ?? 0);

            return [
                'barcode' => $barcode,
                'productName' => (string) ($item['productName'] ?? $barcode),
                'quantity' => (int) ($item['quantity'] ?? 1),
                'lineTotalKurus' => $lineTotalKurus,
                'lineTotal' => $this->formatKurus($lineTotalKurus),
                'actions' => [
                    'increment' => route('pos.item.increment', ['barcode' => $barcode], false),
                    'decrement' => route('pos.item.decrement', ['barcode' => $barcode], false),
                    'remove' => route('pos.item.remove', ['barcode' => $barcode], false),
                ],
            ];
        }, $cartItems);
    }

    /**
     * @return array<int, array<string, mixed>>
     */
    private function serializeSaleSessions(SaleSession $activeSaleSession): array
    {
        return SaleSession::query()
            ->where('pos_session_id', $activeSaleSession->pos_session_id)
            ->where('status', 'active')
            ->orderBy('id')
            ->get()
            ->map(fn (SaleSession $session): array => [
                'id' => $session->id,
                'label' => $session->source_label,
                'isActive' => (int) $session->id === (int) $activeSaleSession->id,
            ])
            ->all();
    }

    /**
     * @return array<int, array<string, mixed>>
     */
    private function serializeHeldSessions(SaleSession $activeSaleSession): array
    {
        return SaleSession::query()
            ->with(['items:id,sale_session_id,quantity,line_total_kurus'])
            ->where('pos_session_id', $activeSaleSession->pos_session_id)
            ->where('status', 'held')
            ->orderByDesc('updated_at')
            ->orderByDesc('id')
            ->get()
            ->map(function (SaleSession $session): array {
                $itemCount = (int) $session->items->sum('quantity');
                $totalKurus = (int) $session->items->sum('line_total_kurus');

                return [
                    'id' => $session->id,
                    'label' => $session->source_label,
                    'itemCount' => $itemCount,
                    'totalAmountKurus' => $totalKurus,
                    'totalAmount' => $this->formatKurus($totalKurus),
                    'updatedAt' => optional($session->updated_at)->diffForHumans(),
                ];
            })
            ->all();
    }

    private function formatKurus(int $kurus): string
    {
        return number_format($kurus / 100, 2, ',', '.') . ' TL';
    }

    private function formatPaymentMethod(string $method): string
    {
        return match ($method) {
            'card' => 'Kart',
            'other' => 'Diger',
            default => 'Nakit',
        };
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

    private function isAsyncRequest(Request $request): bool
    {
        if ($request->expectsJson() || $request->ajax()) {
            return true;
        }

        $accept = mb_strtolower((string) $request->header('accept', ''), 'UTF-8');

        return str_contains($accept, 'application/json');
    }

    private function loadCompanyProfile(\App\Models\Company $company): array
    {
        $raw = (string) (SystemSetting::query()->where('key', $this->companyProfileKey($company->id))->value('value') ?? '');
        $decoded = json_decode($raw, true);
        $data = is_array($decoded) ? $decoded : [];

        $companyTitle = trim((string) ($data['companyTitle'] ?? ''));

        return [
            'companyTitle' => $companyTitle !== '' ? $companyTitle : $company->name,
            'contactName' => (string) ($data['contactName'] ?? ''),
            'contactPhone' => (string) ($data['contactPhone'] ?? ''),
            'contactEmail' => (string) ($data['contactEmail'] ?? ''),
            'taxNumber' => (string) ($data['taxNumber'] ?? ''),
            'taxOffice' => (string) ($data['taxOffice'] ?? ''),
        ];
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
            'paperSize' => in_array((string) $profile->paper_size, ['58mm', '80mm', 'a4'], true)
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

    private function companyProfileKey(int $companyId): string
    {
        return 'company_profile.' . $companyId;
    }
}
