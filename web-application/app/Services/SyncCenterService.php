<?php

namespace App\Services;

use App\Filament\Resources\Companies\CompanyResource;
use App\Filament\Resources\Devices\DeviceResource;
use App\Filament\Resources\SyncBatches\SyncBatchResource;
use App\Models\Device;
use Illuminate\Support\Collection;
use Illuminate\Support\Str;

class SyncCenterService
{
    public function summary(): array
    {
        $devices = Device::query()
            ->with(['company', 'latestSyncBatch'])
            ->get();

        $latestFailedDeviceCount = $devices->filter(fn (Device $device): bool => $device->latestSyncBatch?->status === 'failed')->count();
        $pendingDeviceCount = $devices->filter(function (Device $device): bool {
            $latestBatch = $device->latestSyncBatch;

            if ($latestBatch === null) {
                return false;
            }

            $pendingCount = max((int) $latestBatch->received_event_count - (int) $latestBatch->processed_event_count, 0);

            return in_array($latestBatch->status, ['received', 'processing'], true) || $pendingCount > 0;
        })->count();

        return [
            'activeDeviceCount' => $devices->where('is_active', true)->count(),
            'companiesSyncedToday' => $devices
                ->filter(fn (Device $device): bool => $device->last_sync_at?->isToday() ?? false)
                ->pluck('company_id')
                ->filter()
                ->unique()
                ->count(),
            'latestFailedDeviceCount' => $latestFailedDeviceCount,
            'pendingDeviceCount' => $pendingDeviceCount,
        ];
    }

    public function deviceRows(): Collection
    {
        return Device::query()
            ->with(['company', 'latestSyncBatch'])
            ->get()
            ->sortByDesc(function (Device $device): int {
                return $device->latestSyncBatch?->created_at?->getTimestamp()
                    ?? $device->last_sync_at?->getTimestamp()
                    ?? $device->updated_at?->getTimestamp()
                    ?? 0;
            })
            ->values()
            ->map(function (Device $device): array {
                $latestBatch = $device->latestSyncBatch;
                $pendingCount = $latestBatch === null
                    ? 0
                    : max((int) $latestBatch->received_event_count - (int) $latestBatch->processed_event_count, 0);

                $status = $this->resolveStatus($device, $latestBatch?->status, $pendingCount);

                return [
                    'companyName' => $device->company?->name ?? '-',
                    'deviceName' => $device->device_name ?: 'Adsız cihaz',
                    'deviceUid' => $device->device_uid,
                    'isActive' => $device->is_active,
                    'lastSuccessfulSync' => $device->last_sync_at?->diffForHumans() ?? 'Henüz yok',
                    'lastBatchAt' => $latestBatch?->created_at?->diffForHumans() ?? 'Henüz yok',
                    'receivedCount' => (int) ($latestBatch?->received_event_count ?? 0),
                    'processedCount' => (int) ($latestBatch?->processed_event_count ?? 0),
                    'pendingCount' => $pendingCount,
                    'statusLabel' => $status['label'],
                    'statusColor' => $status['color'],
                    'lastError' => filled($latestBatch?->error_summary)
                        ? Str::limit((string) $latestBatch->error_summary, 140)
                        : 'Yok',
                    'companyUrl' => $device->company_id ? CompanyResource::getUrl('view', ['record' => $device->company_id]) : null,
                    'deviceUrl' => DeviceResource::getUrl('view', ['record' => $device->id]),
                    'batchUrl' => $latestBatch?->id ? SyncBatchResource::getUrl('view', ['record' => $latestBatch->id]) : null,
                ];
            });
    }

    private function resolveStatus(Device $device, ?string $batchStatus, int $pendingCount): array
    {
        if ($batchStatus === 'failed') {
            return ['label' => 'Hatalı', 'color' => 'danger'];
        }

        if (in_array($batchStatus, ['received', 'processing'], true) || $pendingCount > 0) {
            return ['label' => 'Bekliyor', 'color' => 'warning'];
        }

        if ($device->last_sync_at !== null) {
            return ['label' => 'Sağlıklı', 'color' => 'success'];
        }

        return ['label' => 'Henüz senkron yok', 'color' => 'gray'];
    }
}
