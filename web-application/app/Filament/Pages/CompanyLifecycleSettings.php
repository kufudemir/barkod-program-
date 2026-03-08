<?php

namespace App\Filament\Pages;

use App\Services\InactiveCompanyCleanupService;
use BackedEnum;
use Filament\Notifications\Notification;
use Filament\Pages\Page;
use UnitEnum;

class CompanyLifecycleSettings extends Page
{
    protected static string|BackedEnum|null $navigationIcon = 'heroicon-o-clock';
    protected static ?string $navigationLabel = 'Firma Yaşam Döngüsü';
    protected static string|UnitEnum|null $navigationGroup = 'Sistem';
    protected static ?int $navigationSort = 998;
    protected string $view = 'filament.pages.company-lifecycle-settings';

    public bool $cleanupEnabled = false;
    public int $cleanupDays = 30;
    public int $previewCount = 0;
    public ?string $lastRunAt = null;

    public function mount(InactiveCompanyCleanupService $cleanupService): void
    {
        $this->cleanupEnabled = $cleanupService->isEnabled();
        $this->cleanupDays = $cleanupService->thresholdDays();
        $this->previewCount = $cleanupService->previewCount($this->cleanupDays);
        $this->lastRunAt = $cleanupService->lastRunAt()?->diffForHumans();
    }

    public function save(InactiveCompanyCleanupService $cleanupService): void
    {
        $this->cleanupDays = max(1, min(365, (int) $this->cleanupDays));
        $cleanupService->saveSettings($this->cleanupEnabled, $this->cleanupDays);
        $this->previewCount = $cleanupService->previewCount($this->cleanupDays);
        $this->lastRunAt = $cleanupService->lastRunAt()?->diffForHumans();

        Notification::make()
            ->title('Firma temizleme ayarları kaydedildi')
            ->success()
            ->send();
    }

    public function runCleanupNow(InactiveCompanyCleanupService $cleanupService): void
    {
        $deleted = $cleanupService->run($this->cleanupDays);
        $this->previewCount = $cleanupService->previewCount($this->cleanupDays);
        $this->lastRunAt = $cleanupService->lastRunAt()?->diffForHumans();

        Notification::make()
            ->title($deleted > 0 ? "$deleted firma temizlendi" : 'Temizlenecek firma bulunamadı')
            ->success()
            ->send();
    }

    public function refreshPreview(InactiveCompanyCleanupService $cleanupService): void
    {
        $this->cleanupDays = max(1, min(365, (int) $this->cleanupDays));
        $this->previewCount = $cleanupService->previewCount($this->cleanupDays);
    }

    public function getTitle(): string
    {
        return 'Firma Yaşam Döngüsü';
    }

    public function getHeading(): string
    {
        return 'Firma Yaşam Döngüsü';
    }
}
