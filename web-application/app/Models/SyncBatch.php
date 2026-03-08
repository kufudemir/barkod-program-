<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Model;

class SyncBatch extends Model
{
    public $timestamps = false;

    protected $fillable = [
        'company_id',
        'device_id',
        'batch_uuid',
        'received_event_count',
        'processed_event_count',
        'status',
        'error_summary',
        'created_at',
    ];

    protected function casts(): array
    {
        return [
            'created_at' => 'datetime',
        ];
    }

    public function company(): BelongsTo
    {
        return $this->belongsTo(Company::class);
    }

    public function device(): BelongsTo
    {
        return $this->belongsTo(Device::class);
    }
}
