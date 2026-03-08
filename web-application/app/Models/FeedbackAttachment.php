<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class FeedbackAttachment extends Model
{
    use HasFactory;

    public $timestamps = false;

    protected $fillable = [
        'feedback_report_id',
        'feedback_message_id',
        'file_path',
        'mime_type',
    ];

    protected $casts = [
        'created_at' => 'datetime',
    ];

    public function report(): BelongsTo
    {
        return $this->belongsTo(FeedbackReport::class, 'feedback_report_id');
    }

    public function message(): BelongsTo
    {
        return $this->belongsTo(FeedbackMessage::class, 'feedback_message_id');
    }
}
