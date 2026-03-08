<?php

namespace App\Filament\Resources\CompanyLicenses\Tables;

use App\Filament\Resources\CompanyLicenses\CompanyLicenseResource;
use App\Models\CompanyLicense;
use Filament\Actions\Action;
use Filament\Actions\EditAction;
use Filament\Actions\ViewAction;
use Filament\Notifications\Notification;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Filters\SelectFilter;
use Filament\Tables\Table;

class CompanyLicensesTable
{
    public static function configure(Table $table): Table
    {
        return $table
            ->defaultSort('created_at', 'desc')
            ->columns([
                TextColumn::make('company.name')->label('Firma')->searchable()->sortable(),
                TextColumn::make('company.company_code')->label('Firma Kodu')->searchable()->toggleable(),
                TextColumn::make('package.code')->label('Paket')->badge()->sortable(),
                TextColumn::make('package.name')->label('Paket Adı')->searchable()->sortable(),
                TextColumn::make('status')
                    ->label('Durum')
                    ->badge()
                    ->color(fn (string $state): string => match ($state) {
                        'active' => 'success',
                        'suspended' => 'warning',
                        'expired' => 'gray',
                        'cancelled' => 'danger',
                        default => 'gray',
                    })
                    ->formatStateUsing(fn (string $state): string => match ($state) {
                        'active' => 'Aktif',
                        'suspended' => 'Askıda',
                        'expired' => 'Süresi Doldu',
                        'cancelled' => 'İptal',
                        default => $state,
                    }),
                TextColumn::make('starts_at')->label('Başlangıç')->dateTime('d.m.Y H:i')->sortable(),
                TextColumn::make('expires_at')->label('Bitiş')->dateTime('d.m.Y H:i')->placeholder('-')->sortable(),
                TextColumn::make('source')
                    ->label('Kaynak')
                    ->badge()
                    ->formatStateUsing(fn (string $state): string => match ($state) {
                        'manual_bank_transfer' => 'Banka Transferi',
                        default => 'Admin',
                    }),
                TextColumn::make('feature_overrides_count')
                    ->label('Override')
                    ->state(fn (CompanyLicense $record): int => $record->featureOverrides()->count()),
                TextColumn::make('assignedByAdminUser.name')->label('Atayan')->placeholder('-')->toggleable(),
                TextColumn::make('updated_at')->label('Güncellendi')->since(),
            ])
            ->filters([
                SelectFilter::make('status')
                    ->label('Durum')
                    ->options([
                        'active' => 'Aktif',
                        'suspended' => 'Askıda',
                        'expired' => 'Süresi Doldu',
                        'cancelled' => 'İptal',
                    ]),
                SelectFilter::make('company_id')
                    ->label('Firma')
                    ->relationship('company', 'name'),
                SelectFilter::make('package_id')
                    ->label('Paket')
                    ->relationship('package', 'name'),
                SelectFilter::make('source')
                    ->label('Kaynak')
                    ->options([
                        'manual_admin' => 'Admin',
                        'manual_bank_transfer' => 'Banka Transferi',
                    ]),
            ])
            ->recordActions([
                ViewAction::make(),
                EditAction::make(),
                Action::make('activate')
                    ->label('Aktif Et')
                    ->icon('heroicon-o-check-circle')
                    ->color('success')
                    ->visible(fn (CompanyLicense $record): bool => $record->status !== 'active')
                    ->requiresConfirmation()
                    ->action(function (CompanyLicense $record): void {
                        $oldStatus = $record->status;
                        $record->update(['status' => 'active']);

                        CompanyLicenseResource::logEvent(
                            $record->fresh(),
                            'status_changed',
                            ['from' => $oldStatus, 'to' => 'active'],
                        );

                        Notification::make()
                            ->title('Lisans aktif edildi')
                            ->success()
                            ->send();
                    }),
                Action::make('suspend')
                    ->label('Askıya Al')
                    ->icon('heroicon-o-pause-circle')
                    ->color('warning')
                    ->visible(fn (CompanyLicense $record): bool => ! in_array($record->status, ['suspended', 'cancelled'], true))
                    ->requiresConfirmation()
                    ->action(function (CompanyLicense $record): void {
                        $oldStatus = $record->status;
                        $record->update(['status' => 'suspended']);

                        CompanyLicenseResource::logEvent(
                            $record->fresh(),
                            'status_changed',
                            ['from' => $oldStatus, 'to' => 'suspended'],
                        );

                        Notification::make()
                            ->title('Lisans askıya alındı')
                            ->warning()
                            ->send();
                    }),
                Action::make('expire')
                    ->label('Süresi Doldu Yap')
                    ->icon('heroicon-o-clock')
                    ->color('gray')
                    ->visible(fn (CompanyLicense $record): bool => ! in_array($record->status, ['expired', 'cancelled'], true))
                    ->requiresConfirmation()
                    ->action(function (CompanyLicense $record): void {
                        $oldStatus = $record->status;
                        $record->update([
                            'status' => 'expired',
                            'expires_at' => $record->expires_at ?? now(),
                        ]);

                        CompanyLicenseResource::logEvent(
                            $record->fresh(),
                            'status_changed',
                            ['from' => $oldStatus, 'to' => 'expired'],
                        );

                        Notification::make()
                            ->title('Lisans süresi doldu olarak işaretlendi')
                            ->success()
                            ->send();
                    }),
                Action::make('cancel')
                    ->label('İptal Et')
                    ->icon('heroicon-o-x-circle')
                    ->color('danger')
                    ->visible(fn (CompanyLicense $record): bool => $record->status !== 'cancelled')
                    ->requiresConfirmation()
                    ->action(function (CompanyLicense $record): void {
                        $oldStatus = $record->status;
                        $record->update([
                            'status' => 'cancelled',
                            'expires_at' => $record->expires_at ?? now(),
                        ]);

                        CompanyLicenseResource::logEvent(
                            $record->fresh(),
                            'status_changed',
                            ['from' => $oldStatus, 'to' => 'cancelled'],
                        );

                        Notification::make()
                            ->title('Lisans iptal edildi')
                            ->danger()
                            ->send();
                    }),
            ])
            ->toolbarActions([]);
    }
}
