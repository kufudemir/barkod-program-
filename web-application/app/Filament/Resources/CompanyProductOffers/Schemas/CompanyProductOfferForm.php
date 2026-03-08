<?php

namespace App\Filament\Resources\CompanyProductOffers\Schemas;

use Filament\Forms\Components\Select;
use Filament\Forms\Components\Textarea;
use Filament\Forms\Components\TextInput;
use Filament\Forms\Components\Toggle;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class CompanyProductOfferForm
{
    public static function configure(Schema $schema): Schema
    {
        return $schema->components([
            Section::make('Firma Bazli Fiyat')
                ->schema([
                    Select::make('company_id')->relationship('company', 'name')->label('Firma')->required()->disabled(),
                    TextInput::make('barcode')->label('Barkod')->disabled(),
                    TextInput::make('sale_price_kurus')->label('Satış Fiyati (Kurus)')->numeric()->required(),
                    TextInput::make('cost_price_kurus')->label('Alis Fiyati (Kurus)')->numeric()->required(),
                    TextInput::make('group_name')->label('Grup'),
                    Toggle::make('is_active')->label('Aktif'),
                    Textarea::make('note')->label('Not')->rows(3)->columnSpanFull(),
                ])
                ->columns(2),
        ]);
    }
}

