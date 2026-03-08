<?php

namespace App\Filament\Resources\SyncBatches\Schemas;

use Filament\Infolists\Components\TextEntry;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class SyncBatchInfolist
{
    public static function configure(Schema $schema): Schema
    {
        return $schema->components([
            Section::make('Senkron Detayı')
                ->schema([
                    TextEntry::make('batch_uuid')->label('Batch Kimliği')->copyable(),
                    TextEntry::make('company.name')->label('Firma')->placeholder('-'),
                    TextEntry::make('device.device_name')->label('Cihaz')->placeholder('-'),
                    TextEntry::make('status')
                        ->label('Durum')
                        ->badge()
                        ->formatStateUsing(fn (string $state): string => match ($state) {
                            'received' => 'Alındı',
                            'processing' => 'İşleniyor',
                            'processed' => 'İşlendi',
                            'failed' => 'Başarısız',
                            default => ucfirst($state),
                        }),
                    TextEntry::make('received_event_count')->label('Alınan')->numeric(),
                    TextEntry::make('processed_event_count')->label('İşlenen')->numeric(),
                    TextEntry::make('error_summary')->label('Hata Özeti')->placeholder('-')->columnSpanFull(),
                    TextEntry::make('created_at')->label('Oluşma')->dateTime()->placeholder('-'),
                ])
                ->columns(2),
        ]);
    }
}
