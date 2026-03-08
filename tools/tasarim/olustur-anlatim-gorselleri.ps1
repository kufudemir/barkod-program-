param(
    [string]$OutputDir = "dokumanlar/gelistirici-dokumanlari/ekran-goruntuleri"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$targetDir = Join-Path $repoRoot $OutputDir
if (-not (Test-Path $targetDir)) {
    New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
}

Add-Type -AssemblyName System.Drawing

function New-Canvas {
    param(
        [int]$Width = 1600,
        [int]$Height = 900,
        [System.Drawing.Color]$Background = [System.Drawing.Color]::FromArgb(247, 249, 252)
    )

    $bmp = New-Object System.Drawing.Bitmap $Width, $Height
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
    $g.Clear($Background)

    return @{ Bitmap = $bmp; Graphics = $g }
}

function Save-Canvas {
    param(
        [hashtable]$Canvas,
        [string]$FilePath
    )

    $Canvas.Bitmap.Save($FilePath, [System.Drawing.Imaging.ImageFormat]::Png)
    $Canvas.Graphics.Dispose()
    $Canvas.Bitmap.Dispose()
}

function Draw-Card {
    param(
        [System.Drawing.Graphics]$G,
        [int]$X,
        [int]$Y,
        [int]$W,
        [int]$H,
        [System.Drawing.Color]$Fill,
        [System.Drawing.Color]$Border
    )

    $rect = New-Object System.Drawing.Rectangle $X, $Y, $W, $H
    $brush = New-Object System.Drawing.SolidBrush $Fill
    $pen = New-Object System.Drawing.Pen $Border, 2
    $G.FillRectangle($brush, $rect)
    $G.DrawRectangle($pen, $rect)
    $brush.Dispose()
    $pen.Dispose()
}

function Draw-TitleBlock {
    param(
        [System.Drawing.Graphics]$G,
        [string]$Title,
        [string]$Subtitle
    )

    $titleFont = New-Object System.Drawing.Font "Segoe UI", 44, ([System.Drawing.FontStyle]::Bold)
    $subFont = New-Object System.Drawing.Font "Segoe UI", 20, ([System.Drawing.FontStyle]::Regular)
    $titleBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(20, 29, 38))
    $subBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(80, 94, 109))

    $G.DrawString($Title, $titleFont, $titleBrush, 56, 36)
    $G.DrawString($Subtitle, $subFont, $subBrush, 58, 100)

    $titleFont.Dispose(); $subFont.Dispose(); $titleBrush.Dispose(); $subBrush.Dispose()
}

function Draw-Arrow {
    param(
        [System.Drawing.Graphics]$G,
        [int]$X1,
        [int]$Y1,
        [int]$X2,
        [int]$Y2,
        [System.Drawing.Color]$Color
    )

    $pen = New-Object System.Drawing.Pen $Color, 4
    $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::ArrowAnchor
    $G.DrawLine($pen, $X1, $Y1, $X2, $Y2)
    $pen.Dispose()
}

function Draw-BulletList {
    param(
        [System.Drawing.Graphics]$G,
        [string[]]$Items,
        [int]$X,
        [int]$Y,
        [int]$LineHeight = 42
    )

    $font = New-Object System.Drawing.Font "Segoe UI", 20, ([System.Drawing.FontStyle]::Regular)
    $brush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(45, 57, 72))
    for ($i = 0; $i -lt $Items.Count; $i++) {
        $G.DrawString("- $($Items[$i])", $font, $brush, $X, ($Y + ($i * $LineHeight)))
    }
    $font.Dispose(); $brush.Dispose()
}

# Image 1: Platform overview
$canvas1 = New-Canvas
$g1 = $canvas1.Graphics
Draw-TitleBlock -G $g1 -Title "KolayKasa Platform Overview" -Subtitle "Android Mobile POS + Web POS + Back Office"

Draw-Card -G $g1 -X 70 -Y 190 -W 470 -H 330 -Fill ([System.Drawing.Color]::FromArgb(235, 245, 255)) -Border ([System.Drawing.Color]::FromArgb(105, 149, 214))
Draw-Card -G $g1 -X 565 -Y 190 -W 470 -H 330 -Fill ([System.Drawing.Color]::FromArgb(236, 251, 243)) -Border ([System.Drawing.Color]::FromArgb(98, 179, 133))
Draw-Card -G $g1 -X 1060 -Y 190 -W 470 -H 330 -Fill ([System.Drawing.Color]::FromArgb(255, 244, 236)) -Border ([System.Drawing.Color]::FromArgb(222, 155, 91))

