<?php

namespace App\Filament\Resources\CompanyLicenseFeatureOverrides;

use App\Filament\Resources\CompanyLicenseFeatureOverrides\Pages\CreateCompanyLicenseFeatureOverride;
use App\Filament\Resources\CompanyLicenseFeatureOverrides\Pages\EditCompanyLicenseFeatureOverride;
use App\Filament\Resources\CompanyLicenseFeatureOverrides\Pages\ListCompanyLicenseFeatureOverrides;
use App\Filament\Resources\CompanyLicenseFeatureOverrides\Schemas\CompanyLicenseFeatureOverrideForm;
use App\Filament\Resources\CompanyLicenseFeatureOverrides\Tables\CompanyLicenseFeatureOverridesTable;
use App\Models\CompanyLicenseFeatureOverride;
use BackedEnum;
use Filament\Resources\Resource;
use Filament\Schemas\Schema;
use Filament\Tables\Table;

class CompanyLicenseFeatureOverrideResource extends Resource
{
    protected static ?string $model = CompanyLicenseFeatureOverride::class;

    protected static string|BackedEnum|null $navigationIcon = 'heroicon-o-adjustments-horizontal';

    protected static ?string $navigationLabel = 'Firma Feature Override';

    protected static ?string $modelLabel = 'Feature Override';

    protected static ?string $pluralModelLabel = 'Firma Feature Override';

    protected static string|\UnitEnum|null $navigationGroup = 'Lisans ve Paketler';

    protected static ?int $navigationSort = 40;

    public static function form(Schema $schema): Schema
    {
        return CompanyLicenseFeatureOverrideForm::configure($schema);
    }

    public static function table(Table $table): Table
    {
        return CompanyLicenseFeatureOverridesTable::configure($table);
    }

    public static function getPages(): array
    {
        return [
            'index' => ListCompanyLicenseFeatureOverrides::route('/'),
            'create' => CreateCompanyLicenseFeatureOverride::route('/create'),
            'edit' => EditCompanyLicenseFeatureOverride::route('/{record}/edit'),
        ];
    }
}
