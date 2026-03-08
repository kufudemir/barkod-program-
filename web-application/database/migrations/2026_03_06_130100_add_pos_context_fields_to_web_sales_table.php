<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('web_sales', function (Blueprint $table): void {
            $table->foreignId('branch_id')->nullable()->constrained('branches')->nullOnDelete();
            $table->foreignId('register_id')->nullable()->constrained('registers')->nullOnDelete();
            $table->foreignId('pos_session_id')->nullable()->constrained('pos_sessions')->nullOnDelete();
            $table->foreignId('created_by_mobile_user_id')->nullable()->constrained('mobile_users')->nullOnDelete();

            $table->index(['company_id', 'branch_id', 'register_id'], 'web_sales_company_branch_register_idx');
        });
    }

    public function down(): void
    {
        Schema::table('web_sales', function (Blueprint $table): void {
            $table->dropIndex('web_sales_company_branch_register_idx');
            $table->dropConstrainedForeignId('created_by_mobile_user_id');
            $table->dropConstrainedForeignId('pos_session_id');
            $table->dropConstrainedForeignId('register_id');
            $table->dropConstrainedForeignId('branch_id');
        });
    }
};
