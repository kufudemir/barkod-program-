<?php

namespace App\Filament\Resources\GlobalProducts\Pages;

use App\Filament\Resources\GlobalProducts\GlobalProductResource;
use Filament\Resources\Pages\CreateRecord;

class CreateGlobalProduct extends CreateRecord
{
    protected static string $resource = GlobalProductResource::class;
}
