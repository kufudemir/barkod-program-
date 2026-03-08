<?php

namespace App\Filament\Resources\GlobalProducts\Pages;

use App\Filament\Resources\GlobalProducts\GlobalProductResource;
use Filament\Resources\Pages\ListRecords;

class ListGlobalProducts extends ListRecords
{
    protected static string $resource = GlobalProductResource::class;

    protected function getHeaderActions(): array
    {
        return [];
    }
}
