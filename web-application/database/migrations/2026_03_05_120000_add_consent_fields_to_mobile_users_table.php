<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('mobile_users', function (Blueprint $table): void {
            $table->string('consent_version', 32)->nullable()->after('premium_license_mask');
            $table->timestamp('consent_accepted_at')->nullable()->after('consent_version');
        });
    }

    public function down(): void
    {
        Schema::table('mobile_users', function (Blueprint $table): void {
            $table->dropColumn(['consent_version', 'consent_accepted_at']);
        });
    }
};
