<?php

namespace App\Filament\Resources\CompanyLicenses\Schemas;

use App\Models\LicensePackage;
use Filament\Forms\Components\DateTimePicker;
use Filament\Forms\Components\Select;
use Filament\Forms\Components\Textarea;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class CompanyLicenseForm
{
    public static function configure(Schema $schema): Schema
    {
        return $schema->components([
            Section::make('Firma Lisansı')
                ->schema([
                    Select::make('company_id')
                        ->label('Firma')
                        ->relationship('company', 'name')
                        ->searchable()
                        ->preload()
                        ->required(),
                    Select::make('package_id')
                        ->label('Paket Şablonu')
                        ->relationship('package', 'name')
                        ->getOptionLabelFromRecordUsing(fn (LicensePackage $record): string => sprintf('%s - %s', $record->code, $record->name))
                        ->searchable()
                        ->preload()
                        ->required(),
                    Select::make('status')
                        ->label('Durum')
                        ->options([
                            'active' => 'Aktif',
                            'suspended' => 'Askıda',
                            'expired' => 'Süresi Doldu',
                            'cancelled' => 'İptal',
                        ])
                        ->default('active')
                        ->required(),
                    Select::make('source')
                        ->label('Atama Kaynağı')
                        ->options([
                            'manual_admin' => 'Admin',
                            'manual_bank_transfer' => 'Banka Transferi',
                        ])
                        ->default('manual_admin')
                        ->required(),
                    DateTimePicker::make('starts_at')
                        ->label('Başlangıç Tarihi')
                        ->seconds(false)
                        ->default(now())
                        ->required(),
                    DateTimePicker::make('expires_at')
                        ->label('Bitiş Tarihi')
                        ->seconds(false),
                    Textarea::make('note')
                        ->label('Not')
                        ->rows(3)
                        ->columnSpanFull(),
                ])
                ->columns(2),
        ]);
    }
}
