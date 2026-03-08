<?php

namespace App\Filament\Resources\CompanyLicenses\Schemas;

use App\Models\CompanyLicense;
use Filament\Infolists\Components\TextEntry;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class CompanyLicenseInfolist
{
    public static function configure(Schema $schema): Schema
    {
        return $schema->components([
            Section::make('Lisans Detayı')
                ->schema([
                    TextEntry::make('id')->label('Lisans ID'),
                    TextEntry::make('company.name')->label('Firma'),
                    TextEntry::make('company.company_code')->label('Firma Kodu')->placeholder('-'),
                    TextEntry::make('package.code')->label('Paket Kodu')->badge(),
                    TextEntry::make('package.name')->label('Paket'),
                    TextEntry::make('status')
                        ->label('Durum')
                        ->badge()
                        ->formatStateUsing(fn (string $state): string => match ($state) {
                            'active' => 'Aktif',
                            'suspended' => 'Askıda',
                            'expired' => 'Süresi Doldu',
                            'cancelled' => 'İptal',
                            default => $state,
                        }),
                    TextEntry::make('starts_at')->label('Başlangıç')->dateTime()->placeholder('-'),
                    TextEntry::make('expires_at')->label('Bitiş')->dateTime()->placeholder('-'),
                    TextEntry::make('source')
                        ->label('Atama Kaynağı')
                        ->badge()
                        ->formatStateUsing(fn (string $state): string => match ($state) {
                            'manual_bank_transfer' => 'Banka Transferi',
                            default => 'Admin',
                        }),
                    TextEntry::make('assignedByAdminUser.name')->label('Atayan Admin')->placeholder('-'),
                    TextEntry::make('note')->label('Not')->placeholder('-')->columnSpanFull(),
                    TextEntry::make('updated_at')->label('Güncellendi')->since(),
                ])
                ->columns(2),
            Section::make('Override Özeti')
                ->schema([
                    TextEntry::make('feature_overrides_count')
                        ->label('Override Sayısı')
                        ->state(fn (CompanyLicense $record): int => $record->featureOverrides()->count()),
                    TextEntry::make('recent_event')
                        ->label('Son Lisans Olayı')
                        ->state(fn (CompanyLicense $record): string => $record->events()->latest('created_at')->first()?->event_type ?? '-'),
                ])
                ->columns(2),
        ]);
    }
}
