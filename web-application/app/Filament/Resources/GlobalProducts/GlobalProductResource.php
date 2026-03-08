<?php

namespace App\Filament\Resources\GlobalProducts;

use App\Filament\Resources\GlobalProducts\Pages\EditGlobalProduct;
use App\Filament\Resources\GlobalProducts\Pages\ListGlobalProducts;
use App\Filament\Resources\GlobalProducts\Pages\ViewGlobalProduct;
use App\Filament\Resources\GlobalProducts\Schemas\GlobalProductForm;
use App\Filament\Resources\GlobalProducts\Schemas\GlobalProductInfolist;
use App\Filament\Resources\GlobalProducts\Tables\GlobalProductsTable;
use App\Models\GlobalProduct;
use BackedEnum;
use Filament\Resources\Resource;
use Filament\Schemas\Schema;
use Filament\Support\Icons\Heroicon;
use Filament\Tables\Table;

class GlobalProductResource extends Resource
{
    protected static ?string $model = GlobalProduct::class;

    protected static string|BackedEnum|null $navigationIcon = Heroicon::OutlinedQrCode;

    protected static ?string $navigationLabel = 'Global Katalog';

    protected static ?string $modelLabel = 'Global Ürün';

    protected static ?string $pluralModelLabel = 'Global Katalog';

    protected static string|\UnitEnum|null $navigationGroup = 'Katalog ve Senkron';

    public static function form(Schema $schema): Schema
    {
        return GlobalProductForm::configure($schema);
    }

    public static function infolist(Schema $schema): Schema
    {
        return GlobalProductInfolist::configure($schema);
    }

    public static function table(Table $table): Table
    {
        return GlobalProductsTable::configure($table);
    }

    public static function canCreate(): bool
    {
        return false;
    }

    public static function getPages(): array
    {
        return [
            'index' => ListGlobalProducts::route('/'),
            'view' => ViewGlobalProduct::route('/{record}'),
            'edit' => EditGlobalProduct::route('/{record}/edit'),
        ];
    }
}
