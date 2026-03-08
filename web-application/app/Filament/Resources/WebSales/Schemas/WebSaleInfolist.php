<?php

namespace App\Filament\Resources\WebSales\Schemas;

use App\Models\WebSale;
use Filament\Infolists\Components\TextEntry;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class WebSaleInfolist
{
    public static function configure(Schema $schema): Schema
    {
        return $schema
            ->columns(3)
            ->components([
                Section::make('Satış Özeti')
                    ->schema([
                        TextEntry::make('id')->label('Satış no'),
                        TextEntry::make('company.name')->label('Firma'),
                        TextEntry::make('createdByUser.name')->label('Yapan kullanıcı')->placeholder('-'),
                        TextEntry::make('total_items')->label('Toplam ürün'),
                        TextEntry::make('total_amount_kurus')->label('Toplam tutar')->state(fn (WebSale $record): string => number_format($record->total_amount_kurus / 100, 2, ',', '.') . ' TL'),
                        TextEntry::make('total_cost_kurus')->label('Toplam maliyet')->state(fn (WebSale $record): string => number_format($record->total_cost_kurus / 100, 2, ',', '.') . ' TL'),
                        TextEntry::make('profit_kurus')->label('Kâr')->state(fn (WebSale $record): string => number_format($record->profit_kurus / 100, 2, ',', '.') . ' TL'),
                        TextEntry::make('completed_at')->label('Tamamlanma')->dateTime('d.m.Y H:i'),
                    ])
                    ->columns(2)
                    ->columnSpan(1),
                Section::make('Satış Satırları')
                    ->schema([
                        TextEntry::make('items_summary')
                            ->label('Ürünler')
                            ->state(function (WebSale $record): string {
                                return $record->items()
                                    ->orderBy('id')
                                    ->get()
                                    ->map(function ($item): string {
                                        $sale = number_format($item->unit_sale_price_kurus_snapshot / 100, 2, ',', '.') . ' TL';
                                        $line = number_format($item->line_total_kurus / 100, 2, ',', '.') . ' TL';
                                        return sprintf('%s | Barkod: %s | Adet: %d | Birim: %s | Satır: %s', $item->product_name_snapshot, $item->barcode, $item->quantity, $sale, $line);
                                    })
                                    ->implode("\n");
                            })
                            ->placeholder('Satış satırı bulunamadı.'),
                    ])
                    ->columnSpan(2),
            ]);
    }
}