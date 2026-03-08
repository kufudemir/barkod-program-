<?php

namespace App\Filament\Resources\Companies\Schemas;

use Filament\Forms\Components\Placeholder;
use Filament\Forms\Components\Select;
use Filament\Forms\Components\TextInput;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class CompanyForm
{
    public static function configure(Schema $schema): Schema
    {
        return $schema
            ->components([
                Section::make('Firma Bilgisi')
                    ->schema([
                        TextInput::make('name')
                            ->label('Firma Adı')
                            ->required()
                            ->maxLength(255),
                        TextInput::make('company_code')
                            ->label('Firma Kodu')
                            ->helperText('Boş bırakılırsa otomatik üretilir.')
                            ->maxLength(64),
                        TextInput::make('ownerMobileUser.email')
                            ->label('Sahip Kullanıcı')
                            ->disabled()
                            ->dehydrated(false)
                            ->placeholder('Misafir veya admin kaynaklı'),
                        Select::make('created_via')
                            ->label('Oluşum Tipi')
                            ->options([
                                'admin' => 'Admin',
                                'guest' => 'Misafir',
                                'registered_user' => 'Kayıtlı Kullanıcı',
                            ])
                            ->required(),
                        Select::make('status')
                            ->label('Durum')
                            ->options([
                                'active' => 'Aktif',
                                'blocked' => 'Bloklu',
                            ])
                            ->default('active')
                            ->required(),
                        Placeholder::make('tek_cihaz_kurali')
                            ->label('Aktivasyon Notu')
                            ->content('İlk sürümde her firma için yalnızca tek aktif cihaz desteklenir.'),
                    ])
                    ->columns(2),
            ]);
    }
}
