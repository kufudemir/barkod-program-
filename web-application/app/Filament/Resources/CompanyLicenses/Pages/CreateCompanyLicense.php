<?php

namespace App\Filament\Resources\CompanyLicenses\Pages;

use App\Filament\Resources\CompanyLicenses\CompanyLicenseResource;
use Filament\Resources\Pages\CreateRecord;

class CreateCompanyLicense extends CreateRecord
{
    protected static string $resource = CompanyLicenseResource::class;

    protected function mutateFormDataBeforeCreate(array $data): array
    {
        $data['assigned_by_admin_user_id'] = auth()->id();
        $data['starts_at'] = $data['starts_at'] ?? now();

        return $data;
    }

    protected function afterCreate(): void
    {
        CompanyLicenseResource::logEvent(
            $this->record->fresh(),
            'assigned',
            [
                'status' => $this->record->status,
                'package_id' => $this->record->package_id,
                'assigned_by_admin_user_id' => auth()->id(),
            ],
        );
    }
}
