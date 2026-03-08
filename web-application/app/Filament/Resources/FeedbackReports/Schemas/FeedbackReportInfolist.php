<?php

namespace App\Filament\Resources\FeedbackReports\Schemas;

use App\Models\FeedbackReport;
use Filament\Infolists\Components\TextEntry;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class FeedbackReportInfolist
{
    public static function configure(Schema $schema): Schema
    {
        return $schema
            ->columns(2)
            ->components([
                Section::make('Ticket Ozeti')
                    ->schema([
                        TextEntry::make('id')->label('Ticket'),
                        TextEntry::make('title')->label('Baslik'),
                        TextEntry::make('type')
                            ->label('Tur')
                            ->formatStateUsing(fn (string $state): string => match ($state) {
                                'bug' => 'Hata',
                                'feature_request' => 'Ozellik',
                                'general' => 'Genel',
                                default => $state,
                            }),
                        TextEntry::make('source')
                            ->label('Kaynak')
                            ->formatStateUsing(fn (string $state): string => match ($state) {
                                'mobile' => 'Mobil',
                                'web_pos' => 'Web POS',
                                default => $state,
                            }),
                        TextEntry::make('status')
                            ->label('Durum')
                            ->formatStateUsing(fn (string $state): string => match ($state) {
                                'new' => 'Yeni',
                                'reviewing' => 'Incelemede',
                                'answered' => 'Yanitlandi',
                                'closed' => 'Kapali',
                                default => $state,
                            }),
                        TextEntry::make('company.name')->label('Firma')->placeholder('-'),
                        TextEntry::make('mobileUser.email')->label('Kullanici')->placeholder('-'),
                        TextEntry::make('created_at')->label('Olusturma')->dateTime('d.m.Y H:i'),
                        TextEntry::make('updated_at')->label('Son Guncelleme')->dateTime('d.m.Y H:i'),
                        TextEntry::make('description')->label('Aciklama')->columnSpanFull(),
                    ])
                    ->columns(2)
                    ->columnSpan(1),
                Section::make('Mesajlar')
                    ->schema([
                        TextEntry::make('messages_summary')
                            ->label('Konusma')
                            ->state(function (FeedbackReport $record): string {
                                return $record->messages()
                                    ->where('is_internal_note', false)
                                    ->orderBy('id')
                                    ->get()
                                    ->map(function ($message): string {
                                        $author = $message->author_type === 'admin' ? 'ADMIN' : 'KULLANICI';
                                        $time = optional($message->created_at)->format('d.m.Y H:i') ?? '-';
                                        return sprintf('[%s | %s] %s', $author, $time, $message->message);
                                    })
                                    ->implode("\n\n");
                            })
                            ->placeholder('Mesaj bulunamadi.')
                            ->columnSpanFull(),
                    ])
                    ->columnSpan(1),
            ]);
    }
}

