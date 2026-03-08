<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('branches', function (Blueprint $table): void {
            $table->id();
            $table->foreignId('company_id')->constrained()->cascadeOnDelete();
            $table->string('name');
            $table->string('code', 32);
            $table->string('status', 16)->default('active');
            $table->timestamps();

            $table->unique(['company_id', 'code']);
            $table->index(['company_id', 'status']);
        });

        Schema::create('registers', function (Blueprint $table): void {
            $table->id();
            $table->foreignId('branch_id')->constrained('branches')->cascadeOnDelete();
            $table->string('name');
            $table->string('code', 32);
            $table->string('status', 16)->default('active');
            $table->timestamps();

            $table->unique(['branch_id', 'code']);
            $table->index(['branch_id', 'status']);
        });

        Schema::create('pos_sessions', function (Blueprint $table): void {
            $table->id();
            $table->foreignId('register_id')->constrained('registers')->cascadeOnDelete();
            $table->foreignId('opened_by_mobile_user_id')->nullable()->constrained('mobile_users')->nullOnDelete();
            $table->string('status', 16)->default('active');
            $table->timestamp('opened_at');
            $table->timestamp('closed_at')->nullable();
            $table->timestamp('last_activity_at')->nullable();
            $table->timestamps();

            $table->index(['register_id', 'status']);
            $table->index(['status', 'last_activity_at']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('pos_sessions');
        Schema::dropIfExists('registers');
        Schema::dropIfExists('branches');
    }
};
