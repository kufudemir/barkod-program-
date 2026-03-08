<?php

namespace App\Filament\Widgets;

use App\Models\SyncBatch;
use Filament\Actions\ViewAction;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Table;
use Filament\Widgets\TableWidget;
use Illuminate\Database\Eloquent\Builder;

class RecentSyncBatches extends TableWidget
{
    protected int|string|array $columnSpan = 'full';

    public function table(Table $table): Table
    {
        return $table
            ->heading('Son Senkron Kayıtları')
            ->query(fn (): Builder => SyncBatch::query()->with(['company', 'device'])->latest('created_at'))
            ->columns([
                TextColumn::make('batch_uuid')
                    ->label('Batch Kimliği')
                    ->searchable()
                    ->copyable()
                    ->limit(18),
                TextColumn::make('company.name')
                    ->label('Firma')
                    ->searchable()
                    ->placeholder('-'),
                TextColumn::make('device.device_name')
                    ->label('Cihaz')
                    ->placeholder('-')
                    ->limit(20),
                TextColumn::make('processed_event_count')
                    ->label('İşlenen')
                    ->numeric(),
                TextColumn::make('received_event_count')
                    ->label('Alınan')
                    ->numeric(),
                TextColumn::make('status')
                    ->badge()
                    ->color(fn (string $state): string => match ($state) {
                        'processed' => 'success',
                        'processing' => 'warning',
                        'failed' => 'danger',
                        default => 'gray',
                    }),
                TextColumn::make('created_at')
                    ->label('Oluşma')
                    ->since(),
            ])
            ->filters([])
            ->headerActions([])
            ->recordActions([
                ViewAction::make(),
            ])
            ->toolbarActions([]);
    }
}
