<?php

namespace App\Filament\Resources\LicenseRequests;

use App\Filament\Resources\LicenseRequests\Pages\EditLicenseRequest;
use App\Filament\Resources\LicenseRequests\Pages\ListLicenseRequests;
use App\Filament\Resources\LicenseRequests\Pages\ViewLicenseRequest;
use App\Filament\Resources\LicenseRequests\Schemas\LicenseRequestForm;
use App\Filament\Resources\LicenseRequests\Schemas\LicenseRequestInfolist;
use App\Filament\Resources\LicenseRequests\Tables\LicenseRequestsTable;
use App\Models\LicenseRequest;
use BackedEnum;
use Filament\Resources\Resource;
use Filament\Schemas\Schema;
use Filament\Tables\Table;

class LicenseRequestResource extends Resource
{
    protected static ?string $model = LicenseRequest::class;

    protected static string|BackedEnum|null $navigationIcon = 'heroicon-o-banknotes';

    protected static ?string $navigationLabel = 'Lisans Talepleri';

    protected static ?string $modelLabel = 'Lisans Talebi';

    protected static ?string $pluralModelLabel = 'Lisans Talepleri';

    protected static string|\UnitEnum|null $navigationGroup = 'Lisans ve Paketler';

    protected static ?int $navigationSort = 35;

    public static function form(Schema $schema): Schema
    {
        return LicenseRequestForm::configure($schema);
    }

    public static function infolist(Schema $schema): Schema
    {
        return LicenseRequestInfolist::configure($schema);
    }

    public static function table(Table $table): Table
    {
        return LicenseRequestsTable::configure($table);
    }

    public static function canCreate(): bool
    {
        return false;
    }

    public static function getPages(): array
    {
        return [
            'index' => ListLicenseRequests::route('/'),
            'view' => ViewLicenseRequest::route('/{record}'),
            'edit' => EditLicenseRequest::route('/{record}/edit'),
        ];
    }
}
