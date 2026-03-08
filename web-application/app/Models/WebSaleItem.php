<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class WebSaleItem extends Model
{
    protected $fillable = [
        'web_sale_id',
        'barcode',
        'product_name_snapshot',
        'group_name_snapshot',
        'quantity',
        'unit_sale_price_kurus_snapshot',
        'unit_cost_price_kurus_snapshot',
        'line_total_kurus',
        'line_profit_kurus',
    ];

    public function sale(): BelongsTo
    {
        return $this->belongsTo(WebSale::class, 'web_sale_id');
    }
}