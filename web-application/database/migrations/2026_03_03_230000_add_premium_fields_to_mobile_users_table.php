<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('mobile_users', function (Blueprint $table): void {
            $table->string('premium_tier')->default('FREE')->after('status');
            $table->string('premium_source')->default('NONE')->after('premium_tier');
            $table->timestamp('premium_activated_at')->nullable()->after('premium_source');
            $table->timestamp('premium_expires_at')->nullable()->after('premium_activated_at');
            $table->string('premium_license_mask')->nullable()->after('premium_expires_at');
        });
    }

    public function down(): void
    {
        Schema::table('mobile_users', function (Blueprint $table): void {
            $table->dropColumn([
                'premium_tier',
                'premium_source',
                'premium_activated_at',
                'premium_expires_at',
                'premium_license_mask',
            ]);
        });
    }
};
