<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Company;
use App\Models\DeviceCompanyHistory;
use App\Services\MobileUserTokenManager;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class ListOwnedCompaniesController extends Controller
{
    public function __invoke(Request $request, MobileUserTokenManager $tokenManager): JsonResponse
    {
        $mobileUser = $tokenManager->findActiveUserByToken($request->bearerToken());

        $deviceUid = trim((string) $request->query('deviceUid', ''));
        $companies = collect();

        if ($mobileUser !== null) {
            $companies = $mobileUser->companies()
                ->withCount([
                    'productOffers as active_product_offers_count' => fn ($query) => $query->where('is_active', true),
                ])
                ->orderByDesc('updated_at')
                ->get();
        }

        if ($deviceUid !== '') {
            $deviceHistoryCompanies = Company::query()
                ->whereIn('id', DeviceCompanyHistory::query()
                    ->where('device_uid', $deviceUid)
                    ->orderByDesc('last_seen_at')
                    ->pluck('company_id'))
                ->withCount([
                    'productOffers as active_product_offers_count' => fn ($offers) => $offers->where('is_active', true),
                ])
                ->get()
                ->sortByDesc(function (Company $company) use ($deviceUid) {
                    return optional(
                        $company->deviceHistories()->where('device_uid', $deviceUid)->latest('last_seen_at')->first()
                    )->last_seen_at;
                })
                ->values();

            foreach ($deviceHistoryCompanies as $deviceHistoryCompany) {
                if (! $companies->contains('id', $deviceHistoryCompany->id)) {
                    $companies->push($deviceHistoryCompany);
                }
            }
        }

        if ($companies->isEmpty() && $mobileUser === null) {
            return response()->json(['companies' => []]);
        }

        return response()->json([
            'companies' => $companies->map(fn ($company) => [
                'companyId' => $company->id,
                'companyName' => $company->name,
                'companyCode' => $company->company_code,
                'createdVia' => $company->created_via,
                'productCount' => (int) $company->active_product_offers_count,
                'lastSyncedAt' => $company->updated_at?->valueOf(),
            ])->values(),
        ]);
    }
}
