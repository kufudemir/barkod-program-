<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class CompanyLicenseEvent extends Model
{
    public $timestamps = false;

    protected $fillable = [
        'company_license_id',
        'event_type',
        'payload_json',
        'created_at',
    ];

    protected function casts(): array
    {
        return [
            'company_license_id' => 'integer',
            'payload_json' => 'array',
            'created_at' => 'datetime',
        ];
    }

    public function companyLicense(): BelongsTo
    {
        return $this->belongsTo(CompanyLicense::class, 'company_license_id');
    }
}
