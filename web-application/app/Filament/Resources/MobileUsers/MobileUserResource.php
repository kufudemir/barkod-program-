<?php

namespace App\Filament\Resources\MobileUsers;

use App\Filament\Resources\MobileUsers\Pages\EditMobileUser;
use App\Filament\Resources\MobileUsers\Pages\ListMobileUsers;
use App\Filament\Resources\MobileUsers\Pages\ViewMobileUser;
use App\Filament\Resources\MobileUsers\Schemas\MobileUserForm;
use App\Filament\Resources\MobileUsers\Schemas\MobileUserInfolist;
use App\Filament\Resources\MobileUsers\Tables\MobileUsersTable;
use App\Models\MobileUser;
use BackedEnum;
use Filament\Resources\Resource;
use Filament\Schemas\Schema;
use Filament\Support\Icons\Heroicon;
use Filament\Tables\Table;

class MobileUserResource extends Resource
{
    protected static ?string $model = MobileUser::class;

    protected static string|BackedEnum|null $navigationIcon = Heroicon::OutlinedUsers;

    protected static ?string $navigationLabel = 'Mobil Kullanıcılar';

    protected static ?string $modelLabel = 'Mobil Kullanıcı';

    protected static ?string $pluralModelLabel = 'Mobil Kullanıcılar';

    protected static string|\UnitEnum|null $navigationGroup = 'Kullanıcılar ve Firmalar';

    public static function form(Schema $schema): Schema
    {
        return MobileUserForm::configure($schema);
    }

    public static function infolist(Schema $schema): Schema
    {
        return MobileUserInfolist::configure($schema);
    }

    public static function table(Table $table): Table
    {
        return MobileUsersTable::configure($table);
    }

    public static function canCreate(): bool
    {
        return false;
    }

    public static function getPages(): array
    {
        return [
            'index' => ListMobileUsers::route('/'),
            'view' => ViewMobileUser::route('/{record}'),
            'edit' => EditMobileUser::route('/{record}/edit'),
        ];
    }
}

