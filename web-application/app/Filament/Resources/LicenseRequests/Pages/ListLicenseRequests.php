<?php

namespace App\Filament\Resources\LicenseRequests\Pages;

use App\Filament\Resources\LicenseRequests\LicenseRequestResource;
use Filament\Resources\Pages\ListRecords;

class ListLicenseRequests extends ListRecords
{
    protected static string $resource = LicenseRequestResource::class;

    protected function getHeaderActions(): array
    {
        return [];
    }
}
