<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('sale_sessions', function (Blueprint $table): void {
            $table->id();
            $table->foreignId('pos_session_id')->constrained('pos_sessions')->cascadeOnDelete();
            $table->string('source_device_uid', 128)->nullable();
            $table->string('source_label', 64);
            $table->foreignId('created_by_mobile_user_id')->nullable()->constrained('mobile_users')->nullOnDelete();
            $table->string('status', 16)->default('active');
            $table->timestamps();

            $table->index(['pos_session_id', 'status']);
            $table->index(['pos_session_id', 'source_label']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('sale_sessions');
    }
};
