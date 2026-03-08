<?php

namespace App\Filament\Resources\GlobalProducts\Tables;

use App\Models\GlobalProduct;
use Filament\Actions\EditAction;
use Filament\Actions\ViewAction;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Table;

class GlobalProductsTable
{
    public static function configure(Table $table): Table
    {
        return $table
            ->columns([
                TextColumn::make('barcode')->label('Barkod')->searchable()->copyable(),
                TextColumn::make('canonical_name')->label('Ana ürün adı')->searchable()->limit(40),
                TextColumn::make('group_name')->label('Global grup')->searchable()->placeholder('-'),
                TextColumn::make('lastSourceCompany.name')->label('Son firma')->placeholder('-')->searchable(),
                TextColumn::make('candidates_count')
                    ->label('İsim adayı')
                    ->state(fn (GlobalProduct $record): int => $record->candidates()->count()),
                TextColumn::make('last_synced_at')->label('Son senkron')->since()->placeholder('-'),
                TextColumn::make('updated_at')->label('Güncellenme')->since(),
            ])
            ->filters([])
            ->recordActions([
                ViewAction::make(),
                EditAction::make(),
            ])
            ->toolbarActions([]);
    }
}
