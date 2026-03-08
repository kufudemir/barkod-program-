<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class PosSession extends Model
{
    protected $fillable = [
        'register_id',
        'opened_by_mobile_user_id',
        'status',
        'opened_at',
        'closed_at',
        'last_activity_at',
    ];

    protected function casts(): array
    {
        return [
            'opened_at' => 'datetime',
            'closed_at' => 'datetime',
            'last_activity_at' => 'datetime',
        ];
    }

    public function register(): BelongsTo
    {
        return $this->belongsTo(Register::class);
    }

    public function openedByMobileUser(): BelongsTo
    {
        return $this->belongsTo(MobileUser::class, 'opened_by_mobile_user_id');
    }

    public function webSales(): HasMany
    {
        return $this->hasMany(WebSale::class);
    }

    public function saleSessions(): HasMany
    {
        return $this->hasMany(SaleSession::class);
    }

    public function isActive(): bool
    {
        return $this->status === 'active';
    }
}
