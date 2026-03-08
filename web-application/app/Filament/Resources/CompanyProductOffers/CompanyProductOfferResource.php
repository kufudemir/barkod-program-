<?php

namespace App\Filament\Resources\CompanyProductOffers;

use App\Filament\Resources\CompanyProductOffers\Pages\EditCompanyProductOffer;
use App\Filament\Resources\CompanyProductOffers\Pages\ListCompanyProductOffers;
use App\Filament\Resources\CompanyProductOffers\Pages\ViewCompanyProductOffer;
use App\Filament\Resources\CompanyProductOffers\Schemas\CompanyProductOfferForm;
use App\Filament\Resources\CompanyProductOffers\Schemas\CompanyProductOfferInfolist;
use App\Filament\Resources\CompanyProductOffers\Tables\CompanyProductOffersTable;
use App\Models\CompanyProductOffer;
use BackedEnum;
use Filament\Resources\Resource;
use Filament\Schemas\Schema;
use Filament\Support\Icons\Heroicon;
use Filament\Tables\Table;

class CompanyProductOfferResource extends Resource
{
    protected static ?string $model = CompanyProductOffer::class;

    protected static string|BackedEnum|null $navigationIcon = Heroicon::OutlinedCurrencyDollar;

    protected static ?string $navigationLabel = 'Firma Fiyatları';

    protected static ?string $modelLabel = 'Firma Fiyatı';

    protected static ?string $pluralModelLabel = 'Firma Fiyatları';

    protected static string|\UnitEnum|null $navigationGroup = 'Katalog ve Senkron';

    public static function form(Schema $schema): Schema
    {
        return CompanyProductOfferForm::configure($schema);
    }

    public static function infolist(Schema $schema): Schema
    {
        return CompanyProductOfferInfolist::configure($schema);
    }

    public static function table(Table $table): Table
    {
        return CompanyProductOffersTable::configure($table);
    }

    public static function canCreate(): bool
    {
        return false;
    }

    public static function getPages(): array
    {
        return [
            'index' => ListCompanyProductOffers::route('/'),
            'view' => ViewCompanyProductOffer::route('/{record}'),
            'edit' => EditCompanyProductOffer::route('/{record}/edit'),
        ];
    }
}
