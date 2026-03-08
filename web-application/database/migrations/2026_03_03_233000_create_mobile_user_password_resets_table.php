<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('mobile_user_password_resets', function (Blueprint $table): void {
            $table->id();
            $table->foreignId('mobile_user_id')->constrained('mobile_users')->cascadeOnDelete();
            $table->string('email');
            $table->string('code_hash');
            $table->timestamp('expires_at');
            $table->timestamp('used_at')->nullable();
            $table->timestamp('requested_at');
            $table->timestamps();

            $table->index(['mobile_user_id', 'email']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('mobile_user_password_resets');
    }
};
