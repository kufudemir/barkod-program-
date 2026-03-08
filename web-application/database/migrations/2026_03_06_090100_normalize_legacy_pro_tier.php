<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    public function up(): void
    {
        DB::table('mobile_users')
            ->where('premium_tier', 'PRO')
            ->update(['premium_tier' => 'SILVER']);
    }

    public function down(): void
    {
        // Intentionally no-op. SILVER/GOLD tiers are now canonical.
    }
};
