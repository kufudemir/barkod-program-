<?php

namespace App\Filament\Resources\SyncBatches\Tables;

use Filament\Actions\ViewAction;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Filters\SelectFilter;
use Filament\Tables\Table;

class SyncBatchesTable
{
    public static function configure(Table $table): Table
    {
        return $table
            ->defaultSort('created_at', 'desc')
            ->columns([
                TextColumn::make('batch_uuid')->label('Batch Kimliği')->copyable()->searchable()->limit(20),
                TextColumn::make('company.name')->label('Firma')->searchable()->placeholder('-'),
                TextColumn::make('device.device_name')->label('Cihaz')->placeholder('-'),
                TextColumn::make('received_event_count')->label('Alınan')->numeric(),
                TextColumn::make('processed_event_count')->label('İşlenen')->numeric(),
                TextColumn::make('status')
                    ->label('Durum')
                    ->badge()
                    ->formatStateUsing(fn (string $state): string => match ($state) {
                        'received' => 'Alındı',
                        'processing' => 'İşleniyor',
                        'processed' => 'İşlendi',
                        'failed' => 'Başarısız',
                        default => ucfirst($state),
                    }),
                TextColumn::make('error_summary')->label('Hata Özeti')->limit(50)->placeholder('-')->toggleable(isToggledHiddenByDefault: true),
                TextColumn::make('created_at')->label('Oluşma')->since(),
            ])
            ->filters([
                SelectFilter::make('status')->label('Durum')->options([
                    'received' => 'Alındı',
                    'processing' => 'İşleniyor',
                    'processed' => 'İşlendi',
                    'failed' => 'Başarısız',
                ]),
            ])
            ->recordActions([
                ViewAction::make(),
            ])
            ->toolbarActions([]);
    }
}
