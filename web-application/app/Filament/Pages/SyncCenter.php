<?php

namespace App\Filament\Pages;

use App\Services\SyncCenterService;
use BackedEnum;
use Filament\Pages\Page;
use UnitEnum;

class SyncCenter extends Page
{
    protected static string|BackedEnum|null $navigationIcon = 'heroicon-o-signal';
    protected static ?string $navigationLabel = 'Senkron Merkezi';
    protected static string|UnitEnum|null $navigationGroup = 'Katalog ve Senkron';
    protected static ?int $navigationSort = 50;
    protected string $view = 'filament.pages.sync-center';

    public array $summary = [];
    public array $rows = [];

    public function mount(SyncCenterService $syncCenterService): void
    {
        $this->summary = $syncCenterService->summary();
        $this->rows = $syncCenterService->deviceRows()->all();
    }

    public function getTitle(): string
    {
        return 'Senkron Merkezi';
    }

    public function getHeading(): string
    {
        return 'Senkron Merkezi';
    }
}
