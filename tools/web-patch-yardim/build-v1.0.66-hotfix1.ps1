$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$patchRoot = Join-Path $root "web-application\update\v1.0.66-hotfix1"

if (Test-Path $patchRoot) {
    Remove-Item $patchRoot -Recurse -Force
}

$files = @(
    "web-application\app\Models\Device.php",
    "web-application\app\Services\SyncCenterService.php",
    "web-application\app\Providers\Filament\AdminPanelProvider.php",
    "web-application\app\Filament\Pages\SyncCenter.php",
    "web-application\resources\views\filament\pages\sync-center.blade.php",
    "web-application\app\Filament\Widgets\SyncOverview.php",
    "web-application\app\Filament\Widgets\RecentSyncBatches.php",
    "web-application\app\Filament\Resources\SyncBatches\SyncBatchResource.php",
    "web-application\app\Filament\Resources\SyncBatches\Tables\SyncBatchesTable.php",
    "web-application\app\Filament\Resources\SyncBatches\Schemas\SyncBatchInfolist.php",
    "web-application\app\Filament\Resources\SyncBatches\Pages\ViewSyncBatch.php",
    "web-application\app\Filament\Resources\Devices\Tables\DevicesTable.php",
    "web-application\app\Filament\Resources\Devices\Schemas\DeviceInfolist.php",
    "web-application\app\Filament\Resources\Companies\Tables\CompaniesTable.php",
    "web-application\app\Filament\Resources\Companies\Schemas\CompanyInfolist.php",
    "web-application\app\Filament\Pages\CompanyLifecycleSettings.php",
    "web-application\resources\views\filament\pages\company-lifecycle-settings.blade.php",
    "web-application\app\Filament\Pages\SystemMaintenance.php",
    "web-application\resources\views\filament\pages\system-maintenance.blade.php",
    "web-application\app\Filament\Resources\GlobalProducts\GlobalProductResource.php",
    "web-application\app\Filament\Resources\GlobalProducts\Tables\GlobalProductsTable.php",
    "web-application\app\Filament\Resources\GlobalProducts\Schemas\GlobalProductInfolist.php",
    "web-application\app\Filament\Resources\CompanyProductOffers\CompanyProductOfferResource.php",
    "web-application\app\Filament\Resources\CompanyProductOffers\Tables\CompanyProductOffersTable.php"
)

foreach ($file in $files) {
    $source = Join-Path $root $file
    $relative = $file.Substring("web-application\".Length)
    $destination = Join-Path $patchRoot $relative
    $destinationDir = Split-Path $destination -Parent
    New-Item -ItemType Directory -Path $destinationDir -Force | Out-Null
    Copy-Item $source $destination -Force
}

@"
# v1.0.66-hotfix1

Kapsam:
- Faz 4 web tarafı
- Admin senkron merkezi
- Firma / cihaz bazlı senkron özeti
- Teknik batch ekranını ikinci plana alma
- Admin panelde kalan görünür Türkçe metin temizliği

Yükleme:
1. Bu klasördeki dosyaları sunucuda aynı yollarla yükleyin.
2. Migration gerekmez.
3. Setup çalıştırmanız gerekmez.
4. APK veya `latest.json` güncellemesi yoktur.

Kontrol:
- `/admin` açılıyor mu
- `Katalog ve Senkron > Senkron Merkezi` menüsü görünüyor mu
- `Teknik Kayıtlar > Teknik Senkron Kayıtları` menüsü görünüyor mu
- `Sistem Sıfırlama` ve `Firma Yaşam Döngüsü` sayfalarında Türkçe metinler düzgün mü
"@ | Set-Content -Encoding UTF8 (Join-Path $patchRoot "README.md")

$indexPath = Join-Path $root "web-application\update\_indeks\patch-indeksi.md"
$index = Get-Content -Raw $indexPath
if ($index -notmatch "v1\.0\.66-hotfix1") {
    $index += "`r`n- `v1.0.66-hotfix1`: Faz 4 web patch'i, senkron merkezi ve Türkçe metin düzeltmeleri.`r`n"
    Set-Content -Encoding UTF8 $indexPath $index
}
