<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Support\Facades\Hash;

class MobileUser extends Model
{
    use HasFactory;

    protected $fillable = [
        'name',
        'email',
        'password',
        'status',
        'premium_tier',
        'premium_source',
        'premium_activated_at',
        'premium_expires_at',
        'premium_license_mask',
        'consent_version',
        'consent_accepted_at',
        'last_login_at',
    ];

    protected $hidden = [
        'password',
    ];

    protected function casts(): array
    {
        return [
            'last_login_at' => 'datetime',
            'premium_activated_at' => 'datetime',
            'premium_expires_at' => 'datetime',
            'consent_accepted_at' => 'datetime',
            'password' => 'hashed',
        ];
    }

    public function accessTokens(): HasMany
    {
        return $this->hasMany(MobileUserAccessToken::class);
    }

    public function companies(): HasMany
    {
        return $this->hasMany(Company::class, 'owner_mobile_user_id');
    }

    public function companyStaffRoles(): HasMany
    {
        return $this->hasMany(CompanyStaffRole::class, 'mobile_user_id');
    }

    public function licenseRequests(): HasMany
    {
        return $this->hasMany(LicenseRequest::class, 'requested_by_mobile_user_id');
    }

    public function openedPosSessions(): HasMany
    {
        return $this->hasMany(PosSession::class, 'opened_by_mobile_user_id');
    }

    public function feedbackReports(): HasMany
    {
        return $this->hasMany(FeedbackReport::class, 'mobile_user_id');
    }

    public function isActive(): bool
    {
        return $this->status === 'active';
    }

    public function verifyPassword(string $plainPassword): bool
    {
        return Hash::check($plainPassword, $this->password);
    }
}
