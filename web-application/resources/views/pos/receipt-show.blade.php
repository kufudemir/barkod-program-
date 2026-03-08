<!doctype html>
<html lang="tr">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Fis #{{ $sale['id'] }} | KolayKasa</title>
    <style>
        :root {
            --bg: #0b1020;
            --panel: #111a30;
            --line: rgba(148, 163, 184, .22);
            --text: #f8fafc;
            --muted: #cbd5e1;
            --primary: #0ea5e9;
        }
        * { box-sizing: border-box; }
        body {
            margin: 0;
            background: var(--bg);
            color: var(--text);
            font-family: "Segoe UI", "Inter", Arial, sans-serif;
        }
        .receipt-page {
            min-height: 100vh;
        }
        .receipt-page.paper-58 .wrap { max-width: 420px; }
        .receipt-page.paper-80 .wrap { max-width: 500px; }
        .receipt-page.paper-a4 .wrap { max-width: 980px; }
        .topbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 12px;
            padding: 14px 18px;
            border-bottom: 1px solid var(--line);
            background: rgba(17, 26, 48, .95);
            position: sticky;
            top: 0;
            z-index: 20;
            flex-wrap: wrap;
        }
        .title { font-size: 20px; font-weight: 800; }
        .meta { font-size: 13px; color: var(--muted); margin-top: 4px; line-height: 1.6; }
        .badge {
            display: inline-flex;
            align-items: center;
            padding: 3px 8px;
            border-radius: 999px;
            border: 1px solid rgba(14, 165, 233, .5);
            color: #bae6fd;
            font-size: 11px;
            margin-top: 8px;
        }
        .btn {
            border: 1px solid var(--line);
            border-radius: 10px;
            padding: 9px 12px;
            font-weight: 700;
            background: rgba(255, 255, 255, .04);
            color: var(--text);
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
        }
        .btn-primary {
            border: none;
            background: linear-gradient(90deg, #0284c7, #0ea5e9);
            color: #fff;
        }
        .btn-row {
            display: flex;
            gap: 8px;
            align-items: center;
            flex-wrap: wrap;
        }
        .btn-row .btn.active {
            border-color: rgba(14, 165, 233, .75);
            background: rgba(14, 165, 233, .2);
            color: #e0f2fe;
        }
        .wrap {
            padding: 16px;
            margin: 0 auto;
            display: grid;
            gap: 14px;
        }
        .box {
            border: 1px solid var(--line);
            border-radius: 14px;
            background: var(--panel);
            padding: 12px;
        }
        .hint {
            border: 1px solid rgba(14, 165, 233, .4);
            border-radius: 12px;
            background: rgba(14, 165, 233, .16);
            color: #bae6fd;
            padding: 9px 11px;
            font-size: 12px;
            line-height: 1.6;
        }
        .grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 10px;
        }
        .k { font-size: 12px; color: var(--muted); }
        .v { font-size: 16px; font-weight: 700; margin-top: 2px; line-height: 1.4; }
        .table {
            margin-top: 8px;
            border: 1px solid var(--line);
            border-radius: 10px;
            overflow: hidden;
        }
        .row {
            display: grid;
            grid-template-columns: 2fr .6fr .9fr .9fr;
            gap: 8px;
            padding: 9px 10px;
            font-size: 13px;
            align-items: center;
        }
        .row.head {
            background: rgba(255, 255, 255, .05);
            font-weight: 700;
        }
        .row + .row { border-top: 1px solid rgba(148, 163, 184, .14); }
        .right { text-align: right; }
        .total {
            margin-top: 10px;
            display: flex;
            justify-content: space-between;
            gap: 8px;
            font-size: 18px;
            font-weight: 800;
        }
        .payments {
            margin-top: 10px;
            display: grid;
            gap: 6px;
        }
        .payment-row {
            display: flex;
            justify-content: space-between;
            gap: 8px;
            font-size: 13px;
        }
        @media (max-width: 820px) {
            .grid { grid-template-columns: 1fr; }
            .row { grid-template-columns: 1.5fr .6fr .9fr .9fr; }
        }
        @media print {
            .topbar,
            .screen-only {
                display: none !important;
            }
            body {
                background: #fff;
                color: #000;
            }
            .box,
            .table {
                border-color: #d1d5db;
                background: #fff;
            }
            .k { color: #4b5563; }
        }
    </style>
    @if(($print['paper'] ?? '80mm') === '58mm')
        <style>
            @page { size: 58mm auto; margin: 3mm; }
            @media print {
                body { font-size: 11px; }
                .wrap { max-width: 100%; padding: 0; }
                .row { grid-template-columns: 1.7fr .5fr .8fr .8fr; gap: 4px; padding: 6px 4px; font-size: 11px; }
                .v { font-size: 13px; }
                .total { font-size: 14px; }
            }
        </style>
    @elseif(($print['paper'] ?? '80mm') === '80mm')
        <style>
            @page { size: 80mm auto; margin: 4mm; }
            @media print {
                body { font-size: 12px; }
                .wrap { max-width: 100%; padding: 0; }
                .row { grid-template-columns: 1.8fr .55fr .85fr .85fr; gap: 6px; padding: 7px 6px; font-size: 12px; }
                .v { font-size: 14px; }
                .total { font-size: 15px; }
            }
        </style>
    @else
        <style>
            @page { size: A4 portrait; margin: 10mm; }
        </style>
    @endif
</head>
<body>
@php($paperClass = $print['paper'] === '58mm' ? 'paper-58' : ($print['paper'] === 'a4' ? 'paper-a4' : 'paper-80'))
@php($receiptVisible = $receiptProfile['visibleFields'] ?? [])
@php($showCompany = (bool) ($receiptVisible['showCompany'] ?? true))
@php($showTax = (bool) ($receiptVisible['showTax'] ?? true))
@php($showPayment = (bool) ($receiptVisible['showPayment'] ?? true))
@php($showBarcode = (bool) ($receiptVisible['showBarcode'] ?? true))
@php($showDate = (bool) ($receiptVisible['showDate'] ?? true))
@php($showRegister = (bool) ($receiptVisible['showRegister'] ?? true))
@php($headerLines = array_values(array_filter($receiptProfile['headerLines'] ?? [], fn ($line) => trim((string) $line) !== '')))
@php($footerLines = array_values(array_filter($receiptProfile['footerLines'] ?? [], fn ($line) => trim((string) $line) !== '')))
@php($companyTitle = (string) ($companyProfile['companyTitle'] ?? $company->name))
@php($taxOffice = trim((string) ($companyProfile['taxOffice'] ?? '')))
@php($taxNumber = trim((string) ($companyProfile['taxNumber'] ?? '')))
<div class="receipt-page {{ $paperClass }}">
    <div class="topbar">
        <div>
            <div class="title">Fis #{{ $sale['id'] }}</div>
            @if($showCompany)
                <div class="meta">{{ $companyTitle }} / {{ $company->company_code }} - {{ $viewerLabel }}</div>
            @endif
            @if($showTax && ($taxOffice !== '' || $taxNumber !== ''))
                <div class="meta">Vergi: {{ $taxOffice !== '' ? $taxOffice : '-' }} - {{ $taxNumber !== '' ? $taxNumber : '-' }}</div>
            @endif
            <div class="badge">Kagit tipi: {{ $print['paperLabel'] }}</div>
        </div>

        <div class="btn-row screen-only">
            @if(!empty($backUrl))
                <a class="btn" href="{{ $backUrl }}">Satislar ve Fisler</a>
            @endif

            @if(empty($print['isPublic']))
                <a class="btn {{ $print['paper'] === '58mm' ? 'active' : '' }}" href="{{ $print['previewUrls']['58mm'] ?? '#' }}">58mm</a>
                <a class="btn {{ $print['paper'] === '80mm' ? 'active' : '' }}" href="{{ $print['previewUrls']['80mm'] ?? '#' }}">80mm</a>
                <a class="btn {{ $print['paper'] === 'a4' ? 'active' : '' }}" href="{{ $print['previewUrls']['a4'] ?? '#' }}">A4</a>
                <a class="btn btn-primary" href="{{ $print['printUrl'] ?? '#' }}" target="_blank" rel="noopener">Yazdir</a>
                <a class="btn" href="{{ $print['pdfUrl'] ?? '#' }}" target="_blank" rel="noopener">PDF Kaydet</a>
            @else
                <button class="btn btn-primary" type="button" onclick="window.print()">Yazdir / PDF</button>
            @endif
        </div>
    </div>

    <div class="wrap">
        @if(($print['output'] ?? 'print') === 'pdf')
            <div class="hint screen-only">
                PDF kaydetmek icin tarayici yazdirma penceresinde hedefi <strong>PDF</strong> secin.
            </div>
        @endif

        @if(!empty($headerLines))
            <div class="box">
                @foreach($headerLines as $headerLine)
                    <div class="v" style="font-size:14px; text-align:center;">{{ $headerLine }}</div>
                @endforeach
            </div>
        @endif

        <div class="box">
            <div class="grid">
                <div>
                    <div class="k">Sube</div>
                    <div class="v">{{ $sale['branchName'] }}</div>
                </div>
                @if($showRegister)
                    <div>
                        <div class="k">Kasa</div>
                        <div class="v">{{ $sale['registerName'] }}</div>
                    </div>
                @endif
                @if($showDate)
                    <div>
                        <div class="k">Tarih</div>
                        <div class="v">{{ $sale['completedAt'] }}</div>
                    </div>
                @endif
                <div>
                    <div class="k">Toplam urun</div>
                    <div class="v">{{ $sale['itemCount'] }}</div>
                </div>
            </div>

            <div class="table">
                <div class="row head">
                    <div>Urun</div>
                    <div>Adet</div>
                    <div class="right">Birim</div>
                    <div class="right">Tutar</div>
                </div>
                @foreach($sale['items'] as $item)
                    <div class="row">
                        <div>
                            <div style="font-weight:700;">{{ $item['name'] }}</div>
                            @if($showBarcode)
                                <div class="k">{{ $item['barcode'] }}</div>
                            @endif
                        </div>
                        <div>{{ $item['quantity'] }}</div>
                        <div class="right">{{ $item['unitPrice'] }}</div>
                        <div class="right">{{ $item['lineTotal'] }}</div>
                    </div>
                @endforeach
            </div>

            <div class="total">
                <span>Genel Toplam</span>
                <span>{{ $sale['totalAmount'] }}</span>
            </div>

            @if($showPayment && !empty($sale['payments']))
                <div class="payments">
                    @foreach($sale['payments'] as $payment)
                        <div class="payment-row">
                            <span>{{ $payment['methodLabel'] }}</span>
                            <strong>{{ $payment['amount'] }}</strong>
                        </div>
                    @endforeach
                </div>
            @endif

            @if(!empty($footerLines))
                <div class="payments">
                    @foreach($footerLines as $footerLine)
                        <div class="payment-row">
                            <span>{{ $footerLine }}</span>
                        </div>
                    @endforeach
                </div>
            @endif
        </div>
    </div>
</div>

@if(!empty($print['autoPrint']))
    <script>
        window.addEventListener('load', () => {
            setTimeout(() => window.print(), 280);
        });
    </script>
@endif
</body>
</html>
