<?php

namespace App\Filament\Resources\CompanyLicenses\Pages;

use App\Filament\Resources\CompanyLicenses\CompanyLicenseResource;
use Filament\Actions\ViewAction;
use Filament\Resources\Pages\EditRecord;

class EditCompanyLicense extends EditRecord
{
    protected static string $resource = CompanyLicenseResource::class;

    protected string $previousStatus = '';

    protected function getHeaderActions(): array
    {
        return [
            ViewAction::make(),
        ];
    }

    protected function beforeSave(): void
    {
        $this->previousStatus = (string) $this->record->status;
    }

    protected function mutateFormDataBeforeSave(array $data): array
    {
        $data['assigned_by_admin_user_id'] = auth()->id();

        return $data;
    }

    protected function afterSave(): void
    {
        $currentStatus = (string) $this->record->fresh()->status;

        CompanyLicenseResource::logEvent(
            $this->record->fresh(),
            'updated',
            [
                'from_status' => $this->previousStatus,
                'to_status' => $currentStatus,
                'assigned_by_admin_user_id' => auth()->id(),
            ],
        );
    }
}
