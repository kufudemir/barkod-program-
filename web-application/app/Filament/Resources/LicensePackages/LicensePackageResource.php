<?php

namespace App\Filament\Resources\LicensePackages;

use App\Filament\Resources\LicensePackages\Pages\CreateLicensePackage;
use App\Filament\Resources\LicensePackages\Pages\EditLicensePackage;
use App\Filament\Resources\LicensePackages\Pages\ListLicensePackages;
use App\Filament\Resources\LicensePackages\Schemas\LicensePackageForm;
use App\Filament\Resources\LicensePackages\Tables\LicensePackagesTable;
use App\Models\LicensePackage;
use BackedEnum;
use Filament\Resources\Resource;
use Filament\Schemas\Schema;
use Filament\Support\Icons\Heroicon;
use Filament\Tables\Table;

class LicensePackageResource extends Resource
{
    protected static ?string $model = LicensePackage::class;

    protected static string|BackedEnum|null $navigationIcon = Heroicon::OutlinedTag;

    protected static ?string $navigationLabel = 'Paket Şablonları';

    protected static ?string $modelLabel = 'Paket Şablonu';

    protected static ?string $pluralModelLabel = 'Paket Şablonları';

    protected static string|\UnitEnum|null $navigationGroup = 'Lisans ve Paketler';

    protected static ?int $navigationSort = 10;

    public static function form(Schema $schema): Schema
    {
        return LicensePackageForm::configure($schema);
    }

    public static function table(Table $table): Table
    {
        return LicensePackagesTable::configure($table);
    }

    public static function getPages(): array
    {
        return [
            'index' => ListLicensePackages::route('/'),
            'create' => CreateLicensePackage::route('/create'),
            'edit' => EditLicensePackage::route('/{record}/edit'),
        ];
    }
}
