<?php

namespace App\Http\Controllers;

use App\Models\Company;
use App\Models\CompanyProductOffer;
use App\Models\PosSession;
use App\Models\SaleSession;
use App\Models\WebSale;
use App\Services\PosSaleSessionCartService;
use App\Services\PosSaleSessionService;
use App\Services\WebPosService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;
use RuntimeException;

class PosSaleController extends Controller
{
    public function syncState(
        Request $request,
        PosSaleSessionService $saleSessionService,
        PosSaleSessionCartService $saleSessionCartService,
        WebPosService $webPosService,
    ): JsonResponse {
        $company = $this->resolveCompany($request);
        if ($company === null) {
            return response()->json([
                'ok' => false,
                'message' => 'POS oturumu bulunamadi.',
                'redirect' => route('pos.login', [], false),
            ], 401);
        }

        $activePosSession = $this->resolveActivePosSession($request, $company);
        if ($activePosSession === null) {
            return response()->json([
                'ok' => true,
                'message' => 'Aktif POS oturumu bulunamadi.',
                'data' => [
                    'cartItems' => [],
                    'summary' => [
                        'itemCount' => 0,
                        'totalAmountKurus' => 0,
                        'totalAmount' => $this->formatKurus(0),
                    ],
                    'canCheckout' => false,
                    'activeSaleSession' => null,
                    'saleSessions' => [],
                    'heldSessions' => [],
                    'todaySummary' => [
                        'saleCount' => 0,
                        'itemCount' => 0,
                        'totalAmountKurus' => 0,
                        'totalAmount' => $this->formatKurus(0),
                    ],
                    'recentSales' => [],
                    'sync' => [
                        'activeSaleSessionSwitched' => false,
                    ],
                ],
            ]);
        }

        $context = $saleSessionService->resolveContext($request, $activePosSession);
        /** @var SaleSession $activeSaleSession */
        $activeSaleSession = $context['activeSession'];
        $switched = false;

        $latestUpdatedSession = SaleSession::query()
            ->where('pos_session_id', $activePosSession->id)
            ->where('status', 'active')
            ->orderByDesc('updated_at')
            ->orderByDesc('id')
            ->first();

        if (
            $latestUpdatedSession instanceof SaleSession
            && (int) $latestUpdatedSession->id !== (int) $activeSaleSession->id
            && $latestUpdatedSession->updated_at !== null
            && (
                $activeSaleSession->updated_at === null
                || $latestUpdatedSession->updated_at->gt($activeSaleSession->updated_at)
            )
        ) {
            $activeSaleSession = $saleSessionService->switchTab($request, $activePosSession, (int) $latestUpdatedSession->id);
            $switched = true;
        }

        $cartItems = $webPosService->recalculateCart($saleSessionCartService->get($activeSaleSession));
        $summary = $webPosService->summarize($cartItems);

        return response()->json([
            'ok' => true,
            'message' => $switched ? 'Mobil tarama sekmesi one getirildi.' : 'Senkron tamamlandi.',
            'data' => [
                'cartItems' => $this->serializeCartItems($cartItems),
                'summary' => [
                    'itemCount' => (int) ($summary['items'] ?? 0),
                    'totalAmountKurus' => (int) ($summary['totalAmountKurus'] ?? 0),
                    'totalAmount' => $this->formatKurus((int) ($summary['totalAmountKurus'] ?? 0)),
                ],
                'canCheckout' => $cartItems !== [],
                'activeSaleSession' => [
                    'id' => $activeSaleSession->id,
                    'label' => $activeSaleSession->source_label,
                ],
                'saleSessions' => $this->serializeSaleSessions($activeSaleSession),
                'heldSessions' => $this->serializeHeldSessions($activeSaleSession),
                'todaySummary' => $this->buildTodaySummary($company->id, (int) $activePosSession->register_id),
                'recentSales' => $this->buildRecentSales($company->id, (int) $activePosSession->register_id),
                'sync' => [
                    'activeSaleSessionSwitched' => $switched,
                ],
            ],
        ]);
    }

