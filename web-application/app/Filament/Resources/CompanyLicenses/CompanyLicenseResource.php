<?php

namespace App\Filament\Resources\CompanyLicenses;

use App\Filament\Resources\CompanyLicenses\Pages\CreateCompanyLicense;
use App\Filament\Resources\CompanyLicenses\Pages\EditCompanyLicense;
use App\Filament\Resources\CompanyLicenses\Pages\ListCompanyLicenses;
use App\Filament\Resources\CompanyLicenses\Pages\ViewCompanyLicense;
use App\Filament\Resources\CompanyLicenses\Schemas\CompanyLicenseForm;
use App\Filament\Resources\CompanyLicenses\Schemas\CompanyLicenseInfolist;
use App\Filament\Resources\CompanyLicenses\Tables\CompanyLicensesTable;
use App\Models\CompanyLicense;
use App\Models\CompanyLicenseEvent;
use BackedEnum;
use Filament\Resources\Resource;
use Filament\Schemas\Schema;
use Filament\Tables\Table;

class CompanyLicenseResource extends Resource
{
    protected static ?string $model = CompanyLicense::class;

    protected static string|BackedEnum|null $navigationIcon = 'heroicon-o-shield-check';

    protected static ?string $navigationLabel = 'Firma Lisansları';

    protected static ?string $modelLabel = 'Firma Lisansı';

    protected static ?string $pluralModelLabel = 'Firma Lisansları';

    protected static string|\UnitEnum|null $navigationGroup = 'Lisans ve Paketler';

    protected static ?int $navigationSort = 30;

    public static function form(Schema $schema): Schema
    {
        return CompanyLicenseForm::configure($schema);
    }

    public static function infolist(Schema $schema): Schema
    {
        return CompanyLicenseInfolist::configure($schema);
    }

    public static function table(Table $table): Table
    {
        return CompanyLicensesTable::configure($table);
    }

    public static function canDelete($record): bool
    {
        return false;
    }

    public static function logEvent(CompanyLicense $license, string $eventType, array $payload = []): void
    {
        CompanyLicenseEvent::query()->create([
            'company_license_id' => $license->id,
            'event_type' => $eventType,
            'payload_json' => $payload,
            'created_at' => now(),
        ]);
    }

    public static function getPages(): array
    {
        return [
            'index' => ListCompanyLicenses::route('/'),
            'create' => CreateCompanyLicense::route('/create'),
            'view' => ViewCompanyLicense::route('/{record}'),
            'edit' => EditCompanyLicense::route('/{record}/edit'),
        ];
    }
}
