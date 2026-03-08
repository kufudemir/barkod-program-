<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class CompanyLicenseFeatureOverride extends Model
{
    protected $fillable = [
        'company_license_id',
        'feature_flag_id',
        'is_enabled',
        'reason',
    ];

    protected function casts(): array
    {
        return [
            'company_license_id' => 'integer',
            'feature_flag_id' => 'integer',
            'is_enabled' => 'boolean',
        ];
    }

    public function companyLicense(): BelongsTo
    {
        return $this->belongsTo(CompanyLicense::class, 'company_license_id');
    }

    public function featureFlag(): BelongsTo
    {
        return $this->belongsTo(FeatureFlag::class, 'feature_flag_id');
    }
}
