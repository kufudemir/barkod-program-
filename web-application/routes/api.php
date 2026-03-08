<?php

use App\Http\Controllers\Api\CatalogSyncController;
use App\Http\Controllers\Api\CreateLicenseRequestController;
use App\Http\Controllers\Api\ConfirmMobileUserPasswordResetController;
use App\Http\Controllers\Api\CurrentMobileUserController;
use App\Http\Controllers\Api\DeviceActivationController;
use App\Http\Controllers\Api\GetCompanyLicenseController;
use App\Http\Controllers\Api\GetOwnedCompanyCatalogChangesController;
use App\Http\Controllers\Api\GetGlobalCatalogSuggestionController;
use App\Http\Controllers\Api\GetMobileUserPremiumController;
use App\Http\Controllers\Api\GetOwnedCompanyCatalogController;
use App\Http\Controllers\Api\ListDeviceHistoryCompaniesController;
use App\Http\Controllers\Api\ListOwnedCompaniesController;
use App\Http\Controllers\Api\LoginMobileUserController;
use App\Http\Controllers\Api\MobileWebSaleController;
use App\Http\Controllers\Api\LogoutMobileUserController;
use App\Http\Controllers\Api\RefreshCompanyLicenseController;
use App\Http\Controllers\Api\RegisterMobileUserController;
use App\Http\Controllers\Api\RequestMobileUserPasswordResetController;
use App\Http\Controllers\Api\SyncMobileUserPremiumController;
use App\Http\Controllers\Api\SyncMobileUserConsentController;
use App\Http\Controllers\Api\SupportTicketController;
use App\Http\Controllers\Api\UpdateMobileUserPasswordController;
use Illuminate\Support\Facades\Route;

Route::prefix('v1')->group(function (): void {
    Route::post('/auth/register', RegisterMobileUserController::class);
    Route::post('/auth/login', LoginMobileUserController::class);
    Route::post('/auth/logout', LogoutMobileUserController::class);
    Route::get('/auth/me', CurrentMobileUserController::class);
    Route::post('/auth/password', UpdateMobileUserPasswordController::class);
    Route::post('/auth/password/forgot', RequestMobileUserPasswordResetController::class);
    Route::post('/auth/password/reset', ConfirmMobileUserPasswordResetController::class);
    Route::get('/auth/premium', GetMobileUserPremiumController::class);
    Route::post('/auth/premium/sync', SyncMobileUserPremiumController::class);
    Route::post('/auth/consent', SyncMobileUserConsentController::class);
    Route::get('/auth/companies', ListOwnedCompaniesController::class);
    Route::get('/auth/companies/{companyCode}/catalog', GetOwnedCompanyCatalogController::class);
    Route::get('/auth/companies/{companyCode}/catalog/changes', GetOwnedCompanyCatalogChangesController::class);

    Route::get('/catalog/products/{barcode}/suggestion', GetGlobalCatalogSuggestionController::class);
    Route::post('/license/request', CreateLicenseRequestController::class);
    Route::get('/company/license', GetCompanyLicenseController::class);
    Route::post('/company/license/refresh', RefreshCompanyLicenseController::class);
    Route::post('/device/activate', DeviceActivationController::class);
    Route::get('/device/history', ListDeviceHistoryCompaniesController::class);
    Route::post('/sync/catalog-batch', CatalogSyncController::class);

    Route::get('/mobile/web-sale/active', [MobileWebSaleController::class, 'active']);
    Route::post('/mobile/web-sale/scan', [MobileWebSaleController::class, 'scan']);
    Route::post('/mobile/web-sale/item/increment', [MobileWebSaleController::class, 'increment']);
    Route::post('/mobile/web-sale/item/decrement', [MobileWebSaleController::class, 'decrement']);
    Route::post('/mobile/web-sale/item/remove', [MobileWebSaleController::class, 'remove']);
    Route::post('/mobile/web-sale/item/custom-price', [MobileWebSaleController::class, 'setCustomPrice']);
    Route::post('/mobile/web-sale/item/percent-discount', [MobileWebSaleController::class, 'applyPercentDiscount']);
    Route::post('/mobile/web-sale/item/fixed-discount', [MobileWebSaleController::class, 'applyFixedDiscount']);
    Route::post('/mobile/web-sale/item/reset-price', [MobileWebSaleController::class, 'resetPrice']);
    Route::post('/mobile/web-sale/complete', [MobileWebSaleController::class, 'complete']);
    Route::post('/mobile/web-sale/print', [MobileWebSaleController::class, 'print']);
    Route::post('/mobile/sales/publish', [MobileWebSaleController::class, 'publishLocalSale']);

    Route::get('/support/inbox', [SupportTicketController::class, 'inbox']);
    Route::post('/support/tickets', [SupportTicketController::class, 'create']);
    Route::get('/support/tickets/{ticketId}', [SupportTicketController::class, 'show']);
    Route::post('/support/tickets/{ticketId}/reply', [SupportTicketController::class, 'reply']);
    Route::post('/support/tickets/{ticketId}/reopen', [SupportTicketController::class, 'reopen']);
    Route::post('/support/tickets/{ticketId}/attachments', [SupportTicketController::class, 'attach']);
});
