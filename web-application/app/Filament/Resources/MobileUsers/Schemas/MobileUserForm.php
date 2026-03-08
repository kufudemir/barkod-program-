<?php

namespace App\Filament\Resources\MobileUsers\Schemas;

use Filament\Forms\Components\DateTimePicker;
use Filament\Forms\Components\Select;
use Filament\Forms\Components\TextInput;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class MobileUserForm
{
    public static function configure(Schema $schema): Schema
    {
        return $schema->components([
            Section::make('Mobil Kullanıcı')
                ->schema([
                    TextInput::make('name')->label('Ad Soyad')->disabled(),
                    TextInput::make('email')->label('E-posta')->disabled(),
                    Select::make('status')
                        ->label('Durum')
                        ->options([
                            'active' => 'Aktif',
                            'blocked' => 'Bloklu',
                        ])
                        ->required(),
                    Select::make('premium_tier')
                        ->label('Premium Tier')
                        ->options([
                            'FREE' => 'FREE',
                            'PRO' => 'PRO',
                        ])
                        ->required(),
                    Select::make('premium_source')
                        ->label('Premium Kaynağı')
                        ->options([
                            'NONE' => 'Yok',
                            'TRIAL' => 'Deneme',
                            'LICENSE_CODE' => 'Lisans',
                            'GOOGLE_PLAY' => 'Google Play',
                        ])
                        ->required(),
                    DateTimePicker::make('premium_activated_at')->label('Premium Aktifleştirme')->seconds(false),
                    DateTimePicker::make('premium_expires_at')->label('Premium Bitiş')->seconds(false),
                    TextInput::make('premium_license_mask')->label('Lisans Maskesi'),
                    DateTimePicker::make('last_login_at')->label('Son Giriş')->seconds(false)->disabled(),
                ])
                ->columns(2),
        ]);
    }
}
