<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    public function up(): void
    {
        Schema::create('sale_session_items', function (Blueprint $table): void {
            $table->id();
            $table->foreignId('sale_session_id')->constrained('sale_sessions')->cascadeOnDelete();
            $table->string('barcode', 64);
            $table->string('product_name_snapshot');
            $table->string('group_name_snapshot')->nullable();
            $table->integer('quantity')->default(1);
            $table->bigInteger('base_sale_price_kurus');
            $table->bigInteger('applied_sale_price_kurus');
            $table->bigInteger('cost_price_kurus')->default(0);
            $table->string('pricing_mode', 32)->default('list');
            $table->json('pricing_meta_json')->nullable();
            $table->bigInteger('line_total_kurus')->default(0);
            $table->bigInteger('line_profit_kurus')->default(0);
            $table->timestamps();

            $table->unique(['sale_session_id', 'barcode']);
            $table->index(['sale_session_id', 'pricing_mode']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('sale_session_items');
    }
};
