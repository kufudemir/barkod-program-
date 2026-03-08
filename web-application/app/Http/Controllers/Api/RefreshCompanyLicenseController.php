<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\CompanyLicenseSummaryService;
use App\Services\MobileUserTokenManager;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class RefreshCompanyLicenseController extends Controller
{
    public function __invoke(
        Request $request,
        MobileUserTokenManager $tokenManager,
        CompanyLicenseSummaryService $summaryService,
    ): JsonResponse {
        $mobileUser = $tokenManager->findActiveUserByToken($request->bearerToken());

        if ($mobileUser === null) {
            return response()->json(['message' => 'Geçersiz oturum'], 401);
        }

        $payload = $request->validate([
            'companyCode' => ['nullable', 'string', 'max:64'],
        ]);

        try {
            $summary = $summaryService->buildForUser(
                $mobileUser,
                $payload['companyCode'] ?? null,
            );
        } catch (\RuntimeException $exception) {
            return response()->json(['message' => $exception->getMessage()], 404);
        }

        return response()->json($summary);
    }
}
