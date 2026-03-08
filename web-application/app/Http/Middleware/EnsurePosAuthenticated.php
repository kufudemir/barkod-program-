<?php

namespace App\Http\Middleware;

use App\Models\Company;
use App\Models\MobileUser;
use App\Services\PosRoleService;
use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class EnsurePosAuthenticated
{
    public function handle(Request $request, Closure $next, PosRoleService $roleService): Response
    {
        $mobileUserId = (int) $request->session()->get('pos.auth.mobile_user_id', 0);
        $companyId = (int) $request->session()->get('pos.auth.company_id', 0);

        if ($mobileUserId <= 0 || $companyId <= 0) {
            return redirect()->route('pos.login', ['r' => 'missing_auth']);
        }

        $mobileUser = MobileUser::query()
            ->where('id', $mobileUserId)
            ->where('status', 'active')
            ->first();

        if ($mobileUser === null) {
            $request->session()->forget('pos.auth');
            return redirect()->route('pos.login', ['r' => 'mobile_user_not_active']);
        }

        $company = Company::query()
            ->where('id', $companyId)
            ->where('status', 'active')
            ->first();

        if ($company === null) {
            $request->session()->forget('pos.auth');
            return redirect()->route('pos.login', ['r' => 'company_not_accessible']);
        }

        $role = $roleService->resolveRole($mobileUser, $company);
        if ($role === null) {
            $request->session()->forget('pos.auth');
            return redirect()->route('pos.login', ['r' => 'company_not_accessible']);
        }

        $session = (array) $request->session()->get('pos.auth', []);
        if (($session['role'] ?? null) !== $role) {
            $session['role'] = $role;
            $request->session()->put('pos.auth', $session);
        }

        $request->attributes->set('posMobileUser', $mobileUser);
        $request->attributes->set('posCompany', $company);
        $request->attributes->set('posRole', $role);
        $request->attributes->set('posPermissions', $roleService->permissionsForRole($role));

        return $next($request);
    }
}
