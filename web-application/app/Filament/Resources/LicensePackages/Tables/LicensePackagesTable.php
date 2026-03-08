<?php

namespace App\Filament\Resources\LicensePackages\Tables;

use App\Models\LicensePackage;
use Filament\Actions\DeleteAction;
use Filament\Actions\EditAction;
use Filament\Tables\Columns\IconColumn;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Filters\SelectFilter;
use Filament\Tables\Table;

class LicensePackagesTable
{
    public static function configure(Table $table): Table
    {
        return $table
            ->defaultSort('sort_order', 'asc')
            ->columns([
                TextColumn::make('code')
                    ->label('Kod')
                    ->badge()
                    ->searchable()
                    ->copyable(),
                TextColumn::make('name')
                    ->label('Paket')
                    ->searchable()
                    ->sortable(),
                TextColumn::make('description')
                    ->label('Açıklama')
                    ->limit(60)
                    ->placeholder('-'),
                TextColumn::make('features_enabled_count')
                    ->label('Açık Özellik')
                    ->state(fn (LicensePackage $record): int => $record->features()->where('is_enabled', true)->count()),
                TextColumn::make('features_total_count')
                    ->label('Toplam Özellik')
                    ->state(fn (LicensePackage $record): int => $record->features()->count()),
                IconColumn::make('is_active')->label('Aktif')->boolean(),
                TextColumn::make('updated_at')->label('Güncellendi')->since(),
            ])
            ->filters([
                SelectFilter::make('is_active')
                    ->label('Durum')
                    ->options([
                        '1' => 'Aktif',
                        '0' => 'Pasif',
                    ]),
            ])
            ->recordActions([
                EditAction::make(),
                DeleteAction::make()
                    ->visible(fn (LicensePackage $record): bool => ! in_array($record->code, ['FREE', 'SILVER', 'GOLD'], true)),
            ])
            ->toolbarActions([]);
    }
}
