<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class SaleSessionItem extends Model
{
    protected $fillable = [
        'sale_session_id',
        'barcode',
        'product_name_snapshot',
        'group_name_snapshot',
        'quantity',
        'base_sale_price_kurus',
        'applied_sale_price_kurus',
        'cost_price_kurus',
        'pricing_mode',
        'pricing_meta_json',
        'line_total_kurus',
        'line_profit_kurus',
    ];

    protected function casts(): array
    {
        return [
            'pricing_meta_json' => 'array',
        ];
    }

    public function saleSession(): BelongsTo
    {
        return $this->belongsTo(SaleSession::class);
    }
}
