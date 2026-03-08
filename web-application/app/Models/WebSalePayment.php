<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class WebSalePayment extends Model
{
    protected $fillable = [
        'web_sale_id',
        'method',
        'amount_kurus',
    ];

    public function sale(): BelongsTo
    {
        return $this->belongsTo(WebSale::class, 'web_sale_id');
    }
}

