<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class FeatureFlag extends Model
{
    protected $fillable = [
        'key',
        'title',
        'description',
        'scope',
        'is_core',
    ];

    protected function casts(): array
    {
        return [
            'is_core' => 'boolean',
        ];
    }

    public function packageFeatures(): HasMany
    {
        return $this->hasMany(LicensePackageFeature::class, 'feature_flag_id');
    }

    public function companyOverrides(): HasMany
    {
        return $this->hasMany(CompanyLicenseFeatureOverride::class, 'feature_flag_id');
    }
}
