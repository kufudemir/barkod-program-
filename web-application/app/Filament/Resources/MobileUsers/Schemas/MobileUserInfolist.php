<?php

namespace App\Filament\Resources\MobileUsers\Schemas;

use Filament\Infolists\Components\TextEntry;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class MobileUserInfolist
{
    public static function configure(Schema $schema): Schema
    {
        return $schema->components([
            Section::make('Hesap Durumu')
                ->schema([
                    TextEntry::make('name')->label('Ad Soyad'),
                    TextEntry::make('email')->label('E-posta')->copyable(),
                    TextEntry::make('status')->label('Durum')->badge(),
                    TextEntry::make('premium_tier')->label('Premium Tier')->badge(),
                    TextEntry::make('premium_source')->label('Premium Kaynağı')->badge(),
                    TextEntry::make('premium_license_mask')->label('Lisans Maskesi')->placeholder('-'),
                    TextEntry::make('consent_version')->label('Onay Sürümü')->placeholder('-'),
                    TextEntry::make('consent_accepted_at')->label('Onay Zamanı')->dateTime()->placeholder('-'),
                    TextEntry::make('companies_count')
                        ->label('Firma Sayısı')
                        ->state(fn ($record): int => $record->companies()->count()),
                    TextEntry::make('last_login_at')->label('Son Giriş')->dateTime()->placeholder('-'),
                    TextEntry::make('premium_activated_at')->label('Premium Aktifleştirme')->dateTime()->placeholder('-'),
                    TextEntry::make('premium_expires_at')->label('Premium Bitiş')->dateTime()->placeholder('-'),
                    TextEntry::make('created_at')->label('Kayıt Tarihi')->dateTime(),
                    TextEntry::make('updated_at')->label('Güncellenme')->dateTime(),
                ])
                ->columns(2),
        ]);
    }
}
