<?php

namespace App\Filament\Resources\LicenseRequests\Schemas;

use Filament\Infolists\Components\TextEntry;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class LicenseRequestInfolist
{
    public static function configure(Schema $schema): Schema
    {
        return $schema->components([
            Section::make('Talep Detayı')
                ->schema([
                    TextEntry::make('id')->label('Talep No'),
                    TextEntry::make('requester_name')->label('Talep Eden'),
                    TextEntry::make('requester_email')->label('E-posta')->copyable(),
                    TextEntry::make('requester_phone')->label('Telefon')->placeholder('-'),
                    TextEntry::make('company.name')->label('Firma')->placeholder('-'),
                    TextEntry::make('requestedByMobileUser.email')->label('Kullanıcı Hesabı')->placeholder('-'),
                    TextEntry::make('requested_package_code')->label('Talep Paketi')->badge(),
                    TextEntry::make('status')
                        ->label('Durum')
                        ->badge()
                        ->formatStateUsing(fn (string $state): string => match ($state) {
                            'pending_payment' => 'Ödeme Bekleniyor',
                            'payment_review' => 'Ödeme İncelemede',
                            'approved' => 'Onaylandı',
                            'rejected' => 'Reddedildi',
                            'cancelled' => 'İptal',
                            default => $state,
                        }),
                    TextEntry::make('bank_reference_note')->label('Banka Referans Notu')->placeholder('-'),
                    TextEntry::make('admin_note')->label('Admin Notu')->placeholder('-')->columnSpanFull(),
                    TextEntry::make('created_at')->label('Oluşturma')->dateTime(),
                    TextEntry::make('updated_at')->label('Güncelleme')->since(),
                ])
                ->columns(2),
        ]);
    }
}
