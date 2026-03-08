<?php

namespace App\Filament\Resources\CompanyLicenseFeatureOverrides\Schemas;

use App\Models\CompanyLicense;
use App\Models\FeatureFlag;
use Filament\Forms\Components\Select;
use Filament\Forms\Components\Textarea;
use Filament\Forms\Components\Toggle;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class CompanyLicenseFeatureOverrideForm
{
    public static function configure(Schema $schema): Schema
    {
        return $schema->components([
            Section::make('Firma Bazlı Feature Override')
                ->schema([
                    Select::make('company_license_id')
                        ->label('Firma Lisansı')
                        ->options(fn (): array => CompanyLicense::query()
                            ->with(['company:id,name', 'package:id,code,name'])
                            ->orderByDesc('id')
                            ->get()
                            ->mapWithKeys(fn (CompanyLicense $license): array => [
                                $license->id => sprintf(
                                    '#%d - %s / %s',
                                    $license->id,
                                    $license->company?->name ?? 'Firma yok',
                                    $license->package?->code ?? '-',
                                ),
                            ])
                            ->all())
                        ->searchable()
                        ->required(),
                    Select::make('feature_flag_id')
                        ->label('Feature Flag')
                        ->options(fn (): array => FeatureFlag::query()
                            ->orderBy('title')
                            ->get()
                            ->mapWithKeys(fn (FeatureFlag $flag): array => [
                                $flag->id => sprintf('%s (%s)', $flag->title, $flag->key),
                            ])
                            ->all())
                        ->searchable()
                        ->required(),
                    Toggle::make('is_enabled')
                        ->label('Firma İçin Açık')
                        ->required(),
                    Textarea::make('reason')
                        ->label('Gerekçe')
                        ->rows(3)
                        ->columnSpanFull(),
                ])
                ->columns(1),
        ]);
    }
}
