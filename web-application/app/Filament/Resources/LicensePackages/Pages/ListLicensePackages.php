<?php

namespace App\Filament\Resources\LicensePackages\Pages;

use App\Filament\Resources\LicensePackages\LicensePackageResource;
use Filament\Actions\CreateAction;
use Filament\Resources\Pages\ListRecords;

class ListLicensePackages extends ListRecords
{
    protected static string $resource = LicensePackageResource::class;

    protected function getHeaderActions(): array
    {
        return [
            CreateAction::make(),
        ];
    }
}
