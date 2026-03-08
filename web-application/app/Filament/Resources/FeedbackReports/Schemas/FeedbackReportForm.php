<?php

namespace App\Filament\Resources\FeedbackReports\Schemas;

use Filament\Forms\Components\Select;
use Filament\Forms\Components\TextInput;
use Filament\Forms\Components\Textarea;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class FeedbackReportForm
{
    public static function configure(Schema $schema): Schema
    {
        return $schema->components([
            Section::make('Ticket Bilgisi')
                ->schema([
                    TextInput::make('title')
                        ->label('Baslik')
                        ->required()
                        ->maxLength(191),
                    Select::make('type')
                        ->label('Tur')
                        ->options([
                            'bug' => 'Hata',
                            'feature_request' => 'Ozellik',
                            'general' => 'Genel',
                        ])
                        ->required(),
                    Select::make('source')
                        ->label('Kaynak')
                        ->options([
                            'mobile' => 'Mobil',
                            'web_pos' => 'Web POS',
                        ])
                        ->required(),
                    Select::make('status')
                        ->label('Durum')
                        ->options([
                            'new' => 'Yeni',
                            'reviewing' => 'Incelemede',
                            'answered' => 'Yanitlandi',
                            'closed' => 'Kapali',
                        ])
                        ->required(),
                    TextInput::make('company_id')
                        ->label('Firma Id')
                        ->numeric()
                        ->disabled(),
                    TextInput::make('mobile_user_id')
                        ->label('Kullanici Id')
                        ->numeric()
                        ->disabled(),
                    Textarea::make('description')
                        ->label('Aciklama')
                        ->rows(6)
                        ->columnSpanFull(),
                ])
                ->columns(2),
        ]);
    }
}
