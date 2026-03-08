<?php

namespace App\Filament\Resources\FeedbackReports\Tables;

use App\Models\FeedbackMessage;
use App\Models\FeedbackReport;
use Filament\Actions\Action;
use Filament\Actions\EditAction;
use Filament\Actions\ViewAction;
use Filament\Forms\Components\Select;
use Filament\Forms\Components\Textarea;
use Filament\Notifications\Notification;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Filters\SelectFilter;
use Filament\Tables\Table;

class FeedbackReportsTable
{
    public static function configure(Table $table): Table
    {
        return $table
            ->defaultSort('updated_at', 'desc')
            ->columns([
                TextColumn::make('id')->label('Ticket')->sortable(),
                TextColumn::make('title')->label('Baslik')->searchable()->limit(42),
                TextColumn::make('type')
                    ->label('Tur')
                    ->badge()
                    ->formatStateUsing(fn (string $state): string => match ($state) {
                        'bug' => 'Hata',
                        'feature_request' => 'Ozellik',
                        'general' => 'Genel',
                        default => $state,
                    }),
                TextColumn::make('source')
                    ->label('Kaynak')
                    ->badge()
                    ->formatStateUsing(fn (string $state): string => match ($state) {
                        'mobile' => 'Mobil',
                        'web_pos' => 'Web POS',
                        default => $state,
                    }),
                TextColumn::make('status')
                    ->label('Durum')
                    ->badge()
                    ->color(fn (string $state): string => match ($state) {
                        'new' => 'warning',
                        'reviewing' => 'info',
                        'answered' => 'success',
                        'closed' => 'danger',
                        default => 'gray',
                    })
                    ->formatStateUsing(fn (string $state): string => match ($state) {
                        'new' => 'Yeni',
                        'reviewing' => 'Incelemede',
                        'answered' => 'Yanitlandi',
                        'closed' => 'Kapali',
                        default => $state,
                    }),
                TextColumn::make('company.name')->label('Firma')->placeholder('-')->searchable(),
                TextColumn::make('mobileUser.email')->label('Kullanici')->placeholder('-')->searchable()->toggleable(),
                TextColumn::make('messages_count')
                    ->label('Mesaj')
                    ->state(fn (FeedbackReport $record): int => $record->messages()->where('is_internal_note', false)->count())
                    ->sortable(),
                TextColumn::make('updated_at')->label('Son Guncelleme')->since(),
                TextColumn::make('created_at')->label('Olusturma')->since()->toggleable(),
            ])
            ->filters([
                SelectFilter::make('status')
                    ->label('Durum')
                    ->options([
                        'new' => 'Yeni',
                        'reviewing' => 'Incelemede',
                        'answered' => 'Yanitlandi',
                        'closed' => 'Kapali',
                    ]),
                SelectFilter::make('source')
                    ->label('Kaynak')
                    ->options([
                        'mobile' => 'Mobil',
                        'web_pos' => 'Web POS',
                    ]),
                SelectFilter::make('type')
                    ->label('Tur')
                    ->options([
                        'bug' => 'Hata',
                        'feature_request' => 'Ozellik',
                        'general' => 'Genel',
                    ]),
            ])
            ->recordActions([
                ViewAction::make(),
                EditAction::make(),
                Action::make('admin_reply')
                    ->label('Yanitla')
                    ->icon('heroicon-o-paper-airplane')
                    ->color('success')
                    ->form([
                        Textarea::make('message')
                            ->label('Admin Yanit Mesaji')
                            ->rows(4)
                            ->required(),
                        Select::make('next_status')
                            ->label('Sonraki Durum')
                            ->options([
                                'answered' => 'Yanitlandi',
                                'reviewing' => 'Incelemede',
                                'closed' => 'Kapat',
                            ])
                            ->default('answered')
                            ->required(),
                    ])
                    ->action(function (FeedbackReport $record, array $data): void {
                        FeedbackMessage::query()->create([
                            'feedback_report_id' => $record->id,
                            'author_type' => 'admin',
                            'author_id' => auth()->id(),
                            'message' => trim((string) ($data['message'] ?? '')),
                            'is_internal_note' => false,
                        ]);

                        $record->status = (string) ($data['next_status'] ?? 'answered');
                        $record->touch();
                        $record->save();

                        Notification::make()
                            ->title('Ticket yaniti kaydedildi')
                            ->success()
                            ->send();
                    }),
                Action::make('close_ticket')
                    ->label('Kapat')
                    ->icon('heroicon-o-lock-closed')
                    ->color('danger')
                    ->visible(fn (FeedbackReport $record): bool => (string) $record->status !== 'closed')
                    ->requiresConfirmation()
                    ->action(function (FeedbackReport $record): void {
                        $record->status = 'closed';
                        $record->touch();
                        $record->save();

                        Notification::make()
                            ->title('Ticket kapatildi')
                            ->warning()
                            ->send();
                    }),
                Action::make('reopen_ticket')
                    ->label('Yeniden Ac')
                    ->icon('heroicon-o-arrow-path')
                    ->color('info')
                    ->visible(fn (FeedbackReport $record): bool => (string) $record->status === 'closed')
                    ->action(function (FeedbackReport $record): void {
                        $record->status = 'reviewing';
                        $record->touch();
                        $record->save();

                        Notification::make()
                            ->title('Ticket yeniden acildi')
                            ->success()
                            ->send();
                    }),
            ])
            ->toolbarActions([]);
    }
}

