<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('companies', function (Blueprint $table): void {
            $table->foreignId('owner_mobile_user_id')->nullable()->after('company_code')->constrained('mobile_users')->nullOnDelete();
            $table->string('created_via')->default('admin')->after('owner_mobile_user_id')->index();
        });
    }

    public function down(): void
    {
        Schema::table('companies', function (Blueprint $table): void {
            $table->dropConstrainedForeignId('owner_mobile_user_id');
            $table->dropColumn('created_via');
        });
    }
};
