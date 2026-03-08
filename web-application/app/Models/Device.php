<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\Relations\HasOne;
use Illuminate\Database\Eloquent\Model;

class Device extends Model
{
    protected $fillable = [
        'company_id',
        'device_uid',
        'device_name',
        'platform',
        'activation_token_hash',
        'is_active',
        'last_sync_at',
        'last_seen_at',
    ];

    protected function casts(): array
    {
        return [
            'is_active' => 'boolean',
            'last_sync_at' => 'datetime',
            'last_seen_at' => 'datetime',
        ];
    }

    public function company(): BelongsTo
    {
        return $this->belongsTo(Company::class);
    }

    public function syncBatches(): HasMany
    {
        return $this->hasMany(SyncBatch::class);
    }

    public function latestSyncBatch(): HasOne
    {
        return $this->hasOne(SyncBatch::class)->latestOfMany('created_at');
    }
}