    public function searchProducts(Request $request): JsonResponse
    {
        $payload = $request->validate([
            'q' => ['required', 'string', 'max:64'],
        ]);

        $company = $this->resolveCompany($request);
        if ($company === null) {
            return response()->json([
                'ok' => false,
                'message' => 'POS oturumu bulunamadi.',
                'data' => [],
            ], 401);
        }

        $queryText = trim((string) $payload['q']);
        if ($queryText === '') {
            return response()->json([
                'ok' => true,
                'data' => [],
            ]);
        }

        $escaped = addcslashes($queryText, '%_\\');
        $prefixLike = $escaped . '%';
        $containsLike = '%' . $escaped . '%';

        $offers = CompanyProductOffer::query()
            ->leftJoin('global_products as gp', 'gp.barcode', '=', 'company_product_offers.barcode')
            ->select('company_product_offers.*', 'gp.canonical_name as gp_name')
            ->where('company_id', $company->id)
            ->where('is_active', true)
            ->where(function ($query) use ($prefixLike, $containsLike): void {
                $query->where('company_product_offers.barcode', 'like', $prefixLike)
                    ->orWhere('company_product_offers.barcode', 'like', $containsLike)
                    ->orWhere('gp.canonical_name', 'like', $containsLike)
                    ->orWhere('company_product_offers.group_name', 'like', $containsLike)
                    ->orWhere('company_product_offers.note', 'like', $containsLike);
            })
            ->orderByRaw(
                'CASE
                    WHEN company_product_offers.barcode = ? THEN 0
                    WHEN company_product_offers.barcode LIKE ? THEN 1
                    WHEN gp.canonical_name LIKE ? THEN 2
                    WHEN gp.canonical_name LIKE ? THEN 3
                    ELSE 4
                END',
                [$queryText, $prefixLike, $prefixLike, $containsLike],
            )
            ->limit(8)
            ->get();

        $items = $offers->map(function (CompanyProductOffer $offer): array {
            $name = (string) ($offer->getAttribute('gp_name') ?? '');

            return [
                'barcode' => $offer->barcode,
                'name' => $name !== '' ? $name : $offer->barcode,
                'price' => $this->formatKurus((int) $offer->sale_price_kurus),
            ];
        })->all();

        return response()->json([
            'ok' => true,
            'data' => $items,
        ]);
    }

    public function scan(
        Request $request,
        WebPosService $webPosService,
        PosSaleSessionService $saleSessionService,
        PosSaleSessionCartService $saleSessionCartService,
    ): RedirectResponse|JsonResponse {
        $payload = $request->validate([
            'barcode' => ['required', 'string', 'max:64'],
        ]);

        $company = $this->resolveCompany($request);
        if ($company === null) {
            return $this->unauthenticatedResponse($request);
        }
        $activePosSession = $this->resolveActivePosSession($request, $company);
        if ($activePosSession === null) {
            return $this->errorResponse($request, 'Aktif POS oturumu bulunamadi. Once POS oturumu acin.');
        }
        $saleSession = $saleSessionService->resolveContext($request, $activePosSession)['activeSession'];

        try {
            $cartItems = $saleSessionCartService->get($saleSession);
            $updated = $webPosService->addBarcode($company->id, $cartItems, $payload['barcode']);
            $saleSessionCartService->replace($saleSession, $updated);
        } catch (RuntimeException $exception) {
            return $this->errorResponse($request, $exception->getMessage());
        }

        return $this->successResponse(
            request: $request,
            saleSession: $saleSession,
            saleSessionCartService: $saleSessionCartService,
            webPosService: $webPosService,
            message: 'Urun sepete eklendi.',
        );
    }

