<?php

namespace App\Filament\Resources\SyncBatches\Schemas;

use Filament\Forms\Components\Textarea;
use Filament\Forms\Components\TextInput;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class SyncBatchForm
{
    public static function configure(Schema $schema): Schema
    {
        return $schema->components([
            Section::make('Senkron Kaydı')
                ->schema([
                    TextInput::make('batch_uuid')->label('Batch Kimliği')->disabled(),
                    TextInput::make('status')->label('Durum')->disabled(),
                    TextInput::make('received_event_count')->label('Alınan')->disabled(),
                    TextInput::make('processed_event_count')->label('İşlenen')->disabled(),
                    Textarea::make('error_summary')->label('Hata Özeti')->disabled()->rows(4)->columnSpanFull(),
                ])
                ->columns(2),
        ]);
    }
}

