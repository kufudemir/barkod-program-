<?php

namespace App\Filament\Resources\LicensePackageFeatures\Pages;

use App\Filament\Resources\LicensePackageFeatures\LicensePackageFeatureResource;
use Filament\Actions\DeleteAction;
use Filament\Resources\Pages\EditRecord;

class EditLicensePackageFeature extends EditRecord
{
    protected static string $resource = LicensePackageFeatureResource::class;

    protected function getHeaderActions(): array
    {
        return [
            DeleteAction::make(),
        ];
    }
}
