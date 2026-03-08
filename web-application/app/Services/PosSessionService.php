<?php

namespace App\Services;

use App\Models\Branch;
use App\Models\Company;
use App\Models\MobileUser;
use App\Models\PosSession;
use App\Models\Register;
use Illuminate\Http\Request;
use RuntimeException;

class PosSessionService
{
    public function resolveContext(Request $request, Company $company, MobileUser $mobileUser): array
    {
        $this->ensureDefaults($company);

        $branches = Branch::query()
            ->where('company_id', $company->id)
            ->where('status', 'active')
            ->orderBy('name')
            ->get();

        $selectedBranchId = (int) $request->session()->get('pos.auth.branch_id', 0);
        $selectedBranch = $branches->firstWhere('id', $selectedBranchId) ?? $branches->first();

        if (! $selectedBranch instanceof Branch) {
            throw new RuntimeException('Aktif sube bulunamadi.');
        }

        $registers = Register::query()
            ->where('branch_id', $selectedBranch->id)
            ->where('status', 'active')
            ->orderBy('name')
            ->get();

        if ($registers->isEmpty()) {
            $this->createDefaultRegister($selectedBranch);
            $registers = Register::query()
                ->where('branch_id', $selectedBranch->id)
                ->where('status', 'active')
                ->orderBy('name')
                ->get();
        }

        $selectedRegisterId = (int) $request->session()->get('pos.auth.register_id', 0);
        $selectedRegister = $registers->firstWhere('id', $selectedRegisterId) ?? $registers->first();

        if (! $selectedRegister instanceof Register) {
            throw new RuntimeException('Aktif kasa bulunamadi.');
        }

        $isSessionClosed = (bool) $request->session()->get('pos.auth.session_closed', false);
        $activePosSession = $isSessionClosed
            ? null
            : $this->resolveOrCreateActiveSession($request, $selectedRegister, $mobileUser);

        $this->writeAuthContext($request, [
            'branch_id' => $selectedBranch->id,
            'register_id' => $selectedRegister->id,
            'pos_session_id' => $activePosSession?->id,
        ]);

        return [
            'branches' => $branches,
            'registers' => $registers,
            'selectedBranch' => $selectedBranch,
            'selectedRegister' => $selectedRegister,
            'activePosSession' => $activePosSession,
        ];
    }

    public function switchContext(
        Request $request,
        Company $company,
        MobileUser $mobileUser,
        int $branchId,
        int $registerId,
    ): array {
        $branch = Branch::query()
            ->where('id', $branchId)
            ->where('company_id', $company->id)
            ->where('status', 'active')
            ->first();

        if (! $branch instanceof Branch) {
            throw new RuntimeException('Secilen sube bulunamadi.');
        }

        $register = Register::query()
            ->where('id', $registerId)
            ->where('branch_id', $branch->id)
            ->where('status', 'active')
            ->first();

        if (! $register instanceof Register) {
            throw new RuntimeException('Secilen kasa bulunamadi.');
        }

        $activePosSession = $this->resolveOrCreateActiveSession($request, $register, $mobileUser);

        $this->writeAuthContext($request, [
            'branch_id' => $branch->id,
            'register_id' => $register->id,
            'pos_session_id' => $activePosSession->id,
            'session_closed' => false,
        ]);

        return [
            'branch' => $branch,
            'register' => $register,
            'session' => $activePosSession,
        ];
    }

    public function closeCurrentSession(Request $request, Company $company): ?PosSession
    {
        $session = $this->getActiveSessionFromRequest($request, $company);
        if (! $session instanceof PosSession) {
            return null;
        }

        $session->update([
            'status' => 'closed',
            'closed_at' => now(),
            'last_activity_at' => now(),
        ]);

        $this->writeAuthContext($request, [
            'pos_session_id' => null,
            'session_closed' => true,
        ]);

        return $session;
    }

