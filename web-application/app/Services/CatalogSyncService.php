<?php

namespace App\Services;

use App\Models\CompanyProductOffer;
use App\Models\Device;
use App\Models\GlobalProduct;
use App\Models\GlobalProductNameCandidate;
use App\Models\SyncBatch;
use App\Models\SyncEventDedup;
use Illuminate\Support\Arr;
use Illuminate\Support\Carbon;
use Illuminate\Support\Facades\DB;
use InvalidArgumentException;

class CatalogSyncService
{
    public function process(Device $device, string $batchUuid, array $events): array
    {
        $now = now();

        $batch = SyncBatch::query()->create([
            'company_id' => $device->company_id,
            'device_id' => $device->id,
            'batch_uuid' => $batchUuid,
            'received_event_count' => count($events),
            'processed_event_count' => 0,
            'status' => 'processing',
            'error_summary' => null,
            'created_at' => $now,
        ]);

        $results = [];
        $processedCount = 0;
        $rejectedCount = 0;

        try {
            DB::transaction(function () use ($device, $events, $now, &$results, &$processedCount, &$rejectedCount): void {
                foreach ($events as $event) {
                    $eventUuid = (string) Arr::get($event, 'eventUuid', '');
                    $eventType = (string) Arr::get($event, 'type', '');

                    if ($eventUuid === '' || $eventType === '') {
                        $rejectedCount++;
                        $results[] = [
                            'eventUuid' => $eventUuid,
                            'status' => 'rejected',
                            'message' => 'eventUuid ve type zorunludur',
                        ];
                        continue;
                    }

                    $alreadyProcessed = SyncEventDedup::query()
                        ->where('device_id', $device->id)
                        ->where('event_uuid', $eventUuid)
                        ->exists();

                    if ($alreadyProcessed) {
                        $results[] = [
                            'eventUuid' => $eventUuid,
                            'status' => 'duplicate',
                        ];
                        continue;
                    }

                    if ($eventType !== 'PRODUCT_UPSERT') {
                        $rejectedCount++;
                        $results[] = [
                            'eventUuid' => $eventUuid,
                            'status' => 'rejected',
                            'message' => 'Desteklenmeyen event type',
                        ];
                        continue;
                    }

                    $this->processProductUpsert($device, (array) Arr::get($event, 'payload', []), $now);

                    SyncEventDedup::query()->create([
                        'device_id' => $device->id,
                        'event_uuid' => $eventUuid,
                        'processed_at' => $now,
                    ]);

                    $processedCount++;
                    $results[] = [
                        'eventUuid' => $eventUuid,
                        'status' => 'processed',
                    ];
                }
            });

            $batch->forceFill([
                'processed_event_count' => $processedCount,
                'status' => 'processed',
            ])->save();

            $device->forceFill([
                'last_sync_at' => $now,
                'last_seen_at' => $now,
            ])->save();
        } catch (\Throwable $throwable) {
            $batch->forceFill([
                'processed_event_count' => $processedCount,
                'status' => 'failed',
                'error_summary' => $throwable->getMessage(),
            ])->save();

            throw $throwable;
        }

        return [
            'accepted' => $processedCount,
            'rejected' => $rejectedCount,
            'results' => $results,
            'serverTime' => (int) (microtime(true) * 1000),
        ];
    }

    private function processProductUpsert(Device $device, array $payload, Carbon $now): void
    {
        $barcode = trim((string) Arr::get($payload, 'barcode', ''));
        $name = trim((string) Arr::get($payload, 'name', ''));
        $groupName = $this->normalizeOptionalText(Arr::get($payload, 'groupName'));

        if ($barcode === '' || $name === '') {
            throw new InvalidArgumentException('Product payload için barcode ve name zorunludur');
        }

        $product = GlobalProduct::query()->find($barcode);

        if ($product === null) {
            $product = new GlobalProduct([
                'barcode' => $barcode,
                'canonical_name' => $name,
                'group_name' => $groupName,
                'last_source_company_id' => $device->company_id,
                'last_source_device_id' => $device->id,
                'last_synced_at' => $now,
            ]);
            $product->save();
        } else {
            $product->fill([
                'canonical_name' => $name,
                'group_name' => $groupName ?? $product->group_name,
                'last_source_company_id' => $device->company_id,
                'last_source_device_id' => $device->id,
                'last_synced_at' => $now,
            ])->save();
        }

        $candidate = GlobalProductNameCandidate::query()
            ->where('barcode', $barcode)
            ->where('candidate_name', $name)
            ->first();

        if ($candidate === null) {
            GlobalProductNameCandidate::query()->create([
                'barcode' => $barcode,
                'candidate_name' => $name,
                'source_company_id' => $device->company_id,
                'source_device_id' => $device->id,
                'last_seen_at' => $now,
                'seen_count' => 1,
            ]);
        } else {
            $candidate->forceFill([
                'source_company_id' => $device->company_id,
                'source_device_id' => $device->id,
                'last_seen_at' => $now,
                'seen_count' => $candidate->seen_count + 1,
            ])->save();
        }

        CompanyProductOffer::query()->updateOrCreate(
            [
                'company_id' => $device->company_id,
                'barcode' => $barcode,
            ],
            [
                'sale_price_kurus' => (int) Arr::get($payload, 'salePriceKurus', 0),
                'cost_price_kurus' => (int) Arr::get($payload, 'costPriceKurus', 0),
                'group_name' => $groupName,
                'note' => Arr::get($payload, 'note'),
                'is_active' => (bool) Arr::get($payload, 'isActive', true),
                'source_updated_at' => $this->parseEpochMillisToDateTime(Arr::get($payload, 'sourceUpdatedAt')),
                'last_synced_at' => $now,
            ]
        );
    }

    private function normalizeOptionalText(mixed $value): ?string
    {
        $text = trim((string) ($value ?? ''));

        return $text === '' ? null : $text;
    }

    private function parseEpochMillisToDateTime(mixed $value): ?Carbon
    {
        if (! is_numeric($value)) {
            return null;
        }

        return Carbon::createFromTimestampUTC((int) floor(((int) $value) / 1000))
            ->setTimezone(config('app.timezone'));
    }
}
