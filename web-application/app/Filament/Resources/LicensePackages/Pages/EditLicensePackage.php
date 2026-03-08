<?php

namespace App\Filament\Resources\LicensePackages\Pages;

use App\Filament\Resources\LicensePackages\LicensePackageResource;
use App\Models\LicensePackage;
use Filament\Actions\DeleteAction;
use Filament\Resources\Pages\EditRecord;

class EditLicensePackage extends EditRecord
{
    protected static string $resource = LicensePackageResource::class;

    protected function getHeaderActions(): array
    {
        return [
            DeleteAction::make()
                ->visible(fn (LicensePackage $record): bool => ! in_array($record->code, ['FREE', 'SILVER', 'GOLD'], true)),
        ];
    }
}
