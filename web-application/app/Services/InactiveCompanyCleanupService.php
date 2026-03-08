<?php

namespace App\Services;

use App\Models\Company;
use App\Models\CompanyProductOffer;
use App\Models\Device;
use App\Models\DeviceCompanyHistory;
use App\Models\GlobalProduct;
use App\Models\GlobalProductNameCandidate;
use App\Models\SyncBatch;
use App\Models\SyncEventDedup;
use App\Models\SystemSetting;
use Carbon\CarbonImmutable;
use Illuminate\Support\Collection;
use Illuminate\Support\Facades\DB;

class InactiveCompanyCleanupService
{
    public const ENABLED_KEY = 'inactive_company_cleanup_enabled';
    public const DAYS_KEY = 'inactive_company_cleanup_days';
    public const LAST_RUN_KEY = 'inactive_company_cleanup_last_run_at';

    public function isEnabled(): bool
    {
        return SystemSetting::getValue(self::ENABLED_KEY, '0') === '1';
    }

    public function thresholdDays(): int
    {
        return max(1, (int) (SystemSetting::getValue(self::DAYS_KEY, '30') ?? '30'));
    }

    public function saveSettings(bool $enabled, int $days): void
    {
        SystemSetting::putValue(self::ENABLED_KEY, $enabled ? '1' : '0');
        SystemSetting::putValue(self::DAYS_KEY, (string) max(1, $days));
    }

    public function lastRunAt(): ?CarbonImmutable
    {
        $value = SystemSetting::getValue(self::LAST_RUN_KEY);

        return $value ? CarbonImmutable::parse($value) : null;
    }

    public function previewCount(?int $days = null): int
    {
        return $this->candidateCompanies($days)->count();
    }

    public function run(?int $days = null): int
    {
        $companies = $this->candidateCompanies($days);

        if ($companies->isEmpty()) {
            SystemSetting::putValue(self::LAST_RUN_KEY, now()->toIso8601String());
            return 0;
        }

        DB::transaction(function () use ($companies): void {
            foreach ($companies as $company) {
                $deviceIds = Device::query()
                    ->where('company_id', $company->id)
                    ->pluck('id');

                if ($deviceIds->isNotEmpty()) {
                    SyncEventDedup::query()->whereIn('device_id', $deviceIds)->delete();
                    SyncBatch::query()->whereIn('device_id', $deviceIds)->delete();
                }

                CompanyProductOffer::query()->where('company_id', $company->id)->delete();
                DeviceCompanyHistory::query()->where('company_id', $company->id)->delete();
                Device::query()->where('company_id', $company->id)->delete();
                Company::query()->whereKey($company->id)->delete();
            }

            GlobalProductNameCandidate::query()
                ->whereNotIn('barcode', CompanyProductOffer::query()->select('barcode')->distinct())
                ->delete();

            GlobalProduct::query()
                ->whereNotIn('barcode', CompanyProductOffer::query()->select('barcode')->distinct())
                ->delete();
        });

        SystemSetting::putValue(self::LAST_RUN_KEY, now()->toIso8601String());

        return $companies->count();
    }

    public function runIfEnabled(): int
    {
        if (! $this->isEnabled()) {
            return 0;
        }

        $lastRunAt = $this->lastRunAt();
        if ($lastRunAt !== null && $lastRunAt->isSameDay(now())) {
            return 0;
        }

        return $this->run();
    }

    private function candidateCompanies(?int $days = null): Collection
    {
        $thresholdDays = max(1, $days ?? $this->thresholdDays());
        $cutoff = now()->subDays($thresholdDays);

        return Company::query()
            ->where('created_via', 'guest')
            ->whereNull('owner_mobile_user_id')
            ->where('status', 'active')
            ->get()
            ->filter(function (Company $company) use ($cutoff): bool {
                $lastActivity = collect([
                    optional($company->devices()->latest('last_seen_at')->first())->last_seen_at,
                    optional($company->devices()->latest('last_sync_at')->first())->last_sync_at,
                    optional($company->devices()->latest('updated_at')->first())->updated_at,
                    $company->updated_at,
                ])->filter()->sortDesc()->first();

                if ($lastActivity === null) {
                    return true;
                }

                return $lastActivity->lt($cutoff);
            })
            ->values();
    }
}
