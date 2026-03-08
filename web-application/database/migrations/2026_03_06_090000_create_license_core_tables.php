<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('feature_flags', function (Blueprint $table): void {
            $table->id();
            $table->string('key')->unique();
            $table->string('title');
            $table->text('description')->nullable();
            $table->string('scope');
            $table->boolean('is_core')->default(false);
            $table->timestamps();
        });

        Schema::create('license_packages', function (Blueprint $table): void {
            $table->id();
            $table->string('code')->unique();
            $table->string('name');
            $table->text('description')->nullable();
            $table->unsignedInteger('sort_order')->default(0);
            $table->boolean('is_active')->default(true);
            $table->timestamps();
        });

        Schema::create('license_package_features', function (Blueprint $table): void {
            $table->id();
            $table->foreignId('package_id')->constrained('license_packages')->cascadeOnDelete();
            $table->foreignId('feature_flag_id')->constrained('feature_flags')->cascadeOnDelete();
            $table->boolean('is_enabled')->default(false);
            $table->timestamps();
            $table->unique(['package_id', 'feature_flag_id'], 'license_package_features_unique');
        });

        Schema::create('company_licenses', function (Blueprint $table): void {
            $table->id();
            $table->foreignId('company_id')->constrained('companies')->cascadeOnDelete();
            $table->foreignId('package_id')->constrained('license_packages')->restrictOnDelete();
            $table->string('status')->default('active');
            $table->timestamp('starts_at');
            $table->timestamp('expires_at')->nullable();
            $table->foreignId('assigned_by_admin_user_id')->nullable()->constrained('users')->nullOnDelete();
            $table->string('source')->default('manual_admin');
            $table->text('note')->nullable();
            $table->timestamps();
            $table->index(['company_id', 'status']);
        });

        Schema::create('company_license_feature_overrides', function (Blueprint $table): void {
            $table->id();
            $table->foreignId('company_license_id')->constrained('company_licenses')->cascadeOnDelete();
            $table->foreignId('feature_flag_id')->constrained('feature_flags')->cascadeOnDelete();
            $table->boolean('is_enabled');
            $table->text('reason')->nullable();
            $table->timestamps();
            $table->unique(['company_license_id', 'feature_flag_id'], 'company_license_overrides_unique');
        });

        Schema::create('license_requests', function (Blueprint $table): void {
            $table->id();
            $table->foreignId('company_id')->nullable()->constrained('companies')->nullOnDelete();
            $table->foreignId('requested_by_mobile_user_id')->nullable()->constrained('mobile_users')->nullOnDelete();
            $table->string('requester_name');
            $table->string('requester_email');
            $table->string('requester_phone')->nullable();
            $table->string('requested_package_code');
            $table->string('status')->default('pending_payment');
            $table->string('bank_reference_note')->nullable();
            $table->text('admin_note')->nullable();
            $table->timestamps();
            $table->index(['status', 'requested_package_code'], 'license_requests_status_package_idx');
        });

        Schema::create('company_license_events', function (Blueprint $table): void {
            $table->id();
            $table->foreignId('company_license_id')->constrained('company_licenses')->cascadeOnDelete();
            $table->string('event_type');
            $table->json('payload_json')->nullable();
            $table->timestamp('created_at')->useCurrent();
        });

        $now = now();
        $packages = [
            ['code' => 'FREE', 'name' => 'Ucretsiz', 'description' => 'Baslangic paketi', 'sort_order' => 1],
            ['code' => 'SILVER', 'name' => 'Gumus', 'description' => 'Mobil Pro paketi', 'sort_order' => 2],
            ['code' => 'GOLD', 'name' => 'Altin', 'description' => 'Web POS paketi', 'sort_order' => 3],
        ];

        foreach ($packages as $package) {
            DB::table('license_packages')->insert([
                'code' => $package['code'],
                'name' => $package['name'],
                'description' => $package['description'],
                'sort_order' => $package['sort_order'],
                'is_active' => true,
                'created_at' => $now,
                'updated_at' => $now,
            ]);
        }

        $features = [
            ['key' => 'mobile_scan', 'title' => 'Mobil Barkod Okutma', 'scope' => 'mobile', 'is_core' => true],
            ['key' => 'product_management', 'title' => 'Urun Ekleme ve Duzenleme', 'scope' => 'mobile', 'is_core' => true],
            ['key' => 'basic_cart_sale', 'title' => 'Temel Sepet ve Satis', 'scope' => 'mobile', 'is_core' => true],
            ['key' => 'guest_mode', 'title' => 'Misafir Kullanim', 'scope' => 'shared', 'is_core' => true],
            ['key' => 'cloud_sync', 'title' => 'Bulut Senkron', 'scope' => 'shared', 'is_core' => false],
            ['key' => 'reports', 'title' => 'Raporlar', 'scope' => 'mobile', 'is_core' => false],
            ['key' => 'stock_tracking', 'title' => 'Stok Takibi', 'scope' => 'mobile', 'is_core' => false],
            ['key' => 'ocr_name_suggestion', 'title' => 'OCR Isim Onerme', 'scope' => 'mobile', 'is_core' => false],
            ['key' => 'web_search_name_suggestion', 'title' => 'Web Isim Onerme', 'scope' => 'mobile', 'is_core' => false],
            ['key' => 'barkodbankasi_import', 'title' => 'BarkodBankasi Import', 'scope' => 'mobile', 'is_core' => false],
            ['key' => 'bulk_price_update', 'title' => 'Toplu Fiyat Guncelleme', 'scope' => 'mobile', 'is_core' => false],
            ['key' => 'bulk_stock_update', 'title' => 'Toplu Stok Guncelleme', 'scope' => 'mobile', 'is_core' => false],
            ['key' => 'line_price_override', 'title' => 'Satir Bazli Ozel Fiyat', 'scope' => 'mobile', 'is_core' => false],
            ['key' => 'line_percent_discount', 'title' => 'Satir Bazli Yuzde Indirim', 'scope' => 'mobile', 'is_core' => false],
            ['key' => 'line_fixed_discount', 'title' => 'Satir Bazli TL Indirim', 'scope' => 'mobile', 'is_core' => false],
            ['key' => 'ticket_system', 'title' => 'Ticket Sistemi', 'scope' => 'shared', 'is_core' => false],
            ['key' => 'web_pos', 'title' => 'Web POS', 'scope' => 'web_pos', 'is_core' => false],
            ['key' => 'web_multi_device_tabs', 'title' => 'Coklu Cihaz Sekmeleri', 'scope' => 'web_pos', 'is_core' => false],
            ['key' => 'web_hid_scanner', 'title' => 'HID Barkod Okuyucu', 'scope' => 'web_pos', 'is_core' => false],
            ['key' => 'web_held_sales', 'title' => 'Bekleyen Satis', 'scope' => 'web_pos', 'is_core' => false],
            ['key' => 'web_payment_methods', 'title' => 'Odeme Turleri', 'scope' => 'web_pos', 'is_core' => false],
            ['key' => 'receipt_profiles', 'title' => 'Fis Profilleri', 'scope' => 'shared', 'is_core' => false],
        ];

        foreach ($features as $feature) {
            DB::table('feature_flags')->insert([
                'key' => $feature['key'],
                'title' => $feature['title'],
                'description' => null,
                'scope' => $feature['scope'],
                'is_core' => $feature['is_core'],
                'created_at' => $now,
                'updated_at' => $now,
            ]);
        }

        $packageIds = DB::table('license_packages')->pluck('id', 'code')->all();
        $featureIds = DB::table('feature_flags')->pluck('id', 'key')->all();

        $enabledByPackage = [
            'FREE' => [
                'mobile_scan',
                'product_management',
                'basic_cart_sale',
                'guest_mode',
            ],
            'SILVER' => [
                'mobile_scan',
                'product_management',
                'basic_cart_sale',
                'guest_mode',
                'cloud_sync',
                'reports',
                'stock_tracking',
                'ocr_name_suggestion',
                'web_search_name_suggestion',
                'barkodbankasi_import',
                'bulk_price_update',
                'bulk_stock_update',
                'line_price_override',
                'line_percent_discount',
                'line_fixed_discount',
                'ticket_system',
            ],
            'GOLD' => array_keys($featureIds),
        ];

        foreach ($packageIds as $packageCode => $packageId) {
            foreach ($featureIds as $featureKey => $featureId) {
                DB::table('license_package_features')->insert([
                    'package_id' => $packageId,
                    'feature_flag_id' => $featureId,
                    'is_enabled' => in_array($featureKey, $enabledByPackage[$packageCode] ?? [], true),
                    'created_at' => $now,
                    'updated_at' => $now,
                ]);
            }
        }
    }

    public function down(): void
    {
        Schema::dropIfExists('company_license_events');
        Schema::dropIfExists('license_requests');
        Schema::dropIfExists('company_license_feature_overrides');
        Schema::dropIfExists('company_licenses');
        Schema::dropIfExists('license_package_features');
        Schema::dropIfExists('license_packages');
        Schema::dropIfExists('feature_flags');
    }
};
