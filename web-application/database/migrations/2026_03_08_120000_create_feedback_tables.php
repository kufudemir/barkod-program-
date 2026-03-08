<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('feedback_reports', function (Blueprint $table): void {
            $table->id();
            $table->string('type', 32)->index(); // bug | feature_request | general
            $table->string('source', 32)->index(); // mobile | web_pos
            $table->foreignId('company_id')->nullable()->constrained()->nullOnDelete();
            $table->foreignId('mobile_user_id')->nullable()->constrained('mobile_users')->nullOnDelete();
            $table->string('device_uid', 191)->nullable()->index();
            $table->string('app_version', 64)->nullable();
            $table->string('web_url', 512)->nullable();
            $table->string('title', 191);
            $table->text('description');
            $table->string('status', 32)->default('new')->index(); // new | reviewing | answered | closed
            $table->timestamps();
        });

        Schema::create('feedback_messages', function (Blueprint $table): void {
            $table->id();
            $table->foreignId('feedback_report_id')->constrained('feedback_reports')->cascadeOnDelete();
            $table->string('author_type', 32); // user | admin
            $table->unsignedBigInteger('author_id')->nullable();
            $table->text('message');
            $table->boolean('is_internal_note')->default(false);
            $table->timestamp('created_at')->useCurrent();

            $table->index(['feedback_report_id', 'created_at']);
            $table->index(['author_type', 'author_id']);
        });

        Schema::create('feedback_attachments', function (Blueprint $table): void {
            $table->id();
            $table->foreignId('feedback_report_id')->constrained('feedback_reports')->cascadeOnDelete();
            $table->foreignId('feedback_message_id')->nullable()->constrained('feedback_messages')->nullOnDelete();
            $table->string('file_path', 512);
            $table->string('mime_type', 191)->nullable();
            $table->timestamp('created_at')->useCurrent();

            $table->index(['feedback_report_id', 'created_at']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('feedback_attachments');
        Schema::dropIfExists('feedback_messages');
        Schema::dropIfExists('feedback_reports');
    }
};
