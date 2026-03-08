<?php

namespace App\Filament\Pages;

use BackedEnum;
use Filament\Notifications\Notification;
use Filament\Pages\Page;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;
use UnitEnum;

class SystemMaintenance extends Page
{
    protected static string|BackedEnum|null $navigationIcon = 'heroicon-o-exclamation-triangle';
    protected static ?string $navigationLabel = 'Sistem Sıfırlama';
    protected static string|UnitEnum|null $navigationGroup = 'Sistem';
    protected static ?int $navigationSort = 999;
    protected string $view = 'filament.pages.system-maintenance';

    public string $confirmationText = '';
    public array $summary = [];

    public function mount(): void
    {
        $this->refreshSummary();
    }

    public function getTitle(): string
    {
        return 'Sistem Sıfırlama';
    }

    public function getHeading(): string
    {
        return 'Sistem Sıfırlama';
    }

    public function resetSystem(): void
    {
        $expected = 'tüm sistemi sıfırla';

        if (mb_strtolower(trim($this->confirmationText), 'UTF-8') !== $expected) {
            Notification::make()
                ->title('Onay metni hatalı')
                ->body('Devam etmek için "tüm sistemi sıfırla" metnini aynen yazın.')
                ->danger()
                ->send();

            return;
        }

        DB::transaction(function (): void {
            $this->deleteIfExists('mobile_user_password_resets');
            $this->deleteIfExists('mobile_user_access_tokens');
            $this->deleteIfExists('company_staff_roles');
            $this->deleteIfExists('system_settings');
            $this->deleteIfExists('sync_event_dedups');
            $this->deleteIfExists('sync_batches');
            $this->deleteIfExists('web_sale_items');
            $this->deleteIfExists('web_sale_payments');
            $this->deleteIfExists('web_sales');
            $this->deleteIfExists('company_product_offers');
            $this->deleteIfExists('global_product_name_candidates');
            $this->deleteIfExists('global_products');
            $this->deleteIfExists('device_company_histories');
            $this->deleteIfExists('devices');
            $this->deleteIfExists('companies');
            $this->deleteIfExists('mobile_users');
            $this->deleteIfExists('jobs');
            $this->deleteIfExists('job_batches');
            $this->deleteIfExists('cache');
        });

        $this->confirmationText = '';
        $this->refreshSummary();

        Notification::make()
            ->title('Sistem sıfırlandı')
            ->body('Firmalar, cihazlar, katalog, senkron kayıtları ve mobil kullanıcılar temizlendi. Admin panel kullanıcıları korunur. Mobil cihazlarda yeniden giriş ve aktivasyon gerekir.')
            ->success()
            ->send();
    }

    private function refreshSummary(): void
    {
        $this->summary = [
            'mobile_users' => $this->countIfExists('mobile_users'),
            'company_staff_roles' => $this->countIfExists('company_staff_roles'),
            'companies' => $this->countIfExists('companies'),
            'devices' => $this->countIfExists('devices'),
            'device_company_histories' => $this->countIfExists('device_company_histories'),
            'global_products' => $this->countIfExists('global_products'),
            'company_product_offers' => $this->countIfExists('company_product_offers'),
            'sync_batches' => $this->countIfExists('sync_batches'),
            'web_sales' => $this->countIfExists('web_sales'),
            'web_sale_payments' => $this->countIfExists('web_sale_payments'),
        ];
    }

    private function countIfExists(string $table): int
    {
        if (! Schema::hasTable($table)) {
            return 0;
        }

        return DB::table($table)->count();
    }

    private function deleteIfExists(string $table): void
    {
        if (Schema::hasTable($table)) {
            DB::table($table)->delete();
        }
    }
}
