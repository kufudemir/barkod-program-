<?php

namespace App\Filament\Resources\SyncBatches;

use App\Filament\Resources\SyncBatches\Pages\ListSyncBatches;
use App\Filament\Resources\SyncBatches\Pages\ViewSyncBatch;
use App\Filament\Resources\SyncBatches\Schemas\SyncBatchForm;
use App\Filament\Resources\SyncBatches\Schemas\SyncBatchInfolist;
use App\Filament\Resources\SyncBatches\Tables\SyncBatchesTable;
use App\Models\SyncBatch;
use BackedEnum;
use Filament\Resources\Resource;
use Filament\Schemas\Schema;
use Filament\Support\Icons\Heroicon;
use Filament\Tables\Table;

class SyncBatchResource extends Resource
{
    protected static ?string $model = SyncBatch::class;

    protected static string|BackedEnum|null $navigationIcon = Heroicon::OutlinedQueueList;

    protected static ?string $navigationLabel = 'Teknik Senkron Kayıtları';

    protected static ?string $modelLabel = 'Teknik Senkron Kaydı';

    protected static ?string $pluralModelLabel = 'Teknik Senkron Kayıtları';

    protected static string|\UnitEnum|null $navigationGroup = 'Teknik Kayıtlar';

    protected static ?int $navigationSort = 200;

    public static function form(Schema $schema): Schema
    {
        return SyncBatchForm::configure($schema);
    }

    public static function infolist(Schema $schema): Schema
    {
        return SyncBatchInfolist::configure($schema);
    }

    public static function table(Table $table): Table
    {
        return SyncBatchesTable::configure($table);
    }

    public static function canCreate(): bool
    {
        return false;
    }

    public static function canEdit($record): bool
    {
        return false;
    }

    public static function getPages(): array
    {
        return [
            'index' => ListSyncBatches::route('/'),
            'view' => ViewSyncBatch::route('/{record}'),
        ];
    }
}
