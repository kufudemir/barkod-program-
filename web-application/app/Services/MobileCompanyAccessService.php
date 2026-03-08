<?php

namespace App\Services;

use App\Models\Company;
use App\Models\MobileUser;

class MobileCompanyAccessService
{
    public function findAccessibleCompany(MobileUser $mobileUser, string $companyCode): ?Company
    {
        $normalizedCode = trim($companyCode);
        if ($normalizedCode === '') {
            return null;
        }

        return Company::query()
            ->where('company_code', $normalizedCode)
            ->where('status', 'active')
            ->where(function ($query) use ($mobileUser): void {
                $query
                    ->where('owner_mobile_user_id', $mobileUser->id)
                    ->orWhereHas('staffRoles', function ($staffQuery) use ($mobileUser): void {
                        $staffQuery
                            ->where('mobile_user_id', $mobileUser->id)
                            ->where('status', 'active');
                    });
            })
            ->first();
    }
}
