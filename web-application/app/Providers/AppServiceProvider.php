<?php

namespace App\Providers;

use Illuminate\Support\Facades\URL;
use Illuminate\Support\ServiceProvider;

class AppServiceProvider extends ServiceProvider
{
    /**
     * Register any application services.
     */
    public function register(): void
    {
        //
    }

    /**
     * Bootstrap any application services.
     */
    public function boot(): void
    {
        $configuredUrl = trim((string) config('app.url', ''));
        if ($configuredUrl !== '' && ! str_starts_with($configuredUrl, 'http://') && ! str_starts_with($configuredUrl, 'https://')) {
            $configuredUrl = 'https://' . ltrim($configuredUrl, '/');
            config(['app.url' => $configuredUrl]);
        }

        $xHttps = (string) request()->header('x-https', '');
        $forwardedProto = mb_strtolower((string) request()->header('x-forwarded-proto', ''), 'UTF-8');

        if (
            str_starts_with($configuredUrl, 'https://') ||
            $xHttps === '1' ||
            str_contains($forwardedProto, 'https') ||
            request()->isSecure()
        ) {
            URL::forceScheme('https');
        }

        // POS and admin should always share same root session path.
        config(['session.path' => '/']);

        // Normalize accidental literal "null" domain values from .env.
        $sessionDomain = config('session.domain');
        if (is_string($sessionDomain)) {
            $normalizedDomain = trim(mb_strtolower($sessionDomain, 'UTF-8'));
            if ($normalizedDomain === '' || $normalizedDomain === 'null') {
                config(['session.domain' => null]);
            }
        }
    }
}
