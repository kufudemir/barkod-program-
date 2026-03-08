<?php

namespace App\Filament\Resources\LicenseRequests\Pages;

use App\Filament\Resources\LicenseRequests\LicenseRequestResource;
use Filament\Actions\EditAction;
use Filament\Resources\Pages\ViewRecord;

class ViewLicenseRequest extends ViewRecord
{
    protected static string $resource = LicenseRequestResource::class;

    protected function getHeaderActions(): array
    {
        return [
            EditAction::make(),
        ];
    }
}
