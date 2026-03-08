<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Company;
use App\Models\DeviceCompanyHistory;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class ListDeviceHistoryCompaniesController extends Controller
{
    public function __invoke(Request $request): JsonResponse
    {
        $payload = $request->validate([
            'deviceUid' => ['required', 'string', 'max:128'],
        ]);

        $companies = Company::query()
            ->whereIn('id', DeviceCompanyHistory::query()
                ->where('device_uid', $payload['deviceUid'])
                ->orderByDesc('last_seen_at')
                ->pluck('company_id'))
            ->withCount([
                'productOffers as active_product_offers_count' => fn ($offers) => $offers->where('is_active', true),
            ])
            ->get()
            ->sortByDesc(function (Company $company) use ($payload) {
                return optional(
                    $company->deviceHistories()->where('device_uid', $payload['deviceUid'])->latest('last_seen_at')->first()
                )->last_seen_at;
            })
            ->values();

        return response()->json([
            'companies' => $companies->map(fn (Company $company) => [
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
