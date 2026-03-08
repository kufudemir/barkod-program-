<?php

namespace App\Filament\Resources\LicenseRequests\Tables;

use App\Models\Company;
use App\Models\LicenseRequest;
use App\Services\LicenseRequestWorkflowService;
use Filament\Actions\Action;
use Filament\Actions\EditAction;
use Filament\Actions\ViewAction;
use Filament\Forms\Components\Select;
use Filament\Forms\Components\Textarea;
use Filament\Notifications\Notification;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Filters\SelectFilter;
use Filament\Tables\Table;

class LicenseRequestsTable
{
    public static function configure(Table $table): Table
    {
        return $table
            ->defaultSort('created_at', 'desc')
            ->columns([
                TextColumn::make('id')->label('Talep No')->sortable(),
                TextColumn::make('requester_name')->label('Talep Eden')->searchable(),
                TextColumn::make('requester_email')->label('E-posta')->searchable()->copyable(),
                TextColumn::make('company.name')->label('Firma')->placeholder('-')->searchable(),
                TextColumn::make('requested_package_code')->label('Paket')->badge(),
                TextColumn::make('status')
                    ->label('Durum')
                    ->badge()
                    ->color(fn (string $state): string => match ($state) {
                        'pending_payment' => 'warning',
                        'payment_review' => 'info',
                        'approved' => 'success',
                        'rejected' => 'danger',
                        'cancelled' => 'gray',
                        default => 'gray',
                    })
                    ->formatStateUsing(fn (string $state): string => match ($state) {
                        'pending_payment' => 'Ödeme Bekleniyor',
                        'payment_review' => 'Ödeme İncelemede',
                        'approved' => 'Onaylandı',
                        'rejected' => 'Reddedildi',
                        'cancelled' => 'İptal',
                        default => $state,
                    }),
                TextColumn::make('bank_reference_note')->label('Referans')->limit(28)->placeholder('-')->toggleable(),
                TextColumn::make('created_at')->label('Oluşturma')->since(),
                TextColumn::make('updated_at')->label('Güncelleme')->since(),
            ])
            ->filters([
                SelectFilter::make('status')
                    ->label('Durum')
                    ->options([
                        'pending_payment' => 'Ödeme Bekleniyor',
                        'payment_review' => 'Ödeme İncelemede',
                        'approved' => 'Onaylandı',
                        'rejected' => 'Reddedildi',
                        'cancelled' => 'İptal',
                    ]),
                SelectFilter::make('requested_package_code')
                    ->label('Paket')
                    ->options([
                        'SILVER' => 'SILVER',
                        'GOLD' => 'GOLD',
                        'PRO' => 'PRO',
                    ]),
            ])
            ->recordActions([
                ViewAction::make(),
                EditAction::make(),
                Action::make('mark_payment_review')
                    ->label('İncelemeye Al')
                    ->icon('heroicon-o-eye')
                    ->color('info')
                    ->visible(fn (LicenseRequest $record): bool => $record->status === 'pending_payment')
                    ->form([
                        Textarea::make('admin_note')
                            ->label('Admin Notu')
                            ->rows(3),
                    ])
                    ->action(function (LicenseRequest $record, array $data): void {
                        try {
                            app(LicenseRequestWorkflowService::class)->markPaymentReview(
                                $record,
                                $data['admin_note'] ?? null,
                            );
                        } catch (\RuntimeException $exception) {
                            Notification::make()
                                ->title($exception->getMessage())
                                ->danger()
                                ->send();

                            return;
                        }

                        Notification::make()
                            ->title('Talep ödeme inceleme durumuna alındı')
                            ->success()
                            ->send();
                    }),
                Action::make('approve')
                    ->label('Onayla')
                    ->icon('heroicon-o-check-circle')
                    ->color('success')
                    ->visible(fn (LicenseRequest $record): bool => $record->status === 'payment_review')
                    ->form([
                        Select::make('company_id')
                            ->label('Firma')
                            ->options(fn (): array => Company::query()->orderBy('name')->pluck('name', 'id')->all())
                            ->default(fn (LicenseRequest $record): ?int => $record->company_id)
                            ->searchable()
                            ->required(),
                        Select::make('package_code')
                            ->label('Paket')
                            ->options([
                                'SILVER' => 'SILVER',
                                'GOLD' => 'GOLD',
                            ])
                            ->default(fn (LicenseRequest $record): string => in_array($record->requested_package_code, ['SILVER', 'GOLD'], true) ? $record->requested_package_code : 'SILVER')
                            ->required(),
                        Textarea::make('admin_note')
                            ->label('Admin Notu')
                            ->rows(3),
                    ])
                    ->action(function (LicenseRequest $record, array $data): void {
                        try {
                            app(LicenseRequestWorkflowService::class)->approve(
                                $record,
                                (int) $data['company_id'],
                                (string) $data['package_code'],
                                auth()->id(),
                                $data['admin_note'] ?? null,
                            );
                        } catch (\RuntimeException $exception) {
                            Notification::make()
                                ->title($exception->getMessage())
                                ->danger()
                                ->send();

                            return;
                        }

                        Notification::make()
                            ->title('Lisans talebi onaylandı ve firma lisansı atandı')
                            ->success()
                            ->send();
                    }),
                Action::make('reject')
                    ->label('Reddet')
                    ->icon('heroicon-o-x-circle')
                    ->color('danger')
                    ->visible(fn (LicenseRequest $record): bool => $record->status === 'payment_review')
                    ->form([
                        Textarea::make('admin_note')
                            ->label('Red Gerekçesi')
                            ->rows(3)
                            ->required(),
                    ])
                    ->action(function (LicenseRequest $record, array $data): void {
                        try {
                            app(LicenseRequestWorkflowService::class)->reject(
                                $record,
                                $data['admin_note'] ?? null,
                            );
                        } catch (\RuntimeException $exception) {
                            Notification::make()
                                ->title($exception->getMessage())
                                ->danger()
                                ->send();

                            return;
                        }

                        Notification::make()
                            ->title('Lisans talebi reddedildi')
                            ->danger()
                            ->send();
                    }),
                Action::make('cancel')
                    ->label('İptal Et')
                    ->icon('heroicon-o-stop-circle')
                    ->color('gray')
                    ->visible(fn (LicenseRequest $record): bool => in_array($record->status, ['pending_payment', 'payment_review'], true))
                    ->form([
                        Textarea::make('admin_note')
                            ->label('İptal Notu')
                            ->rows(3),
                    ])
                    ->action(function (LicenseRequest $record, array $data): void {
                        try {
                            app(LicenseRequestWorkflowService::class)->cancel(
                                $record,
                                $data['admin_note'] ?? null,
                            );
                        } catch (\RuntimeException $exception) {
                            Notification::make()
                                ->title($exception->getMessage())
                                ->danger()
                                ->send();

                            return;
                        }

                        Notification::make()
                            ->title('Lisans talebi iptal edildi')
                            ->warning()
                            ->send();
                    }),
            ])
            ->toolbarActions([]);
    }
}