    public function increment(
        Request $request,
        string $barcode,
        WebPosService $webPosService,
        PosSaleSessionService $saleSessionService,
        PosSaleSessionCartService $saleSessionCartService,
    ): RedirectResponse|JsonResponse {
        return $this->mutateQuantity(
            request: $request,
            barcode: $barcode,
            webPosService: $webPosService,
            saleSessionService: $saleSessionService,
            saleSessionCartService: $saleSessionCartService,
            mode: 'increment',
        );
    }

    public function decrement(
        Request $request,
        string $barcode,
        WebPosService $webPosService,
        PosSaleSessionService $saleSessionService,
        PosSaleSessionCartService $saleSessionCartService,
    ): RedirectResponse|JsonResponse {
        return $this->mutateQuantity(
            request: $request,
            barcode: $barcode,
            webPosService: $webPosService,
            saleSessionService: $saleSessionService,
            saleSessionCartService: $saleSessionCartService,
            mode: 'decrement',
        );
    }

    public function remove(
        Request $request,
        string $barcode,
        WebPosService $webPosService,
        PosSaleSessionService $saleSessionService,
        PosSaleSessionCartService $saleSessionCartService,
    ): RedirectResponse|JsonResponse {
        $company = $this->resolveCompany($request);
        if ($company === null) {
            return $this->unauthenticatedResponse($request);
        }
        $activePosSession = $this->resolveActivePosSession($request, $company);
        if ($activePosSession === null) {
            return $this->errorResponse($request, 'Aktif POS oturumu bulunamadi. Once POS oturumu acin.');
        }
        $saleSession = $saleSessionService->resolveContext($request, $activePosSession)['activeSession'];

        try {
            $cartItems = $saleSessionCartService->get($saleSession);
            $updated = $webPosService->remove($cartItems, $barcode);
            $saleSessionCartService->replace($saleSession, $updated);
        } catch (RuntimeException $exception) {
            return $this->errorResponse($request, $exception->getMessage());
        }

        return $this->successResponse(
            request: $request,
            saleSession: $saleSession,
            saleSessionCartService: $saleSessionCartService,
            webPosService: $webPosService,
            message: 'Urun sepetten cikarildi.',
        );
    }

    public function clear(
        Request $request,
        PosSaleSessionService $saleSessionService,
        PosSaleSessionCartService $saleSessionCartService,
        WebPosService $webPosService,
    ): RedirectResponse|JsonResponse {
        $company = $this->resolveCompany($request);
        if ($company === null) {
            return $this->unauthenticatedResponse($request);
        }
        $activePosSession = $this->resolveActivePosSession($request, $company);
        if ($activePosSession === null) {
            return $this->errorResponse($request, 'Aktif POS oturumu bulunamadi. Once POS oturumu acin.');
        }
        $saleSession = $saleSessionService->resolveContext($request, $activePosSession)['activeSession'];

        $saleSessionCartService->clear($saleSession);

        return $this->successResponse(
            request: $request,
            saleSession: $saleSession,
            saleSessionCartService: $saleSessionCartService,
            webPosService: $webPosService,
            message: 'Sepet temizlendi.',
        );
    }

    public function hold(
        Request $request,
        PosSaleSessionService $saleSessionService,
        PosSaleSessionCartService $saleSessionCartService,
        WebPosService $webPosService,
    ): RedirectResponse|JsonResponse {
        $company = $this->resolveCompany($request);
        if ($company === null) {
            return $this->unauthenticatedResponse($request);
        }

        $activePosSession = $this->resolveActivePosSession($request, $company);
        if ($activePosSession === null) {
            return $this->errorResponse($request, 'Aktif POS oturumu bulunamadi. Once POS oturumu acin.');
        }

        $activeSaleSession = $saleSessionService->resolveContext($request, $activePosSession)['activeSession'];
        $cartItems = $saleSessionCartService->get($activeSaleSession);
        if ($cartItems === []) {
            return $this->errorResponse($request, 'Bos sepet beklemeye alinamaz.');
        }

        $mobileUserId = (int) $request->session()->get('pos.auth.mobile_user_id', 0);

        try {
            $nextSaleSession = $saleSessionService->holdAndRotate(
                request: $request,
                posSession: $activePosSession,
                saleSession: $activeSaleSession,
                mobileUserId: $mobileUserId > 0 ? $mobileUserId : null,
            );
        } catch (RuntimeException $exception) {
            return $this->errorResponse($request, $exception->getMessage());
        }

        return $this->successResponse(
            request: $request,
            saleSession: $nextSaleSession,
            saleSessionCartService: $saleSessionCartService,
            webPosService: $webPosService,
            message: 'Satis beklemeye alindi.',
        );
    }

