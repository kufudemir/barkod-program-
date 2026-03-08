<?php

namespace App\Filament\Resources\WebSales\Tables;

use Filament\Actions\ViewAction;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Filters\SelectFilter;
use Filament\Tables\Table;

class WebSalesTable
{
    public static function configure(Table $table): Table
    {
        return $table
            ->defaultSort('completed_at', 'desc')
            ->columns([
                TextColumn::make('id')->label('Satış no')->sortable(),
                TextColumn::make('company.name')->label('Firma')->searchable()->sortable(),
                TextColumn::make('createdByUser.name')->label('Yapan kullanıcı')->placeholder('-'),
                TextColumn::make('total_items')->label('Ürün')->sortable(),
                TextColumn::make('total_amount_kurus')
                    ->label('Toplam')
                    ->formatStateUsing(fn (int $state): string => number_format($state / 100, 2, ',', '.') . ' TL')
                    ->sortable(),
                TextColumn::make('profit_kurus')
                    ->label('Kâr')
                    ->formatStateUsing(fn (int $state): string => number_format($state / 100, 2, ',', '.') . ' TL')
                    ->sortable(),
                TextColumn::make('completed_at')->label('Tamamlanma')->since()->sortable(),
            ])
            ->filters([
                SelectFilter::make('company_id')
                    ->label('Firma')
                    ->relationship('company', 'name'),
            ])
            ->recordActions([
                ViewAction::make(),
            ])
            ->toolbarActions([]);
    }
}