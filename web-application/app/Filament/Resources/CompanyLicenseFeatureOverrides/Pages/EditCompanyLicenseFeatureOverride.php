<?php

namespace App\Filament\Resources\CompanyLicenseFeatureOverrides\Pages;

use App\Filament\Resources\CompanyLicenseFeatureOverrides\CompanyLicenseFeatureOverrideResource;
use Filament\Actions\DeleteAction;
use Filament\Resources\Pages\EditRecord;

class EditCompanyLicenseFeatureOverride extends EditRecord
{
    protected static string $resource = CompanyLicenseFeatureOverrideResource::class;

    protected function getHeaderActions(): array
    {
        return [
            DeleteAction::make(),
        ];
    }
}
