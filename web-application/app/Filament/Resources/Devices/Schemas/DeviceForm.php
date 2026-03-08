<?php

namespace App\Filament\Resources\Devices\Schemas;

use Filament\Forms\Components\Select;
use Filament\Forms\Components\TextInput;
use Filament\Forms\Components\Toggle;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class DeviceForm
{
    public static function configure(Schema $schema): Schema
    {
        return $schema->components([
            Section::make('Cihaz Bilgisi')
                ->schema([
                    Select::make('company_id')->relationship('company', 'name')->label('Firma')->required()->disabled(),
                    TextInput::make('device_uid')->label('Cihaz UID')->disabled(),
                    TextInput::make('device_name')->label('Cihaz Adı')->disabled(),
                    TextInput::make('platform')->label('Platform')->disabled(),
                    Toggle::make('is_active')->label('Aktif'),
                    TextInput::make('activation_token_hash')->label('Token Hash')->disabled(),
                ])
                ->columns(2),
        ]);
    }
}
