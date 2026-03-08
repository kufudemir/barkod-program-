<?php

namespace App\Http\Controllers;

use App\Services\PosAuthManager;
use Illuminate\Contracts\View\View;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use RuntimeException;

class PosAuthController extends Controller
{
    public function showLogin(Request $request, PosAuthManager $authManager): View|RedirectResponse
    {
        if ($authManager->resolveAuthenticatedUser($request) !== null && $authManager->resolveAuthenticatedCompany($request) !== null) {
            return redirect()->route('pos.home');
        }

        $emailPrefill = (string) old('email', '');
        $reasonCode = (string) $request->query('r', '');

        $reasonMessage = match ($reasonCode) {
            'missing_auth' => 'POS oturumu bulunamadi. Session yazilmiyor olabilir.',
            'mobile_user_not_active' => 'Mobil kullanici aktif degil veya bulunamadi.',
            'company_not_accessible' => 'Bu mobil kullaniciya bagli aktif firma bulunamadi.',
            default => null,
        };

        return view('pos.login', [
            'ownedCompanies' => $emailPrefill !== '' ? $authManager->listOwnedCompaniesByEmail($emailPrefill) : [],
            'reasonMessage' => $reasonMessage,
        ]);
    }

    public function login(Request $request, PosAuthManager $authManager): RedirectResponse
    {
        $payload = $request->validate([
            'email' => ['required', 'email', 'max:255'],
            'password' => ['required', 'string', 'min:8'],
            'company_code' => ['nullable', 'string', 'max:64'],
        ]);

        try {
            $authManager->attemptLogin(
                $request,
                $payload['email'],
                $payload['password'],
                $payload['company_code'] ?? null,
            );
        } catch (RuntimeException $exception) {
            return back()
                ->withInput($request->except('password'))
                ->withErrors(['email' => $exception->getMessage()]);
        }

        if ((int) $request->session()->get('pos.auth.mobile_user_id', 0) <= 0) {
            return back()
                ->withInput($request->except('password'))
                ->withErrors(['email' => 'POS session yazilamadi. Lutfen tarayici cerezlerini temizleyip tekrar deneyin.']);
        }

        return redirect()->route('pos.home');
    }

    public function logout(Request $request, PosAuthManager $authManager): RedirectResponse
    {
        $authManager->logout($request);

        return redirect()->route('pos.login');
    }
}