$hFont = New-Object System.Drawing.Font "Segoe UI", 28, ([System.Drawing.FontStyle]::Bold)
$bFont = New-Object System.Drawing.Font "Segoe UI", 18, ([System.Drawing.FontStyle]::Regular)
$dark = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(27, 37, 48))

$g1.DrawString("Mobile POS", $hFont, $dark, 105, 225)
$g1.DrawString("Barcode scan", $bFont, $dark, 105, 285)
$g1.DrawString("Product edit", $bFont, $dark, 105, 325)
$g1.DrawString("Cart + sales", $bFont, $dark, 105, 365)
$g1.DrawString("Stock + reports", $bFont, $dark, 105, 405)

$g1.DrawString("Web POS", $hFont, $dark, 600, 225)
$g1.DrawString("Fast checkout", $bFont, $dark, 600, 285)
$g1.DrawString("Session tabs", $bFont, $dark, 600, 325)
$g1.DrawString("Receipt print", $bFont, $dark, 600, 365)
$g1.DrawString("Companion support", $bFont, $dark, 600, 405)

$g1.DrawString("Back Office", $hFont, $dark, 1095, 225)
$g1.DrawString("Company & device", $bFont, $dark, 1095, 285)
$g1.DrawString("License packages", $bFont, $dark, 1095, 325)
$g1.DrawString("Sync monitoring", $bFont, $dark, 1095, 365)
$g1.DrawString("Support tickets", $bFont, $dark, 1095, 405)

Draw-BulletList -G $g1 -Items @(
    "Single platform for mobile and web operations",
    "Offline-first mobile behavior with cloud sync",
    "Role-based web operations and license model"
) -X 78 -Y 585 -LineHeight 52

$hFont.Dispose(); $bFont.Dispose(); $dark.Dispose()
Save-Canvas -Canvas $canvas1 -FilePath (Join-Path $targetDir "01-platform-overview.png")

# Image 2: Mobile flow
$canvas2 = New-Canvas
$g2 = $canvas2.Graphics
Draw-TitleBlock -G $g2 -Title "Mobile POS Flow" -Subtitle "From scan to completed sale"

$stepW = 255
$stepH = 170
$startX = 70
$y = 290
$gap = 45
$stepTitles = @("1. Scan barcode", "2. Product check", "3. Add to cart", "4. Payment", "5. Complete sale")
$stepBodies = @(
    "ML Kit scan`nEAN / UPC",
    "Edit name,`nprice, stock",
    "Qty, discount,`ncustom price",
    "Cash / card /`nother",
    "Receipt +`nreport update"
)
$fills = @(
    [System.Drawing.Color]::FromArgb(233, 245, 255),
    [System.Drawing.Color]::FromArgb(236, 250, 255),
    [System.Drawing.Color]::FromArgb(237, 252, 241),
    [System.Drawing.Color]::FromArgb(255, 247, 236),
    [System.Drawing.Color]::FromArgb(244, 239, 255)
)
$borders = @(
    [System.Drawing.Color]::FromArgb(92, 140, 204),
    [System.Drawing.Color]::FromArgb(92, 171, 209),
    [System.Drawing.Color]::FromArgb(92, 174, 122),
    [System.Drawing.Color]::FromArgb(212, 148, 74),
    [System.Drawing.Color]::FromArgb(126, 95, 191)
)

$tFont = New-Object System.Drawing.Font "Segoe UI", 20, ([System.Drawing.FontStyle]::Bold)
$xFont = New-Object System.Drawing.Font "Segoe UI", 15, ([System.Drawing.FontStyle]::Regular)
$textBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(28, 36, 44))

for ($i = 0; $i -lt 5; $i++) {
    $x = $startX + (($stepW + $gap) * $i)
    Draw-Card -G $g2 -X $x -Y $y -W $stepW -H $stepH -Fill $fills[$i] -Border $borders[$i]
    $g2.DrawString($stepTitles[$i], $tFont, $textBrush, ($x + 16), ($y + 22))
    $g2.DrawString($stepBodies[$i], $xFont, $textBrush, ($x + 16), ($y + 75))
    if ($i -lt 4) {
        Draw-Arrow -G $g2 -X1 ($x + $stepW + 8) -Y1 ($y + 84) -X2 ($x + $stepW + $gap - 8) -Y2 ($y + 84) -Color ([System.Drawing.Color]::FromArgb(102, 116, 133))
    }
}

