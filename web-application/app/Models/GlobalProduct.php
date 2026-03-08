<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class GlobalProduct extends Model
{
    protected $primaryKey = 'barcode';
    public $incrementing = false;
    protected $keyType = 'string';

    protected $fillable = [
        'barcode',
        'canonical_name',
        'group_name',
        'last_source_company_id',
        'last_source_device_id',
        'last_synced_at',
    ];

    protected function casts(): array
    {
        return [
            'last_synced_at' => 'datetime',
        ];
    }

    public function lastSourceCompany(): BelongsTo
    {
        return $this->belongsTo(Company::class, 'last_source_company_id');
    }

    public function lastSourceDevice(): BelongsTo
    {
        return $this->belongsTo(Device::class, 'last_source_device_id');
    }

    public function candidates(): HasMany
    {
        return $this->hasMany(GlobalProductNameCandidate::class, 'barcode', 'barcode');
    }

    public function companyOffers(): HasMany
    {
        return $this->hasMany(CompanyProductOffer::class, 'barcode', 'barcode');
    }
}