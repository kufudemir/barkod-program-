<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\MobileCompanyAccessService;
use App\Services\MobileUserTokenManager;
use Illuminate\Support\Carbon;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class GetOwnedCompanyCatalogChangesController extends Controller
{
    public function __invoke(
        Request $request,
        string $companyCode,
        MobileUserTokenManager $tokenManager,
        MobileCompanyAccessService $companyAccessService
    ): JsonResponse {
        $mobileUser = $tokenManager->findActiveUserByToken($request->bearerToken());
        if ($mobileUser === null) {
            return response()->json(['message' => 'Gecersiz oturum'], 401);
        }

        $company = $companyAccessService->findAccessibleCompany($mobileUser, $companyCode);
        if ($company === null) {
            return response()->json(['message' => 'Firma bulunamadi'], 404);
        }

        $validated = $request->validate([
            'sinceUpdatedAt' => ['nullable', 'integer', 'min:0'],
            'limit' => ['nullable', 'integer', 'min:1', 'max:500'],
        ]);

        $sinceUpdatedAt = (int) ($validated['sinceUpdatedAt'] ?? 0);
        $limit = (int) ($validated['limit'] ?? 200);

        $query = $company->productOffers()
            ->with('globalProduct')
            ->orderBy('updated_at')
            ->orderBy('id');

        if ($sinceUpdatedAt > 0) {
            $query->where(
                'updated_at',
                '>',
                Carbon::createFromTimestampUTC((int) floor($sinceUpdatedAt / 1000))
                    ->setTimezone(config('app.timezone'))
            );
        }

        $offers = $query->limit($limit + 1)->get();
        $hasMore = $offers->count() > $limit;
        $items = $hasMore ? $offers->take($limit) : $offers;

        $maxCursor = $sinceUpdatedAt;
        $changes = $items->map(static function ($offer) use (&$maxCursor) {
            $updatedAt = (int) ($offer->updated_at?->valueOf() ?? now()->valueOf());
            if ($updatedAt > $maxCursor) {
                $maxCursor = $updatedAt;
            }

            return [
                'barcode' => $offer->barcode,
                'name' => $offer->globalProduct?->canonical_name ?: $offer->barcode,
                'groupName' => $offer->group_name ?: $offer->globalProduct?->group_name,
                'salePriceKurus' => $offer->sale_price_kurus,
                'costPriceKurus' => $offer->cost_price_kurus,
                'note' => $offer->note,
                'isActive' => (bool) $offer->is_active,
                'updatedAt' => $updatedAt,
            ];
        })->values();

        return response()->json([
            'company' => [
                'companyId' => $company->id,
                'companyName' => $company->name,
                'companyCode' => $company->company_code,
            ],
            'sinceUpdatedAt' => $sinceUpdatedAt,
            'nextCursor' => $maxCursor,
            'hasMore' => $hasMore,
            'changes' => $changes,
        ]);
    }
}
