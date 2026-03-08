<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class CompanyLicense extends Model
{
    protected $fillable = [
        'company_id',
        'package_id',
        'status',
        'starts_at',
        'expires_at',
        'assigned_by_admin_user_id',
        'source',
        'note',
    ];

    protected function casts(): array
    {
        return [
            'company_id' => 'integer',
            'package_id' => 'integer',
            'assigned_by_admin_user_id' => 'integer',
            'starts_at' => 'datetime',
            'expires_at' => 'datetime',
        ];
    }

    public function company(): BelongsTo
    {
        return $this->belongsTo(Company::class, 'company_id');
    }

    public function package(): BelongsTo
    {
        return $this->belongsTo(LicensePackage::class, 'package_id');
    }

    public function assignedByAdminUser(): BelongsTo
    {
        return $this->belongsTo(User::class, 'assigned_by_admin_user_id');
    }

    public function featureOverrides(): HasMany
    {
        return $this->hasMany(CompanyLicenseFeatureOverride::class, 'company_license_id');
    }

    public function events(): HasMany
    {
        return $this->hasMany(CompanyLicenseEvent::class, 'company_license_id');
    }
}
