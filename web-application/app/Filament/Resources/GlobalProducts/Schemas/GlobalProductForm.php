<?php

namespace App\Filament\Resources\GlobalProducts\Schemas;

use Filament\Forms\Components\TextInput;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class GlobalProductForm
{
    public static function configure(Schema $schema): Schema
    {
        return $schema->components([
            Section::make('Global Ürün')
                ->schema([
                    TextInput::make('barcode')->label('Barkod')->disabled(),
                    TextInput::make('canonical_name')->label('Ana ürün adı')->required()->maxLength(255),
                    TextInput::make('group_name')->label('Global ürün grubu')->maxLength(100),
                ])
                ->columns(2),
        ]);
    }
}
