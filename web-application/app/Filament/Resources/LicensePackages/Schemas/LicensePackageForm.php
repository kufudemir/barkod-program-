<?php

namespace App\Filament\Resources\LicensePackages\Schemas;

use Filament\Forms\Components\Placeholder;
use Filament\Forms\Components\Textarea;
use Filament\Forms\Components\TextInput;
use Filament\Forms\Components\Toggle;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class LicensePackageForm
{
    public static function configure(Schema $schema): Schema
    {
        return $schema->components([
            Section::make('Paket Şablonu')
                ->schema([
                    TextInput::make('code')
                        ->label('Paket Kodu')
                        ->required()
                        ->maxLength(20)
                        ->helperText('Teknik kod örneği: FREE, SILVER, GOLD'),
                    TextInput::make('name')
                        ->label('Paket Adı')
                        ->required()
                        ->maxLength(100),
                    TextInput::make('sort_order')
                        ->label('Sıra')
                        ->integer()
                        ->default(1)
                        ->required(),
                    Toggle::make('is_active')
                        ->label('Aktif')
                        ->default(true),
                    Textarea::make('description')
                        ->label('Açıklama')
                        ->rows(3)
                        ->columnSpanFull(),
                    Placeholder::make('matrix_note')
                        ->label('Not')
                        ->content('Paketin özellik matrisi "Paket Özellik Matrisi" ekranından yönetilir.')
                        ->columnSpanFull(),
                ])
                ->columns(2),
        ]);
    }
}
