<?php

namespace App\Filament\Resources\FeatureFlags\Schemas;

use Filament\Forms\Components\Select;
use Filament\Forms\Components\Textarea;
use Filament\Forms\Components\TextInput;
use Filament\Forms\Components\Toggle;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class FeatureFlagForm
{
    public static function configure(Schema $schema): Schema
    {
        return $schema->components([
            Section::make('Feature Flag')
                ->schema([
                    TextInput::make('key')
                        ->label('Key')
                        ->required()
                        ->maxLength(120),
                    TextInput::make('title')
                        ->label('Başlık')
                        ->required()
                        ->maxLength(150),
                    Select::make('scope')
                        ->label('Kapsam')
                        ->options([
                            'mobile' => 'Mobil',
                            'web_pos' => 'Web POS',
                            'admin' => 'Admin',
                            'shared' => 'Ortak',
                        ])
                        ->required(),
                    Toggle::make('is_core')
                        ->label('Core Özellik')
                        ->default(false)
                        ->helperText('Core özellikler her zaman açıktır.'),
                    Textarea::make('description')
                        ->label('Açıklama')
                        ->rows(3)
                        ->columnSpanFull(),
                ])
                ->columns(2),
        ]);
    }
}
