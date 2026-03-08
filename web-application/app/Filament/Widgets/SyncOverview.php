<?php

namespace App\Filament\Widgets;

use App\Models\Company;
use App\Models\Device;
use App\Models\SyncBatch;
use Filament\Widgets\StatsOverviewWidget as BaseWidget;
use Filament\Widgets\StatsOverviewWidget\Stat;

class SyncOverview extends BaseWidget
{
    protected function getStats(): array
    {
        return [
            Stat::make('Toplam Firma', (string) Company::query()->count())
                ->description('Sistemde tanımlı firma sayısı'),
            Stat::make('Aktif Cihaz', (string) Device::query()->where('is_active', true)->count())
                ->description('Firma başına tek aktif cihaz modeli'),
            Stat::make(
                'Bugün Senkron Yapan Firma',
                (string) Device::query()
                    ->whereNotNull('last_sync_at')
                    ->whereDate('last_sync_at', today())
                    ->distinct('company_id')
                    ->count('company_id')
            )->description('Son başarılı senkron bugün gerçekleşti'),
            Stat::make(
                'Hatalı Senkron',
                (string) SyncBatch::query()->where('status', 'failed')->count()
            )->description('İnceleme gerektiren senkron kayıtları'),
        ];
    }
}
