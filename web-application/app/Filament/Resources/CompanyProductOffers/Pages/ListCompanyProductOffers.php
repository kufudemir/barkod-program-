<?php

namespace App\Filament\Resources\CompanyProductOffers\Pages;

use App\Filament\Resources\CompanyProductOffers\CompanyProductOfferResource;
use Filament\Resources\Pages\ListRecords;

class ListCompanyProductOffers extends ListRecords
{
    protected static string $resource = CompanyProductOfferResource::class;

    protected function getHeaderActions(): array
    {
        return [];
    }
}
