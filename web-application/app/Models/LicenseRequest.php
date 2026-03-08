<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class LicenseRequest extends Model
{
    protected $fillable = [
        'company_id',
        'requested_by_mobile_user_id',
        'requester_name',
        'requester_email',
        'requester_phone',
        'requested_package_code',
        'status',
        'bank_reference_note',
        'admin_note',
    ];

    protected function casts(): array
    {
        return [
            'company_id' => 'integer',
            'requested_by_mobile_user_id' => 'integer',
        ];
    }

    public function company(): BelongsTo
    {
        return $this->belongsTo(Company::class, 'company_id');
    }

    public function requestedByMobileUser(): BelongsTo
    {
        return $this->belongsTo(MobileUser::class, 'requested_by_mobile_user_id');
    }
}
