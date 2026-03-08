<?php

namespace App\Filament\Resources\FeatureFlags\Tables;

use Filament\Actions\EditAction;
use Filament\Tables\Columns\IconColumn;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Filters\SelectFilter;
use Filament\Tables\Table;

class FeatureFlagsTable
{
    public static function configure(Table $table): Table
    {
        return $table
            ->defaultSort('key', 'asc')
            ->columns([
                TextColumn::make('key')->label('Key')->searchable()->copyable(),
                TextColumn::make('title')->label('Başlık')->searchable()->sortable(),
                TextColumn::make('scope')
                    ->label('Kapsam')
                    ->badge()
                    ->formatStateUsing(fn (string $state): string => match ($state) {
                        'mobile' => 'Mobil',
                        'web_pos' => 'Web POS',
                        'admin' => 'Admin',
                        default => 'Ortak',
                    }),
                IconColumn::make('is_core')->label('Core')->boolean(),
                TextColumn::make('description')->label('Açıklama')->limit(50)->placeholder('-')->toggleable(isToggledHiddenByDefault: true),
                TextColumn::make('updated_at')->label('Güncellendi')->since(),
            ])
            ->filters([
                SelectFilter::make('scope')
                    ->label('Kapsam')
                    ->options([
                        'mobile' => 'Mobil',
                        'web_pos' => 'Web POS',
                        'admin' => 'Admin',
                        'shared' => 'Ortak',
                    ]),
                SelectFilter::make('is_core')
                    ->label('Core')
                    ->options([
                        '1' => 'Evet',
                        '0' => 'Hayır',
                    ]),
            ])
            ->recordActions([
                EditAction::make(),
            ])
            ->toolbarActions([]);
    }
}
