<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('receipt_profiles', function (Blueprint $table): void {
            $table->id();
            $table->foreignId('company_id')->constrained()->cascadeOnDelete();
            $table->foreignId('branch_id')->nullable()->constrained()->nullOnDelete();
            $table->string('name', 120);
            $table->string('paper_size', 16)->default('80mm');
            $table->json('header_json')->nullable();
            $table->json('footer_json')->nullable();
            $table->json('visible_fields_json')->nullable();
            $table->json('field_order_json')->nullable();
            $table->string('print_mode', 16)->default('browser');
            $table->boolean('is_default')->default(false);
            $table->timestamps();

            $table->index(['company_id', 'branch_id']);
            $table->index(['company_id', 'is_default']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('receipt_profiles');
    }
};
