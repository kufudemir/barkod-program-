<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('web_sale_items', function (Blueprint $table): void {
            $table->id();
            $table->foreignId('web_sale_id')->constrained('web_sales')->cascadeOnDelete();
            $table->string('barcode', 64);
            $table->string('product_name_snapshot');
            $table->string('group_name_snapshot')->nullable();
            $table->unsignedInteger('quantity');
            $table->unsignedBigInteger('unit_sale_price_kurus_snapshot');
            $table->unsignedBigInteger('unit_cost_price_kurus_snapshot');
            $table->unsignedBigInteger('line_total_kurus');
            $table->bigInteger('line_profit_kurus');
            $table->timestamps();

            $table->index('barcode');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('web_sale_items');
    }
};