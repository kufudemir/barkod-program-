<?php

namespace App\Http\Controllers;

use App\Models\Company;
use App\Models\CompanyStaffRole;
use App\Models\Device;
use App\Models\MobileUser;
use App\Models\PosSession;
use App\Services\PosAuthManager;
use App\Services\PosRoleService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class PosManagementController extends Controller
{
    public function overview(
        Request $request,
        PosAuthManager $authManager,
    ): JsonResponse {
        $mobileUser = $authManager->resolveAuthenticatedUser($request);
        $company = $authManager->resolveAuthenticatedCompany($request);
        if (! $mobileUser instanceof MobileUser || ! $company instanceof Company) {
            return response()->json([
                'ok' => false,
                'message' => 'POS oturumu bulunamadi.',
                'redirect' => route('pos.login', [], false),
            ], 401);
        }

        $roleSummary = $authManager->resolveRoleSummary($request);

        return response()->json([
            'ok' => true,
            'data' => $this->buildOverviewPayload($company, $roleSummary),
        ]);
    }

    public function toggleDevice(
        Request $request,
        Device $device,
        PosAuthManager $authManager,
    ): JsonResponse {
        $mobileUser = $authManager->resolveAuthenticatedUser($request);
        $company = $authManager->resolveAuthenticatedCompany($request);
        if (! $mobileUser instanceof MobileUser || ! $company instanceof Company) {
            return response()->json([
                'ok' => false,
                'message' => 'POS oturumu bulunamadi.',
                'redirect' => route('pos.login', [], false),
            ], 401);
        }

        $roleSummary = $authManager->resolveRoleSummary($request);
        $permissions = is_array($roleSummary['permissions'] ?? null) ? $roleSummary['permissions'] : [];
        if (!((bool) ($permissions['canManageDevices'] ?? false))) {
            return response()->json([
                'ok' => false,
                'message' => 'Cihaz yonetimi icin yetkiniz yok.',
            ], 403);
        }

        if ((int) $device->company_id !== (int) $company->id) {
            return response()->json([
                'ok' => false,
                'message' => 'Cihaz bu firmaya ait degil.',
            ], 404);
        }

        $payload = $request->validate([
            'is_active' => ['nullable', 'boolean'],
        ]);

        $nextState = array_key_exists('is_active', $payload)
            ? (bool) $payload['is_active']
            : !((bool) $device->is_active);

        $device->update([
            'is_active' => $nextState,
        ]);

        return response()->json([
            'ok' => true,
            'message' => $nextState ? 'Cihaz aktif edildi.' : 'Cihaz pasife alindi.',
            'data' => $this->buildOverviewPayload($company, $roleSummary),
        ]);
    }

    public function closeSession(
        Request $request,
        PosSession $posSession,
        PosAuthManager $authManager,
    ): JsonResponse {
        $mobileUser = $authManager->resolveAuthenticatedUser($request);
        $company = $authManager->resolveAuthenticatedCompany($request);
        if (! $mobileUser instanceof MobileUser || ! $company instanceof Company) {
            return response()->json([
                'ok' => false,
                'message' => 'POS oturumu bulunamadi.',
                'redirect' => route('pos.login', [], false),
            ], 401);
        }

        $roleSummary = $authManager->resolveRoleSummary($request);
        $permissions = is_array($roleSummary['permissions'] ?? null) ? $roleSummary['permissions'] : [];
        if (!((bool) ($permissions['canManagePosContext'] ?? false))) {
            return response()->json([
                'ok' => false,
                'message' => 'POS oturumu yonetmek icin yetkiniz yok.',
            ], 403);
        }

        $belongsToCompany = PosSession::query()
            ->where('id', $posSession->id)
            ->whereHas('register.branch', function ($query) use ($company): void {
                $query->where('company_id', $company->id);
            })
            ->exists();

        if (! $belongsToCompany) {
            return response()->json([
                'ok' => false,
                'message' => 'POS oturumu bu firmaya ait degil.',
            ], 404);
        }

        if ($posSession->status !== 'active') {
            return response()->json([
                'ok' => false,
                'message' => 'POS oturumu zaten kapali.',
            ], 422);
        }

        $posSession->update([
            'status' => 'closed',
            'closed_at' => now(),
            'last_activity_at' => now(),
        ]);

        $currentSessionId = (int) $request->session()->get('pos.auth.pos_session_id', 0);
        $closedCurrentSession = false;
        if ($currentSessionId === (int) $posSession->id) {
            $auth = (array) $request->session()->get('pos.auth', []);
            $auth['pos_session_id'] = null;
            $auth['sale_session_id'] = null;
            $auth['session_closed'] = true;
            $request->session()->put('pos.auth', $auth);
            $request->session()->save();
            $closedCurrentSession = true;
        }

        return response()->json([
            'ok' => true,
            'message' => 'POS oturumu kapatildi.',
            'data' => array_merge(
                $this->buildOverviewPayload($company, $roleSummary),
                ['reload' => $closedCurrentSession],
            ),
        ]);
    }

    public function upsertStaff(
        Request $request,
        PosAuthManager $authManager,
        PosRoleService $roleService,
    ): JsonResponse {
        $mobileUser = $authManager->resolveAuthenticatedUser($request);
        $company = $authManager->resolveAuthenticatedCompany($request);
        if (! $mobileUser instanceof MobileUser || ! $company instanceof Company) {
            return response()->json([
                'ok' => false,
                'message' => 'POS oturumu bulunamadi.',
                'redirect' => route('pos.login', [], false),
            ], 401);
        }

        $roleSummary = $authManager->resolveRoleSummary($request);
        $permissions = is_array($roleSummary['permissions'] ?? null) ? $roleSummary['permissions'] : [];
        if (!((bool) ($permissions['canManagePersonnel'] ?? false))) {
            return response()->json([
                'ok' => false,
                'message' => 'Personel yonetimi sadece owner icin aciktir.',
            ], 403);
        }

        $payload = $request->validate([
            'email' => ['required', 'email', 'max:255'],
            'role' => ['required', 'in:manager,cashier'],
        ]);

        $targetUser = MobileUser::query()
            ->where('email', mb_strtolower(trim((string) $payload['email']), 'UTF-8'))
            ->where('status', 'active')
            ->first();

        if (! $targetUser instanceof MobileUser) {
            return response()->json([
                'ok' => false,
                'message' => 'Bu e-posta ile aktif mobil kullanici bulunamadi.',
            ], 422);
        }

        if ((int) $targetUser->id === (int) $company->owner_mobile_user_id) {
            return response()->json([
                'ok' => false,
                'message' => 'Firma sahibi zaten owner rolunde.',
            ], 422);
        }

        $role = mb_strtolower(trim((string) $payload['role']), 'UTF-8');
        if (!in_array($role, [PosRoleService::ROLE_MANAGER, PosRoleService::ROLE_CASHIER], true)) {
            return response()->json([
                'ok' => false,
                'message' => 'Gecersiz personel rolu.',
            ], 422);
        }

        CompanyStaffRole::query()->updateOrCreate(
            [
                'company_id' => $company->id,
                'mobile_user_id' => $targetUser->id,
            ],
            [
                'role' => $role,
                'status' => 'active',
                'created_by_mobile_user_id' => $mobileUser->id,
            ],
        );

        return response()->json([
            'ok' => true,
            'message' => 'Personel rolu kaydedildi.',
            'data' => $this->buildOverviewPayload($company, $roleSummary),
        ]);
    }

    public function removeStaff(
        Request $request,
        CompanyStaffRole $staffRole,
        PosAuthManager $authManager,
    ): JsonResponse {
        $mobileUser = $authManager->resolveAuthenticatedUser($request);
        $company = $authManager->resolveAuthenticatedCompany($request);
        if (! $mobileUser instanceof MobileUser || ! $company instanceof Company) {
            return response()->json([
                'ok' => false,
                'message' => 'POS oturumu bulunamadi.',
                'redirect' => route('pos.login', [], false),
            ], 401);
        }

        $roleSummary = $authManager->resolveRoleSummary($request);
        $permissions = is_array($roleSummary['permissions'] ?? null) ? $roleSummary['permissions'] : [];
        if (!((bool) ($permissions['canManagePersonnel'] ?? false))) {
            return response()->json([
                'ok' => false,
                'message' => 'Personel yonetimi sadece owner icin aciktir.',
            ], 403);
        }

        if ((int) $staffRole->company_id !== (int) $company->id) {
            return response()->json([
                'ok' => false,
                'message' => 'Personel kaydi bu firmaya ait degil.',
            ], 404);
        }

        $staffRole->delete();

        return response()->json([
            'ok' => true,
            'message' => 'Personel kaydi silindi.',
            'data' => $this->buildOverviewPayload($company, $roleSummary),
        ]);
    }

    /**
     * @param array{role: string|null, roleLabel: string, permissions: array<string, bool>} $roleSummary
     * @return array<string, mixed>
     */
    private function buildOverviewPayload(Company $company, array $roleSummary): array
    {
        $company->loadMissing('ownerMobileUser');

        $devices = Device::query()
            ->where('company_id', $company->id)
            ->orderByDesc('last_seen_at')
            ->orderByDesc('updated_at')
            ->limit(100)
            ->get()
            ->map(fn (Device $device): array => [
                'id' => $device->id,
                'name' => $device->device_name,
                'uidMasked' => $this->maskDeviceUid((string) $device->device_uid),
                'platform' => $device->platform,
                'isActive' => (bool) $device->is_active,
                'lastSeenAt' => optional($device->last_seen_at)?->toIso8601String(),
                'lastSeenHuman' => optional($device->last_seen_at)?->diffForHumans() ?? '-',
                'lastSyncAt' => optional($device->last_sync_at)?->toIso8601String(),
                'lastSyncHuman' => optional($device->last_sync_at)?->diffForHumans() ?? '-',
            ])
            ->all();

        $sessions = PosSession::query()
            ->with(['register.branch:id,name,company_id', 'register:id,name,branch_id', 'openedByMobileUser:id,name,email'])
            ->whereHas('register.branch', function ($query) use ($company): void {
                $query->where('company_id', $company->id);
            })
            ->orderByRaw("CASE WHEN status = 'active' THEN 0 ELSE 1 END")
            ->orderByDesc('last_activity_at')
            ->orderByDesc('opened_at')
            ->limit(40)
            ->get()
            ->map(fn (PosSession $session): array => [
                'id' => $session->id,
                'status' => $session->status,
                'registerName' => $session->register?->name ?? '-',
                'branchName' => $session->register?->branch?->name ?? '-',
                'openedBy' => $session->openedByMobileUser?->email ?? '-',
                'openedAtHuman' => optional($session->opened_at)?->diffForHumans() ?? '-',
                'lastActivityHuman' => optional($session->last_activity_at)?->diffForHumans() ?? '-',
            ])
            ->all();

        $staffRows = CompanyStaffRole::query()
            ->with('mobileUser:id,name,email,status')
            ->where('company_id', $company->id)
            ->orderByRaw("CASE role WHEN 'manager' THEN 0 WHEN 'cashier' THEN 1 ELSE 2 END")
            ->orderByDesc('status')
            ->orderByDesc('updated_at')
            ->get();

        $staff = [];
        if ($company->ownerMobileUser instanceof MobileUser) {
            $staff[] = [
                'id' => 0,
                'name' => $company->ownerMobileUser->name,
                'email' => $company->ownerMobileUser->email,
                'role' => PosRoleService::ROLE_OWNER,
                'roleLabel' => 'Owner',
                'status' => 'active',
                'isOwner' => true,
            ];
        }

        foreach ($staffRows as $row) {
            $staff[] = [
                'id' => (int) $row->id,
                'name' => $row->mobileUser?->name ?? '-',
                'email' => $row->mobileUser?->email ?? '-',
                'role' => (string) $row->role,
                'roleLabel' => match ((string) $row->role) {
                    PosRoleService::ROLE_MANAGER => 'Manager',
                    PosRoleService::ROLE_CASHIER => 'Kasiyer',
                    default => '-',
                },
                'status' => (string) $row->status,
                'isOwner' => false,
            ];
        }

        return [
            'roleSummary' => $roleSummary,
            'devices' => $devices,
            'sessions' => $sessions,
            'staff' => $staff,
        ];
    }

    private function maskDeviceUid(string $uid): string
    {
        $trimmed = trim($uid);
        if ($trimmed === '') {
            return '-';
        }

        $len = mb_strlen($trimmed, 'UTF-8');
        if ($len <= 10) {
            return $trimmed;
        }

        $prefix = mb_substr($trimmed, 0, 4, 'UTF-8');
        $suffix = mb_substr($trimmed, -4, null, 'UTF-8');

        return $prefix . '...' . $suffix;
    }
}
