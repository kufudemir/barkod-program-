<?php

namespace App\Filament\Resources\CompanyLicenses\Pages;

use App\Filament\Resources\CompanyLicenses\CompanyLicenseResource;
use Filament\Actions\CreateAction;
use Filament\Resources\Pages\ListRecords;

class ListCompanyLicenses extends ListRecords
{
    protected static string $resource = CompanyLicenseResource::class;

    protected function getHeaderActions(): array
    {
        return [
            CreateAction::make(),
        ];
    }
}