Draw-Card -G $g2 -X 70 -Y 530 -W 1460 -H 290 -Fill ([System.Drawing.Color]::FromArgb(242, 246, 251)) -Border ([System.Drawing.Color]::FromArgb(203, 214, 226))
$boxTitleFont = New-Object System.Drawing.Font "Segoe UI", 24, ([System.Drawing.FontStyle]::Bold)
$boxBodyFont = New-Object System.Drawing.Font "Segoe UI", 18, ([System.Drawing.FontStyle]::Regular)
$g2.DrawString("Operational highlights", $boxTitleFont, $textBrush, 100, 560)
Draw-BulletList -G $g2 -Items @(
    "Offline-first sales continuity",
    "Outbox queue for reliable sync",
    "Kurus-based money model for pricing consistency",
    "Bulk updates and reporting screens"
) -X 100 -Y 615 -LineHeight 44

$tFont.Dispose(); $xFont.Dispose(); $boxTitleFont.Dispose(); $boxBodyFont.Dispose(); $textBrush.Dispose()
Save-Canvas -Canvas $canvas2 -FilePath (Join-Path $targetDir "02-mobile-pos-flow.png")

# Image 3: Web POS + companion
$canvas3 = New-Canvas
$g3 = $canvas3.Graphics
Draw-TitleBlock -G $g3 -Title "Web POS + Mobile Companion" -Subtitle "Phone-assisted scanning for browser checkout"

# Web screen
Draw-Card -G $g3 -X 80 -Y 190 -W 980 -H 620 -Fill ([System.Drawing.Color]::FromArgb(251, 253, 255)) -Border ([System.Drawing.Color]::FromArgb(168, 186, 205))
Draw-Card -G $g3 -X 110 -Y 240 -W 920 -H 70 -Fill ([System.Drawing.Color]::FromArgb(235, 244, 253)) -Border ([System.Drawing.Color]::FromArgb(152, 178, 204))
Draw-Card -G $g3 -X 110 -Y 330 -W 560 -H 450 -Fill ([System.Drawing.Color]::FromArgb(255, 255, 255)) -Border ([System.Drawing.Color]::FromArgb(210, 220, 230))
Draw-Card -G $g3 -X 690 -Y 330 -W 340 -H 450 -Fill ([System.Drawing.Color]::FromArgb(248, 251, 255)) -Border ([System.Drawing.Color]::FromArgb(188, 204, 220))

$th = New-Object System.Drawing.Font "Segoe UI", 22, ([System.Drawing.FontStyle]::Bold)
$tb = New-Object System.Drawing.Font "Segoe UI", 16, ([System.Drawing.FontStyle]::Regular)
$b = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(28, 40, 54))

$g3.DrawString("Web POS", $th, $b, 120, 250)
$g3.DrawString("Barcode input", $tb, $b, 890, 262)
$g3.DrawString("Cart list", $th, $b, 130, 345)
$g3.DrawString("Payment panel", $th, $b, 705, 345)
$g3.DrawString("Item 1  x2", $tb, $b, 135, 400)
$g3.DrawString("Item 2  x1", $tb, $b, 135, 440)
$g3.DrawString("Item 3  x4", $tb, $b, 135, 480)
$g3.DrawString("Cash / Card / Other", $tb, $b, 705, 400)
$g3.DrawString("Complete sale", $tb, $b, 705, 440)
$g3.DrawString("Print receipt", $tb, $b, 705, 480)

# Phone mock
Draw-Card -G $g3 -X 1180 -Y 240 -W 310 -H 540 -Fill ([System.Drawing.Color]::FromArgb(34, 40, 49)) -Border ([System.Drawing.Color]::FromArgb(70, 78, 90))
Draw-Card -G $g3 -X 1200 -Y 290 -W 270 -H 430 -Fill ([System.Drawing.Color]::FromArgb(236, 244, 255)) -Border ([System.Drawing.Color]::FromArgb(156, 178, 206))
$g3.DrawString("Companion", $th, $b, 1220, 315)
$g3.DrawString("Scan barcode", $tb, $b, 1220, 380)
$g3.DrawString("Send to web cart", $tb, $b, 1220, 420)
$g3.DrawString("Adjust qty/price", $tb, $b, 1220, 460)

