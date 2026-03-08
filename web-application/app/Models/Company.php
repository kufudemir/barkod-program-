<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Support\Str;

class Company extends Model
{
    protected $fillable = [
        'name',
        'company_code',
        'owner_mobile_user_id',
        'created_via',
        'status',
    ];

    protected static function booted(): void
    {
        static::creating(function (Company $company): void {
            if (filled($company->company_code)) {
                return;
            }

            $base = Str::upper(Str::slug(Str::limit($company->name, 12, ''), '-'));
            $base = str_replace('-', '', $base);
            $base = Str::limit($base, 8, '');

            do {
                $candidate = sprintf('%s-%03d', $base ?: 'FIRMA', random_int(100, 999));
            } while (static::query()->where('company_code', $candidate)->exists());

            $company->company_code = $candidate;
        });
    }

    public function devices(): HasMany
    {
        return $this->hasMany(Device::class);
    }

    public function productOffers(): HasMany
    {
        return $this->hasMany(CompanyProductOffer::class);
    }

    public function branches(): HasMany
    {
        return $this->hasMany(Branch::class);
    }

    public function ownerMobileUser(): BelongsTo
    {
        return $this->belongsTo(MobileUser::class, 'owner_mobile_user_id');
    }

    public function deviceHistories(): HasMany
    {
        return $this->hasMany(DeviceCompanyHistory::class);
    }

    public function licenses(): HasMany
    {
        return $this->hasMany(CompanyLicense::class, 'company_id');
    }

    public function staffRoles(): HasMany
    {
        return $this->hasMany(CompanyStaffRole::class, 'company_id');
    }

    public function licenseRequests(): HasMany
    {
        return $this->hasMany(LicenseRequest::class, 'company_id');
    }

    public function feedbackReports(): HasMany
    {
        return $this->hasMany(FeedbackReport::class, 'company_id');
    }

    public function isActive(): bool
    {
        return $this->status === 'active';
    }
}
