<?php

namespace App\Filament\Resources\CompanyLicenses\Pages;

use App\Filament\Resources\CompanyLicenses\CompanyLicenseResource;
use Filament\Actions\EditAction;
use Filament\Resources\Pages\ViewRecord;

class ViewCompanyLicense extends ViewRecord
{
    protected static string $resource = CompanyLicenseResource::class;

    protected function getHeaderActions(): array
    {
        return [
            EditAction::make(),
        ];
    }
}