    public function complete(
        Request $request,
        WebPosService $webPosService,
        PosSaleSessionService $saleSessionService,
        PosSaleSessionCartService $saleSessionCartService,
    ): RedirectResponse|JsonResponse {
        $payload = $request->validate([
            'payment_method' => ['required', 'in:cash,card,other'],
        ]);

        $company = $this->resolveCompany($request);
        if ($company === null) {
            return $this->unauthenticatedResponse($request);
        }
        $activePosSession = $this->resolveActivePosSession($request, $company);
        if ($activePosSession === null) {
            return $this->errorResponse($request, 'Aktif POS oturumu bulunamadi. Once POS oturumu acin.');
        }
        $activeSaleSession = $saleSessionService->resolveContext($request, $activePosSession)['activeSession'];
        $mobileUserId = (int) $request->session()->get('pos.auth.mobile_user_id', 0);
        $branchId = (int) $request->session()->get('pos.auth.branch_id', 0);
        $registerId = (int) $request->session()->get('pos.auth.register_id', 0);

        $cartItems = $saleSessionCartService->get($activeSaleSession);

        try {
            $sale = $webPosService->completeSale(
                $company,
                $cartItems,
                null,
                $mobileUserId > 0 ? $mobileUserId : null,
                $branchId > 0 ? $branchId : null,
                $registerId > 0 ? $registerId : null,
                $activePosSession->id,
                (string) $payload['payment_method'],
            );
            $nextSaleSession = $saleSessionService->rotateAfterCheckout(
                request: $request,
                posSession: $activePosSession,
                saleSession: $activeSaleSession,
                mobileUserId: $mobileUserId > 0 ? $mobileUserId : null,
            );
        } catch (RuntimeException $exception) {
            return $this->errorResponse($request, $exception->getMessage());
        }

        return $this->successResponse(
            request: $request,
            saleSession: $nextSaleSession,
            saleSessionCartService: $saleSessionCartService,
            webPosService: $webPosService,
            message: 'Satis tamamlandi. Satis no: #' . $sale->id . ' · Odeme: ' . $this->formatPaymentMethod((string) $payload['payment_method']) . '.',
            sale: $sale,
        );
    }

    public function resumeHeld(
        Request $request,
        PosSaleSessionService $saleSessionService,
        PosSaleSessionCartService $saleSessionCartService,
        WebPosService $webPosService,
    ): RedirectResponse|JsonResponse {
        $payload = $request->validate([
            'sale_session_id' => ['required', 'integer', 'min:1'],
        ]);

        $company = $this->resolveCompany($request);
        if ($company === null) {
            return $this->unauthenticatedResponse($request);
        }

        $activePosSession = $this->resolveActivePosSession($request, $company);
        if ($activePosSession === null) {
            return $this->errorResponse($request, 'Aktif POS oturumu bulunamadi. Once POS oturumu acin.');
        }

        try {
            $session = $saleSessionService->resumeHeldTab($request, $activePosSession, (int) $payload['sale_session_id']);
        } catch (RuntimeException $exception) {
            return $this->errorResponse($request, $exception->getMessage());
        }

        return $this->successResponse(
            request: $request,
            saleSession: $session,
            saleSessionCartService: $saleSessionCartService,
            webPosService: $webPosService,
            message: 'Bekleyen satis geri acildi.',
        );
    }

