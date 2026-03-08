<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class ReceiptProfile extends Model
{
    use HasFactory;

    protected $fillable = [
        'company_id',
        'branch_id',
        'name',
        'paper_size',
        'header_json',
        'footer_json',
        'visible_fields_json',
        'field_order_json',
        'print_mode',
        'is_default',
    ];

    protected $casts = [
        'header_json' => 'array',
        'footer_json' => 'array',
        'visible_fields_json' => 'array',
        'field_order_json' => 'array',
        'is_default' => 'boolean',
    ];

    public function company(): BelongsTo
    {
        return $this->belongsTo(Company::class);
    }

    public function branch(): BelongsTo
    {
        return $this->belongsTo(Branch::class);
    }
}
