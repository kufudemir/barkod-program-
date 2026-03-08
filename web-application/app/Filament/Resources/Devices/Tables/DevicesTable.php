<?php

namespace App\Filament\Resources\Devices\Tables;

use App\Models\Device;
use Filament\Actions\Action;
use Filament\Actions\EditAction;
use Filament\Actions\ViewAction;
use Filament\Notifications\Notification;
use Filament\Tables\Columns\IconColumn;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Filters\SelectFilter;
use Filament\Tables\Table;

class DevicesTable
{
    public static function configure(Table $table): Table
    {
        return $table
            ->defaultSort('last_sync_at', 'desc')
            ->columns([
                TextColumn::make('company.name')->label('Firma')->searchable(),
                TextColumn::make('device_name')->label('Cihaz')->searchable()->sortable(),
                TextColumn::make('device_uid')->label('UID')->searchable()->copyable()->toggleable(isToggledHiddenByDefault: true),
                IconColumn::make('is_active')->label('Aktif')->boolean(),
                TextColumn::make('latestSyncBatch.status')
                    ->label('Son Durum')
                    ->badge()
                    ->state(fn (Device $record): string => match ($record->latestSyncBatch?->status) {
                        'received' => 'Alındı',
                        'processing' => 'İşleniyor',
                        'processed' => 'İşlendi',
                        'failed' => 'Başarısız',
                        default => 'Henüz yok',
                    }),
                TextColumn::make('last_sync_at')->label('Son Başarılı Senkron')->since()->placeholder('-'),
                TextColumn::make('last_seen_at')->label('Son Görülme')->since()->placeholder('-'),
            ])
            ->filters([
                SelectFilter::make('is_active')
                    ->label('Durum')
                    ->options(['1' => 'Aktif', '0' => 'Pasif']),
            ])
            ->recordActions([
                ViewAction::make(),
                EditAction::make(),
                Action::make('deactivate')
                    ->label('Pasife Al')
                    ->requiresConfirmation()
                    ->visible(fn (Device $record): bool => $record->is_active)
                    ->action(function (Device $record): void {
                        $record->update([
                            'is_active' => false,
                            'activation_token_hash' => null,
                        ]);
                        Notification::make()->title('Cihaz pasife alındı')->success()->send();
                    }),
            ])
            ->toolbarActions([]);
    }
}
