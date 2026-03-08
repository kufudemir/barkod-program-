<?php

namespace App\Services;

use App\Models\Company;
use App\Models\MobileUser;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Http\Request;
use RuntimeException;

class PosAuthManager
{
    public function __construct(
        private readonly PosRoleService $roleService,
    ) {
    }

    public function attemptLogin(Request $request, string $email, string $password, ?string $companyCode = null): void
    {
        $mobileUser = MobileUser::query()
            ->where('email', mb_strtolower(trim($email), 'UTF-8'))
            ->where('status', 'active')
            ->first();

        if ($mobileUser === null || ! $mobileUser->verifyPassword($password)) {
            throw new RuntimeException('E-posta veya sifre hatali.');
        }

        $company = $this->resolveCompany($mobileUser, $companyCode);

        if ($company === null) {
            throw new RuntimeException('Bu hesap icin aktif firma bulunamadi.');
        }

        $role = $this->roleService->resolveRole($mobileUser, $company);
        if ($role === null) {
            throw new RuntimeException('Bu hesap secili firmada web POS yetkisine sahip degil.');
        }

        $request->session()->regenerate();

        $request->session()->put('pos.auth', [
            'mobile_user_id' => $mobileUser->id,
            'company_id' => $company->id,
            'company_code' => $company->company_code,
            'role' => $role,
            'branch_id' => null,
            'register_id' => null,
            'pos_session_id' => null,
            'sale_session_id' => null,
            'session_closed' => false,
            'logged_at' => now()->toIso8601String(),
        ]);
        $request->session()->save();
    }

    public function logout(Request $request): void
    {
        $request->session()->forget('pos.auth');
        $request->session()->invalidate();
        $request->session()->regenerateToken();
    }

    public function resolveAuthenticatedUser(Request $request): ?MobileUser
    {
        $mobileUserId = (int) $request->session()->get('pos.auth.mobile_user_id', 0);
        if ($mobileUserId <= 0) {
            return null;
        }

        return MobileUser::query()
            ->where('id', $mobileUserId)
            ->where('status', 'active')
            ->first();
    }

    public function resolveAuthenticatedCompany(Request $request): ?Company
    {
        $mobileUser = $this->resolveAuthenticatedUser($request);
        $companyId = (int) $request->session()->get('pos.auth.company_id', 0);

        if (! $mobileUser instanceof MobileUser || $companyId <= 0) {
            return null;
        }

        $company = Company::query()
            ->where('id', $companyId)
            ->where('status', 'active')
            ->first();

        if (! $company instanceof Company) {
            return null;
        }

        $role = $this->roleService->resolveRole($mobileUser, $company);
        if ($role === null) {
            return null;
        }

        $session = (array) $request->session()->get('pos.auth', []);
        if (($session['role'] ?? null) !== $role) {
            $session['role'] = $role;
            $request->session()->put('pos.auth', $session);
            $request->session()->save();
        }

        return $company;
    }

    /**
     * @return array<int, array{name: string, companyCode: string, role: string|null, roleLabel: string}>
     */
    public function listOwnedCompaniesByEmail(string $email): array
    {
        $mobileUser = MobileUser::query()
            ->where('email', mb_strtolower(trim($email), 'UTF-8'))
            ->where('status', 'active')
            ->first();

        if ($mobileUser === null) {
            return [];
        }

        return $this->accessibleCompaniesQuery((int) $mobileUser->id)
            ->where('status', 'active')
            ->orderBy('name')
            ->get()
            ->map(function (Company $company) use ($mobileUser): array {
                $role = $this->roleService->resolveRole($mobileUser, $company);

                return [
                    'name' => $company->name,
                    'companyCode' => $company->company_code,
                    'role' => $role,
                    'roleLabel' => $this->roleService->labelFor($role),
                ];
            })
            ->all();
    }

    public function switchCompany(Request $request, string $companyCode): void
    {
        $mobileUser = $this->resolveAuthenticatedUser($request);
        if (! $mobileUser instanceof MobileUser) {
            throw new RuntimeException('POS oturumu bulunamadi.');
        }

        $company = $this->resolveCompany($mobileUser, $companyCode);

        if ($company === null) {
            throw new RuntimeException('Secilen firma bulunamadi.');
        }

        $role = $this->roleService->resolveRole($mobileUser, $company);
        if ($role === null) {
            throw new RuntimeException('Secili firma icin web POS yetkiniz bulunmuyor.');
        }

        $session = (array) $request->session()->get('pos.auth', []);
        $session['company_id'] = $company->id;
        $session['company_code'] = $company->company_code;
        $session['role'] = $role;
        $session['branch_id'] = null;
        $session['register_id'] = null;
        $session['pos_session_id'] = null;
        $session['sale_session_id'] = null;
        $session['session_closed'] = false;
        $request->session()->put('pos.auth', $session);
        $request->session()->save();
    }

    /**
     * @return array{role: string|null, roleLabel: string, permissions: array<string, bool>}
     */
    public function resolveRoleSummary(Request $request): array
    {
        $mobileUser = $this->resolveAuthenticatedUser($request);
        $company = $this->resolveAuthenticatedCompany($request);

        if (! $mobileUser instanceof MobileUser || ! $company instanceof Company) {
            return $this->roleService->summaryFor(null);
        }

        $role = $this->roleService->resolveRole($mobileUser, $company);

        return $this->roleService->summaryFor($role);
    }

    private function resolveCompany(MobileUser $mobileUser, ?string $companyCode): ?Company
    {
        $query = $this->accessibleCompaniesQuery((int) $mobileUser->id)
            ->where('status', 'active');

        if (filled($companyCode)) {
            $query->where('company_code', trim((string) $companyCode));
        } else {
            $query->orderByDesc('updated_at');
        }

        return $query->first();
    }

    private function accessibleCompaniesQuery(int $mobileUserId): Builder
    {
        $roles = $this->roleService->roles();

        return Company::query()
            ->where(function ($query) use ($mobileUserId, $roles): void {
                $query
                    ->where('owner_mobile_user_id', $mobileUserId)
                    ->orWhereHas('staffRoles', function ($staffQuery) use ($mobileUserId, $roles): void {
                        $staffQuery
                            ->where('mobile_user_id', $mobileUserId)
                            ->where('status', 'active')
                            ->whereIn('role', $roles);
                    });
            });
    }
}
