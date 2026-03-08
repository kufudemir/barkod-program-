<?php

namespace App\Filament\Resources\LicenseRequests\Schemas;

use Filament\Forms\Components\Select;
use Filament\Forms\Components\Textarea;
use Filament\Forms\Components\TextInput;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class LicenseRequestForm
{
    public static function configure(Schema $schema): Schema
    {
        return $schema->components([
            Section::make('Lisans Talebi')
                ->schema([
                    TextInput::make('requester_name')
                        ->label('Talep Eden')
                        ->required(),
                    TextInput::make('requester_email')
                        ->label('E-posta')
                        ->email()
                        ->required(),
                    TextInput::make('requester_phone')
                        ->label('Telefon'),
                    Select::make('company_id')
                        ->label('Firma')
                        ->relationship('company', 'name')
                        ->searchable()
                        ->preload(),
                    Select::make('requested_by_mobile_user_id')
                        ->label('Talep Eden Kullanıcı')
                        ->relationship('requestedByMobileUser', 'email')
                        ->searchable()
                        ->preload(),
                    Select::make('requested_package_code')
                        ->label('Talep Edilen Paket')
                        ->options([
                            'SILVER' => 'SILVER',
                            'GOLD' => 'GOLD',
                            'PRO' => 'PRO',
                        ])
                        ->required(),
                    Select::make('status')
                        ->label('Durum')
                        ->options([
                            'pending_payment' => 'Ödeme Bekleniyor',
                            'payment_review' => 'Ödeme İncelemede',
                            'approved' => 'Onaylandı',
                            'rejected' => 'Reddedildi',
                            'cancelled' => 'İptal',
                        ])
                        ->required(),
                    TextInput::make('bank_reference_note')
                        ->label('Banka Referans Notu'),
                    Textarea::make('admin_note')
                        ->label('Admin Notu')
                        ->rows(4)
                        ->columnSpanFull(),
                ])
                ->columns(2),
        ]);
    }
}