    public function discardHeld(
        Request $request,
        PosSaleSessionService $saleSessionService,
        PosSaleSessionCartService $saleSessionCartService,
        WebPosService $webPosService,
    ): RedirectResponse|JsonResponse {
        $payload = $request->validate([
            'sale_session_id' => ['required', 'integer', 'min:1'],
        ]);

        $company = $this->resolveCompany($request);
        if ($company === null) {
            return $this->unauthenticatedResponse($request);
        }

        $activePosSession = $this->resolveActivePosSession($request, $company);
        if ($activePosSession === null) {
            return $this->errorResponse($request, 'Aktif POS oturumu bulunamadi. Once POS oturumu acin.');
        }

        try {
            $saleSessionService->discardHeldTab($activePosSession, (int) $payload['sale_session_id']);
            $currentSession = $saleSessionService->resolveContext($request, $activePosSession)['activeSession'];
        } catch (RuntimeException $exception) {
            return $this->errorResponse($request, $exception->getMessage());
        }

        return $this->successResponse(
            request: $request,
            saleSession: $currentSession,
            saleSessionCartService: $saleSessionCartService,
            webPosService: $webPosService,
            message: 'Bekleyen satis silindi.',
        );
    }

    private function mutateQuantity(
        Request $request,
        string $barcode,
        WebPosService $webPosService,
        PosSaleSessionService $saleSessionService,
        PosSaleSessionCartService $saleSessionCartService,
        string $mode,
    ): RedirectResponse|JsonResponse {
        $company = $this->resolveCompany($request);
        if ($company === null) {
            return $this->unauthenticatedResponse($request);
        }
        $activePosSession = $this->resolveActivePosSession($request, $company);
        if ($activePosSession === null) {
            return $this->errorResponse($request, 'Aktif POS oturumu bulunamadi. Once POS oturumu acin.');
        }
        $saleSession = $saleSessionService->resolveContext($request, $activePosSession)['activeSession'];

        try {
            $cartItems = $saleSessionCartService->get($saleSession);
            $updated = $mode === 'increment'
                ? $webPosService->increment($cartItems, $barcode)
                : $webPosService->decrement($cartItems, $barcode);

            $saleSessionCartService->replace($saleSession, $updated);
        } catch (RuntimeException $exception) {
            return $this->errorResponse($request, $exception->getMessage());
        }

        return $this->successResponse(
            request: $request,
            saleSession: $saleSession,
            saleSessionCartService: $saleSessionCartService,
            webPosService: $webPosService,
            message: 'Sepet guncellendi.',
        );
    }

