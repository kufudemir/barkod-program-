<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('global_product_name_candidates', function (Blueprint $table) {
            $table->id();
            $table->string('barcode');
            $table->string('candidate_name');
            $table->foreignId('source_company_id')->nullable()->constrained('companies')->nullOnDelete();
            $table->foreignId('source_device_id')->nullable()->constrained('devices')->nullOnDelete();
            $table->timestamp('last_seen_at')->nullable();
            $table->unsignedInteger('seen_count')->default(1);
            $table->timestamps();

            $table->unique(['barcode', 'candidate_name']);
            $table->foreign('barcode')->references('barcode')->on('global_products')->cascadeOnDelete();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('global_product_name_candidates');
    }
};
