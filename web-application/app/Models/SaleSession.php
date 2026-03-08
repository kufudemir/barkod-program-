<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class SaleSession extends Model
{
    protected $fillable = [
        'pos_session_id',
        'source_device_uid',
        'source_label',
        'created_by_mobile_user_id',
        'status',
    ];

    public function posSession(): BelongsTo
    {
        return $this->belongsTo(PosSession::class);
    }

    public function createdByMobileUser(): BelongsTo
    {
        return $this->belongsTo(MobileUser::class, 'created_by_mobile_user_id');
    }

    public function items(): HasMany
    {
        return $this->hasMany(SaleSessionItem::class);
    }
}
