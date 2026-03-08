<?php

use App\Http\Controllers\PublicLegalPagesController;
use App\Http\Controllers\PosAuthController;
use App\Http\Controllers\PosManagementController;
use App\Http\Controllers\PosReceiptController;
use App\Http\Controllers\PosSaleController;
use App\Http\Controllers\PosShellController;
use App\Http\Controllers\PosSupportController;
use App\Http\Controllers\SetupController;
use Illuminate\Support\Facades\Route;

Route::get('/', [PublicLegalPagesController::class, 'home']);
Route::get('/paketler', [PublicLegalPagesController::class, 'packages']);
Route::get('/apk', [PublicLegalPagesController::class, 'apk']);
Route::get('/kullanici-olustur', [PublicLegalPagesController::class, 'userCreateGuide']);
Route::get('/misafir-basla', [PublicLegalPagesController::class, 'guestGuide']);
Route::get('/lisans-talebi', [PublicLegalPagesController::class, 'licenseRequest'])->name('public.license-request');
Route::post('/lisans-talebi', [PublicLegalPagesController::class, 'submitLicenseRequest'])->name('public.license-request.submit');
Route::get('/aydinlatma-metni', [PublicLegalPagesController::class, 'disclosure']);
Route::get('/veri-kullanimi', [PublicLegalPagesController::class, 'dataUsage']);

Route::get('/health', function () {
    return response()->json([
        'status' => 'ok',
        'time' => now()->toIso8601String(),
        'timezone' => config('app.timezone'),
    ]);
});

Route::get('/setup', [SetupController::class, 'show'])->name('setup.show');
Route::post('/setup', [SetupController::class, 'run'])->name('setup.run');

Route::prefix('pos')->middleware('pos.nocache')->group(function (): void {
    Route::get('/login', [PosAuthController::class, 'showLogin'])->name('pos.login');
    Route::post('/login', [PosAuthController::class, 'login'])->name('pos.login.submit');
    Route::get('/receipts/public/{webSale}', [PosReceiptController::class, 'publicShow'])
        ->middleware('signed')
        ->name('pos.receipts.public');

    Route::middleware('pos.auth')->group(function (): void {
        Route::get('/', PosShellController::class)->name('pos.home');
        Route::post('/switch-company', [PosShellController::class, 'switchCompany'])->name('pos.switch-company');
        Route::post('/session/context', [PosShellController::class, 'switchContext'])->name('pos.session.context');
        Route::post('/session/open', [PosShellController::class, 'openSession'])->name('pos.session.open');
        Route::post('/session/close', [PosShellController::class, 'closeSession'])->name('pos.session.close');
        Route::post('/company/profile', [PosShellController::class, 'updateCompanyProfile'])->name('pos.company.profile');
        Route::post('/receipt/profile', [PosShellController::class, 'updateReceiptProfile'])->name('pos.receipt.profile');
        Route::post('/sale-session/create', [PosShellController::class, 'createSaleSession'])->name('pos.sale-session.create');
        Route::post('/sale-session/switch', [PosShellController::class, 'switchSaleSession'])->name('pos.sale-session.switch');
        Route::post('/sale-session/close', [PosShellController::class, 'closeSaleSession'])->name('pos.sale-session.close');
        Route::get('/search/products', [PosSaleController::class, 'searchProducts'])->name('pos.product.search');
        Route::get('/manage/overview', [PosManagementController::class, 'overview'])->name('pos.manage.overview');
        Route::post('/manage/devices/{device}/toggle', [PosManagementController::class, 'toggleDevice'])->name('pos.manage.devices.toggle');
        Route::post('/manage/sessions/{posSession}/close', [PosManagementController::class, 'closeSession'])->name('pos.manage.sessions.close');
        Route::post('/manage/staff/upsert', [PosManagementController::class, 'upsertStaff'])->name('pos.manage.staff.upsert');
        Route::post('/manage/staff/{staffRole}/delete', [PosManagementController::class, 'removeStaff'])->name('pos.manage.staff.delete');
        Route::get('/support/inbox', [PosSupportController::class, 'inbox'])->name('pos.support.inbox');
        Route::get('/support/tickets/{ticketId}', [PosSupportController::class, 'show'])->name('pos.support.show');
        Route::post('/support/tickets', [PosSupportController::class, 'create'])->name('pos.support.create');
        Route::post('/support/tickets/{ticketId}/reply', [PosSupportController::class, 'reply'])->name('pos.support.reply');
        Route::post('/support/tickets/{ticketId}/reopen', [PosSupportController::class, 'reopen'])->name('pos.support.reopen');
        Route::post('/scan', [PosSaleController::class, 'scan'])->name('pos.scan');
        Route::post('/item/{barcode}/increment', [PosSaleController::class, 'increment'])->name('pos.item.increment');
        Route::post('/item/{barcode}/decrement', [PosSaleController::class, 'decrement'])->name('pos.item.decrement');
        Route::post('/item/{barcode}/remove', [PosSaleController::class, 'remove'])->name('pos.item.remove');
        Route::post('/cart/clear', [PosSaleController::class, 'clear'])->name('pos.cart.clear');
        Route::post('/sale-session/hold', [PosSaleController::class, 'hold'])->name('pos.sale-session.hold');
        Route::post('/sale-session/held/resume', [PosSaleController::class, 'resumeHeld'])->name('pos.sale-session.held.resume');
        Route::post('/sale-session/held/discard', [PosSaleController::class, 'discardHeld'])->name('pos.sale-session.held.discard');
        Route::post('/checkout', [PosSaleController::class, 'complete'])->name('pos.checkout');
        Route::get('/sync/state', [PosSaleController::class, 'syncState'])->name('pos.sync.state');
        Route::get('/receipts', [PosReceiptController::class, 'index'])->name('pos.receipts.index');
        Route::get('/receipts/{webSale}', [PosReceiptController::class, 'show'])->name('pos.receipts.show');
        Route::get('/receipts/{webSale}/json', [PosReceiptController::class, 'detailsJson'])->name('pos.receipts.details-json');
        Route::post('/receipts/{webSale}/update', [PosReceiptController::class, 'updateJson'])->name('pos.receipts.update-json');
        Route::post('/receipts/{webSale}/delete', [PosReceiptController::class, 'deleteJson'])->name('pos.receipts.delete-json');
        Route::post('/held/{saleSession}/resume', [PosReceiptController::class, 'resumeHeld'])->name('pos.held.resume');
        Route::post('/held/{saleSession}/discard', [PosReceiptController::class, 'discardHeld'])->name('pos.held.discard');
        Route::post('/logout', [PosAuthController::class, 'logout'])->name('pos.logout');
    });
});
