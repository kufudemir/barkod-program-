<?php

namespace App\Filament\Resources\Devices\Schemas;

use Filament\Infolists\Components\IconEntry;
use Filament\Infolists\Components\TextEntry;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class DeviceInfolist
{
    public static function configure(Schema $schema): Schema
    {
        return $schema->components([
            Section::make('Cihaz Durumu')
                ->schema([
                    TextEntry::make('company.name')->label('Firma'),
                    TextEntry::make('device_uid')->label('Cihaz UID')->copyable(),
                    TextEntry::make('device_name')->label('Cihaz Adı'),
                    TextEntry::make('platform')->label('Platform'),
                    IconEntry::make('is_active')->label('Aktif')->boolean(),
                    TextEntry::make('latestSyncBatch.status')
                        ->label('Son Batch Durumu')
                        ->state(fn ($record): string => match ($record->latestSyncBatch?->status) {
                            'received' => 'Alındı',
                            'processing' => 'İşleniyor',
                            'processed' => 'İşlendi',
                            'failed' => 'Başarısız',
                            default => 'Henüz yok',
                        }),
                    TextEntry::make('latestSyncBatch.error_summary')->label('Son Hata')->placeholder('-')->columnSpanFull(),
                    TextEntry::make('last_sync_at')->label('Son Başarılı Senkron')->dateTime()->placeholder('-'),
                    TextEntry::make('last_seen_at')->label('Son Görülme')->dateTime()->placeholder('-'),
                    TextEntry::make('created_at')->label('Oluşturulma')->dateTime()->placeholder('-'),
                    TextEntry::make('updated_at')->label('Güncellenme')->dateTime()->placeholder('-'),
                ])
                ->columns(2),
        ]);
    }
}
