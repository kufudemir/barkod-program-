<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('device_company_histories', function (Blueprint $table): void {
            $table->id();
            $table->string('device_uid', 128)->index();
            $table->foreignId('company_id')->constrained()->cascadeOnDelete();
            $table->timestamp('first_seen_at')->nullable();
            $table->timestamp('last_seen_at')->nullable()->index();
            $table->string('activation_source', 32)->default('activation');
            $table->timestamps();

            $table->unique(['device_uid', 'company_id']);
        });

        if (Schema::hasTable('devices')) {
            $rows = DB::table('devices')
                ->select('device_uid', 'company_id', 'created_at', 'updated_at', 'last_seen_at')
                ->whereNotNull('company_id')
                ->get();

            foreach ($rows as $row) {
                DB::table('device_company_histories')->updateOrInsert(
                    [
                        'device_uid' => $row->device_uid,
                        'company_id' => $row->company_id,
                    ],
                    [
                        'first_seen_at' => $row->created_at ?? now(),
                        'last_seen_at' => $row->last_seen_at ?? $row->updated_at ?? now(),
                        'activation_source' => 'migration_backfill',
                        'created_at' => now(),
                        'updated_at' => now(),
                    ]
                );
            }
        }
    }

    public function down(): void
    {
        Schema::dropIfExists('device_company_histories');
    }
};
