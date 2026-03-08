<?php

namespace App\Filament\Resources\FeedbackReports;

use App\Filament\Resources\FeedbackReports\Pages\EditFeedbackReport;
use App\Filament\Resources\FeedbackReports\Pages\ListFeedbackReports;
use App\Filament\Resources\FeedbackReports\Pages\ViewFeedbackReport;
use App\Filament\Resources\FeedbackReports\Schemas\FeedbackReportForm;
use App\Filament\Resources\FeedbackReports\Schemas\FeedbackReportInfolist;
use App\Filament\Resources\FeedbackReports\Tables\FeedbackReportsTable;
use App\Models\FeedbackReport;
use BackedEnum;
use Filament\Resources\Resource;
use Filament\Schemas\Schema;
use Filament\Tables\Table;

class FeedbackReportResource extends Resource
{
    protected static ?string $model = FeedbackReport::class;

    protected static string|BackedEnum|null $navigationIcon = 'heroicon-o-chat-bubble-left-right';

    protected static ?string $navigationLabel = 'Ticket Merkezi';

    protected static ?string $modelLabel = 'Ticket';

    protected static ?string $pluralModelLabel = 'Ticket Kayitlari';

    protected static string|\UnitEnum|null $navigationGroup = 'Sistem';

    protected static ?int $navigationSort = 120;

    public static function form(Schema $schema): Schema
    {
        return FeedbackReportForm::configure($schema);
    }

    public static function infolist(Schema $schema): Schema
    {
        return FeedbackReportInfolist::configure($schema);
    }

    public static function table(Table $table): Table
    {
        return FeedbackReportsTable::configure($table);
    }

    public static function canCreate(): bool
    {
        return false;
    }

    public static function getPages(): array
    {
        return [
            'index' => ListFeedbackReports::route('/'),
            'view' => ViewFeedbackReport::route('/{record}'),
            'edit' => EditFeedbackReport::route('/{record}/edit'),
        ];
    }
}

