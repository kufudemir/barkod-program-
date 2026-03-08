<?php

namespace App\Filament\Resources\WebSales;

use App\Filament\Resources\WebSales\Pages\ListWebSales;
use App\Filament\Resources\WebSales\Pages\ViewWebSale;
use App\Filament\Resources\WebSales\Schemas\WebSaleInfolist;
use App\Filament\Resources\WebSales\Tables\WebSalesTable;
use App\Models\WebSale;
use BackedEnum;
use Filament\Resources\Resource;
use Filament\Schemas\Schema;
use Filament\Support\Icons\Heroicon;
use Filament\Tables\Table;

class WebSaleResource extends Resource
{
    protected static ?string $model = WebSale::class;

    protected static string|BackedEnum|null $navigationIcon = Heroicon::OutlinedReceiptPercent;

    protected static ?string $navigationLabel = 'Web Satışlar';

    protected static ?string $modelLabel = 'Web satışı';

    protected static ?string $pluralModelLabel = 'Web satışları';

    protected static string|\UnitEnum|null $navigationGroup = 'Web POS';

    public static function infolist(Schema $schema): Schema
    {
        return WebSaleInfolist::configure($schema);
    }

    public static function table(Table $table): Table
    {
        return WebSalesTable::configure($table);
    }

    public static function canCreate(): bool
    {
        return false;
    }

    public static function canEdit($record): bool
    {
        return false;
    }

    public static function getPages(): array
    {
        return [
            'index' => ListWebSales::route('/'),
            'view' => ViewWebSale::route('/{record}'),
        ];
    }
}