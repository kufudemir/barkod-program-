<?php

namespace App\Filament\Resources\MobileUsers\Tables;

use Filament\Actions\EditAction;
use Filament\Actions\ViewAction;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Filters\SelectFilter;
use Filament\Tables\Table;

class MobileUsersTable
{
    public static function configure(Table $table): Table
    {
        return $table
            ->columns([
                TextColumn::make('name')->label('Ad Soyad')->searchable()->sortable(),
                TextColumn::make('email')->label('E-posta')->searchable()->copyable(),
                TextColumn::make('status')->label('Durum')->badge(),
                TextColumn::make('premium_tier')->label('Premium')->badge(),
                TextColumn::make('premium_source')
                    ->label('Premium Kaynağı')
                    ->badge()
                    ->formatStateUsing(fn (string $state): string => match ($state) {
                        'TRIAL' => 'Deneme',
                        'LICENSE_CODE' => 'Lisans',
                        'GOOGLE_PLAY' => 'Google Play',
                        default => 'Yok',
                    }),
                TextColumn::make('consent_version')->label('Onay Sürümü')->placeholder('-'),
                TextColumn::make('consent_accepted_at')->label('Onay Zamanı')->since()->placeholder('-'),
                TextColumn::make('companies_count')
                    ->label('Firma')
                    ->state(fn ($record): int => $record->companies()->count()),
                TextColumn::make('last_login_at')->label('Son Giriş')->since()->placeholder('-'),
                TextColumn::make('updated_at')->label('Güncellendi')->since(),
            ])
            ->filters([
                SelectFilter::make('status')
                    ->label('Durum')
                    ->options([
                        'active' => 'Aktif',
                        'blocked' => 'Bloklu',
                    ]),
                SelectFilter::make('premium_tier')
                    ->label('Premium')
                    ->options([
                        'FREE' => 'FREE',
                        'PRO' => 'PRO',
                    ]),
            ])
            ->recordActions([
                ViewAction::make(),
                EditAction::make(),
            ])
            ->toolbarActions([]);
    }
}
