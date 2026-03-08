<?php

namespace App\Filament\Resources\CompanyProductOffers\Pages;

use App\Filament\Resources\CompanyProductOffers\CompanyProductOfferResource;
use Filament\Actions\DeleteAction;
use Filament\Actions\ViewAction;
use Filament\Resources\Pages\EditRecord;

class EditCompanyProductOffer extends EditRecord
{
    protected static string $resource = CompanyProductOfferResource::class;

    protected function getHeaderActions(): array
    {
        return [
            ViewAction::make(),
            DeleteAction::make(),
        ];
    }
}