    public function openCurrentSession(Request $request, Company $company, MobileUser $mobileUser): PosSession
    {
        $registerId = (int) $request->session()->get('pos.auth.register_id', 0);

        $register = Register::query()
            ->where('id', $registerId)
            ->whereHas('branch', function ($query) use ($company): void {
                $query->where('company_id', $company->id);
            })
            ->where('status', 'active')
            ->first();

        if (! $register instanceof Register) {
            throw new RuntimeException('Aktif kasa secimi bulunamadi.');
        }

        $session = PosSession::query()
            ->where('register_id', $register->id)
            ->where('status', 'active')
            ->latest('opened_at')
            ->first();

        if (! $session instanceof PosSession) {
            $session = PosSession::query()->create([
                'register_id' => $register->id,
                'opened_by_mobile_user_id' => $mobileUser->id,
                'status' => 'active',
                'opened_at' => now(),
                'last_activity_at' => now(),
            ]);
        } else {
            $session->update([
                'last_activity_at' => now(),
            ]);
        }

        $this->writeAuthContext($request, [
            'pos_session_id' => $session->id,
            'session_closed' => false,
        ]);

        return $session;
    }

    public function getActiveSessionFromRequest(Request $request, Company $company): ?PosSession
    {
        $sessionId = (int) $request->session()->get('pos.auth.pos_session_id', 0);
        if ($sessionId <= 0) {
            return null;
        }

        return PosSession::query()
            ->where('id', $sessionId)
            ->where('status', 'active')
            ->whereHas('register.branch', function ($query) use ($company): void {
                $query->where('company_id', $company->id);
            })
            ->first();
    }

    public function touch(PosSession $session): void
    {
        $session->update([
            'last_activity_at' => now(),
        ]);
    }

    private function resolveOrCreateActiveSession(Request $request, Register $register, MobileUser $mobileUser): PosSession
    {
        $sessionId = (int) $request->session()->get('pos.auth.pos_session_id', 0);

        if ($sessionId > 0) {
            $existing = PosSession::query()
                ->where('id', $sessionId)
                ->where('register_id', $register->id)
                ->where('status', 'active')
                ->first();

            if ($existing instanceof PosSession) {
                $this->touch($existing);
                return $existing;
            }
        }

        $active = PosSession::query()
            ->where('register_id', $register->id)
            ->where('status', 'active')
            ->latest('opened_at')
            ->first();

        if ($active instanceof PosSession) {
            $this->touch($active);
            return $active;
        }

        return PosSession::query()->create([
            'register_id' => $register->id,
            'opened_by_mobile_user_id' => $mobileUser->id,
            'status' => 'active',
            'opened_at' => now(),
            'last_activity_at' => now(),
        ]);
    }

    private function ensureDefaults(Company $company): void
    {
        $activeBranchCount = Branch::query()
            ->where('company_id', $company->id)
            ->where('status', 'active')
            ->count();

        if ($activeBranchCount === 0) {
            $branch = Branch::query()->create([
                'company_id' => $company->id,
                'name' => 'Ana Sube',
                'code' => $this->nextBranchCode($company->id),
                'status' => 'active',
            ]);

            $this->createDefaultRegister($branch);
            return;
        }

        $branchWithoutRegister = Branch::query()
            ->where('company_id', $company->id)
            ->where('status', 'active')
            ->whereDoesntHave('registers', function ($query): void {
                $query->where('status', 'active');
            })
            ->first();

        if ($branchWithoutRegister instanceof Branch) {
            $this->createDefaultRegister($branchWithoutRegister);
        }
    }

    private function createDefaultRegister(Branch $branch): Register
    {
        return Register::query()->create([
            'branch_id' => $branch->id,
            'name' => 'Kasa 1',
            'code' => $this->nextRegisterCode($branch->id),
            'status' => 'active',
        ]);
    }

    private function nextBranchCode(int $companyId): string
    {
        $seed = Branch::query()->where('company_id', $companyId)->count() + 1;

        do {
            $candidate = 'SUBE-' . str_pad((string) $seed, 3, '0', STR_PAD_LEFT);
            $exists = Branch::query()
                ->where('company_id', $companyId)
                ->where('code', $candidate)
                ->exists();
            $seed++;
        } while ($exists);

        return $candidate;
    }

    private function nextRegisterCode(int $branchId): string
    {
        $seed = Register::query()->where('branch_id', $branchId)->count() + 1;

        do {
            $candidate = 'KASA-' . str_pad((string) $seed, 2, '0', STR_PAD_LEFT);
            $exists = Register::query()
                ->where('branch_id', $branchId)
                ->where('code', $candidate)
                ->exists();
            $seed++;
        } while ($exists);

        return $candidate;
    }

    private function writeAuthContext(Request $request, array $context): void
    {
        $auth = (array) $request->session()->get('pos.auth', []);

        foreach ($context as $key => $value) {
            $auth[$key] = $value;
        }

        $request->session()->put('pos.auth', $auth);
        $request->session()->save();
    }
}
