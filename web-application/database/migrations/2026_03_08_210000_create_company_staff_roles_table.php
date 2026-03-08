<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('company_staff_roles', function (Blueprint $table): void {
            $table->id();
            $table->foreignId('company_id')->constrained()->cascadeOnDelete();
            $table->foreignId('mobile_user_id')->constrained('mobile_users')->cascadeOnDelete();
            $table->string('role', 24)->index();
            $table->string('status', 24)->default('active')->index();
            $table->foreignId('created_by_mobile_user_id')->nullable()->constrained('mobile_users')->nullOnDelete();
            $table->timestamps();

            $table->unique(['company_id', 'mobile_user_id']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('company_staff_roles');
    }
};

