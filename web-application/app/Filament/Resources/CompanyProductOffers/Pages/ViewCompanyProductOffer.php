<?php

namespace App\Filament\Resources\CompanyProductOffers\Pages;

use App\Filament\Resources\CompanyProductOffers\CompanyProductOfferResource;
use Filament\Actions\EditAction;
use Filament\Resources\Pages\ViewRecord;

class ViewCompanyProductOffer extends ViewRecord
{
    protected static string $resource = CompanyProductOfferResource::class;

    protected function getHeaderActions(): array
    {
        return [
            EditAction::make(),
        ];
    }
}