Draw-Arrow -G $g3 -X1 1150 -Y1 450 -X2 1030 -Y2 450 -Color ([System.Drawing.Color]::FromArgb(88, 120, 170))
$apiFont = New-Object System.Drawing.Font "Segoe UI", 15, ([System.Drawing.FontStyle]::Bold)
$g3.DrawString("/api/v1/mobile/web-sale/*", $apiFont, $b, 855, 420)

$th.Dispose(); $tb.Dispose(); $b.Dispose(); $apiFont.Dispose()
Save-Canvas -Canvas $canvas3 -FilePath (Join-Path $targetDir "03-web-pos-companion.png")

# Image 4: Sync and API architecture
$canvas4 = New-Canvas
$g4 = $canvas4.Graphics
Draw-TitleBlock -G $g4 -Title "Sync and API Architecture" -Subtitle "Data flow between mobile, web services, and database"

Draw-Card -G $g4 -X 100 -Y 260 -W 360 -H 170 -Fill ([System.Drawing.Color]::FromArgb(233, 245, 255)) -Border ([System.Drawing.Color]::FromArgb(92, 140, 204))
Draw-Card -G $g4 -X 100 -Y 500 -W 360 -H 170 -Fill ([System.Drawing.Color]::FromArgb(236, 252, 240)) -Border ([System.Drawing.Color]::FromArgb(95, 171, 127))
Draw-Card -G $g4 -X 620 -Y 380 -W 390 -H 190 -Fill ([System.Drawing.Color]::FromArgb(250, 244, 255)) -Border ([System.Drawing.Color]::FromArgb(142, 112, 201))
Draw-Card -G $g4 -X 1160 -Y 220 -W 320 -H 170 -Fill ([System.Drawing.Color]::FromArgb(255, 248, 237)) -Border ([System.Drawing.Color]::FromArgb(217, 160, 88))
Draw-Card -G $g4 -X 1160 -Y 460 -W 320 -H 210 -Fill ([System.Drawing.Color]::FromArgb(241, 246, 252)) -Border ([System.Drawing.Color]::FromArgb(157, 176, 196))

$hf = New-Object System.Drawing.Font "Segoe UI", 22, ([System.Drawing.FontStyle]::Bold)
$bf = New-Object System.Drawing.Font "Segoe UI", 16, ([System.Drawing.FontStyle]::Regular)
$br = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(30, 41, 56))

$g4.DrawString("Android App", $hf, $br, 125, 285)
$g4.DrawString("Outbox queue", $bf, $br, 125, 335)
$g4.DrawString("Catalog changes", $bf, $br, 125, 370)

$g4.DrawString("Web POS / Admin", $hf, $br, 125, 525)
$g4.DrawString("Operational UI", $bf, $br, 125, 575)
$g4.DrawString("Support + licenses", $bf, $br, 125, 610)

$g4.DrawString("Laravel API Layer", $hf, $br, 645, 420)
$g4.DrawString("/api/v1/...", $bf, $br, 645, 470)
$g4.DrawString("Auth, device, sync, sales", $bf, $br, 645, 505)

$g4.DrawString("MySQL", $hf, $br, 1255, 270)
$g4.DrawString("Companies", $bf, $br, 1225, 315)
$g4.DrawString("Products", $bf, $br, 1225, 345)

$g4.DrawString("Core Domains", $hf, $br, 1185, 490)
$g4.DrawString("Sales / Sessions", $bf, $br, 1185, 540)
$g4.DrawString("Licenses / Tickets", $bf, $br, 1185, 575)
$g4.DrawString("Sync batches", $bf, $br, 1185, 610)

Draw-Arrow -G $g4 -X1 465 -Y1 345 -X2 610 -Y2 430 -Color ([System.Drawing.Color]::FromArgb(89, 117, 153))
Draw-Arrow -G $g4 -X1 465 -Y1 585 -X2 610 -Y2 510 -Color ([System.Drawing.Color]::FromArgb(89, 117, 153))
Draw-Arrow -G $g4 -X1 1015 -Y1 430 -X2 1150 -Y2 305 -Color ([System.Drawing.Color]::FromArgb(102, 116, 133))
Draw-Arrow -G $g4 -X1 1015 -Y1 520 -X2 1150 -Y2 575 -Color ([System.Drawing.Color]::FromArgb(102, 116, 133))

$hf.Dispose(); $bf.Dispose(); $br.Dispose()
Save-Canvas -Canvas $canvas4 -FilePath (Join-Path $targetDir "04-sync-api-architecture.png")

Write-Output "Generated screenshots in: $targetDir"
