<?php

namespace App\Services;

use App\Models\Company;
use App\Models\CompanyLicense;
use App\Models\FeatureFlag;
use App\Models\MobileUser;

class CompanyLicenseSummaryService
{
    public function __construct(
        private readonly CompanyLicenseResolver $resolver,
    ) {
    }

    /**
     * @return array<string, mixed>
     */
    public function buildForUser(MobileUser $mobileUser, ?string $companyCode = null): array
    {
        $companyQuery = Company::query()
            ->where('owner_mobile_user_id', $mobileUser->id);

        if (filled($companyCode)) {
            $companyQuery->where('company_code', trim((string) $companyCode));
        } else {
            $companyQuery->orderByDesc('updated_at');
        }

        $company = $companyQuery->first();

        if ($company === null) {
            throw new \RuntimeException('Firma bulunamadı');
        }

        $activeLicense = CompanyLicense::query()
            ->with('package')
            ->where('company_id', $company->id)
            ->where('status', 'active')
            ->where('starts_at', '<=', now())
            ->where(function ($query): void {
                $query->whereNull('expires_at')->orWhere('expires_at', '>=', now());
            })
            ->latest('id')
            ->first();

        $latestLicense = $activeLicense ?: CompanyLicense::query()
            ->with('package')
            ->where('company_id', $company->id)
            ->latest('id')
            ->first();

        $features = [];

        if ($activeLicense !== null) {
            $features = FeatureFlag::query()
                ->orderBy('key')
                ->get()
                ->map(function (FeatureFlag $feature) use ($activeLicense): array {
                    return [
                        'key' => $feature->key,
                        'title' => $feature->title,
                        'scope' => $feature->scope,
                        'isCore' => (bool) $feature->is_core,
                        'isEnabled' => $this->resolver->resolveFeature($activeLicense, $feature->key),
                    ];
                })
                ->values()
                ->all();
        }

        return [
            'company' => [
                'companyId' => $company->id,
                'companyName' => $company->name,
                'companyCode' => $company->company_code,
            ],
            'license' => $latestLicense ? [
                'licenseId' => $latestLicense->id,
                'packageCode' => $latestLicense->package?->code,
                'packageName' => $latestLicense->package?->name,
                'status' => $latestLicense->status,
                'startsAt' => $latestLicense->starts_at?->valueOf(),
                'expiresAt' => $latestLicense->expires_at?->valueOf(),
                'source' => $latestLicense->source,
                'note' => $latestLicense->note,
                'isCurrentlyActive' => $activeLicense?->id === $latestLicense->id,
            ] : null,
            'pendingRequestCount' => $company->licenseRequests()
                ->whereIn('status', ['pending_payment', 'payment_review'])
                ->count(),
            'features' => $features,
            'generatedAt' => now()->valueOf(),
        ];
    }
}
