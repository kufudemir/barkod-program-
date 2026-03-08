<?php

namespace App\Filament\Resources\LicensePackageFeatures\Pages;

use App\Filament\Resources\LicensePackageFeatures\LicensePackageFeatureResource;
use Filament\Actions\CreateAction;
use Filament\Resources\Pages\ListRecords;

class ListLicensePackageFeatures extends ListRecords
{
    protected static string $resource = LicensePackageFeatureResource::class;

    protected function getHeaderActions(): array
    {
        return [
            CreateAction::make(),
        ];
    }
}
