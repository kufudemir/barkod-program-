<?php

namespace App\Filament\Resources\Companies\Tables;

use App\Models\Company;
use Filament\Actions\Action;
use Filament\Actions\EditAction;
use Filament\Actions\ViewAction;
use Filament\Notifications\Notification;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Filters\SelectFilter;
use Filament\Tables\Table;

class CompaniesTable
{
    public static function configure(Table $table): Table
    {
        return $table
            ->columns([
                TextColumn::make('name')->label('Firma')->searchable()->sortable(),
                TextColumn::make('company_code')->label('Kod')->searchable()->copyable(),
                TextColumn::make('created_via')
                    ->label('Kaynak')
                    ->badge()
                    ->formatStateUsing(fn (string $state): string => match ($state) {
                        'guest' => 'Misafir',
                        'registered_user' => 'Kayıtlı',
                        default => 'Admin',
                    }),
                TextColumn::make('ownerMobileUser.email')
                    ->label('Sahip')
                    ->placeholder('-')
                    ->toggleable(),
                TextColumn::make('status')->label('Durum')->badge(),
                TextColumn::make('cleanup_status')
                    ->label('Temizlik Uygunluğu')
                    ->state(fn (Company $record): string => $record->created_via === 'guest' && $record->owner_mobile_user_id === null ? 'Takip ediliyor' : 'Korunuyor')
                    ->badge()
                    ->color(fn (string $state): string => $state === 'Takip ediliyor' ? 'warning' : 'success')
                    ->toggleable(),
                TextColumn::make('devices_count')
                    ->label('Cihaz')
                    ->state(fn (Company $record): int => $record->devices()->count()),
                TextColumn::make('last_sync_at')
                    ->label('Son Senkron')
                    ->state(fn (Company $record): ?string => optional($record->devices()->latest('last_sync_at')->first()?->last_sync_at)?->diffForHumans())
                    ->placeholder('-'),
                TextColumn::make('updated_at')->label('Güncellendi')->since(),
            ])
            ->filters([
                SelectFilter::make('status')
                    ->label('Durum')
                    ->options([
                        'active' => 'Aktif',
                        'blocked' => 'Bloklu',
                    ]),
                SelectFilter::make('created_via')
                    ->label('Oluşum Tipi')
                    ->options([
                        'admin' => 'Admin',
                        'guest' => 'Misafir',
                        'registered_user' => 'Kayıtlı Kullanıcı',
                    ]),
            ])
            ->recordActions([
                ViewAction::make(),
                EditAction::make(),
                Action::make('resetDevice')
                    ->label('Cihazı Sıfırla')
                    ->icon('heroicon-o-arrow-path')
                    ->requiresConfirmation()
                    ->action(function (Company $record): void {
                        $record->devices()->update([
                            'is_active' => false,
                            'activation_token_hash' => null,
                        ]);

                        Notification::make()
                            ->title('Firma cihazı sıfırlandı')
                            ->success()
                            ->send();
                    }),
            ])
            ->toolbarActions([]);
    }
}
