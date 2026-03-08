<?php

namespace App\Filament\Resources\CompanyProductOffers\Schemas;

use Filament\Infolists\Components\IconEntry;
use Filament\Infolists\Components\TextEntry;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class CompanyProductOfferInfolist
{
    public static function configure(Schema $schema): Schema
    {
        return $schema->components([
            Section::make('Firma Bazli Kayıt')
                ->schema([
                    TextEntry::make('company.name')->label('Firma'),
                    TextEntry::make('barcode')->label('Barkod')->copyable(),
                    TextEntry::make('globalProduct.canonical_name')->label('Global Ürün')->placeholder('-'),
                    TextEntry::make('sale_price_kurus')->label('Satış')->formatStateUsing(fn (int $state): string => number_format($state / 100, 2, ',', '.') . ' TL'),
                    TextEntry::make('cost_price_kurus')->label('Alis')->formatStateUsing(fn (int $state): string => number_format($state / 100, 2, ',', '.') . ' TL'),
                    TextEntry::make('group_name')->label('Grup')->placeholder('-'),
                    TextEntry::make('note')->label('Not')->placeholder('-')->columnSpanFull(),
                    IconEntry::make('is_active')->label('Aktif')->boolean(),
                    TextEntry::make('source_updated_at')->label('Kaynak Güncelleme')->dateTime()->placeholder('-'),
                    TextEntry::make('last_synced_at')->label('Son Senkron')->dateTime()->placeholder('-'),
                ])
                ->columns(2),
        ]);
    }
}