    private function resolveCompany(Request $request): ?Company
    {
        $company = $request->attributes->get('posCompany');

        return $company instanceof Company ? $company : null;
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

    private function successResponse(
        Request $request,
        SaleSession $saleSession,
        PosSaleSessionCartService $saleSessionCartService,
        WebPosService $webPosService,
        string $message,
        ?WebSale $sale = null,
    ): RedirectResponse|JsonResponse {
        if (! $this->isAsyncRequest($request)) {
            return back()->with('success', $message);
        }

        $cartItems = $webPosService->recalculateCart($saleSessionCartService->get($saleSession));
        $summary = $webPosService->summarize($cartItems);

        $payload = [
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
        ];

        $company = $this->resolveCompany($request);
        $activePosSession = $company instanceof Company
            ? $this->resolveActivePosSession($request, $company)
            : null;

        if ($company instanceof Company && $activePosSession instanceof PosSession) {
            $payload['data']['todaySummary'] = $this->buildTodaySummary($company->id, (int) $activePosSession->register_id);
            $payload['data']['recentSales'] = $this->buildRecentSales($company->id, (int) $activePosSession->register_id);
        }

        if ($sale !== null) {
            if (Schema::hasTable('web_sale_payments')) {
                $sale->loadMissing('register:id,name', 'payments:id,web_sale_id,method,amount_kurus');
                $primaryPayment = $sale->payments->first();
            } else {
                $sale->loadMissing('register:id,name');
                $primaryPayment = null;
            }
            $paymentMethod = (string) ($primaryPayment?->method ?? 'cash');

            $payload['data']['sale'] = [
                'id' => $sale->id,
                'totalAmount' => $this->formatKurus((int) $sale->total_amount_kurus),
                'totalItems' => (int) $sale->total_items,
                'registerName' => $sale->register?->name,
                'completedAt' => 'az once',
                'paymentMethod' => $paymentMethod,
                'paymentMethodLabel' => $this->formatPaymentMethod($paymentMethod),
            ];
        }

        return response()->json($payload);
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

    private function buildTodaySummary(int $companyId, int $registerId): array
    {
        $todaySales = WebSale::query()
            ->where('company_id', $companyId)
            ->where(function ($query) use ($registerId): void {
                $query
                    ->where('register_id', $registerId)
                    ->orWhereNull('register_id');
            })
            ->whereDate('completed_at', today())
            ->get();

        $totalAmountKurus = (int) $todaySales->sum('total_amount_kurus');

        return [
            'saleCount' => $todaySales->count(),
            'itemCount' => (int) $todaySales->sum('total_items'),
            'totalAmountKurus' => $totalAmountKurus,
            'totalAmount' => $this->formatKurus($totalAmountKurus),
        ];
    }

    /**
     * @return array<int, array<string, mixed>>
     */
    private function buildRecentSales(int $companyId, int $registerId): array
    {
        $hasWebSalePayments = Schema::hasTable('web_sale_payments');
        $query = WebSale::query()
            ->where('company_id', $companyId)
            ->where(function ($innerQuery) use ($registerId): void {
                $innerQuery
                    ->where('register_id', $registerId)
                    ->orWhereNull('register_id');
            })
            ->with(['register:id,name'])
            ->latest('completed_at')
            ->limit(8);

        $rows = $query->get();
        $paymentMethodMap = $hasWebSalePayments
            ? $this->resolvePaymentMethodMap(
                $rows
                    ->pluck('id')
                    ->map(fn ($id): int => (int) $id)
                    ->all()
            )
            : [];

        return $rows
            ->map(function (WebSale $sale) use ($hasWebSalePayments, $paymentMethodMap): array {
                $paymentMethod = $hasWebSalePayments
                    ? (string) ($paymentMethodMap[(int) $sale->id] ?? 'cash')
                    : 'cash';

                return [
                    'id' => $sale->id,
                    'items' => (int) $sale->total_items,
                    'totalItems' => (int) $sale->total_items,
                    'totalAmount' => $this->formatKurus((int) $sale->total_amount_kurus),
                    'completedAt' => optional($sale->completed_at)->diffForHumans(),
                    'registerName' => $sale->register?->name ?? 'Mobil POS',
                    'paymentMethod' => $paymentMethod,
                    'paymentMethodLabel' => $this->formatPaymentMethod($paymentMethod),
                ];
            })
            ->all();
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

    private function isAsyncRequest(Request $request): bool
    {
        if ($request->expectsJson() || $request->ajax()) {
            return true;
        }

        $accept = mb_strtolower((string) $request->header('accept', ''), 'UTF-8');

        return str_contains($accept, 'application/json');
    }

    private function resolveActivePosSession(Request $request, Company $company): ?PosSession
    {
        $posSessionId = (int) $request->session()->get('pos.auth.pos_session_id', 0);
        if ($posSessionId <= 0) {
            return null;
        }

        return PosSession::query()
            ->where('id', $posSessionId)
            ->where('status', 'active')
            ->whereHas('register.branch', function ($query) use ($company): void {
                $query->where('company_id', $company->id);
            })
            ->first();
    }
}
