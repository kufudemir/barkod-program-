<?php

namespace App\Filament\Resources\LicensePackageFeatures\Schemas;

use App\Models\FeatureFlag;
use App\Models\LicensePackage;
use Filament\Forms\Components\Select;
use Filament\Forms\Components\Toggle;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class LicensePackageFeatureForm
{
    public static function configure(Schema $schema): Schema
    {
        return $schema->components([
            Section::make('Paket Özellik Kaydı')
                ->schema([
                    Select::make('package_id')
                        ->label('Paket Şablonu')
                        ->relationship('package', 'name')
                        ->getOptionLabelFromRecordUsing(fn (LicensePackage $record): string => sprintf('%s - %s', $record->code, $record->name))
                        ->searchable()
                        ->preload()
                        ->required(),
                    Select::make('feature_flag_id')
                        ->label('Feature Flag')
                        ->relationship('featureFlag', 'title')
                        ->getOptionLabelFromRecordUsing(fn (FeatureFlag $record): string => sprintf('%s (%s)', $record->title, $record->key))
                        ->searchable()
                        ->preload()
                        ->required(),
                    Toggle::make('is_enabled')
                        ->label('Paket İçin Açık')
                        ->default(false)
                        ->required(),
                ])
                ->columns(1),
        ]);
    }
}
