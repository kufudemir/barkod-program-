<?php

namespace App\Filament\Resources\CompanyProductOffers\Tables;

use Filament\Actions\EditAction;
use Filament\Actions\ViewAction;
use Filament\Tables\Columns\IconColumn;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Filters\SelectFilter;
use Filament\Tables\Table;

class CompanyProductOffersTable
{
    public static function configure(Table $table): Table
    {
        return $table
            ->columns([
                TextColumn::make('company.name')->label('Firma')->searchable(),
                TextColumn::make('barcode')->label('Barkod')->searchable()->copyable(),
                TextColumn::make('globalProduct.canonical_name')->label('Ürün')->searchable()->limit(36),
                TextColumn::make('sale_price_kurus')->label('Satış')->formatStateUsing(fn (int $state): string => number_format($state / 100, 2, ',', '.') . ' TL')->sortable(),
                TextColumn::make('cost_price_kurus')->label('Alış')->formatStateUsing(fn (int $state): string => number_format($state / 100, 2, ',', '.') . ' TL')->sortable(),
                TextColumn::make('group_name')->label('Grup')->searchable()->placeholder('-'),
                IconColumn::make('is_active')->label('Aktif')->boolean(),
                TextColumn::make('last_synced_at')->label('Son Senkron')->since()->placeholder('-'),
            ])
            ->filters([
                SelectFilter::make('company_id')->relationship('company', 'name')->label('Firma'),
                SelectFilter::make('is_active')->label('Durum')->options(['1' => 'Aktif', '0' => 'Pasif']),
            ])
            ->recordActions([
                ViewAction::make(),
                EditAction::make(),
            ])
            ->toolbarActions([]);
    }
}
