<?php

namespace App\Filament\Resources\LicensePackageFeatures;

use App\Filament\Resources\LicensePackageFeatures\Pages\CreateLicensePackageFeature;
use App\Filament\Resources\LicensePackageFeatures\Pages\EditLicensePackageFeature;
use App\Filament\Resources\LicensePackageFeatures\Pages\ListLicensePackageFeatures;
use App\Filament\Resources\LicensePackageFeatures\Schemas\LicensePackageFeatureForm;
use App\Filament\Resources\LicensePackageFeatures\Tables\LicensePackageFeaturesTable;
use App\Models\LicensePackageFeature;
use BackedEnum;
use Filament\Resources\Resource;
use Filament\Schemas\Schema;
use Filament\Tables\Table;

class LicensePackageFeatureResource extends Resource
{
    protected static ?string $model = LicensePackageFeature::class;

    protected static string|BackedEnum|null $navigationIcon = 'heroicon-o-adjustments-horizontal';

    protected static ?string $navigationLabel = 'Paket Özellik Matrisi';

    protected static ?string $modelLabel = 'Paket Özellik Kaydı';

    protected static ?string $pluralModelLabel = 'Paket Özellik Matrisi';

    protected static string|\UnitEnum|null $navigationGroup = 'Lisans ve Paketler';

    protected static ?int $navigationSort = 20;

    public static function form(Schema $schema): Schema
    {
        return LicensePackageFeatureForm::configure($schema);
    }

    public static function table(Table $table): Table
    {
        return LicensePackageFeaturesTable::configure($table);
    }

    public static function getPages(): array
    {
        return [
            'index' => ListLicensePackageFeatures::route('/'),
            'create' => CreateLicensePackageFeature::route('/create'),
            'edit' => EditLicensePackageFeature::route('/{record}/edit'),
        ];
    }
}
