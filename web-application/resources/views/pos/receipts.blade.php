<!doctype html>
<html lang="tr">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="csrf-token" content="{{ csrf_token() }}">
    <title>Satislar ve Fisler | KolayKasa</title>
    <style>
        :root {
            --bg: #0b1020;
            --panel: #111a30;
            --line: rgba(148, 163, 184, .22);
            --text: #f8fafc;
            --muted: #cbd5e1;
            --primary: #0ea5e9;
            --danger: #ef4444;
        }
        * { box-sizing: border-box; }
        body { margin: 0; background: var(--bg); color: var(--text); font-family: "Segoe UI", "Inter", Arial, sans-serif; }
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
        }
        .title { font-size: 20px; font-weight: 800; }
        .meta { font-size: 13px; color: var(--muted); margin-top: 4px; line-height: 1.6; }
        .btn {
            border: 1px solid var(--line);
            border-radius: 10px;
            padding: 9px 12px;
            font-weight: 700;
            background: rgba(255,255,255,.04);
            color: var(--text);
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            justify-content: center;
        }
        .btn-primary {
            border: none;
            background: linear-gradient(90deg, #0284c7, #0ea5e9);
            color: #fff;
        }
        .btn-danger {
            border: none;
            background: rgba(239, 68, 68, .2);
            color: #fecaca;
        }
        .wrap { padding: 16px; display: grid; gap: 14px; }
        .alert {
            border-radius: 12px;
            padding: 10px 12px;
            font-size: 13px;
            line-height: 1.7;
            border: 1px solid var(--line);
            background: rgba(255,255,255,.03);
        }
        .alert.success { border-color: rgba(16,185,129,.45); background: rgba(16,185,129,.14); color: #a7f3d0; }
        .alert.error { border-color: rgba(239,68,68,.45); background: rgba(239,68,68,.14); color: #fecaca; }
        .layout { display: grid; gap: 14px; grid-template-columns: 1fr 1fr; }
        .box { border: 1px solid var(--line); border-radius: 14px; background: var(--panel); padding: 12px; }
        .box h3 { margin: 0 0 8px; font-size: 18px; }
        .box p { margin: 0; color: var(--muted); line-height: 1.7; font-size: 13px; }
        .list { margin-top: 10px; display: grid; gap: 8px; }
        .item {
            border: 1px solid var(--line);
            border-radius: 10px;
            padding: 10px;
            display: grid;
            gap: 8px;
        }
        .item-head {
            display: flex;
            justify-content: space-between;
            gap: 8px;
            align-items: center;
            font-weight: 700;
        }
        .item-meta {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
            color: var(--muted);
            font-size: 13px;
            line-height: 1.6;
        }
        .item-actions {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
        }
        .inline-form { margin: 0; }
        .toast-root {
            position: fixed;
            top: 78px;
            right: 16px;
            z-index: 120;
            display: grid;
            gap: 8px;
            width: min(360px, calc(100vw - 24px));
            pointer-events: none;
        }
        .toast {
            pointer-events: auto;
            border-radius: 12px;
            border: 1px solid var(--line);
            background: rgba(15, 23, 42, .97);
            color: var(--text);
            padding: 10px 12px;
            display: grid;
            grid-template-columns: 1fr auto;
            gap: 8px;
            align-items: start;
            box-shadow: 0 12px 24px rgba(0, 0, 0, .35);
        }
        .toast.success { border-color: rgba(16,185,129,.45); background: rgba(16,185,129,.16); color: #a7f3d0; }
        .toast.error { border-color: rgba(239,68,68,.45); background: rgba(239,68,68,.16); color: #fecaca; }
        .toast-close {
            border: none;
            background: transparent;
            color: inherit;
            font-size: 14px;
            font-weight: 700;
            cursor: pointer;
            padding: 0;
            line-height: 1;
        }
        .modal-backdrop {
            position: fixed;
            inset: 0;
            background: rgba(2, 6, 23, .7);
            z-index: 130;
            display: none;
            align-items: center;
            justify-content: center;
            padding: 14px;
        }
        .modal-card {
            width: min(920px, 100%);
            max-height: calc(100vh - 32px);
            overflow: auto;
            border: 1px solid var(--line);
            border-radius: 14px;
            background: rgba(11, 16, 32, .98);
            box-shadow: 0 18px 30px rgba(0, 0, 0, .45);
            padding: 14px;
        }
        .modal-title {
            margin: 0 0 10px;
            font-size: 18px;
            font-weight: 800;
        }
        .modal-actions {
            margin-top: 12px;
            display: flex;
            gap: 8px;
            justify-content: flex-end;
            flex-wrap: wrap;
        }
        .receipt-actions {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
            margin-bottom: 12px;
        }
        .receipt-summary-grid {
            display: grid;
            gap: 8px;
            grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
            margin-bottom: 10px;
        }
        .receipt-summary-card {
            border: 1px solid var(--line);
            border-radius: 10px;
            padding: 8px;
            background: rgba(255, 255, 255, .03);
        }
        .receipt-summary-card .k {
            font-size: 12px;
            color: var(--muted);
        }
        .receipt-summary-card .v {
            margin-top: 4px;
            font-size: 14px;
            font-weight: 700;
        }
        .receipt-table {
            border: 1px solid var(--line);
            border-radius: 10px;
            overflow: hidden;
            margin-bottom: 10px;
        }
        .receipt-row {
            display: grid;
            grid-template-columns: 2fr .8fr 1fr 1fr;
            gap: 8px;
            align-items: center;
            padding: 8px 10px;
            font-size: 13px;
        }
        .receipt-row.head {
            background: rgba(255, 255, 255, .06);
            font-weight: 700;
        }
        .receipt-row + .receipt-row {
            border-top: 1px solid rgba(148, 163, 184, .18);
        }
        .receipt-right { text-align: right; }
        .receipt-row input { width: 100%; }
        .receipt-edit-footer {
            display: grid;
            gap: 8px;
            grid-template-columns: 1fr auto;
            align-items: center;
        }
        @media (max-width: 980px) {
            .layout { grid-template-columns: 1fr; }
        }
        @media (max-width: 740px) {
            .receipt-row { grid-template-columns: 1fr; }
            .receipt-right { text-align: left; }
            .receipt-edit-footer { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
<div class="topbar">
    <div>
        <div class="title">Satislar ve Fisler</div>
        <div class="meta">{{ $company->name }} / {{ $company->company_code }} · {{ $mobileUser->email }}</div>
    </div>
    <a class="btn btn-primary" href="{{ route('pos.home', [], false) }}">POS'a Don</a>
</div>

<div class="wrap">
    <div id="toast-root" class="toast-root" aria-live="polite" aria-atomic="true"></div>
    @if(session('success'))
        <div class="alert success">{{ session('success') }}</div>
    @endif
    @if(session('error'))
        <div class="alert error">{{ session('error') }}</div>
    @endif
    @if($errors->any())
        <div class="alert error">{{ $errors->first() }}</div>
    @endif

    <div class="layout">
        <div class="box">
            <h3>Bekleyen Satislar</h3>
            <p>Aktif POS oturumuna ait bekletilen satislari buradan geri acabilirsiniz.</p>
            <div class="list">
                @if($activePosSessionId === null)
                    <div class="item">
                        <div class="item-meta">Bekleyen satislari gormek icin once aktif POS oturumu acin.</div>
                    </div>
                @elseif($heldSales === [])
                    <div class="item">
                        <div class="item-meta">Bekleyen satis bulunmuyor.</div>
                    </div>
                @else
                    @foreach($heldSales as $held)
                        <div class="item">
                            <div class="item-head">
                                <span>{{ $held['label'] }} #{{ $held['id'] }}</span>
                                <span>{{ $held['totalAmount'] }}</span>
                            </div>
                            <div class="item-meta">
                                <span>{{ $held['itemCount'] }} urun</span>
                                <span>Son guncelleme: {{ $held['updatedAt'] }}</span>
                            </div>
                            <div class="item-actions">
                                <form class="inline-form" method="post" action="{{ route('pos.held.resume', ['saleSession' => $held['id']], false) }}">
                                    @csrf
                                    <button class="btn btn-primary" type="submit">Geri Ac</button>
                                </form>
                                <form class="inline-form" method="post" action="{{ route('pos.held.discard', ['saleSession' => $held['id']], false) }}" onsubmit="return confirm('Bu bekleyen satis silinsin mi?');">
                                    @csrf
                                    <button class="btn btn-danger" type="submit">Sil</button>
                                </form>
                            </div>
                        </div>
                    @endforeach
                @endif
            </div>
        </div>

        <div class="box">
            <h3>Tamamlanan Satis Fisleri</h3>
            <p>Son 30 satis popup fis ekraninda acilir. Yazdirma, duzenleme ve silme popup icinden yapilir.</p>
            <div id="completed-sales-list" class="list">
                @if($completedSales === [])
                    <div class="item" id="completed-sales-empty">
                        <div class="item-meta">Tamamlanan satis bulunmuyor.</div>
                    </div>
                @else
                    @foreach($completedSales as $sale)
                        <div class="item" data-sale-row data-sale-id="{{ $sale['id'] }}">
                            <div class="item-head">
                                <span data-sale-row-title>#{{ $sale['id'] }} / {{ $sale['registerName'] }}</span>
                                <span data-sale-row-total>{{ $sale['totalAmount'] }}</span>
                            </div>
                            <div class="item-meta">
                                <span data-sale-row-items>{{ $sale['itemCount'] }} urun</span>
                                <span data-sale-row-payment>{{ $sale['paymentMethodLabel'] ?? 'Nakit' }}</span>
                                <span data-sale-row-time>{{ $sale['completedAt'] }}</span>
                            </div>
                            <div class="item-actions">
                                <button class="btn btn-primary js-open-sale-receipt" type="button" data-sale-id="{{ $sale['id'] }}">Fis Popup Ac</button>
                            </div>
                        </div>
                    @endforeach
                @endif
            </div>
        </div>
    </div>
</div>

<div id="sale-receipt-modal" class="modal-backdrop">
    <div class="modal-card">
        <h3 id="sale-receipt-title" class="modal-title">Satis Fisi</h3>
        <div class="receipt-actions">
            <button id="sale-receipt-print-58" type="button" class="btn">58mm Yazdir</button>
            <button id="sale-receipt-print-80" type="button" class="btn">80mm Yazdir</button>
            <button id="sale-receipt-pdf-a4" type="button" class="btn">A4 PDF</button>
            <button id="sale-receipt-edit-toggle" type="button" class="btn btn-primary">Duzenle</button>
            <button id="sale-receipt-delete" type="button" class="btn btn-danger">Satisi Sil</button>
        </div>
        <div id="sale-receipt-summary" class="receipt-summary-grid"></div>
        <div id="sale-receipt-table" class="receipt-table"></div>
        <div id="sale-receipt-edit-panel" style="display:none;">
            <div class="receipt-edit-footer">
                <div>
                    <label for="sale-receipt-payment-method" style="display:block; font-size:12px; color:var(--muted); margin-bottom:4px;">Odeme Turu</label>
                    <select id="sale-receipt-payment-method">
                        <option value="cash">Nakit</option>
                        <option value="card">Kart</option>
                        <option value="other">Diger</option>
                    </select>
                </div>
                <div style="display:flex; gap:8px; justify-content:flex-end; align-items:flex-end;">
                    <button id="sale-receipt-edit-cancel" type="button" class="btn">Vazgec</button>
                    <button id="sale-receipt-edit-save" type="button" class="btn btn-primary">Kaydet</button>
                </div>
            </div>
        </div>
        <div class="modal-actions">
            <button id="sale-receipt-close" type="button" class="btn">Kapat</button>
        </div>
    </div>
</div>

<script>
    (() => {
        const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content') || '';
        const toastRoot = document.getElementById('toast-root');
        const completedSalesList = document.getElementById('completed-sales-list');
        const saleReceiptModal = document.getElementById('sale-receipt-modal');
        const saleReceiptTitle = document.getElementById('sale-receipt-title');
        const saleReceiptSummary = document.getElementById('sale-receipt-summary');
        const saleReceiptTable = document.getElementById('sale-receipt-table');
        const saleReceiptCloseButton = document.getElementById('sale-receipt-close');
        const saleReceiptPrint58Button = document.getElementById('sale-receipt-print-58');
        const saleReceiptPrint80Button = document.getElementById('sale-receipt-print-80');
        const saleReceiptPdfA4Button = document.getElementById('sale-receipt-pdf-a4');
        const saleReceiptDeleteButton = document.getElementById('sale-receipt-delete');
        const saleReceiptEditToggleButton = document.getElementById('sale-receipt-edit-toggle');
        const saleReceiptEditPanel = document.getElementById('sale-receipt-edit-panel');
        const saleReceiptEditCancelButton = document.getElementById('sale-receipt-edit-cancel');
        const saleReceiptEditSaveButton = document.getElementById('sale-receipt-edit-save');
        const saleReceiptPaymentMethodSelect = document.getElementById('sale-receipt-payment-method');

        const receiptDetailsUrlTemplate = @json(route('pos.receipts.details-json', ['webSale' => '__SALE_ID__'], false));
        const receiptUpdateUrlTemplate = @json(route('pos.receipts.update-json', ['webSale' => '__SALE_ID__'], false));
        const receiptDeleteUrlTemplate = @json(route('pos.receipts.delete-json', ['webSale' => '__SALE_ID__'], false));

        let isLoading = false;
        let modalState = null;

        const escapeHtml = (value) => String(value)
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#039;');

        const buildReceiptRoute = (template, saleId) => String(template || '').replace('__SALE_ID__', String(Number(saleId || 0)));

        const parseTlTextToKurus = (rawValue) => {
            const normalized = String(rawValue || '')
                .trim()
                .replaceAll('TL', '')
                .replaceAll('tl', '')
                .replace(/\s+/g, '')
                .replace(/\./g, '')
                .replace(',', '.');
            const value = Number.parseFloat(normalized);
            if (!Number.isFinite(value) || value <= 0) return null;
            return Math.round(value * 100);
        };

        const showFeedback = (message, type) => {
            if (!message || !toastRoot) return;
            const toast = document.createElement('div');
            toast.className = `toast ${type === 'error' ? 'error' : 'success'}`;
            toast.innerHTML = `<div>${escapeHtml(message)}</div><button type="button" class="toast-close" aria-label="Kapat">x</button>`;
            const close = () => { if (toast.parentElement) toast.parentElement.removeChild(toast); };
            toast.querySelector('.toast-close')?.addEventListener('click', close);
            toastRoot.appendChild(toast);
            setTimeout(close, 2000);
        };

        const closeSaleReceiptModal = () => {
            if (saleReceiptModal) saleReceiptModal.style.display = 'none';
            modalState = null;
        };

        const setLoading = (loading) => {
            isLoading = loading;
            if (!saleReceiptTitle || !saleReceiptSummary || !saleReceiptTable) return;
            if (loading) {
                saleReceiptTitle.textContent = 'Satis Fisi Yukleniyor...';
                saleReceiptSummary.innerHTML = '';
                saleReceiptTable.innerHTML = '<div class="receipt-row">Yukleniyor...</div>';
            }
        };

        const renderModal = () => {
            if (!modalState || !saleReceiptSummary || !saleReceiptTable || !saleReceiptTitle || !saleReceiptPaymentMethodSelect) return;
            const sale = modalState.sale || {};
            const items = Array.isArray(sale.itemsEditable) ? sale.itemsEditable : [];
            saleReceiptTitle.textContent = `Satis Fisi #${Number(sale.id || 0)}`;
            saleReceiptPaymentMethodSelect.value = String((sale.payments?.[0]?.method || 'cash'));
            saleReceiptSummary.innerHTML = `
                <div class="receipt-summary-card"><div class="k">Sube</div><div class="v">${escapeHtml(sale.branchName || '-')}</div></div>
                <div class="receipt-summary-card"><div class="k">Kasa</div><div class="v">${escapeHtml(sale.registerName || '-')}</div></div>
                <div class="receipt-summary-card"><div class="k">Tarih</div><div class="v">${escapeHtml(sale.completedAt || '-')}</div></div>
                <div class="receipt-summary-card"><div class="k">Toplam</div><div class="v">${escapeHtml(sale.totalAmount || '0,00 TL')}</div></div>
            `;
            saleReceiptTable.innerHTML = `
                <div class="receipt-row head"><div>Urun</div><div>Adet</div><div class="receipt-right">Birim Fiyat</div><div class="receipt-right">Tutar</div></div>
                ${items.map((item) => {
                    const quantity = Number(item.quantity || 1);
                    const unitPrice = Number(item.unitSalePriceKurus || 0) / 100;
                    const linePrice = quantity * unitPrice;
                    if (!modalState.isEditing) {
                        return `<div class="receipt-row"><div><div style="font-weight:700;">${escapeHtml(item.name || '-')}</div><div style="font-size:12px; color:var(--muted);">${escapeHtml(item.barcode || '')}</div></div><div>${quantity}</div><div class="receipt-right">${unitPrice.toLocaleString('tr-TR',{minimumFractionDigits:2,maximumFractionDigits:2})} TL</div><div class="receipt-right">${linePrice.toLocaleString('tr-TR',{minimumFractionDigits:2,maximumFractionDigits:2})} TL</div></div>`;
                    }
                    return `<div class="receipt-row" data-receipt-item-id="${Number(item.id || 0)}"><div><div style="font-weight:700;">${escapeHtml(item.name || '-')}</div><div style="font-size:12px; color:var(--muted);">${escapeHtml(item.barcode || '')}</div></div><div><input type="number" min="1" step="1" class="receipt-edit-qty" value="${quantity}"></div><div class="receipt-right"><input type="text" class="receipt-edit-unit" value="${escapeHtml(item.unitSalePrice || '0,00')}" placeholder="0,00"></div><div class="receipt-right">${linePrice.toLocaleString('tr-TR',{minimumFractionDigits:2,maximumFractionDigits:2})} TL</div></div>`;
                }).join('')}
            `;
        };

        const setEditMode = (enabled) => {
            if (!modalState || !saleReceiptEditPanel || !saleReceiptEditToggleButton) return;
            modalState.isEditing = enabled;
            saleReceiptEditPanel.style.display = enabled ? '' : 'none';
            saleReceiptEditToggleButton.textContent = enabled ? 'Gorunume Don' : 'Duzenle';
            renderModal();
        };

        const updateRow = (sale) => {
            if (!completedSalesList || !sale) return;
            const row = completedSalesList.querySelector(`[data-sale-row][data-sale-id="${Number(sale.id || 0)}"]`);
            if (!(row instanceof HTMLElement)) return;
            row.querySelector('[data-sale-row-title]')?.replaceChildren(document.createTextNode(`#${Number(sale.id || 0)} / ${String(sale.registerName || '-')}`));
            row.querySelector('[data-sale-row-total]')?.replaceChildren(document.createTextNode(String(sale.totalAmount || '0,00 TL')));
            row.querySelector('[data-sale-row-items]')?.replaceChildren(document.createTextNode(`${Number(sale.itemCount || 0)} urun`));
            row.querySelector('[data-sale-row-payment]')?.replaceChildren(document.createTextNode(String(sale.payments?.[0]?.methodLabel || 'Nakit')));
            row.querySelector('[data-sale-row-time]')?.replaceChildren(document.createTextNode(String(sale.completedAt || '-')));
        };

        const removeRow = (saleId) => {
            if (!completedSalesList) return;
            completedSalesList.querySelector(`[data-sale-row][data-sale-id="${Number(saleId || 0)}"]`)?.remove();
            if (!completedSalesList.querySelector('[data-sale-row]') && !completedSalesList.querySelector('#completed-sales-empty')) {
                completedSalesList.innerHTML = '<div class="item" id="completed-sales-empty"><div class="item-meta">Tamamlanan satis bulunmuyor.</div></div>';
            }
        };

        const openModal = async (saleId) => {
            const numericSaleId = Number(saleId || 0);
            if (numericSaleId <= 0 || isLoading) return;
            const detailsUrl = buildReceiptRoute(receiptDetailsUrlTemplate, numericSaleId);
            if (saleReceiptModal) saleReceiptModal.style.display = 'flex';
            setLoading(true);
            try {
                const response = await fetch(detailsUrl, { method: 'GET', credentials: 'same-origin', headers: { 'Accept': 'application/json', 'X-Requested-With': 'XMLHttpRequest' } });
                const payload = await response.json();
                if (!response.ok || payload?.ok === false) {
                    showFeedback(payload?.message || 'Fis detayi alinamadi.', 'error');
                    closeSaleReceiptModal();
                    return;
                }
                modalState = { saleId: numericSaleId, isEditing: false, sale: payload?.data?.sale ?? null, print: payload?.data?.print ?? {} };
                setEditMode(false);
            } catch (_error) {
                showFeedback('Fis detayi alinirken baglanti hatasi olustu.', 'error');
                closeSaleReceiptModal();
            } finally {
                setLoading(false);
            }
        };

        completedSalesList?.addEventListener('click', (event) => {
            if (!(event.target instanceof Element)) {
                return;
            }
            const trigger = event.target.closest('.js-open-sale-receipt');
            if (!(trigger instanceof HTMLElement)) return;
            const saleId = Number(trigger.dataset.saleId || 0);
            if (saleId > 0) openModal(saleId);
        });

        saleReceiptCloseButton?.addEventListener('click', closeSaleReceiptModal);
        saleReceiptModal?.addEventListener('click', (event) => { if (event.target === saleReceiptModal) closeSaleReceiptModal(); });
        saleReceiptPrint58Button?.addEventListener('click', () => { const url = String(modalState?.print?.paper58PrintUrl || ''); if (url !== '') window.open(url, '_blank', 'noopener'); });
        saleReceiptPrint80Button?.addEventListener('click', () => { const url = String(modalState?.print?.paper80PrintUrl || ''); if (url !== '') window.open(url, '_blank', 'noopener'); });
        saleReceiptPdfA4Button?.addEventListener('click', () => { const url = String(modalState?.print?.a4PdfUrl || ''); if (url !== '') window.open(url, '_blank', 'noopener'); });
        saleReceiptEditToggleButton?.addEventListener('click', () => { if (!modalState) return; setEditMode(!Boolean(modalState.isEditing)); });
        saleReceiptEditCancelButton?.addEventListener('click', () => setEditMode(false));

        saleReceiptEditSaveButton?.addEventListener('click', async () => {
            if (!modalState || !saleReceiptTable) return;
            const rows = Array.from(saleReceiptTable.querySelectorAll('[data-receipt-item-id]'));
            if (rows.length === 0) { showFeedback('Duzenlenecek satis satiri bulunamadi.', 'error'); return; }
            const items = [];
            for (const row of rows) {
                const itemId = Number(row.getAttribute('data-receipt-item-id') || 0);
                const quantity = Number((row.querySelector('.receipt-edit-qty')?.value || '').trim());
                const unitSalePriceKurus = parseTlTextToKurus(row.querySelector('.receipt-edit-unit')?.value || '');
                if (itemId <= 0 || !Number.isFinite(quantity) || quantity < 1 || unitSalePriceKurus === null || unitSalePriceKurus < 1) {
                    showFeedback('Satis duzenleme alanlarinda gecersiz deger var.', 'error');
                    return;
                }
                items.push({ id: itemId, quantity: Math.round(quantity), unit_sale_price_kurus: unitSalePriceKurus });
            }
            const updateUrl = buildReceiptRoute(receiptUpdateUrlTemplate, modalState.saleId);
            try {
                const response = await fetch(updateUrl, {
                    method: 'POST',
                    credentials: 'same-origin',
                    headers: { 'Accept': 'application/json', 'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest', 'X-CSRF-TOKEN': csrfToken },
                    body: JSON.stringify({ payment_method: String(saleReceiptPaymentMethodSelect?.value || 'cash'), items }),
                });
                const payload = await response.json();
                if (!response.ok || payload?.ok === false) { showFeedback(payload?.message || 'Satis fis kaydi guncellenemedi.', 'error'); return; }
                modalState.sale = payload?.data?.sale ?? modalState.sale;
                setEditMode(false);
                updateRow(modalState.sale);
                showFeedback(payload?.message || 'Satis fis kaydi guncellendi.', 'success');
            } catch (_error) {
                showFeedback('Satis fis kaydi guncellenirken baglanti hatasi olustu.', 'error');
            }
        });

        saleReceiptDeleteButton?.addEventListener('click', async () => {
            if (!modalState) return;
            if (!window.confirm('Bu satis kaydi silinsin mi? Islem geri alinamaz.')) return;
            const deleteUrl = buildReceiptRoute(receiptDeleteUrlTemplate, modalState.saleId);
            try {
                const response = await fetch(deleteUrl, { method: 'POST', credentials: 'same-origin', headers: { 'Accept': 'application/json', 'X-Requested-With': 'XMLHttpRequest', 'X-CSRF-TOKEN': csrfToken } });
                const payload = await response.json();
                if (!response.ok || payload?.ok === false) { showFeedback(payload?.message || 'Satis kaydi silinemedi.', 'error'); return; }
                removeRow(modalState.saleId);
                closeSaleReceiptModal();
                showFeedback(payload?.message || 'Satis kaydi silindi.', 'success');
            } catch (_error) {
                showFeedback('Satis kaydi silinirken baglanti hatasi olustu.', 'error');
            }
        });
    })();
</script>
</body>
</html>

