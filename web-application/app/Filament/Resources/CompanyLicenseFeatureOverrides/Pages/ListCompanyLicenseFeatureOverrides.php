<?php

namespace App\Filament\Resources\CompanyLicenseFeatureOverrides\Pages;

use App\Filament\Resources\CompanyLicenseFeatureOverrides\CompanyLicenseFeatureOverrideResource;
use Filament\Actions\CreateAction;
use Filament\Resources\Pages\ListRecords;

class ListCompanyLicenseFeatureOverrides extends ListRecords
{
    protected static string $resource = CompanyLicenseFeatureOverrideResource::class;

    protected function getHeaderActions(): array
    {
        return [
            CreateAction::make(),
        ];
    }
}
