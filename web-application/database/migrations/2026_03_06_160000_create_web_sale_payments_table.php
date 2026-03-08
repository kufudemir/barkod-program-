<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('web_sale_payments', function (Blueprint $table): void {
            $table->id();
            $table->foreignId('web_sale_id')->constrained('web_sales')->cascadeOnDelete();
            $table->string('method', 24);
            $table->unsignedBigInteger('amount_kurus');
            $table->timestamps();

            $table->index(['web_sale_id', 'method']);
            $table->index('method');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('web_sale_payments');
    }
};

