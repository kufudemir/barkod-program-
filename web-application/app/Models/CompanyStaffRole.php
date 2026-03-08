<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class CompanyStaffRole extends Model
{
    protected $fillable = [
        'company_id',
        'mobile_user_id',
        'role',
        'status',
        'created_by_mobile_user_id',
    ];

    public function company(): BelongsTo
    {
        return $this->belongsTo(Company::class);
    }

    public function mobileUser(): BelongsTo
    {
        return $this->belongsTo(MobileUser::class);
    }

    public function createdBy(): BelongsTo
    {
        return $this->belongsTo(MobileUser::class, 'created_by_mobile_user_id');
    }
}

