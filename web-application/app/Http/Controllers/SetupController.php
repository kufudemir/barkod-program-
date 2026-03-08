<?php

namespace App\Http\Controllers;

use App\Models\User;
use Illuminate\Contracts\View\View;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Artisan;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;
use Throwable;

class SetupController extends Controller
{
    public function show(Request $request): View
    {
        abort_unless($this->isEnabled(), 404);
        $this->validateSecret($request);

        return view('setup', [
            'secret' => (string) $request->query('secret', ''),
            'locked' => $this->isLocked(),
            'appName' => (string) config('app.name', 'barkod.space'),
        ]);
    }

    public function run(Request $request): RedirectResponse
    {
        $this->guard($request);
        $locked = $this->isLocked();

        $rules = [
            'secret' => ['required', 'string'],
            'action' => ['required', 'string', 'in:migrate_cache_clear,migrate_only,cache_clear_only'],
        ];
        $messages = [];

        if (! $locked) {
            $rules['name'] = ['required', 'string', 'max:255'];
            $rules['email'] = ['required', 'email', 'max:255'];
            $rules['password'] = ['required', 'string', 'min:8'];
            $messages = [
                'name.required' => 'Admin adı zorunludur.',
                'email.required' => 'Admin e-posta adresi zorunludur.',
                'email.email' => 'Admin e-posta adresi geçersiz.',
                'password.required' => 'Admin şifresi zorunludur.',
                'password.min' => 'Admin şifresi en az 8 karakter olmalıdır.',
            ];
        }

        $validator = Validator::make($request->all(), $rules, $messages);

        if ($validator->fails()) {
            return redirect()
                ->route('setup.show', ['secret' => (string) $request->input('secret')])
                ->withErrors($validator)
                ->withInput();
        }

        $action = (string) $request->input('action', 'migrate_cache_clear');
        $outputBlocks = [];

        try {
            if ($action === 'migrate_only' || $action === 'migrate_cache_clear') {
                Artisan::call('migrate', ['--force' => true]);
                $outputBlocks[] = "== migrate ==\n" . trim(Artisan::output());
            }

            if (! $locked) {
                User::query()->updateOrCreate(
                    ['email' => (string) $request->input('email')],
                    [
                        'name' => (string) $request->input('name'),
                        'password' => Hash::make((string) $request->input('password')),
                    ]
                );
            }

            if ($action === 'cache_clear_only' || $action === 'migrate_cache_clear') {
                try {
                    Artisan::call('optimize:clear');
                    $outputBlocks[] = "== optimize:clear ==\n" . trim(Artisan::output());
                } catch (Throwable $cacheThrowable) {
                    $outputBlocks[] = "== optimize:clear ==\n[UYARI] Cache temizleme başarısız: {$cacheThrowable->getMessage()}";
                }
            }

            $this->writeLock();

            return redirect()
                ->route('setup.show', ['secret' => (string) $request->input('secret')])
                ->with('setup_success', true)
                ->with('artisan_output', trim(implode("\n\n", $outputBlocks)));
        } catch (Throwable $throwable) {
            return redirect()
                ->route('setup.show', ['secret' => (string) $request->input('secret')])
                ->withInput()
                ->with('setup_error', $throwable->getMessage());
        }
    }

    private function guard(Request $request): void
    {
        abort_unless($this->isEnabled(), 404);

        $this->validateSecret($request);
    }

    private function validateSecret(Request $request): void
    {
        $configuredSecret = trim((string) config('app.setup_secret', ''));
        abort_if($configuredSecret === '', 403, 'APP_SETUP_SECRET tanımlı değil.');

        $incomingSecret = trim((string) $request->query('secret', $request->input('secret', '')));
        abort_unless($incomingSecret !== '' && hash_equals($configuredSecret, $incomingSecret), 403, 'Kurulum erişim anahtarı geçersiz.');
    }

    private function isEnabled(): bool
    {
        return (bool) config('app.setup_enabled', false);
    }

    private function isLocked(): bool
    {
        return file_exists($this->lockFilePath());
    }

    private function writeLock(): void
    {
        $lockDirectory = dirname($this->lockFilePath());

        if (! is_dir($lockDirectory)) {
            mkdir($lockDirectory, 0755, true);
        }

        file_put_contents($this->lockFilePath(), now()->toIso8601String());
    }

    private function lockFilePath(): string
    {
        return storage_path('app/setup-installed.lock');
    }
}
