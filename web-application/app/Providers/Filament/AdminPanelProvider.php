<?php

namespace App\Providers\Filament;

use App\Filament\Widgets\SyncOverview;
use Filament\Http\Middleware\Authenticate;
use Filament\Http\Middleware\AuthenticateSession;
use Filament\Http\Middleware\DisableBladeIconComponents;
use Filament\Http\Middleware\DispatchServingFilamentEvent;
use Filament\Pages\Dashboard;
use Filament\Panel;
use Filament\PanelProvider;
use Filament\Support\Colors\Color;
use Filament\View\PanelsRenderHook;
use Filament\Widgets\AccountWidget;
use Filament\Widgets\FilamentInfoWidget;
use Illuminate\Support\HtmlString;
use Illuminate\Cookie\Middleware\AddQueuedCookiesToResponse;
use Illuminate\Cookie\Middleware\EncryptCookies;
use Illuminate\Foundation\Http\Middleware\VerifyCsrfToken;
use Illuminate\Routing\Middleware\SubstituteBindings;
use Illuminate\Session\Middleware\StartSession;
use Illuminate\View\Middleware\ShareErrorsFromSession;

class AdminPanelProvider extends PanelProvider
{
    public function panel(Panel $panel): Panel
    {
        return $panel
            ->default()
            ->id('admin')
            ->path('admin')
            ->brandName('barkod.space')
            ->login()
            ->colors([
                'primary' => Color::Amber,
            ])
            ->discoverResources(in: app_path('Filament/Resources'), for: 'App\\Filament\\Resources')
            ->discoverPages(in: app_path('Filament/Pages'), for: 'App\\Filament\\Pages')
            ->pages([
                Dashboard::class,
            ])
            ->discoverWidgets(in: app_path('Filament/Widgets'), for: 'App\\Filament\\Widgets')
            ->widgets([
                AccountWidget::class,
                SyncOverview::class,
                FilamentInfoWidget::class,
            ])
            ->renderHook(
                PanelsRenderHook::HEAD_END,
                fn (): HtmlString => new HtmlString(<<<'HTML'
                    <style>
                        .fi-main input:not([type="checkbox"]):not([type="radio"]):not([type="range"]),
                        .fi-main textarea,
                        .fi-main select {
                            background-color: rgba(255, 255, 255, 0.04) !important;
                            border: 1px solid rgba(255, 255, 255, 0.18) !important;
                            color: #f9fafb !important;
                            box-shadow: none !important;
                        }

                        .fi-main input::placeholder,
                        .fi-main textarea::placeholder {
                            color: #9ca3af !important;
                            opacity: 1 !important;
                        }

                        .fi-main input:hover,
                        .fi-main textarea:hover,
                        .fi-main select:hover {
                            border-color: rgba(255, 255, 255, 0.3) !important;
                        }

                        .fi-main input:focus,
                        .fi-main textarea:focus,
                        .fi-main select:focus {
                            border-color: rgba(245, 158, 11, 0.9) !important;
                            box-shadow: 0 0 0 2px rgba(245, 158, 11, 0.2) !important;
                        }
                    </style>
                HTML),
            )
            ->middleware([
                EncryptCookies::class,
                AddQueuedCookiesToResponse::class,
                StartSession::class,
                AuthenticateSession::class,
                ShareErrorsFromSession::class,
                VerifyCsrfToken::class,
                SubstituteBindings::class,
                DisableBladeIconComponents::class,
                DispatchServingFilamentEvent::class,
            ])
            ->authMiddleware([
                Authenticate::class,
            ]);
    }
}
