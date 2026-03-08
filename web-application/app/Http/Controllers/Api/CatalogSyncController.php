<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\CatalogSyncService;
use App\Services\DeviceTokenManager;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Throwable;

class CatalogSyncController extends Controller
{
    public function __invoke(
        Request $request,
        DeviceTokenManager $tokenManager,
        CatalogSyncService $catalogSyncService
    ): JsonResponse {
        $device = $tokenManager->findActiveDeviceByToken($request->bearerToken());

        if ($device === null) {
            return response()->json(['message' => 'Geçersiz veya pasif aktivasyon tokeni'], 401);
        }

        $payload = $request->validate([
            'batchUuid' => ['required', 'string', 'max:64'],
            'deviceUid' => ['required', 'string', 'max:128'],
            'events' => ['required', 'array', 'min:1'],
        ]);

        if ($payload['deviceUid'] !== $device->device_uid) {
            return response()->json(['message' => 'deviceUid eşleşmiyor'], 409);
        }

        try {
            return response()->json(
                $catalogSyncService->process(
                    device: $device,
                    batchUuid: $payload['batchUuid'],
                    events: $payload['events']
                )
            );
        } catch (Throwable $throwable) {
            return response()->json(['message' => $throwable->getMessage()], 422);
        }
    }
}
