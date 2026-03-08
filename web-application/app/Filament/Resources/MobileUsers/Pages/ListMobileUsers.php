<?php

namespace App\Filament\Resources\MobileUsers\Pages;

use App\Filament\Resources\MobileUsers\MobileUserResource;
use Filament\Resources\Pages\ListRecords;

class ListMobileUsers extends ListRecords
{
    protected static string $resource = MobileUserResource::class;
}
