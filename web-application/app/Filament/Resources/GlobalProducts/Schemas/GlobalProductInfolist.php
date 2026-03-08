<?php

namespace App\Filament\Resources\GlobalProducts\Schemas;

use App\Models\GlobalProduct;
use Filament\Infolists\Components\TextEntry;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class GlobalProductInfolist
{
    public static function configure(Schema $schema): Schema
    {
        return $schema
            ->columns(3)
            ->components([
                Section::make('Global ürün detayı')
                    ->schema([
                        TextEntry::make('barcode')->label('Barkod')->copyable(),
                        TextEntry::make('canonical_name')->label('Ana ürün adı'),
                        TextEntry::make('group_name')->label('Global ürün grubu')->placeholder('-'),
                        TextEntry::make('lastSourceCompany.name')->label('Son firma')->placeholder('-'),
                        TextEntry::make('lastSourceDevice.device_name')->label('Son cihaz')->placeholder('-'),
                        TextEntry::make('candidate_names')
                            ->label('Son 5 isim adayı')
                            ->state(fn (GlobalProduct $record): string => $record->candidates()->latest('last_seen_at')->limit(5)->pluck('candidate_name')->implode("\n"))
                            ->placeholder('-'),
                        TextEntry::make('last_synced_at')->label('Son senkron')->dateTime()->placeholder('-'),
                        TextEntry::make('updated_at')->label('Güncellenme')->dateTime()->placeholder('-'),
                    ])
                    ->columns(2)
                    ->columnSpan(2),
                Section::make('Bu ürün için kullanılan son fiyatlar')
                    ->schema([
                        TextEntry::make('recent_company_prices')
                            ->label('Son 20 firma fiyatı')
                            ->state(function (GlobalProduct $record): string {
                                return $record->companyOffers()
                                    ->with('company')
                                    ->orderByDesc('updated_at')
                                    ->limit(20)
                                    ->get()
                                    ->map(function ($offer): string {
                                        $companyName = $offer->company?->name ?? 'Bilinmeyen firma';
                                        $sale = number_format($offer->sale_price_kurus / 100, 2, ',', '.') . ' TL';
                                        $cost = number_format($offer->cost_price_kurus / 100, 2, ',', '.') . ' TL';

                                        return "{$companyName} - Satış: {$sale} / Alış: {$cost}";
                                    })
                                    ->implode("\n");
                            })
                            ->placeholder('Henüz firma fiyatı yok.'),
                    ])
                    ->columnSpan(1),
            ]);
    }
}
