<?php

namespace App\Filament\Resources\Companies\Schemas;

use App\Models\Company;
use Filament\Infolists\Components\TextEntry;
use Filament\Schemas\Components\Section;
use Filament\Schemas\Schema;

class CompanyInfolist
{
    public static function configure(Schema $schema): Schema
    {
        return $schema
            ->components([
                Section::make('Firma Özeti')
                    ->schema([
                        TextEntry::make('name')->label('Firma Adı'),
                        TextEntry::make('company_code')->label('Firma Kodu')->copyable(),
                        TextEntry::make('created_via')
                            ->label('Oluşum Tipi')
                            ->badge()
                            ->formatStateUsing(fn (string $state): string => match ($state) {
                                'guest' => 'Misafir',
                                'registered_user' => 'Kayıtlı Kullanıcı',
                                default => 'Admin',
                            }),
                        TextEntry::make('ownerMobileUser.email')
                            ->label('Sahip Kullanıcı')
                            ->placeholder('-'),
                        TextEntry::make('status')->label('Durum')->badge(),
                        TextEntry::make('cleanup_tracking')
                            ->label('Temizlik Takibi')
                            ->state(fn (Company $record): string => $record->created_via === 'guest' && $record->owner_mobile_user_id === null ? 'Etkin olmayan firma temizliğine dahil olabilir' : 'Temizlikten korunur')
                            ->badge(),
                        TextEntry::make('devices_count')
                            ->label('Toplam Cihaz')
                            ->state(fn (Company $record): int => $record->devices()->count()),
                        TextEntry::make('active_devices_count')
                            ->label('Aktif Cihaz')
                            ->state(fn (Company $record): int => $record->devices()->where('is_active', true)->count()),
                        TextEntry::make('last_sync_at')
                            ->label('Son Senkron')
                            ->state(fn (Company $record): ?string => optional($record->devices()->latest('last_sync_at')->first()?->last_sync_at)?->diffForHumans())
                            ->placeholder('-'),
                        TextEntry::make('created_at')->label('Oluşturulma')->dateTime()->placeholder('-'),
                        TextEntry::make('updated_at')->label('Güncellenme')->dateTime()->placeholder('-'),
                    ])
                    ->columns(2),
            ]);
    }
}
