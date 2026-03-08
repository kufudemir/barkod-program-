<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class LicensePackageFeature extends Model
{
    protected $fillable = [
        'package_id',
        'feature_flag_id',
        'is_enabled',
    ];

    protected function casts(): array
    {
        return [
            'package_id' => 'integer',
            'feature_flag_id' => 'integer',
            'is_enabled' => 'boolean',
        ];
    }

    public function package(): BelongsTo
    {
        return $this->belongsTo(LicensePackage::class, 'package_id');
    }

    public function featureFlag(): BelongsTo
    {
        return $this->belongsTo(FeatureFlag::class, 'feature_flag_id');
    }
}
