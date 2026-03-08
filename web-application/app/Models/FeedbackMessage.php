<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class FeedbackMessage extends Model
{
    use HasFactory;

    public $timestamps = false;

    protected $fillable = [
        'feedback_report_id',
        'author_type',
        'author_id',
        'message',
        'is_internal_note',
    ];

    protected $casts = [
        'is_internal_note' => 'boolean',
        'created_at' => 'datetime',
    ];

    public function report(): BelongsTo
    {
        return $this->belongsTo(FeedbackReport::class, 'feedback_report_id');
    }

    public function attachments(): HasMany
    {
        return $this->hasMany(FeedbackAttachment::class, 'feedback_message_id');
    }
}
