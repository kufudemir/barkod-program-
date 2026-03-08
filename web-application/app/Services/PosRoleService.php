<?php

namespace App\Services;

use App\Models\Company;
use App\Models\CompanyStaffRole;
use App\Models\MobileUser;

class PosRoleService
{
    public const ROLE_OWNER = 'owner';
    public const ROLE_MANAGER = 'manager';
    public const ROLE_CASHIER = 'cashier';

    /**
     * @return array<int, string>
     */
    public function roles(): array
    {
        return [
            self::ROLE_OWNER,
            self::ROLE_MANAGER,
            self::ROLE_CASHIER,
        ];
    }

    public function resolveRole(MobileUser $mobileUser, Company $company): ?string
    {
        if ((int) $company->owner_mobile_user_id === (int) $mobileUser->id) {
            return self::ROLE_OWNER;
        }

        $role = CompanyStaffRole::query()
            ->where('company_id', $company->id)
            ->where('mobile_user_id', $mobileUser->id)
            ->where('status', 'active')
            ->value('role');

        $normalized = mb_strtolower(trim((string) $role), 'UTF-8');

        return in_array($normalized, $this->roles(), true)
            ? $normalized
            : null;
    }

    /**
     * @return array<string, bool>
     */
    public function permissionsForRole(?string $role): array
    {
        $normalized = mb_strtolower(trim((string) $role), 'UTF-8');

        return match ($normalized) {
            self::ROLE_OWNER => [
                'canOperatePos' => true,
                'canManagePosContext' => true,
                'canManageCompanyProfile' => true,
                'canManageReceiptProfile' => true,
                'canManageDevices' => true,
                'canManagePersonnel' => true,
                'canEditPastSales' => true,
            ],
            self::ROLE_MANAGER => [
                'canOperatePos' => true,
                'canManagePosContext' => true,
                'canManageCompanyProfile' => true,
                'canManageReceiptProfile' => true,
                'canManageDevices' => true,
                'canManagePersonnel' => false,
                'canEditPastSales' => true,
            ],
            self::ROLE_CASHIER => [
                'canOperatePos' => true,
                'canManagePosContext' => false,
                'canManageCompanyProfile' => false,
                'canManageReceiptProfile' => false,
                'canManageDevices' => false,
                'canManagePersonnel' => false,
                'canEditPastSales' => false,
            ],
            default => [
                'canOperatePos' => false,
                'canManagePosContext' => false,
                'canManageCompanyProfile' => false,
                'canManageReceiptProfile' => false,
                'canManageDevices' => false,
                'canManagePersonnel' => false,
                'canEditPastSales' => false,
            ],
        };
    }

    /**
     * @return array{role: string|null, roleLabel: string, permissions: array<string, bool>}
     */
    public function summaryFor(?string $role): array
    {
        $normalized = mb_strtolower(trim((string) $role), 'UTF-8');
        $permissions = $this->permissionsForRole($normalized);

        return [
            'role' => $normalized !== '' ? $normalized : null,
            'roleLabel' => $this->labelFor($normalized),
            'permissions' => $permissions,
        ];
    }

    public function labelFor(?string $role): string
    {
        return match (mb_strtolower(trim((string) $role), 'UTF-8')) {
            self::ROLE_OWNER => 'Owner',
            self::ROLE_MANAGER => 'Manager',
            self::ROLE_CASHIER => 'Kasiyer',
            default => 'Yetkisiz',
        };
    }
}

