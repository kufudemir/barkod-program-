<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class WebSale extends Model
{
    protected $fillable = [
        'company_id',
        'branch_id',
        'register_id',
        'pos_session_id',
        'created_by_user_id',
        'created_by_mobile_user_id',
        'total_items',
        'total_amount_kurus',
        'total_cost_kurus',
        'profit_kurus',
        'completed_at',
    ];

    protected function casts(): array
    {
        return [
            'completed_at' => 'datetime',
        ];
    }

    public function company(): BelongsTo
    {
        return $this->belongsTo(Company::class);
    }

    public function createdByUser(): BelongsTo
    {
        return $this->belongsTo(User::class, 'created_by_user_id');
    }

    public function createdByMobileUser(): BelongsTo
    {
        return $this->belongsTo(MobileUser::class, 'created_by_mobile_user_id');
    }

    public function branch(): BelongsTo
    {
        return $this->belongsTo(Branch::class);
    }

    public function register(): BelongsTo
    {
        return $this->belongsTo(Register::class);
    }

    public function posSession(): BelongsTo
    {
        return $this->belongsTo(PosSession::class);
    }

    public function items(): HasMany
    {
        return $this->hasMany(WebSaleItem::class);
    }

    public function payments(): HasMany
    {
        return $this->hasMany(WebSalePayment::class);
    }
}
