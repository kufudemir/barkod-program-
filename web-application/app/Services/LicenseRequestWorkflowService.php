<?php

namespace App\Services;

use App\Models\Company;
use App\Models\CompanyLicense;
use App\Models\CompanyLicenseEvent;
use App\Models\LicensePackage;
use App\Models\LicenseRequest;
use App\Models\MobileUser;
use Illuminate\Support\Facades\DB;

class LicenseRequestWorkflowService
{
    public function createRequest(
        string $requesterName,
        string $requesterEmail,
        ?string $requesterPhone,
        string $requestedPackageCode,
        ?string $bankReferenceNote,
        ?string $companyCode,
        ?MobileUser $mobileUser,
    ): LicenseRequest {
        $normalizedPackage = $this->normalizePackageCode($requestedPackageCode);
        $companyId = null;

        if (filled($companyCode)) {
            $company = Company::query()
                ->where('company_code', trim((string) $companyCode))
                ->first();

            if ($company !== null) {
                $companyId = $company->id;
            }
        }

        return LicenseRequest::query()->create([
            'company_id' => $companyId,
            'requested_by_mobile_user_id' => $mobileUser?->id,
            'requester_name' => $requesterName,
            'requester_email' => mb_strtolower(trim($requesterEmail), 'UTF-8'),
            'requester_phone' => filled($requesterPhone) ? trim((string) $requesterPhone) : null,
            'requested_package_code' => $normalizedPackage,
            'status' => 'pending_payment',
            'bank_reference_note' => filled($bankReferenceNote) ? trim((string) $bankReferenceNote) : null,
            'admin_note' => null,
        ]);
    }

    public function markPaymentReview(LicenseRequest $licenseRequest, ?string $adminNote = null): LicenseRequest
    {
        $this->assertTransitionAllowed($licenseRequest->status, 'payment_review');

        $licenseRequest->forceFill([
            'status' => 'payment_review',
            'admin_note' => $this->mergeAdminNote($licenseRequest->admin_note, $adminNote),
        ])->save();

        return $licenseRequest->fresh();
    }

    public function approve(
        LicenseRequest $licenseRequest,
        int $companyId,
        string $packageCode,
        ?int $adminUserId,
        ?string $adminNote = null,
    ): CompanyLicense {
        $this->assertTransitionAllowed($licenseRequest->status, 'approved');

        return DB::transaction(function () use ($licenseRequest, $companyId, $packageCode, $adminUserId, $adminNote): CompanyLicense {
            $normalizedPackageCode = $this->normalizePackageCode($packageCode);
            $package = LicensePackage::query()
                ->where('code', $normalizedPackageCode)
                ->first();

            if ($package === null) {
                throw new \RuntimeException("Paket bulunamadı: {$normalizedPackageCode}");
            }

            $activeLicenses = CompanyLicense::query()
                ->where('company_id', $companyId)
                ->where('status', 'active')
                ->get();

            foreach ($activeLicenses as $activeLicense) {
                $activeLicense->forceFill(['status' => 'suspended'])->save();

                CompanyLicenseEvent::query()->create([
                    'company_license_id' => $activeLicense->id,
                    'event_type' => 'auto_suspended_by_new_license',
                    'payload_json' => [
                        'license_request_id' => $licenseRequest->id,
                    ],
                    'created_at' => now(),
                ]);
            }

            $companyLicense = CompanyLicense::query()->create([
                'company_id' => $companyId,
                'package_id' => $package->id,
                'status' => 'active',
                'starts_at' => now(),
                'expires_at' => null,
                'assigned_by_admin_user_id' => $adminUserId,
                'source' => 'manual_bank_transfer',
                'note' => $adminNote,
            ]);

            CompanyLicenseEvent::query()->create([
                'company_license_id' => $companyLicense->id,
                'event_type' => 'assigned_from_license_request',
                'payload_json' => [
                    'license_request_id' => $licenseRequest->id,
                    'package_code' => $normalizedPackageCode,
                ],
                'created_at' => now(),
            ]);

            $licenseRequest->forceFill([
                'company_id' => $companyId,
                'requested_package_code' => $normalizedPackageCode,
                'status' => 'approved',
                'admin_note' => $this->mergeAdminNote($licenseRequest->admin_note, $adminNote),
            ])->save();

            return $companyLicense;
        });
    }

    public function reject(LicenseRequest $licenseRequest, ?string $adminNote = null): LicenseRequest
    {
        $this->assertTransitionAllowed($licenseRequest->status, 'rejected');

        $licenseRequest->forceFill([
            'status' => 'rejected',
            'admin_note' => $this->mergeAdminNote($licenseRequest->admin_note, $adminNote),
        ])->save();

        return $licenseRequest->fresh();
    }

    public function cancel(LicenseRequest $licenseRequest, ?string $adminNote = null): LicenseRequest
    {
        $this->assertTransitionAllowed($licenseRequest->status, 'cancelled');

        $licenseRequest->forceFill([
            'status' => 'cancelled',
            'admin_note' => $this->mergeAdminNote($licenseRequest->admin_note, $adminNote),
        ])->save();

        return $licenseRequest->fresh();
    }

    public function normalizePackageCode(string $packageCode): string
    {
        $normalized = strtoupper(trim($packageCode));

        return $normalized === 'PRO' ? 'SILVER' : $normalized;
    }

    private function assertTransitionAllowed(string $currentStatus, string $nextStatus): void
    {
        $allowed = match ($currentStatus) {
            'pending_payment' => ['payment_review', 'cancelled'],
            'payment_review' => ['approved', 'rejected', 'cancelled'],
            default => [],
        };

        if (! in_array($nextStatus, $allowed, true)) {
            throw new \RuntimeException("Geçersiz durum geçişi: {$currentStatus} -> {$nextStatus}");
        }
    }

    private function mergeAdminNote(?string $current, ?string $new): ?string
    {
        $new = trim((string) $new);

        if ($new === '') {
            return $current;
        }

        if (blank($current)) {
            return $new;
        }

        return trim($current) . "\n" . $new;
    }
}
