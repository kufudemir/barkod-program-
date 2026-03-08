<?php

namespace App\Services;

use App\Models\CompanyLicense;
use App\Models\FeatureFlag;

class CompanyLicenseResolver
{
    public function resolveFeature(CompanyLicense $license, string $featureKey): bool
    {
        $feature = FeatureFlag::query()
            ->where('key', $featureKey)
            ->first();

        if ($feature === null) {
            return false;
        }

        // Resolution order: core > company override > package matrix.
        if ($feature->is_core) {
            return true;
        }

        $override = $license->featureOverrides()
            ->where('feature_flag_id', $feature->id)
            ->first();

        if ($override !== null) {
            return (bool) $override->is_enabled;
        }

        $matrix = $license->package
            ?->features()
            ->where('feature_flag_id', $feature->id)
            ->first();

        return (bool) ($matrix?->is_enabled ?? false);
    }
}
