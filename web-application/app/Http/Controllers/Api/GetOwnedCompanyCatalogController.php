<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\MobileCompanyAccessService;
use App\Services\MobileUserTokenManager;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class GetOwnedCompanyCatalogController extends Controller
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

        $offers = $company->productOffers()
            ->with('globalProduct')
            ->where('is_active', true)
            ->orderByDesc('updated_at')
            ->get();

        $cursor = (int) $offers->max(static fn ($offer): int => (int) ($offer->updated_at?->valueOf() ?? 0));

        return response()->json([
            'company' => [
                'companyId' => $company->id,
                'companyName' => $company->name,
                'companyCode' => $company->company_code,
            ],
            'products' => $offers->map(static function ($offer) {
                return [
                    'barcode' => $offer->barcode,
                    'name' => $offer->globalProduct?->canonical_name ?: $offer->barcode,
                    'groupName' => $offer->group_name ?: $offer->globalProduct?->group_name,
                    'salePriceKurus' => $offer->sale_price_kurus,
                    'costPriceKurus' => $offer->cost_price_kurus,
                    'note' => $offer->note,
                    'updatedAt' => (int) ($offer->updated_at?->valueOf() ?? now()->valueOf()),
                ];
            })->values(),
            'cursor' => $cursor,
        ]);
    }
}
