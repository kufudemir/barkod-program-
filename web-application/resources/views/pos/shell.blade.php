<!doctype html>
<html lang="tr">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="csrf-token" content="{{ csrf_token() }}">
    <title>Web POS | KolayKasa</title>
    <style>
        :root {
            --bg: #0b1020;
            --panel: #111a30;
            --line: rgba(148, 163, 184, .22);
            --text: #f8fafc;
            --muted: #cbd5e1;
            --primary: #0ea5e9;
            --danger: #ef4444;
            --ok: #10b981;
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
            overflow: visible;
        }
        .title { font-size: 20px; font-weight: 800; }
        .meta { font-size: 13px; color: var(--muted); margin-top: 4px; line-height: 1.6; }
        .actions { display: flex; gap: 8px; align-items: center; position: relative; }
        .btn-small {
            border: 1px solid var(--line);
            background: rgba(255, 255, 255, .04);
            color: var(--text);
            border-radius: 10px;
            padding: 8px 12px;
            font-weight: 700;
            cursor: pointer;
        }
        .topbar-menu-panel {
            position: absolute;
            right: 0;
            top: calc(100% + 8px);
            width: min(460px, calc(100vw - 24px));
            border: 1px solid var(--line);
            border-radius: 14px;
            background: rgba(11, 16, 32, .98);
            box-shadow: 0 18px 30px rgba(0, 0, 0, .45);
            padding: 12px;
            display: none;
            z-index: 120;
        }
        .topbar-menu-panel.open { display: block; }
        .topbar-menu-section + .topbar-menu-section {
            margin-top: 10px;
            padding-top: 10px;
            border-top: 1px solid rgba(148, 163, 184, .16);
        }
        .topbar-menu-title {
            font-size: 12px;
            color: var(--muted);
            margin-bottom: 6px;
            font-weight: 700;
        }
        .topbar-menu-grid {
            display: grid;
            gap: 8px;
            grid-template-columns: 1fr auto;
        }
        .topbar-menu-grid-3 {
            display: grid;
            gap: 8px;
            grid-template-columns: 1fr 1fr auto;
        }
        .topbar-menu-inline {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
        }
        .topbar-menu-muted {
            font-size: 12px;
            color: var(--muted);
            line-height: 1.5;
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
        .toast-root {
            position: fixed;
            top: 78px;
            right: 16px;
            z-index: 300;
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
            z-index: 220;
            display: none;
            align-items: center;
            justify-content: center;
            padding: 14px;
        }
        .modal-card {
            width: min(560px, 100%);
            border: 1px solid var(--line);
            border-radius: 14px;
            background: rgba(11, 16, 32, .98);
            box-shadow: 0 18px 30px rgba(0, 0, 0, .45);
            padding: 14px;
        }
        .receipt-modal-card {
            width: min(920px, 100%);
            max-height: calc(100vh - 32px);
            overflow: auto;
        }
        .modal-title {
            margin: 0 0 10px;
            font-size: 18px;
            font-weight: 800;
        }
        .modal-grid {
            display: grid;
            gap: 8px;
            grid-template-columns: 1fr 1fr;
        }
        .modal-check-grid {
            display: grid;
            gap: 8px;
            grid-template-columns: 1fr 1fr;
            margin-top: 8px;
        }
        .modal-check-item {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 13px;
            color: var(--muted);
        }
        .modal-actions {
            margin-top: 12px;
            display: flex;
            gap: 8px;
            justify-content: flex-end;
            flex-wrap: wrap;
        }
        .support-layout {
            display: grid;
            grid-template-columns: minmax(220px, 260px) minmax(0, 1fr);
            gap: 10px;
            min-height: 420px;
        }
        .support-sidebar {
            border: 1px solid var(--line);
            border-radius: 10px;
            padding: 10px;
            display: grid;
            gap: 8px;
            align-content: start;
        }
        .support-detail {
            border: 1px solid var(--line);
            border-radius: 10px;
            padding: 10px;
            display: grid;
            grid-template-rows: auto auto minmax(140px, 1fr) auto;
            gap: 8px;
        }
        .support-list {
            display: grid;
            gap: 8px;
            max-height: 300px;
            overflow: auto;
            padding-right: 2px;
        }
        .support-ticket-item {
            border: 1px solid rgba(148, 163, 184, .24);
            border-radius: 10px;
            background: rgba(255, 255, 255, .03);
            padding: 8px;
            text-align: left;
            color: var(--text);
            cursor: pointer;
        }
        .support-ticket-item.active {
            border-color: rgba(14, 165, 233, .8);
            background: rgba(14, 165, 233, .13);
        }
        .support-ticket-meta {
            margin-top: 4px;
            font-size: 12px;
            color: var(--muted);
            line-height: 1.45;
        }
        .support-status {
            display: inline-flex;
            align-items: center;
            border-radius: 999px;
            border: 1px solid rgba(148, 163, 184, .25);
            padding: 3px 8px;
            font-size: 11px;
            font-weight: 700;
        }
        .support-status.new { border-color: rgba(250, 204, 21, .55); color: #fde68a; }
        .support-status.reviewing { border-color: rgba(14, 165, 233, .55); color: #7dd3fc; }
        .support-status.answered { border-color: rgba(16, 185, 129, .55); color: #6ee7b7; }
        .support-status.closed { border-color: rgba(239, 68, 68, .55); color: #fca5a5; }
        .support-messages {
            border: 1px solid var(--line);
            border-radius: 10px;
            background: rgba(255, 255, 255, .03);
            padding: 8px;
            overflow: auto;
            display: grid;
            gap: 8px;
        }
        .support-message {
            border: 1px solid rgba(148, 163, 184, .22);
            border-radius: 8px;
            padding: 8px;
            line-height: 1.6;
            font-size: 13px;
            background: rgba(15, 23, 42, .65);
        }
        .support-message.user { border-color: rgba(14, 165, 233, .45); }
        .support-message.admin { border-color: rgba(16, 185, 129, .45); }
        .support-message .head {
            display: flex;
            justify-content: space-between;
            gap: 8px;
            font-size: 11px;
            color: var(--muted);
            margin-bottom: 4px;
            text-transform: uppercase;
            letter-spacing: .02em;
        }
        .support-empty {
            border: 1px dashed rgba(148, 163, 184, .3);
            border-radius: 10px;
            padding: 10px;
            color: var(--muted);
            font-size: 13px;
        }
        .manage-grid {
            display: grid;
            gap: 10px;
            grid-template-columns: 1fr;
        }
        .manage-card {
            border: 1px solid var(--line);
            border-radius: 10px;
            padding: 10px;
            background: rgba(255, 255, 255, .03);
        }
        .manage-card h4 {
            margin: 0 0 8px;
            font-size: 14px;
            font-weight: 800;
        }
        .manage-row {
            border: 1px solid rgba(148, 163, 184, .2);
            border-radius: 8px;
            padding: 8px;
            display: grid;
            gap: 6px;
            background: rgba(15, 23, 42, .65);
        }
        .manage-row + .manage-row { margin-top: 8px; }
        .manage-row-head {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 8px;
            font-size: 13px;
            font-weight: 700;
        }
        .manage-row-meta {
            font-size: 12px;
            color: var(--muted);
            line-height: 1.5;
        }
        .manage-row-actions {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
        }
        .manage-pill {
            display: inline-flex;
            align-items: center;
            border-radius: 999px;
            border: 1px solid rgba(148, 163, 184, .25);
            padding: 3px 8px;
            font-size: 11px;
            font-weight: 700;
        }
        .manage-pill.active {
            border-color: rgba(16, 185, 129, .55);
            color: #6ee7b7;
        }
        .manage-pill.inactive {
            border-color: rgba(239, 68, 68, .55);
            color: #fca5a5;
        }
        .manage-empty {
            border: 1px dashed rgba(148, 163, 184, .3);
            border-radius: 8px;
            padding: 10px;
            color: var(--muted);
            font-size: 12px;
            line-height: 1.6;
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
        .receipt-right {
            text-align: right;
        }
        .receipt-row input {
            width: 100%;
        }
        .receipt-edit-footer {
            display: grid;
            gap: 8px;
            grid-template-columns: 1fr auto;
            align-items: center;
        }
        .recent-sale-item {
            border: 1px solid var(--line);
            border-radius: 10px;
            padding: 9px;
            cursor: pointer;
            transition: border-color .15s ease, background .15s ease;
        }
        .recent-sale-item:hover {
            border-color: rgba(14, 165, 233, .55);
            background: rgba(14, 165, 233, .08);
        }
        .cards { display: grid; gap: 12px; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); }
        .card { border: 1px solid var(--line); border-radius: 14px; background: var(--panel); padding: 12px; }
        .card .k { font-size: 12px; color: var(--muted); margin-bottom: 5px; }
        .card .v { font-size: 22px; font-weight: 800; line-height: 1.3; }
        .layout { display: grid; gap: 14px; grid-template-columns: 1.65fr .9fr; }
        .box { border: 1px solid var(--line); border-radius: 14px; background: var(--panel); padding: 12px; }
        .box h3 { margin: 0 0 7px; font-size: 17px; }
        .box p { margin: 0; color: var(--muted); line-height: 1.7; font-size: 13px; }
        .scanner-status {
            margin-top: 8px;
            padding: 8px 10px;
            border-radius: 10px;
            border: 1px solid var(--line);
            display: inline-flex;
            gap: 8px;
            align-items: center;
            font-size: 12px;
            color: var(--muted);
            background: rgba(255, 255, 255, .03);
        }
        .scanner-status .dot {
            width: 8px;
            height: 8px;
            border-radius: 999px;
            background: #64748b;
            box-shadow: 0 0 0 0 rgba(100, 116, 139, .35);
        }
        .scanner-status.ready {
            border-color: rgba(16, 185, 129, .45);
            color: #a7f3d0;
            background: rgba(16, 185, 129, .1);
        }
        .scanner-status.ready .dot {
            background: #10b981;
            box-shadow: 0 0 0 3px rgba(16, 185, 129, .18);
        }
        .scanner-status.busy {
            border-color: rgba(14, 165, 233, .45);
            color: #bae6fd;
            background: rgba(14, 165, 233, .12);
        }
        .scanner-status.busy .dot {
            background: #0ea5e9;
            box-shadow: 0 0 0 3px rgba(14, 165, 233, .18);
        }
        .scanner-status.paused {
            border-color: rgba(245, 158, 11, .45);
            color: #fde68a;
            background: rgba(245, 158, 11, .12);
        }
        .scanner-status.paused .dot {
            background: #f59e0b;
            box-shadow: 0 0 0 3px rgba(245, 158, 11, .18);
        }
        .scanner-status.disabled {
            border-color: rgba(148, 163, 184, .36);
            color: #cbd5e1;
            background: rgba(148, 163, 184, .08);
        }
        .scanner-status.disabled .dot {
            background: #94a3b8;
            box-shadow: none;
        }
        .company-row { margin-top: 10px; display: grid; gap: 8px; grid-template-columns: 1fr auto; }
        .pos-context-row { margin-top: 8px; display: grid; gap: 8px; grid-template-columns: 1fr 1fr auto; }
        .barcode-row { display: grid; gap: 8px; grid-template-columns: 1fr auto; margin-top: 8px; }
        .input-with-suggestions { position: relative; }
        .barcode-suggestions {
            position: absolute;
            left: 0;
            right: 0;
            top: calc(100% + 4px);
            border: 1px solid var(--line);
            border-radius: 10px;
            background: rgba(11, 16, 32, .98);
            box-shadow: 0 16px 26px rgba(0, 0, 0, .35);
            z-index: 40;
            overflow: hidden;
            max-height: 260px;
            overflow-y: auto;
            display: none;
        }
        .barcode-suggestion-item {
            width: 100%;
            text-align: left;
            border: none;
            border-top: 1px solid rgba(148, 163, 184, .18);
            background: transparent;
            color: var(--text);
            padding: 9px 11px;
            cursor: pointer;
            font-size: 13px;
            line-height: 1.5;
        }
        .barcode-suggestion-item:first-child { border-top: none; }
        .barcode-suggestion-item:hover,
        .barcode-suggestion-item.active {
            background: rgba(14, 165, 233, .16);
        }
        .barcode-suggestion-meta {
            display: block;
            color: var(--muted);
            font-size: 12px;
        }
        input, select {
            width: 100%;
            border: 1px solid var(--line);
            border-radius: 10px;
            background: rgba(15, 23, 42, .7);
            color: var(--text);
            padding: 11px 12px;
            outline: none;
        }
        input:focus, select:focus { border-color: rgba(14,165,233,.75); box-shadow: 0 0 0 2px rgba(14,165,233,.16); }
        .btn-primary {
            border: none;
            border-radius: 10px;
            padding: 10px 12px;
            font-weight: 700;
            background: linear-gradient(90deg, #0284c7, #0ea5e9);
            color: #fff;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            justify-content: center;
        }
        .btn-danger {
            border: none;
            border-radius: 9px;
            padding: 7px 9px;
            background: rgba(239, 68, 68, .18);
            color: #fecaca;
            cursor: pointer;
            font-weight: 700;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            justify-content: center;
        }
        .btn-muted {
            border: 1px solid var(--line);
            border-radius: 9px;
            padding: 7px 9px;
            background: rgba(255,255,255,.04);
            color: var(--text);
            cursor: pointer;
            font-weight: 700;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            justify-content: center;
        }
        .btn-primary:disabled, .btn-muted:disabled, .btn-danger:disabled { opacity: .65; cursor: not-allowed; }
        .cart-table { margin-top: 10px; border: 1px solid var(--line); border-radius: 10px; overflow: hidden; }
        .row { display: grid; grid-template-columns: 2fr .7fr .85fr .95fr; gap: 8px; padding: 9px 10px; font-size: 13px; align-items: center; }
        .row.head { background: rgba(255, 255, 255, .05); font-weight: 700; }
        .row + .row { border-top: 1px solid rgba(148, 163, 184, .14); }
        .product { display: flex; flex-direction: column; gap: 3px; }
        .product .name { font-weight: 700; line-height: 1.5; }
        .product .code { font-size: 11px; color: var(--muted); }
        .qty { display: flex; align-items: center; gap: 6px; }
        .qty span { min-width: 20px; text-align: center; font-weight: 700; }
        .empty { color: var(--muted); padding: 14px 10px; font-size: 13px; }
        .hint { margin-top: 8px; font-size: 12px; color: #93c5fd; line-height: 1.7; }
        .totals { display: grid; gap: 10px; margin-top: 10px; }
        .line { display: flex; justify-content: space-between; gap: 8px; font-size: 14px; }
        .line strong { font-size: 17px; }
        .checkout { width: 100%; margin-top: 10px; }
        .secondary-actions { margin-top: 8px; display: flex; gap: 8px; flex-wrap: wrap; }
        .sale-tabs-wrap { margin-top: 10px; }
        .sale-tabs-title { font-size: 12px; color: var(--muted); margin-bottom: 6px; }
        .sale-tabs {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
            align-items: center;
        }
        .sale-tab-form { margin: 0; }
        .sale-tab { position: relative; display: inline-flex; align-items: center; }
        .sale-tab-btn {
            border: 1px solid var(--line);
            border-radius: 999px;
            padding: 7px 30px 7px 12px;
            font-size: 12px;
            font-weight: 700;
            background: rgba(255,255,255,.03);
            color: var(--text);
            cursor: pointer;
        }
        .sale-tab-btn.active {
            border-color: rgba(14,165,233,.7);
            background: rgba(14,165,233,.22);
            color: #e0f2fe;
        }
        .sale-tab-btn.new {
            border-style: dashed;
            color: #bae6fd;
        }
        .sale-tab-close-form {
            position: absolute;
            right: 8px;
            top: 50%;
            transform: translateY(-50%);
        }
        .sale-tab-close-btn {
            border: none;
            border-radius: 999px;
            background: transparent;
            color: #fecaca;
            width: 16px;
            height: 16px;
            line-height: 14px;
            text-align: center;
            font-size: 12px;
            font-weight: 800;
            cursor: pointer;
            padding: 0;
        }
        .sale-tab-close-btn:hover { background: rgba(239, 68, 68, .22); }
        .held-tabs-wrap { margin-top: 10px; }
        .held-tabs { display: flex; gap: 8px; flex-wrap: wrap; align-items: center; }
        .held-tab { position: relative; display: inline-flex; align-items: center; }
        .held-tab-btn {
            border: 1px solid rgba(245, 158, 11, .55);
            border-radius: 999px;
            padding: 7px 30px 7px 12px;
            font-size: 12px;
            font-weight: 700;
            background: rgba(245, 158, 11, .16);
            color: #fde68a;
            cursor: pointer;
        }
        .held-tab-btn:hover {
            border-color: rgba(245, 158, 11, .75);
            background: rgba(245, 158, 11, .22);
        }
        .held-empty {
            color: var(--muted);
            font-size: 12px;
            line-height: 1.6;
            padding: 4px 0;
        }
        @media (max-width: 1100px) { .layout { grid-template-columns: 1fr; } }
        @media (max-width: 740px) {
            .pos-context-row { grid-template-columns: 1fr; }
            .topbar-menu-grid, .topbar-menu-grid-3, .modal-grid, .modal-check-grid { grid-template-columns: 1fr; }
            .receipt-row { grid-template-columns: 1fr; }
            .receipt-right { text-align: left; }
            .receipt-edit-footer { grid-template-columns: 1fr; }
            .support-layout { grid-template-columns: 1fr; }
            .support-detail { grid-template-rows: auto auto minmax(180px, 1fr) auto; }
        }
    </style>
</head>
<body>
@php($canManagePosContext = (bool) ($rolePermissions['canManagePosContext'] ?? false))
@php($canManageCompanyProfile = (bool) ($rolePermissions['canManageCompanyProfile'] ?? false))
@php($canManageReceiptProfile = (bool) ($rolePermissions['canManageReceiptProfile'] ?? false))
@php($canManageDevices = (bool) ($rolePermissions['canManageDevices'] ?? false))
@php($canManagePersonnel = (bool) ($rolePermissions['canManagePersonnel'] ?? false))
@php($canEditPastSales = (bool) ($rolePermissions['canEditPastSales'] ?? false))
<div class="topbar">
    <div>
        <div class="title">Web POS</div>
        <div class="meta">{{ $company->name }} / {{ $company->company_code }} · {{ $mobileUser->email }} · Rol: {{ $roleSummary['roleLabel'] ?? 'Yetkisiz' }}</div>
    </div>
    <div class="actions">
        <button id="topbar-menu-toggle" class="btn-small" type="button" aria-expanded="false" aria-controls="topbar-menu-panel">Menu</button>

        <div id="topbar-menu-panel" class="topbar-menu-panel" role="menu" aria-hidden="true">
            <div class="topbar-menu-section">
                <div class="topbar-menu-title">Isletme ve POS</div>
                <form class="topbar-menu-grid" method="post" action="{{ route('pos.switch-company', [], false) }}">
                    @csrf
                    <select name="company_code" aria-label="Firma secimi">
                        @foreach($ownedCompanies as $ownedCompany)
                            <option value="{{ $ownedCompany['companyCode'] }}" {{ $ownedCompany['companyCode'] === $company->company_code ? 'selected' : '' }}>
                                {{ $ownedCompany['name'] }} / {{ $ownedCompany['companyCode'] }} · {{ $ownedCompany['roleLabel'] ?? '-' }}
                            </option>
                        @endforeach
                    </select>
                    <button type="submit" class="btn-muted">Firma Degistir</button>
                </form>

                <form class="topbar-menu-grid-3" method="post" action="{{ route('pos.session.context', [], false) }}" style="margin-top:8px;">
                    @csrf
                    <select id="pos-branch-select" name="branch_id" aria-label="Sube secimi" required {{ !$canManagePosContext ? 'disabled' : '' }}>
                        @foreach($posContext['branches'] as $branch)
                            <option value="{{ $branch['id'] }}" {{ (int) $branch['id'] === (int) $posContext['selectedBranchId'] ? 'selected' : '' }}>
                                {{ $branch['name'] }} / {{ $branch['code'] }}
                            </option>
                        @endforeach
                    </select>
                    <select id="pos-register-select" name="register_id" aria-label="Kasa secimi" required {{ !$canManagePosContext ? 'disabled' : '' }}>
                        @foreach($posContext['registers'] as $register)
                            <option value="{{ $register['id'] }}" {{ (int) $register['id'] === (int) $posContext['selectedRegisterId'] ? 'selected' : '' }}>
                                {{ $register['name'] }} / {{ $register['code'] }}
                            </option>
                        @endforeach
                    </select>
                    <button type="submit" class="btn-muted" {{ !$canManagePosContext ? 'disabled' : '' }}>Kasa Uygula</button>
                </form>

                <div class="topbar-menu-inline" style="margin-top:8px;">
                    <form method="post" action="{{ route('pos.session.open', [], false) }}">
                        @csrf
                        <button type="submit" class="btn-muted" {{ (!$canManagePosContext || !empty($posContext['activeSession']['id'])) ? 'disabled' : '' }}>POS Oturumu Ac</button>
                    </form>
                    <form method="post" action="{{ route('pos.session.close', [], false) }}">
                        @csrf
                        <button type="submit" class="btn-danger" {{ (!$canManagePosContext || empty($posContext['activeSession']['id'])) ? 'disabled' : '' }}>POS Oturumu Kapat</button>
                    </form>
                </div>
            </div>

            <div class="topbar-menu-section">
                <div class="topbar-menu-title">Firma Menusu</div>
                <div class="topbar-menu-inline">
                    <button id="open-company-profile" type="button" class="btn-muted" {{ !$canManageCompanyProfile ? 'disabled' : '' }}>Firma Bilgileri</button>
                    <button id="open-receipt-profile" type="button" class="btn-muted" {{ !$canManageReceiptProfile ? 'disabled' : '' }}>Fis Ayarlari</button>
                    <button id="open-support-inbox" type="button" class="btn-muted">Destek Gelen Kutusu</button>
                    <button id="open-support-create" type="button" class="btn-muted">Yeni Ticket</button>
                    <button id="open-device-management" type="button" class="btn-muted" {{ !$canManageDevices ? 'disabled' : '' }}>Cihazlari Yonet</button>
                    <a class="btn-muted" href="{{ route('pos.receipts.index', [], false) }}">Satislari Fisle Gor</a>
                </div>
                <div class="topbar-menu-muted" style="margin-top:8px;">
                    Rol tabanli erisim: {{ $roleSummary['roleLabel'] ?? '-' }}.
                </div>
            </div>

            <div class="topbar-menu-section">
                <form method="post" action="{{ route('pos.logout', [], false) }}">
                    @csrf
                    <button class="btn-danger" type="submit">Cikis</button>
                </form>
            </div>
        </div>
    </div>
</div>

<div class="wrap">
    <div id="toast-root" class="toast-root" aria-live="polite" aria-atomic="true"></div>
    @php($initialFeedbackPayload = [
        'success' => session('success'),
        'error' => session('error'),
        'errors' => $errors->all(),
    ])
    <script id="initial-feedback-json" type="application/json">{{ json_encode($initialFeedbackPayload, JSON_UNESCAPED_UNICODE) }}</script>

    <div id="company-profile-modal" class="modal-backdrop">
        <div class="modal-card">
            <h3 class="modal-title">Firma Bilgileri</h3>
            <form id="company-profile-form" method="post" action="{{ route('pos.company.profile', [], false) }}">
                @csrf
                <div class="modal-grid">
                    <input type="text" name="company_title" value="{{ $companyProfile['companyTitle'] }}" placeholder="Firma unvani">
                    <input type="text" name="contact_name" value="{{ $companyProfile['contactName'] }}" placeholder="Yetkili kisi">
                    <input type="text" name="contact_phone" value="{{ $companyProfile['contactPhone'] }}" placeholder="Iletisim telefonu">
                    <input type="email" name="contact_email" value="{{ $companyProfile['contactEmail'] }}" placeholder="Iletisim e-postasi">
                    <input type="text" name="tax_number" value="{{ $companyProfile['taxNumber'] }}" placeholder="Vergi no">
                    <input type="text" name="tax_office" value="{{ $companyProfile['taxOffice'] }}" placeholder="Vergi dairesi">
                </div>
                <div class="modal-actions">
                    <button id="close-company-profile" type="button" class="btn-muted">Vazgec</button>
                    <button type="submit" class="btn-primary">Kaydet</button>
                </div>
            </form>
        </div>
    </div>

    <div id="receipt-profile-modal" class="modal-backdrop">
        <div class="modal-card">
            <h3 class="modal-title">Fis Ayarlari</h3>
            <form id="receipt-profile-form" method="post" action="{{ route('pos.receipt.profile', [], false) }}">
                @csrf
                <input type="hidden" name="branch_id" value="{{ (int) $posContext['selectedBranchId'] }}">
                <div class="modal-grid">
                    <input type="text" name="profile_name" value="{{ $receiptProfile['name'] ?? 'Varsayilan Fis Profili' }}" placeholder="Profil adi">
                    <select name="paper_size" aria-label="Kagit tipi">
                        <option value="58mm" {{ ($receiptProfile['paperSize'] ?? '80mm') === '58mm' ? 'selected' : '' }}>58mm</option>
                        <option value="80mm" {{ ($receiptProfile['paperSize'] ?? '80mm') === '80mm' ? 'selected' : '' }}>80mm</option>
                        <option value="a4" {{ ($receiptProfile['paperSize'] ?? '80mm') === 'a4' ? 'selected' : '' }}>A4</option>
                    </select>
                    <select name="print_mode" aria-label="Yazdirma modu">
                        <option value="browser" {{ ($receiptProfile['printMode'] ?? 'browser') === 'browser' ? 'selected' : '' }}>Tarayici Yazdirma</option>
                        <option value="pdf" {{ ($receiptProfile['printMode'] ?? 'browser') === 'pdf' ? 'selected' : '' }}>PDF Oncelikli</option>
                    </select>
                    <input type="text" name="header_line_1" value="{{ $receiptProfile['headerLines'][0] ?? '' }}" placeholder="Baslik satiri 1">
                    <input type="text" name="header_line_2" value="{{ $receiptProfile['headerLines'][1] ?? '' }}" placeholder="Baslik satiri 2">
                    <input type="text" name="footer_line_1" value="{{ $receiptProfile['footerLines'][0] ?? '' }}" placeholder="Alt bilgi satiri 1">
                    <input type="text" name="footer_line_2" value="{{ $receiptProfile['footerLines'][1] ?? '' }}" placeholder="Alt bilgi satiri 2">
                </div>

                <div class="modal-check-grid">
                    <label class="modal-check-item">
                        <input type="checkbox" name="show_company" value="1" {{ !empty($receiptProfile['visibleFields']['showCompany']) ? 'checked' : '' }}>
                        Firma bilgisi goster
                    </label>
                    <label class="modal-check-item">
                        <input type="checkbox" name="show_tax" value="1" {{ !empty($receiptProfile['visibleFields']['showTax']) ? 'checked' : '' }}>
                        Vergi bilgisi goster
                    </label>
                    <label class="modal-check-item">
                        <input type="checkbox" name="show_payment" value="1" {{ !empty($receiptProfile['visibleFields']['showPayment']) ? 'checked' : '' }}>
                        Odeme satirlari goster
                    </label>
                    <label class="modal-check-item">
                        <input type="checkbox" name="show_barcode" value="1" {{ !empty($receiptProfile['visibleFields']['showBarcode']) ? 'checked' : '' }}>
                        Barkod satiri goster
                    </label>
                    <label class="modal-check-item">
                        <input type="checkbox" name="show_date" value="1" {{ !empty($receiptProfile['visibleFields']['showDate']) ? 'checked' : '' }}>
                        Tarih alani goster
                    </label>
                    <label class="modal-check-item">
                        <input type="checkbox" name="show_register" value="1" {{ !empty($receiptProfile['visibleFields']['showRegister']) ? 'checked' : '' }}>
                        Kasa alani goster
                    </label>
                </div>

                <div class="modal-actions">
                    <button id="close-receipt-profile" type="button" class="btn-muted">Vazgec</button>
                    <button type="submit" class="btn-primary">Kaydet</button>
                </div>
            </form>
        </div>
    </div>

    <div id="device-management-modal" class="modal-backdrop">
        <div class="modal-card" style="width:min(980px, 100%); max-height:calc(100vh - 24px); overflow:auto;">
            <h3 class="modal-title">Cihaz ve Oturum Yonetimi</h3>
            <div class="topbar-menu-muted" style="margin-bottom:10px;">
                Cihazlar, aktif POS oturumlari ve personel rolleri burada gorunur. Yetkiler role gore uygulanir.
            </div>

            <div class="manage-grid">
                <div class="manage-card">
                    <h4>Bagli Cihazlar</h4>
                    <div id="manage-device-list"></div>
                    <div id="manage-device-empty" class="manage-empty" style="display:none;">Bu firmaya bagli cihaz bulunamadi.</div>
                </div>

                <div class="manage-card">
                    <h4>POS Oturumlari</h4>
                    <div id="manage-session-list"></div>
                    <div id="manage-session-empty" class="manage-empty" style="display:none;">POS oturumu bulunamadi.</div>
                </div>

                <div class="manage-card">
                    <h4>Personel Rolleri</h4>
                    @if($canManagePersonnel)
                        <form id="manage-staff-form" style="display:grid; gap:8px; grid-template-columns:1fr auto auto; margin-bottom:10px;">
                            <input id="manage-staff-email" type="email" name="email" placeholder="personel@firma.com" required>
                            <select id="manage-staff-role" name="role" required>
                                <option value="manager">Manager</option>
                                <option value="cashier">Kasiyer</option>
                            </select>
                            <button type="submit" class="btn-primary">Ekle/Guncelle</button>
                        </form>
                    @endif
                    <div id="manage-staff-list"></div>
                    <div id="manage-staff-empty" class="manage-empty" style="display:none;">Personel kaydi bulunamadi.</div>
                </div>
            </div>

            <div class="modal-actions">
                <button id="device-management-close" type="button" class="btn-muted">Kapat</button>
            </div>
        </div>
    </div>

    <div id="support-modal" class="modal-backdrop">
        <div class="modal-card" style="width:min(1080px, 100%); max-height:calc(100vh - 24px); overflow:auto;">
            <h3 class="modal-title">Destek ve Geri Bildirim</h3>
            <div class="support-layout">
                <div class="support-sidebar">
                    <select id="support-status-filter" aria-label="Ticket durum filtresi">
                        <option value="">Tum Durumlar</option>
                        <option value="new">Yeni</option>
                        <option value="reviewing">Incelemede</option>
                        <option value="answered">Yanitlandi</option>
                        <option value="closed">Kapali</option>
                    </select>
                    <div style="display:flex; gap:8px;">
                        <button id="support-refresh" type="button" class="btn-muted" style="flex:1;">Yenile</button>
                        <button id="support-start-create" type="button" class="btn-primary" style="flex:1;">Yeni Ticket</button>
                    </div>
                    <div id="support-list" class="support-list"></div>
                    <div id="support-list-empty" class="support-empty" style="display:none;">
                        Ticket bulunamadi.
                    </div>
                </div>

                <div class="support-detail">
                    <div id="support-create-panel" style="display:none;">
                        <div class="support-empty" style="margin-bottom:8px;">
                            Yeni ticket olusturarak hata, istek veya genel geri bildirim iletebilirsiniz.
                        </div>
                        <div class="modal-grid">
                            <select id="support-create-type" aria-label="Ticket turu">
                                <option value="bug">Hata Bildirimi</option>
                                <option value="feature_request">Ozellik Istegi</option>
                                <option value="general">Genel</option>
                            </select>
                            <input id="support-create-title" type="text" maxlength="191" placeholder="Kisa baslik">
                        </div>
                        <textarea id="support-create-description" rows="6" maxlength="20000" placeholder="Detayli aciklama" style="margin-top:8px;"></textarea>
                        <div class="modal-actions">
                            <button id="support-create-cancel" type="button" class="btn-muted">Vazgec</button>
                            <button id="support-create-submit" type="button" class="btn-primary">Ticket Olustur</button>
                        </div>
                    </div>

                    <div id="support-detail-panel">
                        <div id="support-detail-empty" class="support-empty">
                            Soldaki listeden bir ticket secin veya yeni ticket olusturun.
                        </div>

                        <div id="support-detail-content" style="display:none;">
                            <div style="display:flex; justify-content:space-between; gap:8px; align-items:flex-start;">
                                <div>
                                    <div id="support-detail-title" style="font-size:16px; font-weight:700;"></div>
                                    <div id="support-detail-meta" class="support-ticket-meta"></div>
                                </div>
                                <span id="support-detail-status" class="support-status"></span>
                            </div>
                            <div id="support-messages" class="support-messages"></div>
                            <textarea id="support-reply-text" rows="4" maxlength="20000" placeholder="Yanit yazin"></textarea>
                            <div class="modal-actions">
                                <button id="support-reopen" type="button" class="btn-muted" style="display:none;">Yeniden Ac</button>
                                <button id="support-reply-submit" type="button" class="btn-primary">Yaniti Gonder</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-actions">
                <button id="support-close" type="button" class="btn-muted">Kapat</button>
            </div>
        </div>
    </div>

    <div id="sale-receipt-modal" class="modal-backdrop">
        <div class="modal-card receipt-modal-card">
            <h3 id="sale-receipt-title" class="modal-title">Satis Fisi</h3>
            <div class="receipt-actions">
                <button id="sale-receipt-print-58" type="button" class="btn-muted">58mm Yazdir</button>
                <button id="sale-receipt-print-80" type="button" class="btn-muted">80mm Yazdir</button>
                <button id="sale-receipt-pdf-a4" type="button" class="btn-muted">A4 PDF</button>
                @if($canEditPastSales)
                    <button id="sale-receipt-edit-toggle" type="button" class="btn-primary">Duzenle</button>
                    <button id="sale-receipt-delete" type="button" class="btn-danger">Satisi Sil</button>
                @endif
            </div>
            <div id="sale-receipt-summary" class="receipt-summary-grid"></div>
            <div id="sale-receipt-table" class="receipt-table"></div>
            @if($canEditPastSales)
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
                            <button id="sale-receipt-edit-cancel" type="button" class="btn-muted">Vazgec</button>
                            <button id="sale-receipt-edit-save" type="button" class="btn-primary">Kaydet</button>
                        </div>
                    </div>
                </div>
            @endif
            <div class="modal-actions">
                <button id="sale-receipt-close" type="button" class="btn-muted">Kapat</button>
            </div>
        </div>
    </div>

    <div class="cards">
        <div class="card">
            <div class="k">Bugunku satis</div>
            <div id="today-sale-count" class="v">{{ $todaySummary['saleCount'] }}</div>
        </div>
        <div class="card">
            <div class="k">Bugunku urun adedi</div>
            <div id="today-item-count" class="v">{{ $todaySummary['itemCount'] }}</div>
        </div>
        <div class="card">
            <div class="k">Bugunku ciro</div>
            <div id="today-total-amount" class="v">{{ $todaySummary['totalAmount'] }}</div>
        </div>
        <div class="card">
            <div class="k">Aktif POS oturumu</div>
            <div class="v" style="font-size:18px;">#{{ $posContext['activeSession']['id'] ?? '-' }} / {{ $posContext['activeSession']['status'] ?? 'kapali' }}</div>
            <div class="k" style="margin-top:8px;">{{ $posContext['activeSession']['openedAt'] ?? '-' }}</div>
        </div>
    </div>

    @if(empty($posContext['activeSession']['id']))
        <div class="alert error">Aktif POS oturumu kapali. Once POS oturumu ac butonuna basarak devam edin.</div>
    @endif

    <div class="layout">
        <div class="box">
            <h3>Satis</h3>
            <p>Barkodu okutun veya yazin. HID barkod okuyucu klavye gibi calisiyorsa Enter ile otomatik eklenir.</p>
            <div id="scanner-status" class="scanner-status {{ empty($posContext['activeSession']['id']) ? 'disabled' : 'ready' }}" aria-live="polite">
                <span class="dot"></span>
                <span id="scanner-status-text">{{ empty($posContext['activeSession']['id']) ? 'Tarayici pasif (POS oturumu kapali)' : 'Tarayici hazir' }}</span>
            </div>

            @if(!empty($posContext['activeSession']['id']))
                <div class="sale-tabs-wrap">
                    <div class="sale-tabs-title">Satis sekmeleri</div>
                    <div
                        id="sale-tabs-container"
                        class="sale-tabs"
                        data-switch-url="{{ route('pos.sale-session.switch', [], false) }}"
                        data-close-url="{{ route('pos.sale-session.close', [], false) }}"
                        data-create-url="{{ route('pos.sale-session.create', [], false) }}"
                        data-csrf="{{ csrf_token() }}"
                    >
                        @php($tabCount = count($saleSessionContext['sessions']))
                        @foreach($saleSessionContext['sessions'] as $sessionTab)
                            <div class="sale-tab">
                                <form class="sale-tab-form js-pos-action" data-pos-action="sale-tab-switch" method="post" action="{{ route('pos.sale-session.switch', [], false) }}">
                                    @csrf
                                    <input type="hidden" name="sale_session_id" value="{{ $sessionTab['id'] }}">
                                    <button
                                        type="submit"
                                        class="sale-tab-btn {{ (int) $sessionTab['id'] === (int) ($saleSessionContext['activeSessionId'] ?? 0) ? 'active' : '' }}"
                                    >
                                        {{ $sessionTab['label'] }} #{{ $sessionTab['id'] }}
                                    </button>
                                </form>

                                @if($tabCount > 1)
                                    <form class="sale-tab-form sale-tab-close-form js-pos-action" data-pos-action="sale-tab-close" method="post" action="{{ route('pos.sale-session.close', [], false) }}" onsubmit="return confirm('Bu sekmeyi kapatmak istiyor musunuz?');">
                                        @csrf
                                        <input type="hidden" name="sale_session_id" value="{{ $sessionTab['id'] }}">
                                        <button type="submit" class="sale-tab-close-btn" title="Sekmeyi kapat">x</button>
                                    </form>
                                @endif
                            </div>
                        @endforeach

                        <form class="sale-tab-form js-pos-action" data-pos-action="sale-tab-create" method="post" action="{{ route('pos.sale-session.create', [], false) }}">
                            @csrf
                            <button type="submit" class="sale-tab-btn new">+ Yeni Sekme</button>
                        </form>
                    </div>
                </div>

                <div class="held-tabs-wrap">
                    <div class="sale-tabs-title">Bekleyen satislar</div>
                    <div
                        id="held-tabs-container"
                        class="held-tabs"
                        data-resume-url="{{ route('pos.sale-session.held.resume', [], false) }}"
                        data-discard-url="{{ route('pos.sale-session.held.discard', [], false) }}"
                        data-csrf="{{ csrf_token() }}"
                    >
                        @if(empty($saleSessionContext['heldSessions']))
                            <div class="held-empty">Bekleyen satis yok.</div>
                        @else
                            @foreach($saleSessionContext['heldSessions'] as $heldSession)
                                <div class="held-tab">
                                    <form class="sale-tab-form js-pos-action" data-pos-action="held-resume" method="post" action="{{ route('pos.sale-session.held.resume', [], false) }}">
                                        @csrf
                                        <input type="hidden" name="sale_session_id" value="{{ $heldSession['id'] }}">
                                        <button type="submit" class="held-tab-btn">
                                            {{ $heldSession['label'] }} #{{ $heldSession['id'] }} · {{ $heldSession['itemCount'] }} urun · {{ $heldSession['totalAmount'] }}
                                        </button>
                                    </form>
                                    <form class="sale-tab-form sale-tab-close-form js-pos-action" data-pos-action="held-discard" method="post" action="{{ route('pos.sale-session.held.discard', [], false) }}" onsubmit="return confirm('Bu bekleyen satis silinsin mi?');">
                                        @csrf
                                        <input type="hidden" name="sale_session_id" value="{{ $heldSession['id'] }}">
                                        <button type="submit" class="sale-tab-close-btn" title="Bekleyen satisi sil">x</button>
                                    </form>
                                </div>
                            @endforeach
                        @endif
                    </div>
                </div>
            @endif

            <form class="barcode-row js-pos-action" data-pos-action="scan" method="post" action="{{ route('pos.scan', [], false) }}">
                @csrf
                <div class="input-with-suggestions">
                    <input id="barcode-input" type="text" name="barcode" value="{{ $barcodePrefill }}" placeholder="Barkod veya urun adi yazin" autocomplete="off" required>
                    <div id="barcode-suggestions" class="barcode-suggestions"></div>
                </div>
                <button type="submit" class="btn-primary" {{ empty($posContext['activeSession']['id']) ? 'disabled' : '' }}>Sepete Ekle</button>
            </form>

            <div class="cart-table">
                <div class="row head">
                    <div>Urun</div>
                    <div>Adet</div>
                    <div>Tutar</div>
                    <div>Islem</div>
                </div>
                <div id="pos-cart-body">
                    @forelse($cartItems as $item)
                        <div class="row">
                            <div class="product">
                                <span class="name">{{ $item['productName'] }}</span>
                                <span class="code">{{ $item['barcode'] }}</span>
                            </div>
                            <div class="qty">
                                <form class="js-pos-action" data-pos-action="decrement" method="post" action="{{ route('pos.item.decrement', ['barcode' => $item['barcode']], false) }}">
                                    @csrf
                                    <button type="submit" class="btn-muted" {{ empty($posContext['activeSession']['id']) ? 'disabled' : '' }}>-</button>
                                </form>
                                <span>{{ $item['quantity'] }}</span>
                                <form class="js-pos-action" data-pos-action="increment" method="post" action="{{ route('pos.item.increment', ['barcode' => $item['barcode']], false) }}">
                                    @csrf
                                    <button type="submit" class="btn-muted" {{ empty($posContext['activeSession']['id']) ? 'disabled' : '' }}>+</button>
                                </form>
                            </div>
                            <div>{{ number_format(((int) $item['lineTotalKurus']) / 100, 2, ',', '.') }} TL</div>
                            <div>
                                <form class="js-pos-action" data-pos-action="remove" method="post" action="{{ route('pos.item.remove', ['barcode' => $item['barcode']], false) }}">
                                    @csrf
                                    <button class="btn-danger" type="submit" {{ empty($posContext['activeSession']['id']) ? 'disabled' : '' }}>Kaldir</button>
                                </form>
                            </div>
                        </div>
                    @empty
                        <div class="empty">Sepet bos. Barkod okutunca urun buraya duser.</div>
                    @endforelse
                </div>
            </div>
            <div class="hint">
                Secili sube/kasa: {{ $posContext['selectedBranchId'] }} / {{ $posContext['selectedRegisterId'] }}
                <span id="active-sale-label" style="{{ empty($saleSessionContext['activeLabel']) ? 'display:none;' : '' }}">
                    · Aktif sekme: {{ $saleSessionContext['activeLabel'] ?? '' }}
                </span>
            </div>
        </div>

        <div class="box">
            <h3>Sepet Ozeti</h3>
            <div class="totals">
                <div class="line"><span>Sepetteki urun adedi</span><span id="pos-summary-item-count">{{ $cartSummary['itemCount'] }}</span></div>
                <div class="line"><span>Sepet toplami</span><strong id="pos-summary-total-amount">{{ $cartSummary['totalAmount'] }}</strong></div>
            </div>

            <form class="js-pos-action" data-pos-action="checkout" method="post" action="{{ route('pos.checkout', [], false) }}">
                @csrf
                <div style="margin-bottom:8px;">
                    <label for="payment-method" style="display:block; font-size:12px; color:var(--muted); margin-bottom:4px;">Odeme turu</label>
                    <select id="payment-method" name="payment_method" {{ empty($posContext['activeSession']['id']) ? 'disabled' : '' }}>
                        <option value="cash" selected>Nakit</option>
                        <option value="card">Kart</option>
                        <option value="other">Diger</option>
                    </select>
                </div>
                <button id="pos-checkout-button" class="btn-primary checkout" type="submit" {{ empty($cartItems) || empty($posContext['activeSession']['id']) ? 'disabled' : '' }}>
                    Satisi Tamamla
                </button>
            </form>
            <div class="secondary-actions">
                <form class="js-pos-action" data-pos-action="clear" method="post" action="{{ route('pos.cart.clear', [], false) }}" style="flex:1;">
                    @csrf
                    <button class="btn-muted" type="submit" style="width:100%;" {{ empty($posContext['activeSession']['id']) ? 'disabled' : '' }}>Sepeti Temizle</button>
                </form>
                <form class="js-pos-action" data-pos-action="hold" method="post" action="{{ route('pos.sale-session.hold', [], false) }}" style="flex:1;">
                    @csrf
                    <button class="btn-muted" type="submit" style="width:100%;" {{ empty($posContext['activeSession']['id']) ? 'disabled' : '' }}>Satisi Beklet</button>
                </form>
            </div>

            <h3 style="margin-top:14px;">Son Satislar</h3>
            <p id="recent-sales-empty" style="{{ empty($recentSales) ? '' : 'display:none;' }}">Secili kasada web satis kaydi yok.</p>
            <div id="recent-sales-list" style="{{ empty($recentSales) ? 'display:none;' : 'display:grid;' }} gap:9px; margin-top:8px;">
                @foreach($recentSales as $sale)
                    <div class="recent-sale-item" data-sale-id="{{ $sale['id'] }}">
                        <div style="display:flex; justify-content:space-between; gap:8px; font-weight:700;">
                            <span>#{{ $sale['id'] }}{{ !empty($sale['registerName']) ? ' / ' . $sale['registerName'] : '' }}</span>
                            <span>{{ $sale['totalAmount'] }}</span>
                        </div>
                        <div style="font-size:12px; color:var(--muted); margin-top:4px;">
                            {{ $sale['items'] }} urun · {{ $sale['paymentMethodLabel'] ?? 'Nakit' }} · {{ $sale['completedAt'] }}
                        </div>
                    </div>
                @endforeach
            </div>
        </div>
    </div>
</div>

<script>
    (() => {
        const input = document.getElementById('barcode-input');
        const toastRoot = document.getElementById('toast-root');
        const initialFeedbackJson = document.getElementById('initial-feedback-json');
        const cartBody = document.getElementById('pos-cart-body');
        const summaryItemCount = document.getElementById('pos-summary-item-count');
        const summaryTotalAmount = document.getElementById('pos-summary-total-amount');
        const checkoutButton = document.getElementById('pos-checkout-button');
        const todaySaleCount = document.getElementById('today-sale-count');
        const todayItemCount = document.getElementById('today-item-count');
        const todayTotalAmount = document.getElementById('today-total-amount');
        const saleTabsContainer = document.getElementById('sale-tabs-container');
        const heldTabsContainer = document.getElementById('held-tabs-container');
        const menuToggle = document.getElementById('topbar-menu-toggle');
        const menuPanel = document.getElementById('topbar-menu-panel');
        const companyProfileModal = document.getElementById('company-profile-modal');
        const openCompanyProfileButton = document.getElementById('open-company-profile');
        const closeCompanyProfileButton = document.getElementById('close-company-profile');
        const companyProfileForm = document.getElementById('company-profile-form');
        const receiptProfileModal = document.getElementById('receipt-profile-modal');
        const openReceiptProfileButton = document.getElementById('open-receipt-profile');
        const closeReceiptProfileButton = document.getElementById('close-receipt-profile');
        const receiptProfileForm = document.getElementById('receipt-profile-form');
        const supportModal = document.getElementById('support-modal');
        const deviceManagementModal = document.getElementById('device-management-modal');
        const openDeviceManagementButton = document.getElementById('open-device-management');
        const closeDeviceManagementButton = document.getElementById('device-management-close');
        const manageDeviceList = document.getElementById('manage-device-list');
        const manageDeviceEmpty = document.getElementById('manage-device-empty');
        const manageSessionList = document.getElementById('manage-session-list');
        const manageSessionEmpty = document.getElementById('manage-session-empty');
        const manageStaffList = document.getElementById('manage-staff-list');
        const manageStaffEmpty = document.getElementById('manage-staff-empty');
        const manageStaffForm = document.getElementById('manage-staff-form');
        const manageStaffEmail = document.getElementById('manage-staff-email');
        const manageStaffRole = document.getElementById('manage-staff-role');
        const openSupportInboxButton = document.getElementById('open-support-inbox');
        const openSupportCreateButton = document.getElementById('open-support-create');
        const supportCloseButton = document.getElementById('support-close');
        const supportList = document.getElementById('support-list');
        const supportListEmpty = document.getElementById('support-list-empty');
        const supportStatusFilter = document.getElementById('support-status-filter');
        const supportRefreshButton = document.getElementById('support-refresh');
        const supportStartCreateButton = document.getElementById('support-start-create');
        const supportCreatePanel = document.getElementById('support-create-panel');
        const supportCreateType = document.getElementById('support-create-type');
        const supportCreateTitle = document.getElementById('support-create-title');
        const supportCreateDescription = document.getElementById('support-create-description');
        const supportCreateCancelButton = document.getElementById('support-create-cancel');
        const supportCreateSubmitButton = document.getElementById('support-create-submit');
        const supportDetailEmpty = document.getElementById('support-detail-empty');
        const supportDetailContent = document.getElementById('support-detail-content');
        const supportDetailTitle = document.getElementById('support-detail-title');
        const supportDetailMeta = document.getElementById('support-detail-meta');
        const supportDetailStatus = document.getElementById('support-detail-status');
        const supportMessages = document.getElementById('support-messages');
        const supportReplyText = document.getElementById('support-reply-text');
        const supportReplySubmitButton = document.getElementById('support-reply-submit');
        const supportReopenButton = document.getElementById('support-reopen');
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
        const activeSaleLabel = document.getElementById('active-sale-label');
        const scanForm = document.querySelector('form[data-pos-action="scan"]');
        const suggestionsBox = document.getElementById('barcode-suggestions');
        const recentSalesList = document.getElementById('recent-sales-list');
        const recentSalesEmpty = document.getElementById('recent-sales-empty');
        const branchSelect = document.getElementById('pos-branch-select');
        const registerSelect = document.getElementById('pos-register-select');
        const scannerStatus = document.getElementById('scanner-status');
        const scannerStatusText = document.getElementById('scanner-status-text');
        const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content') || '';
        const productSearchUrl = @json(route('pos.product.search', [], false));
        const companyProfileUrl = @json(route('pos.company.profile', [], false));
        const receiptProfileUrl = @json(route('pos.receipt.profile', [], false));
        const supportInboxUrl = @json(route('pos.support.inbox', [], false));
        const supportCreateUrl = @json(route('pos.support.create', [], false));
        const supportShowUrlTemplate = @json(route('pos.support.show', ['ticketId' => '__TICKET_ID__'], false));
        const supportReplyUrlTemplate = @json(route('pos.support.reply', ['ticketId' => '__TICKET_ID__'], false));
        const supportReopenUrlTemplate = @json(route('pos.support.reopen', ['ticketId' => '__TICKET_ID__'], false));
        const manageOverviewUrl = @json(route('pos.manage.overview', [], false));
        const manageToggleDeviceUrlTemplate = @json(route('pos.manage.devices.toggle', ['device' => '__DEVICE_ID__'], false));
        const manageCloseSessionUrlTemplate = @json(route('pos.manage.sessions.close', ['posSession' => '__SESSION_ID__'], false));
        const manageStaffUpsertUrl = @json(route('pos.manage.staff.upsert', [], false));
        const manageStaffDeleteUrlTemplate = @json(route('pos.manage.staff.delete', ['staffRole' => '__STAFF_ROLE_ID__'], false));
        const posSyncStateUrl = @json(route('pos.sync.state', [], false));
        const receiptDetailsUrlTemplate = @json(route('pos.receipts.details-json', ['webSale' => '__SALE_ID__'], false));
        const receiptUpdateUrlTemplate = @json(route('pos.receipts.update-json', ['webSale' => '__SALE_ID__'], false));
        const receiptDeleteUrlTemplate = @json(route('pos.receipts.delete-json', ['webSale' => '__SALE_ID__'], false));
        const hasActiveSession = {{ empty($posContext['activeSession']['id']) ? 'false' : 'true' }};
        const canManageDevices = {{ $canManageDevices ? 'true' : 'false' }};
        const canManagePersonnel = {{ $canManagePersonnel ? 'true' : 'false' }};
        const registerOptionsByBranch = @json($posContext['registerOptionsByBranch']);
        let searchTimer = null;
        let searchAbort = null;
        let suggestions = [];
        let activeSuggestionIndex = -1;
        let scannerBuffer = '';
        let scannerLastKeyAt = 0;
        let focusKeepAliveTimer = null;
        let syncTimer = null;
        let syncInFlight = false;
        let actionInFlightCount = 0;
        let isReceiptLoading = false;
        let receiptModalState = null;
        let supportTickets = [];
        let supportActiveTicketId = null;
        let managePayload = null;

        if (!input || !cartBody || !summaryItemCount || !summaryTotalAmount || !checkoutButton) {
            return;
        }

        const focusInput = () => {
            if (hasActiveSession) {
                input.focus();
            }
        };

        const isOverlayOpen = () => {
            const menuIsOpen = Boolean(menuPanel?.classList.contains('open'));
            const companyModalIsOpen = Boolean(companyProfileModal && companyProfileModal.style.display === 'flex');
            const receiptProfileModalIsOpen = Boolean(receiptProfileModal && receiptProfileModal.style.display === 'flex');
            const receiptModalIsOpen = Boolean(saleReceiptModal && saleReceiptModal.style.display === 'flex');
            const supportModalIsOpen = Boolean(supportModal && supportModal.style.display === 'flex');
            const deviceManagementModalIsOpen = Boolean(deviceManagementModal && deviceManagementModal.style.display === 'flex');
            return menuIsOpen || companyModalIsOpen || receiptProfileModalIsOpen || receiptModalIsOpen || supportModalIsOpen || deviceManagementModalIsOpen;
        };

        const shouldKeepScannerFocus = () => hasActiveSession && !document.hidden && !isOverlayOpen();

        const setScannerState = (state, message = '') => {
            if (!scannerStatus || !scannerStatusText) {
                return;
            }

            scannerStatus.classList.remove('ready', 'busy', 'paused', 'disabled');
            scannerStatus.classList.add(state);

            if (message.trim() !== '') {
                scannerStatusText.textContent = message;
                return;
            }

            if (state === 'ready') {
                scannerStatusText.textContent = 'Tarayici hazir';
                return;
            }
            if (state === 'busy') {
                scannerStatusText.textContent = 'Tarama isleniyor...';
                return;
            }
            if (state === 'paused') {
                scannerStatusText.textContent = 'Tarayici beklemede';
                return;
            }

            scannerStatusText.textContent = 'Tarayici pasif (POS oturumu kapali)';
        };

        const keepScannerFocus = () => {
            if (!input || !shouldKeepScannerFocus()) {
                return;
            }

            if (document.activeElement !== input) {
                input.focus({ preventScroll: true });
            }
        };

        const resetScannerBuffer = () => {
            scannerBuffer = '';
            scannerLastKeyAt = 0;
        };

        const isEditableElement = (target) => {
            if (!(target instanceof HTMLElement)) {
                return false;
            }

            if (target.isContentEditable) {
                return true;
            }

            const tag = target.tagName.toLowerCase();
            if (tag === 'textarea' || tag === 'select') {
                return true;
            }

            if (tag !== 'input') {
                return false;
            }

            const inputType = String(target.getAttribute('type') || 'text').toLowerCase();
            const textTypes = new Set([
                'text',
                'search',
                'email',
                'number',
                'password',
                'tel',
                'url',
            ]);

            return textTypes.has(inputType);
        };

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
            if (!Number.isFinite(value) || value <= 0) {
                return null;
            }

            return Math.round(value * 100);
        };

        const formatKurusLabel = (kurus) => {
            const safeKurus = Number.isFinite(Number(kurus))
                ? Math.max(0, Math.round(Number(kurus)))
                : 0;

            return `${(safeKurus / 100).toLocaleString('tr-TR', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2,
            })} TL`;
        };

        const normalizeReceiptEditableItems = (rawItems) => {
            if (!Array.isArray(rawItems)) {
                return [];
            }

            return rawItems.map((item) => {
                const id = Number(item?.id || 0);
                const quantity = Math.max(1, Math.round(Number(item?.quantity || 1)));
                const directKurus = Number(item?.unitSalePriceKurus);
                const parsedKurus = parseTlTextToKurus(item?.unitSalePrice || item?.unitPrice || '');
                const unitSalePriceKurus = Number.isFinite(directKurus)
                    ? Math.max(0, Math.round(directKurus))
                    : (parsedKurus ?? 0);

                return {
                    id,
                    barcode: String(item?.barcode || ''),
                    name: String(item?.name || '-'),
                    quantity,
                    unitSalePriceKurus,
                    unitSalePrice: (unitSalePriceKurus / 100).toLocaleString('tr-TR', {
                        minimumFractionDigits: 2,
                        maximumFractionDigits: 2,
                    }),
                };
            });
        };

        const resolveReceiptItems = (sale) => {
            const editableItems = normalizeReceiptEditableItems(sale?.itemsEditable);
            if (editableItems.length > 0) {
                return editableItems;
            }

            if (!Array.isArray(sale?.items)) {
                return [];
            }

            return normalizeReceiptEditableItems(
                sale.items.map((item) => ({
                    id: 0,
                    barcode: item?.barcode || '',
                    name: item?.name || '-',
                    quantity: item?.quantity || 1,
                    unitSalePrice: item?.unitPrice || item?.unitSalePrice || '0,00',
                })),
            );
        };

        const refreshReceiptRowLineTotal = (row) => {
            if (!(row instanceof HTMLElement)) {
                return 0;
            }

            const qtyInput = row.querySelector('.receipt-edit-qty');
            const unitInput = row.querySelector('.receipt-edit-unit');
            const lineTotalEl = row.querySelector('.receipt-edit-line-total');
            if (!(qtyInput instanceof HTMLInputElement) || !(unitInput instanceof HTMLInputElement)) {
                return 0;
            }

            const quantity = Math.max(1, Math.round(Number(qtyInput.value || 1)));
            const unitSalePriceKurus = parseTlTextToKurus(unitInput.value || '') ?? 0;
            const lineTotalKurus = quantity * unitSalePriceKurus;

            if (lineTotalEl instanceof HTMLElement) {
                lineTotalEl.textContent = formatKurusLabel(lineTotalKurus);
            }

            return lineTotalKurus;
        };

        const refreshReceiptEditPreview = () => {
            if (!receiptModalState?.isEditing || !saleReceiptTable || !saleReceiptSummary) {
                return;
            }

            let totalKurus = 0;
            const rows = saleReceiptTable.querySelectorAll('[data-receipt-item-id]');
            rows.forEach((row) => {
                totalKurus += refreshReceiptRowLineTotal(row);
            });

            const totalEl = saleReceiptSummary.querySelector('[data-receipt-summary-total]');
            if (totalEl instanceof HTMLElement) {
                totalEl.textContent = formatKurusLabel(totalKurus);
            }
        };

        const showFeedback = (message, type) => {
            if (!message) return;
            if (!toastRoot) return;

            const toast = document.createElement('div');
            toast.className = `toast ${type === 'error' ? 'error' : 'success'}`;
            toast.innerHTML = `
                <div>${escapeHtml(message)}</div>
                <button type="button" class="toast-close" aria-label="Kapat">x</button>
            `;

            const close = () => {
                if (toast.parentElement) {
                    toast.parentElement.removeChild(toast);
                }
            };

            toast.querySelector('.toast-close')?.addEventListener('click', close);
            toastRoot.appendChild(toast);
            setTimeout(close, 2000);
        };

        const supportStatusLabel = (status) => {
            if (status === 'new') return 'Yeni';
            if (status === 'reviewing') return 'Incelemede';
            if (status === 'answered') return 'Yanitlandi';
            if (status === 'closed') return 'Kapali';
            return status || '-';
        };

        const formatDateTime = (epochMs) => {
            const value = Number(epochMs || 0);
            if (!Number.isFinite(value) || value <= 0) {
                return '-';
            }
            return new Date(value).toLocaleString('tr-TR');
        };

        const buildSupportRoute = (template, ticketId) => {
            return String(template || '').replace('__TICKET_ID__', String(Number(ticketId || 0)));
        };

        const buildManageDeviceRoute = (deviceId) => {
            return String(manageToggleDeviceUrlTemplate || '').replace('__DEVICE_ID__', String(Number(deviceId || 0)));
        };

        const buildManageSessionRoute = (sessionId) => {
            return String(manageCloseSessionUrlTemplate || '').replace('__SESSION_ID__', String(Number(sessionId || 0)));
        };

        const buildManageStaffDeleteRoute = (staffRoleId) => {
            return String(manageStaffDeleteUrlTemplate || '').replace('__STAFF_ROLE_ID__', String(Number(staffRoleId || 0)));
        };

        const resetSupportCreateForm = () => {
            if (supportCreateType instanceof HTMLSelectElement) supportCreateType.value = 'bug';
            if (supportCreateTitle instanceof HTMLInputElement) supportCreateTitle.value = '';
            if (supportCreateDescription instanceof HTMLTextAreaElement) supportCreateDescription.value = '';
        };

        const setSupportCreateMode = (enabled) => {
            if (!supportCreatePanel) return;
            supportCreatePanel.style.display = enabled ? 'block' : 'none';
            if (enabled) {
                if (supportDetailEmpty) supportDetailEmpty.style.display = 'none';
                if (supportDetailContent) supportDetailContent.style.display = 'none';
                setTimeout(() => supportCreateTitle?.focus(), 50);
            }
        };

        const renderSupportMessages = (ticket) => {
            if (!supportMessages) return;

            const messages = Array.isArray(ticket?.messages) ? ticket.messages : [];
            if (messages.length === 0) {
                supportMessages.innerHTML = `<div class="support-empty">Bu ticket icin mesaj bulunamadi.</div>`;
                return;
            }

            supportMessages.innerHTML = messages.map((message) => {
                const authorType = String(message?.authorType || 'user');
                const authorLabel = authorType === 'admin' ? 'Admin' : 'Kullanici';
                return `
                    <div class="support-message ${authorType === 'admin' ? 'admin' : 'user'}">
                        <div class="head">
                            <span>${escapeHtml(authorLabel)}</span>
                            <span>${escapeHtml(formatDateTime(message?.createdAt))}</span>
                        </div>
                        <div>${escapeHtml(String(message?.message || ''))}</div>
                    </div>
                `;
            }).join('');
        };

        const setSupportDetail = (ticket) => {
            supportActiveTicketId = Number(ticket?.ticketId || 0) || null;
            if (!supportDetailTitle || !supportDetailMeta || !supportDetailStatus || !supportDetailContent || !supportDetailEmpty) {
                return;
            }

            if (!ticket) {
                supportDetailEmpty.style.display = 'block';
                supportDetailContent.style.display = 'none';
                if (supportReopenButton) supportReopenButton.style.display = 'none';
                return;
            }

            supportDetailEmpty.style.display = 'none';
            supportDetailContent.style.display = 'grid';
            supportDetailTitle.textContent = ticket.title || `Ticket #${ticket.ticketId}`;
            supportDetailMeta.textContent = `#${ticket.ticketId} · ${formatDateTime(ticket.createdAt)} · Son guncelleme ${formatDateTime(ticket.updatedAt)}`;
            supportDetailStatus.textContent = supportStatusLabel(ticket.status);
            supportDetailStatus.className = `support-status ${String(ticket.status || '').toLowerCase()}`;
            if (supportReopenButton) {
                supportReopenButton.style.display = String(ticket.status || '') === 'closed' ? 'inline-flex' : 'none';
            }
            if (supportReplyText instanceof HTMLTextAreaElement) {
                supportReplyText.value = '';
                supportReplyText.disabled = String(ticket.status || '') === 'closed';
            }
            if (supportReplySubmitButton instanceof HTMLButtonElement) {
                supportReplySubmitButton.disabled = String(ticket.status || '') === 'closed';
            }

            renderSupportMessages(ticket);
        };

        const renderSupportList = () => {
            if (!supportList || !supportListEmpty) return;

            if (!Array.isArray(supportTickets) || supportTickets.length === 0) {
                supportList.innerHTML = '';
                supportListEmpty.style.display = 'block';
                return;
            }

            supportListEmpty.style.display = 'none';
            supportList.innerHTML = supportTickets.map((ticket) => {
                const ticketId = Number(ticket?.ticketId || 0);
                const isActive = supportActiveTicketId !== null && ticketId === supportActiveTicketId;
                const status = String(ticket?.status || '');
                const title = String(ticket?.title || `Ticket #${ticketId}`);
                return `
                    <button
                        type="button"
                        class="support-ticket-item ${isActive ? 'active' : ''}"
                        data-support-ticket-id="${ticketId}">
                        <div style="display:flex; justify-content:space-between; gap:8px; align-items:flex-start;">
                            <strong>#${ticketId}</strong>
                            <span class="support-status ${escapeHtml(status)}">${escapeHtml(supportStatusLabel(status))}</span>
                        </div>
                        <div style="margin-top:4px; font-weight:600;">${escapeHtml(title)}</div>
                        <div class="support-ticket-meta">
                            ${escapeHtml(formatDateTime(ticket?.updatedAt || ticket?.createdAt))}
                        </div>
                    </button>
                `;
            }).join('');
        };

        const fetchSupportInbox = async ({ keepSelection = true } = {}) => {
            if (!supportInboxUrl) return;

            const params = new URLSearchParams();
            if (supportStatusFilter instanceof HTMLSelectElement && supportStatusFilter.value.trim() !== '') {
                params.set('status', supportStatusFilter.value.trim());
            }
            const requestUrl = params.toString() !== '' ? `${supportInboxUrl}?${params.toString()}` : supportInboxUrl;

            try {
                const response = await fetch(requestUrl, {
                    method: 'GET',
                    credentials: 'same-origin',
                    headers: {
                        'Accept': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest',
                    },
                });
                const payload = await response.json();
                if (!response.ok || payload?.ok === false) {
                    showFeedback(payload?.message || 'Ticket kutusu yuklenemedi.', 'error');
                    return;
                }

                supportTickets = Array.isArray(payload?.tickets) ? payload.tickets : [];
                if (!keepSelection || !supportTickets.some((ticket) => Number(ticket?.ticketId || 0) === supportActiveTicketId)) {
                    supportActiveTicketId = supportTickets.length > 0 ? Number(supportTickets[0].ticketId || 0) : null;
                }
                renderSupportList();
                if (supportActiveTicketId !== null) {
                    await loadSupportTicket(supportActiveTicketId, { silentOnError: true });
                } else {
                    setSupportDetail(null);
                }
            } catch (_error) {
                showFeedback('Ticket kutusu yuklenirken baglanti hatasi olustu.', 'error');
            }
        };

        const loadSupportTicket = async (ticketId, { silentOnError = false } = {}) => {
            const numericTicketId = Number(ticketId || 0);
            if (!Number.isFinite(numericTicketId) || numericTicketId <= 0) {
                setSupportDetail(null);
                return;
            }

            const requestUrl = buildSupportRoute(supportShowUrlTemplate, numericTicketId);
            try {
                const response = await fetch(requestUrl, {
                    method: 'GET',
                    credentials: 'same-origin',
                    headers: {
                        'Accept': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest',
                    },
                });
                const payload = await response.json();
                if (!response.ok || payload?.ok === false) {
                    if (!silentOnError) {
                        showFeedback(payload?.message || 'Ticket detayi yuklenemedi.', 'error');
                    }
                    return;
                }

                const detail = payload?.data || null;
                if (detail && typeof detail === 'object') {
                    const index = supportTickets.findIndex((ticket) => Number(ticket?.ticketId || 0) === numericTicketId);
                    if (index >= 0) {
                        supportTickets[index] = {
                            ...supportTickets[index],
                            ...detail,
                            updatedAt: detail.updatedAt || supportTickets[index].updatedAt,
                        };
                    }
                }
                setSupportDetail(detail);
                renderSupportList();
            } catch (_error) {
                if (!silentOnError) {
                    showFeedback('Ticket detayi alinirken baglanti hatasi olustu.', 'error');
                }
            }
        };

        const openSupportModal = async ({ createMode = false } = {}) => {
            if (!supportModal) return;
            supportModal.style.display = 'flex';
            setSupportCreateMode(createMode);
            if (createMode) {
                resetSupportCreateForm();
            }
            if (hasActiveSession) {
                setScannerState('paused', 'Tarayici beklemede (destek ekrani acik)');
            }
            await fetchSupportInbox({ keepSelection: !createMode });
        };

        const closeSupportModal = () => {
            if (!supportModal) return;
            supportModal.style.display = 'none';
            setSupportCreateMode(false);
            if (hasActiveSession) {
                setScannerState('ready');
                setTimeout(keepScannerFocus, 60);
            }
        };

        const renderManageDevices = (devices) => {
            if (!manageDeviceList || !manageDeviceEmpty) {
                return;
            }

            const rows = Array.isArray(devices) ? devices : [];
            if (rows.length === 0) {
                manageDeviceList.innerHTML = '';
                manageDeviceEmpty.style.display = 'block';
                return;
            }

            manageDeviceEmpty.style.display = 'none';
            manageDeviceList.innerHTML = rows.map((device) => {
                const deviceId = Number(device?.id || 0);
                const isActive = Boolean(device?.isActive);
                const toggleLabel = isActive ? 'Pasife Al' : 'Aktif Et';

                return `
                    <div class="manage-row">
                        <div class="manage-row-head">
                            <span>${escapeHtml(String(device?.name || '-'))}</span>
                            <span class="manage-pill ${isActive ? 'active' : 'inactive'}">${isActive ? 'Aktif' : 'Pasif'}</span>
                        </div>
                        <div class="manage-row-meta">
                            UID: ${escapeHtml(String(device?.uidMasked || '-'))} · Platform: ${escapeHtml(String(device?.platform || '-'))}<br>
                            Son gorulme: ${escapeHtml(String(device?.lastSeenHuman || '-'))} · Son senkron: ${escapeHtml(String(device?.lastSyncHuman || '-'))}
                        </div>
                        ${canManageDevices ? `
                            <div class="manage-row-actions">
                                <button type="button" class="btn-muted js-manage-toggle-device" data-device-id="${deviceId}" data-next-active="${isActive ? '0' : '1'}">${toggleLabel}</button>
                            </div>
                        ` : ''}
                    </div>
                `;
            }).join('');
        };

        const renderManageSessions = (sessions) => {
            if (!manageSessionList || !manageSessionEmpty) {
                return;
            }

            const rows = Array.isArray(sessions) ? sessions : [];
            if (rows.length === 0) {
                manageSessionList.innerHTML = '';
                manageSessionEmpty.style.display = 'block';
                return;
            }

            manageSessionEmpty.style.display = 'none';
            manageSessionList.innerHTML = rows.map((session) => {
                const sessionId = Number(session?.id || 0);
                const isActive = String(session?.status || '') === 'active';

                return `
                    <div class="manage-row">
                        <div class="manage-row-head">
                            <span>#${sessionId} · ${escapeHtml(String(session?.registerName || '-'))}</span>
                            <span class="manage-pill ${isActive ? 'active' : 'inactive'}">${isActive ? 'Aktif' : 'Kapali'}</span>
                        </div>
                        <div class="manage-row-meta">
                            Sube: ${escapeHtml(String(session?.branchName || '-'))} · Acan: ${escapeHtml(String(session?.openedBy || '-'))}<br>
                            Acilis: ${escapeHtml(String(session?.openedAtHuman || '-'))} · Son aktivite: ${escapeHtml(String(session?.lastActivityHuman || '-'))}
                        </div>
                        ${(canManageDevices && isActive) ? `
                            <div class="manage-row-actions">
                                <button type="button" class="btn-danger js-manage-close-session" data-session-id="${sessionId}">Oturumu Kapat</button>
                            </div>
                        ` : ''}
                    </div>
                `;
            }).join('');
        };

        const renderManageStaff = (staffRows) => {
            if (!manageStaffList || !manageStaffEmpty) {
                return;
            }

            const rows = Array.isArray(staffRows) ? staffRows : [];
            if (rows.length === 0) {
                manageStaffList.innerHTML = '';
                manageStaffEmpty.style.display = 'block';
                return;
            }

            manageStaffEmpty.style.display = 'none';
            manageStaffList.innerHTML = rows.map((staff) => {
                const staffRoleId = Number(staff?.id || 0);
                const isOwner = Boolean(staff?.isOwner);
                const canDelete = canManagePersonnel && !isOwner && staffRoleId > 0;
                const statusLabel = String(staff?.status || 'active') === 'active' ? 'Aktif' : 'Pasif';

                return `
                    <div class="manage-row">
                        <div class="manage-row-head">
                            <span>${escapeHtml(String(staff?.name || '-'))} · ${escapeHtml(String(staff?.roleLabel || '-'))}</span>
                            <span class="manage-pill ${statusLabel === 'Aktif' ? 'active' : 'inactive'}">${statusLabel}</span>
                        </div>
                        <div class="manage-row-meta">${escapeHtml(String(staff?.email || '-'))}</div>
                        ${canDelete ? `
                            <div class="manage-row-actions">
                                <button type="button" class="btn-danger js-manage-delete-staff" data-staff-role-id="${staffRoleId}">Kaldir</button>
                            </div>
                        ` : ''}
                    </div>
                `;
            }).join('');
        };

        const renderManageOverview = (payload) => {
            managePayload = payload && typeof payload === 'object' ? payload : {};
            renderManageDevices(managePayload?.devices || []);
            renderManageSessions(managePayload?.sessions || []);
            renderManageStaff(managePayload?.staff || []);
        };

        const fetchManageOverview = async () => {
            if (!manageOverviewUrl) {
                return;
            }

            try {
                const response = await fetch(manageOverviewUrl, {
                    method: 'GET',
                    credentials: 'same-origin',
                    headers: {
                        'Accept': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest',
                    },
                });
                const payload = await response.json();
                if (!response.ok || payload?.ok === false) {
                    showFeedback(payload?.message || 'Yonetim verileri alinamadi.', 'error');
                    return;
                }

                renderManageOverview(payload?.data || {});
            } catch (_error) {
                showFeedback('Yonetim verileri alinirken baglanti hatasi olustu.', 'error');
            }
        };

        const openDeviceManagementModal = async () => {
            if (!deviceManagementModal || !canManageDevices) {
                return;
            }

            deviceManagementModal.style.display = 'flex';
            if (hasActiveSession) {
                setScannerState('paused', 'Tarayici beklemede (cihaz yonetimi acik)');
            }
            await fetchManageOverview();
        };

        const closeDeviceManagementModal = () => {
            if (!deviceManagementModal) {
                return;
            }

            deviceManagementModal.style.display = 'none';
            if (hasActiveSession) {
                setScannerState('ready');
                setTimeout(keepScannerFocus, 60);
            }
        };

        const openMenu = () => {
            if (!menuPanel || !menuToggle) {
                return;
            }
            menuPanel.classList.add('open');
            menuPanel.setAttribute('aria-hidden', 'false');
            menuToggle.setAttribute('aria-expanded', 'true');
            if (hasActiveSession) {
                setScannerState('paused', 'Tarayici beklemede (menu acik)');
            }
        };

        const closeMenu = () => {
            if (!menuPanel || !menuToggle) {
                return;
            }
            menuPanel.classList.remove('open');
            menuPanel.setAttribute('aria-hidden', 'true');
            menuToggle.setAttribute('aria-expanded', 'false');
            if (hasActiveSession) {
                setScannerState('ready');
                setTimeout(keepScannerFocus, 60);
            }
        };

        const openCompanyProfileModal = () => {
            if (!companyProfileModal) {
                return;
            }
            companyProfileModal.style.display = 'flex';
            if (hasActiveSession) {
                setScannerState('paused', 'Tarayici beklemede (firma formu acik)');
            }
        };

        const closeCompanyProfileModal = () => {
            if (!companyProfileModal) {
                return;
            }
            companyProfileModal.style.display = 'none';
            if (hasActiveSession) {
                setScannerState('ready');
                setTimeout(keepScannerFocus, 60);
            }
        };

        const openReceiptProfileModal = () => {
            if (!receiptProfileModal) {
                return;
            }
            receiptProfileModal.style.display = 'flex';
            if (hasActiveSession) {
                setScannerState('paused', 'Tarayici beklemede (fis ayarlari acik)');
            }
        };

        const closeReceiptProfileModal = () => {
            if (!receiptProfileModal) {
                return;
            }
            receiptProfileModal.style.display = 'none';
            if (hasActiveSession) {
                setScannerState('ready');
                setTimeout(keepScannerFocus, 60);
            }
        };

        const closeSaleReceiptModal = () => {
            if (!saleReceiptModal) {
                return;
            }
            saleReceiptModal.style.display = 'none';
            receiptModalState = null;
            if (hasActiveSession) {
                setScannerState('ready');
                setTimeout(keepScannerFocus, 60);
            }
        };

        const setSaleReceiptLoading = (loading) => {
            isReceiptLoading = loading;
            if (!saleReceiptTable || !saleReceiptSummary || !saleReceiptTitle) {
                return;
            }

            if (loading) {
                saleReceiptTitle.textContent = 'Satis Fisi Yukleniyor...';
                saleReceiptSummary.innerHTML = '';
                saleReceiptTable.innerHTML = '<div class="receipt-row">Yukleniyor...</div>';
            }
        };

        const renderSaleReceiptModal = () => {
            if (!receiptModalState || !saleReceiptSummary || !saleReceiptTable || !saleReceiptTitle || !saleReceiptPaymentMethodSelect) {
                return;
            }

            const sale = receiptModalState.sale || {};
            const items = resolveReceiptItems(sale);
            const paymentMethod = String((sale.payments?.[0]?.method || 'cash'));

            saleReceiptTitle.textContent = `Satis Fisi #${Number(sale.id || 0)}`;
            saleReceiptPaymentMethodSelect.value = paymentMethod;

            saleReceiptSummary.innerHTML = `
                <div class="receipt-summary-card">
                    <div class="k">Sube</div>
                    <div class="v">${escapeHtml(sale.branchName || '-')}</div>
                </div>
                <div class="receipt-summary-card">
                    <div class="k">Kasa</div>
                    <div class="v">${escapeHtml(sale.registerName || '-')}</div>
                </div>
                <div class="receipt-summary-card">
                    <div class="k">Tarih</div>
                    <div class="v">${escapeHtml(sale.completedAt || '-')}</div>
                </div>
                <div class="receipt-summary-card">
                    <div class="k">Toplam</div>
                    <div class="v" data-receipt-summary-total>${escapeHtml(sale.totalAmount || '0,00 TL')}</div>
                </div>
            `;

            saleReceiptTable.innerHTML = `
                <div class="receipt-row head">
                    <div>Urun</div>
                    <div>Adet</div>
                    <div class="receipt-right">Birim Fiyat</div>
                    <div class="receipt-right">Tutar</div>
                </div>
                ${items.map((item) => {
                    const quantity = Math.max(1, Math.round(Number(item.quantity || 1)));
                    const unitPriceKurus = Math.max(0, Math.round(Number(item.unitSalePriceKurus || 0)));
                    const linePriceKurus = quantity * unitPriceKurus;
                    const editableId = Number(item.id || 0);
                    const canEdit = Boolean(receiptModalState.isEditing && editableId > 0);

                    if (!canEdit) {
                        return `
                            <div class="receipt-row">
                                <div>
                                    <div style="font-weight:700;">${escapeHtml(item.name || '-')}</div>
                                    <div style="font-size:12px; color:var(--muted);">${escapeHtml(item.barcode || '')}</div>
                                </div>
                                <div>${quantity}</div>
                                <div class="receipt-right">${formatKurusLabel(unitPriceKurus)}</div>
                                <div class="receipt-right">${formatKurusLabel(linePriceKurus)}</div>
                            </div>
                        `;
                    }

                    return `
                        <div class="receipt-row" data-receipt-item-id="${editableId}">
                            <div>
                                <div style="font-weight:700;">${escapeHtml(item.name || '-')}</div>
                                <div style="font-size:12px; color:var(--muted);">${escapeHtml(item.barcode || '')}</div>
                            </div>
                            <div><input type="number" min="1" step="1" class="receipt-edit-qty" value="${quantity}"></div>
                            <div class="receipt-right"><input type="text" class="receipt-edit-unit" value="${escapeHtml(item.unitSalePrice || '0,00')}" placeholder="0,00"></div>
                            <div class="receipt-right receipt-edit-line-total">${formatKurusLabel(linePriceKurus)}</div>
                        </div>
                    `;
                }).join('')}
            `;

            refreshReceiptEditPreview();
        };

        const setReceiptEditMode = (enabled) => {
            if (!receiptModalState || !saleReceiptEditPanel || !saleReceiptEditToggleButton) {
                return;
            }
            receiptModalState.isEditing = enabled;
            saleReceiptEditPanel.style.display = enabled ? '' : 'none';
            saleReceiptEditToggleButton.textContent = enabled ? 'Gorunume Don' : 'Duzenle';
            renderSaleReceiptModal();
        };

        const openSaleReceiptModal = async (saleId) => {
            const numericSaleId = Number(saleId || 0);
            if (numericSaleId <= 0 || isReceiptLoading) {
                return;
            }

            const detailsUrl = buildReceiptRoute(receiptDetailsUrlTemplate, numericSaleId);
            if (detailsUrl.trim() === '') {
                return;
            }

            if (saleReceiptModal) {
                saleReceiptModal.style.display = 'flex';
            }

            if (hasActiveSession) {
                setScannerState('paused', 'Tarayici beklemede (fis popup acik)');
            }

            setSaleReceiptLoading(true);

            try {
                const response = await fetch(detailsUrl, {
                    method: 'GET',
                    credentials: 'same-origin',
                    headers: {
                        'Accept': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest',
                    },
                });

                const payload = await response.json();
                if (!response.ok || payload?.ok === false) {
                    showFeedback(payload?.message || 'Fis detayi alinamadi.', 'error');
                    closeSaleReceiptModal();
                    return;
                }

                receiptModalState = {
                    saleId: numericSaleId,
                    isEditing: false,
                    sale: payload?.data?.sale ?? null,
                    print: payload?.data?.print ?? {},
                };
                setReceiptEditMode(false);
            } catch (_error) {
                showFeedback('Fis detayi alinirken baglanti hatasi olustu.', 'error');
                closeSaleReceiptModal();
            } finally {
                setSaleReceiptLoading(false);
            }
        };

        const createCartRow = (item) => {
            const productName = escapeHtml(item.productName || item.barcode || '');
            const barcode = escapeHtml(item.barcode || '');
            const qty = Number(item.quantity || 0);
            const lineTotal = escapeHtml(item.lineTotal || '0,00 TL');
            const incrementUrl = escapeHtml(item.actions?.increment || '#');
            const decrementUrl = escapeHtml(item.actions?.decrement || '#');
            const removeUrl = escapeHtml(item.actions?.remove || '#');
            const disabledAttr = hasActiveSession ? '' : 'disabled';

            return `
                <div class="row">
                    <div class="product">
                        <span class="name">${productName}</span>
                        <span class="code">${barcode}</span>
                    </div>
                    <div class="qty">
                        <form class="js-pos-action" data-pos-action="decrement" method="post" action="${decrementUrl}">
                            <input type="hidden" name="_token" value="${escapeHtml(csrfToken)}">
                            <button type="submit" class="btn-muted" ${disabledAttr}>-</button>
                        </form>
                        <span>${qty}</span>
                        <form class="js-pos-action" data-pos-action="increment" method="post" action="${incrementUrl}">
                            <input type="hidden" name="_token" value="${escapeHtml(csrfToken)}">
                            <button type="submit" class="btn-muted" ${disabledAttr}>+</button>
                        </form>
                    </div>
                    <div>${lineTotal}</div>
                    <div>
                        <form class="js-pos-action" data-pos-action="remove" method="post" action="${removeUrl}">
                            <input type="hidden" name="_token" value="${escapeHtml(csrfToken)}">
                            <button class="btn-danger" type="submit" ${disabledAttr}>Kaldir</button>
                        </form>
                    </div>
                </div>
            `;
        };

        const createRecentSaleRow = (sale) => {
            const saleId = Number(sale?.id || 0);
            const registerName = sale?.registerName ? ` / ${escapeHtml(sale.registerName)}` : '';
            const paymentLabel = sale?.paymentMethodLabel ? ` · ${escapeHtml(sale.paymentMethodLabel)}` : '';
            const totalAmount = escapeHtml(sale?.totalAmount || '0,00 TL');
            const totalItems = Number(sale?.totalItems || 0);
            const completedAt = escapeHtml(sale?.completedAt || 'az once');

            return `
                <div class="recent-sale-item" data-sale-id="${saleId}">
                    <div style="display:flex; justify-content:space-between; gap:8px; font-weight:700;">
                        <span>#${saleId}${registerName}</span>
                        <span>${totalAmount}</span>
                    </div>
                    <div style="font-size:12px; color:var(--muted); margin-top:4px;">
                        ${totalItems} urun${paymentLabel} · ${completedAt}
                    </div>
                </div>
            `;
        };

        const renderRecentSales = (sales) => {
            if (!recentSalesList || !recentSalesEmpty) {
                return;
            }

            const rows = Array.isArray(sales) ? sales : [];
            if (rows.length === 0) {
                recentSalesList.style.display = 'none';
                recentSalesList.innerHTML = '';
                recentSalesEmpty.style.display = '';
                return;
            }

            recentSalesEmpty.style.display = 'none';
            recentSalesList.style.display = 'grid';
            recentSalesList.innerHTML = rows.slice(0, 8).map(createRecentSaleRow).join('');
        };

        const prependRecentSale = (sale) => {
            if (!recentSalesList || !recentSalesEmpty) {
                return;
            }

            recentSalesEmpty.style.display = 'none';
            recentSalesList.style.display = 'grid';
            recentSalesList.insertAdjacentHTML('afterbegin', createRecentSaleRow(sale));

            const rows = recentSalesList.querySelectorAll('.recent-sale-item');
            rows.forEach((row, index) => {
                if (index >= 8) {
                    row.remove();
                }
            });
        };

        const renderHeldTabs = (heldSessions) => {
            if (!heldTabsContainer) {
                return;
            }

            const sessions = Array.isArray(heldSessions) ? heldSessions : [];
            const resumeUrl = String(heldTabsContainer.dataset.resumeUrl || '');
            const discardUrl = String(heldTabsContainer.dataset.discardUrl || '');
            const token = escapeHtml(String(heldTabsContainer.dataset.csrf || csrfToken));

            if (sessions.length === 0) {
                heldTabsContainer.innerHTML = '<div class="held-empty">Bekleyen satis yok.</div>';
                return;
            }

            heldTabsContainer.innerHTML = sessions.map((session) => {
                const id = Number(session?.id || 0);
                const label = escapeHtml(String(session?.label || `Bekleyen ${id}`));
                const itemCount = Number(session?.itemCount || 0);
                const totalAmount = escapeHtml(String(session?.totalAmount || '0,00 TL'));
                const updatedAt = escapeHtml(String(session?.updatedAt || ''));
                const meta = updatedAt !== '' ? `${itemCount} urun · ${totalAmount} · ${updatedAt}` : `${itemCount} urun · ${totalAmount}`;

                return `
                    <div class="held-tab">
                        <form class="sale-tab-form js-pos-action" data-pos-action="held-resume" method="post" action="${escapeHtml(resumeUrl)}">
                            <input type="hidden" name="_token" value="${token}">
                            <input type="hidden" name="sale_session_id" value="${id}">
                            <button type="submit" class="held-tab-btn">${label} #${id} · ${meta}</button>
                        </form>
                        <form class="sale-tab-form sale-tab-close-form js-pos-action" data-pos-action="held-discard" method="post" action="${escapeHtml(discardUrl)}" onsubmit="return confirm('Bu bekleyen satis silinsin mi?');">
                            <input type="hidden" name="_token" value="${token}">
                            <input type="hidden" name="sale_session_id" value="${id}">
                            <button type="submit" class="sale-tab-close-btn" title="Bekleyen satisi sil">x</button>
                        </form>
                    </div>
                `;
            }).join('');
        };

        const renderSaleTabs = (saleSessions, activeSessionId, heldSessions = []) => {
            if (!saleTabsContainer) {
                renderHeldTabs(heldSessions);
                return;
            }

            const sessions = Array.isArray(saleSessions) ? saleSessions : [];
            const switchUrl = String(saleTabsContainer.dataset.switchUrl || '');
            const closeUrl = String(saleTabsContainer.dataset.closeUrl || '');
            const createUrl = String(saleTabsContainer.dataset.createUrl || '');
            const token = escapeHtml(String(saleTabsContainer.dataset.csrf || csrfToken));
            const canClose = sessions.length > 1;

            const tabHtml = sessions.map((session) => {
                const id = Number(session?.id || 0);
                const label = escapeHtml(String(session?.label || `Sekme ${id}`));
                const isActive = Number(activeSessionId || 0) === id || Boolean(session?.isActive);
                const activeClass = isActive ? 'active' : '';

                return `
                    <div class="sale-tab">
                        <form class="sale-tab-form js-pos-action" data-pos-action="sale-tab-switch" method="post" action="${escapeHtml(switchUrl)}">
                            <input type="hidden" name="_token" value="${token}">
                            <input type="hidden" name="sale_session_id" value="${id}">
                            <button type="submit" class="sale-tab-btn ${activeClass}">
                                ${label} #${id}
                            </button>
                        </form>
                        ${canClose ? `
                            <form class="sale-tab-form sale-tab-close-form js-pos-action" data-pos-action="sale-tab-close" method="post" action="${escapeHtml(closeUrl)}" onsubmit="return confirm('Bu sekmeyi kapatmak istiyor musunuz?');">
                                <input type="hidden" name="_token" value="${token}">
                                <input type="hidden" name="sale_session_id" value="${id}">
                                <button type="submit" class="sale-tab-close-btn" title="Sekmeyi kapat">x</button>
                            </form>
                        ` : ''}
                    </div>
                `;
            }).join('');

            const createHtml = `
                <form class="sale-tab-form js-pos-action" data-pos-action="sale-tab-create" method="post" action="${escapeHtml(createUrl)}">
                    <input type="hidden" name="_token" value="${token}">
                    <button type="submit" class="sale-tab-btn new">+ Yeni Sekme</button>
                </form>
            `;

            saleTabsContainer.innerHTML = `${tabHtml}${createHtml}`;
            renderHeldTabs(heldSessions);
        };

        const hideSuggestions = () => {
            if (!suggestionsBox) {
                return;
            }

            suggestions = [];
            activeSuggestionIndex = -1;
            suggestionsBox.style.display = 'none';
            suggestionsBox.innerHTML = '';
        };

        const renderSuggestions = (items) => {
            if (!suggestionsBox) {
                return;
            }

            suggestions = Array.isArray(items) ? items : [];
            activeSuggestionIndex = -1;

            if (suggestions.length === 0) {
                hideSuggestions();
                return;
            }

            suggestionsBox.innerHTML = suggestions.map((item, index) => `
                <button type="button" class="barcode-suggestion-item" data-index="${index}">
                    <strong>${escapeHtml(item.name || item.barcode || '')}</strong>
                    <span class="barcode-suggestion-meta">${escapeHtml(item.barcode || '')} · ${escapeHtml(item.price || '')}</span>
                </button>
            `).join('');
            suggestionsBox.style.display = 'block';
        };

        const refreshActiveSuggestion = () => {
            if (!suggestionsBox) {
                return;
            }

            suggestionsBox.querySelectorAll('.barcode-suggestion-item').forEach((button, index) => {
                if (index === activeSuggestionIndex) {
                    button.classList.add('active');
                } else {
                    button.classList.remove('active');
                }
            });
        };

        const pickSuggestion = (index, autoSubmit = true) => {
            const item = suggestions[index];
            if (!item || !input) {
                return;
            }

            input.value = String(item.barcode || '');
            hideSuggestions();

            if (autoSubmit && scanForm) {
                submitAction(scanForm);
            }
        };

        const fetchSuggestions = (term) => {
            if (!hasActiveSession || !suggestionsBox) {
                return;
            }

            const trimmed = String(term || '').trim();
            if (trimmed.length < 1) {
                hideSuggestions();
                return;
            }

            if (searchTimer) {
                clearTimeout(searchTimer);
            }

            searchTimer = setTimeout(async () => {
                try {
                    if (searchAbort) {
                        searchAbort.abort();
                    }

                    searchAbort = new AbortController();
                    const response = await fetch(`${productSearchUrl}?q=${encodeURIComponent(trimmed)}`, {
                        method: 'GET',
                        credentials: 'same-origin',
                        headers: {
                            'Accept': 'application/json',
                            'X-Requested-With': 'XMLHttpRequest',
                        },
                        signal: searchAbort.signal,
                    });

                    if (!response.ok) {
                        hideSuggestions();
                        return;
                    }

                    const payload = await response.json();
                    renderSuggestions(payload?.data ?? []);
                } catch (_error) {
                    hideSuggestions();
                }
            }, 180);
        };

        const updateCartView = (data) => {
            const items = Array.isArray(data?.cartItems) ? data.cartItems : [];
            if (items.length === 0) {
                cartBody.innerHTML = '<div class="empty">Sepet bos. Barkod okutunca urun buraya duser.</div>';
            } else {
                cartBody.innerHTML = items.map(createCartRow).join('');
            }

            summaryItemCount.textContent = String(data?.summary?.itemCount ?? 0);
            summaryTotalAmount.textContent = String(data?.summary?.totalAmount ?? '0,00 TL');
            checkoutButton.disabled = !Boolean(data?.canCheckout) || !hasActiveSession;

            if (activeSaleLabel) {
                const label = String(data?.activeSaleSession?.label ?? '').trim();
                if (label === '') {
                    activeSaleLabel.style.display = 'none';
                } else {
                    activeSaleLabel.style.display = '';
                    activeSaleLabel.textContent = ` · Aktif sekme: ${label}`;
                }
            }

            if (data?.todaySummary) {
                if (todaySaleCount) {
                    todaySaleCount.textContent = String(data.todaySummary.saleCount ?? '0');
                }
                if (todayItemCount) {
                    todayItemCount.textContent = String(data.todaySummary.itemCount ?? '0');
                }
                if (todayTotalAmount) {
                    todayTotalAmount.textContent = String(data.todaySummary.totalAmount ?? '0,00 TL');
                }
            }

            if (Array.isArray(data?.recentSales)) {
                renderRecentSales(data.recentSales);
            }
        };

        const updateRegisterOptions = () => {
            if (!branchSelect || !registerSelect) {
                return;
            }

            const branchId = String(branchSelect.value || '');
            const options = Array.isArray(registerOptionsByBranch[branchId]) ? registerOptionsByBranch[branchId] : [];
            const currentValue = String(registerSelect.value || '');

            registerSelect.innerHTML = '';

            options.forEach((register, index) => {
                const option = document.createElement('option');
                option.value = String(register.id);
                option.textContent = `${register.name} / ${register.code}`;
                if (String(register.id) === currentValue || (index === 0 && currentValue === '')) {
                    option.selected = true;
                }
                registerSelect.appendChild(option);
            });
        };

        const setFormPending = (form, pending) => {
            form.querySelectorAll('button').forEach((button) => {
                button.disabled = pending || !hasActiveSession;
            });
        };

        const requestSyncState = async ({ force = false, showSwitchToast = false } = {}) => {
            if (!hasActiveSession || syncInFlight) {
                return;
            }

            if (!force && actionInFlightCount > 0) {
                return;
            }

            syncInFlight = true;
            try {
                const response = await fetch(posSyncStateUrl, {
                    method: 'GET',
                    credentials: 'same-origin',
                    headers: {
                        'Accept': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest',
                    },
                });

                const payload = await response.json();
                if (!response.ok || payload?.ok === false) {
                    if (response.status === 401 && payload?.redirect) {
                        window.location.href = payload.redirect;
                    }
                    return;
                }

                if (payload?.data) {
                    updateCartView(payload.data);
                    renderSaleTabs(payload.data.saleSessions, payload.data.activeSaleSession?.id, payload.data.heldSessions);
                    if (showSwitchToast && payload?.data?.sync?.activeSaleSessionSwitched) {
                        showFeedback('Telefon taramasi algilandi. Ilgili satis sekmesi one alindi.', 'success');
                    }
                }
            } catch (_error) {
                // Sessiz gec: polling ag hatalarinda kullaniciyi rahatsiz etme.
            } finally {
                syncInFlight = false;
            }
        };

        const startSyncPolling = () => {
            if (!hasActiveSession || syncTimer !== null) {
                return;
            }

            syncTimer = window.setInterval(() => {
                requestSyncState({ force: false, showSwitchToast: true });
            }, 1200);
        };

        const stopSyncPolling = () => {
            if (syncTimer !== null) {
                window.clearInterval(syncTimer);
                syncTimer = null;
            }
        };

        const submitAction = async (form) => {
            if (!hasActiveSession) {
                showFeedback('Aktif POS oturumu yok. Once POS oturumu acin.', 'error');
                setScannerState('disabled');
                return;
            }

            const isScanAction = form.dataset.posAction === 'scan';
            if (isScanAction) {
                setScannerState('busy');
            }

            setFormPending(form, true);
            actionInFlightCount += 1;
            try {
                const response = await fetch(form.action, {
                    method: 'POST',
                    credentials: 'same-origin',
                    headers: {
                        'Accept': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest',
                        'X-CSRF-TOKEN': csrfToken,
                    },
                    body: new FormData(form),
                });

                let payload = null;
                try {
                    payload = await response.json();
                } catch (_error) {
                    payload = null;
                }

                if (!payload) {
                    showFeedback('Sunucu yaniti okunamadi.', 'error');
                    return;
                }

                if (!response.ok || payload.ok === false) {
                    if (payload.redirect) {
                        window.location.href = payload.redirect;
                        return;
                    }
                    showFeedback(payload.message || 'Islem basarisiz.', 'error');
                    return;
                }

                if (payload.data) {
                    updateCartView(payload.data);
                    renderSaleTabs(payload.data.saleSessions, payload.data.activeSaleSession?.id, payload.data.heldSessions);
                    if (payload.data.sale) {
                        prependRecentSale(payload.data.sale);
                    }
                }

                showFeedback(payload.message || 'Islem tamamlandi.', 'success');

                if (isScanAction) {
                    input.value = '';
                    hideSuggestions();
                    resetScannerBuffer();
                }

                focusInput();
                await requestSyncState({ force: true, showSwitchToast: false });
            } catch (_error) {
                showFeedback('Baglanti hatasi olustu. Lutfen tekrar deneyin.', 'error');
            } finally {
                actionInFlightCount = Math.max(0, actionInFlightCount - 1);
                setFormPending(form, false);
                if (isScanAction) {
                    setScannerState('ready');
                }
            }
        };

        document.addEventListener('submit', (event) => {
            const target = event.target;
            if (!(target instanceof HTMLFormElement)) return;
            if (!target.classList.contains('js-pos-action')) return;

            event.preventDefault();
            submitAction(target);
        });

        if (companyProfileForm instanceof HTMLFormElement) {
            companyProfileForm.addEventListener('submit', async (event) => {
                event.preventDefault();
                const formData = new FormData(companyProfileForm);

                try {
                    const response = await fetch(companyProfileUrl, {
                        method: 'POST',
                        credentials: 'same-origin',
                        headers: {
                            'Accept': 'application/json',
                            'X-Requested-With': 'XMLHttpRequest',
                            'X-CSRF-TOKEN': csrfToken,
                        },
                        body: formData,
                    });

                    const payload = await response.json();
                    if (!response.ok || payload?.ok === false) {
                        showFeedback(payload?.message || 'Firma bilgileri kaydedilemedi.', 'error');
                        return;
                    }

                    showFeedback(payload?.message || 'Firma bilgileri guncellendi.', 'success');
                    closeCompanyProfileModal();
                } catch (_error) {
                    showFeedback('Firma bilgileri kayit sirasinda baglanti hatasi olustu.', 'error');
                }
            });
        }

        if (receiptProfileForm instanceof HTMLFormElement) {
            receiptProfileForm.addEventListener('submit', async (event) => {
                event.preventDefault();
                const formData = new FormData(receiptProfileForm);

                try {
                    const response = await fetch(receiptProfileUrl, {
                        method: 'POST',
                        credentials: 'same-origin',
                        headers: {
                            'Accept': 'application/json',
                            'X-Requested-With': 'XMLHttpRequest',
                            'X-CSRF-TOKEN': csrfToken,
                        },
                        body: formData,
                    });

                    const payload = await response.json();
                    if (!response.ok || payload?.ok === false) {
                        showFeedback(payload?.message || 'Fis ayarlari kaydedilemedi.', 'error');
                        return;
                    }

                    showFeedback(payload?.message || 'Fis ayarlari guncellendi.', 'success');
                    closeReceiptProfileModal();
                } catch (_error) {
                    showFeedback('Fis ayarlari kayit sirasinda baglanti hatasi olustu.', 'error');
                }
            });
        }

        supportRefreshButton?.addEventListener('click', async () => {
            await fetchSupportInbox({ keepSelection: true });
        });

        supportStatusFilter?.addEventListener('change', async () => {
            await fetchSupportInbox({ keepSelection: false });
        });

        supportStartCreateButton?.addEventListener('click', () => {
            setSupportCreateMode(true);
            resetSupportCreateForm();
        });

        supportCreateCancelButton?.addEventListener('click', () => {
            setSupportCreateMode(false);
            if (supportActiveTicketId !== null) {
                loadSupportTicket(supportActiveTicketId, { silentOnError: true });
            } else {
                setSupportDetail(null);
            }
        });

        supportCreateSubmitButton?.addEventListener('click', async () => {
            const type = String(supportCreateType?.value || 'bug').trim();
            const title = String(supportCreateTitle?.value || '').trim();
            const description = String(supportCreateDescription?.value || '').trim();

            if (title.length < 3 || description.length < 5) {
                showFeedback('Ticket basligi ve aciklamasi zorunludur.', 'error');
                return;
            }

            try {
                const response = await fetch(supportCreateUrl, {
                    method: 'POST',
                    credentials: 'same-origin',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest',
                        'X-CSRF-TOKEN': csrfToken,
                    },
                    body: JSON.stringify({ type, title, description }),
                });
                const payload = await response.json();
                if (!response.ok || payload?.ok === false) {
                    showFeedback(payload?.message || 'Ticket olusturulamadi.', 'error');
                    return;
                }

                showFeedback(payload?.message || 'Ticket olusturuldu.', 'success');
                setSupportCreateMode(false);
                supportActiveTicketId = Number(payload?.data?.ticketId || payload?.data?.ticket?.ticketId || payload?.data?.id || 0) || null;
                await fetchSupportInbox({ keepSelection: true });
            } catch (_error) {
                showFeedback('Ticket olusturma sirasinda baglanti hatasi olustu.', 'error');
            }
        });

        supportList?.addEventListener('click', async (event) => {
            const target = event.target;
            if (!(target instanceof Element)) return;
            const item = target.closest('[data-support-ticket-id]');
            if (!(item instanceof HTMLElement)) return;

            const ticketId = Number(item.dataset.supportTicketId || 0);
            if (!Number.isFinite(ticketId) || ticketId <= 0) return;

            supportActiveTicketId = ticketId;
            setSupportCreateMode(false);
            renderSupportList();
            await loadSupportTicket(ticketId, { silentOnError: false });
        });

        supportReplySubmitButton?.addEventListener('click', async () => {
            const ticketId = Number(supportActiveTicketId || 0);
            const message = String(supportReplyText?.value || '').trim();
            if (ticketId <= 0) {
                showFeedback('Lutfen bir ticket secin.', 'error');
                return;
            }
            if (message.length < 2) {
                showFeedback('Yanit metni bos olamaz.', 'error');
                return;
            }

            const requestUrl = buildSupportRoute(supportReplyUrlTemplate, ticketId);
            try {
                const response = await fetch(requestUrl, {
                    method: 'POST',
                    credentials: 'same-origin',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest',
                        'X-CSRF-TOKEN': csrfToken,
                    },
                    body: JSON.stringify({ message }),
                });
                const payload = await response.json();
                if (!response.ok || payload?.ok === false) {
                    showFeedback(payload?.message || 'Yanit gonderilemedi.', 'error');
                    return;
                }

                showFeedback(payload?.message || 'Yanit gonderildi.', 'success');
                await fetchSupportInbox({ keepSelection: true });
            } catch (_error) {
                showFeedback('Yanit gonderimi sirasinda baglanti hatasi olustu.', 'error');
            }
        });

        supportReopenButton?.addEventListener('click', async () => {
            const ticketId = Number(supportActiveTicketId || 0);
            if (ticketId <= 0) return;

            const requestUrl = buildSupportRoute(supportReopenUrlTemplate, ticketId);
            try {
                const response = await fetch(requestUrl, {
                    method: 'POST',
                    credentials: 'same-origin',
                    headers: {
                        'Accept': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest',
                        'X-CSRF-TOKEN': csrfToken,
                    },
                });
                const payload = await response.json();
                if (!response.ok || payload?.ok === false) {
                    showFeedback(payload?.message || 'Ticket yeniden acilamadi.', 'error');
                    return;
                }

                showFeedback(payload?.message || 'Ticket yeniden acildi.', 'success');
                await fetchSupportInbox({ keepSelection: true });
            } catch (_error) {
                showFeedback('Ticket yeniden acma sirasinda baglanti hatasi olustu.', 'error');
            }
        });

        openDeviceManagementButton?.addEventListener('click', async () => {
            closeMenu();
            await openDeviceManagementModal();
        });

        closeDeviceManagementButton?.addEventListener('click', () => {
            closeDeviceManagementModal();
        });

        deviceManagementModal?.addEventListener('click', (event) => {
            if (event.target === deviceManagementModal) {
                closeDeviceManagementModal();
            }
        });

        manageDeviceList?.addEventListener('click', async (event) => {
            const button = event.target instanceof Element
                ? event.target.closest('.js-manage-toggle-device')
                : null;
            if (!(button instanceof HTMLElement)) {
                return;
            }

            const deviceId = Number(button.dataset.deviceId || 0);
            if (deviceId <= 0) {
                return;
            }

            const nextActive = String(button.dataset.nextActive || '0') === '1';
            const requestUrl = buildManageDeviceRoute(deviceId);
            try {
                const response = await fetch(requestUrl, {
                    method: 'POST',
                    credentials: 'same-origin',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest',
                        'X-CSRF-TOKEN': csrfToken,
                    },
                    body: JSON.stringify({ is_active: nextActive }),
                });
                const payload = await response.json();
                if (!response.ok || payload?.ok === false) {
                    showFeedback(payload?.message || 'Cihaz durumu guncellenemedi.', 'error');
                    return;
                }

                showFeedback(payload?.message || 'Cihaz durumu guncellendi.', 'success');
                renderManageOverview(payload?.data || {});
            } catch (_error) {
                showFeedback('Cihaz guncellenirken baglanti hatasi olustu.', 'error');
            }
        });

        manageSessionList?.addEventListener('click', async (event) => {
            const button = event.target instanceof Element
                ? event.target.closest('.js-manage-close-session')
                : null;
            if (!(button instanceof HTMLElement)) {
                return;
            }

            const sessionId = Number(button.dataset.sessionId || 0);
            if (sessionId <= 0) {
                return;
            }

            const confirmed = window.confirm('Bu POS oturumunu kapatmak istiyor musunuz?');
            if (!confirmed) {
                return;
            }

            const requestUrl = buildManageSessionRoute(sessionId);
            try {
                const response = await fetch(requestUrl, {
                    method: 'POST',
                    credentials: 'same-origin',
                    headers: {
                        'Accept': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest',
                        'X-CSRF-TOKEN': csrfToken,
                    },
                });
                const payload = await response.json();
                if (!response.ok || payload?.ok === false) {
                    showFeedback(payload?.message || 'POS oturumu kapatilamadi.', 'error');
                    return;
                }

                showFeedback(payload?.message || 'POS oturumu kapatildi.', 'success');
                renderManageOverview(payload?.data || {});
                if (payload?.data?.reload) {
                    window.location.reload();
                }
            } catch (_error) {
                showFeedback('POS oturumu kapatilirken baglanti hatasi olustu.', 'error');
            }
        });

        manageStaffForm?.addEventListener('submit', async (event) => {
            event.preventDefault();
            if (!canManagePersonnel) {
                showFeedback('Personel yonetimi icin yetkiniz yok.', 'error');
                return;
            }

            const email = String(manageStaffEmail?.value || '').trim();
            const role = String(manageStaffRole?.value || '').trim();
            if (email === '' || role === '') {
                showFeedback('Personel e-postasi ve rol zorunludur.', 'error');
                return;
            }

            try {
                const response = await fetch(manageStaffUpsertUrl, {
                    method: 'POST',
                    credentials: 'same-origin',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest',
                        'X-CSRF-TOKEN': csrfToken,
                    },
                    body: JSON.stringify({ email, role }),
                });
                const payload = await response.json();
                if (!response.ok || payload?.ok === false) {
                    showFeedback(payload?.message || 'Personel kaydedilemedi.', 'error');
                    return;
                }

                showFeedback(payload?.message || 'Personel kaydedildi.', 'success');
                if (manageStaffEmail instanceof HTMLInputElement) {
                    manageStaffEmail.value = '';
                }
                renderManageOverview(payload?.data || {});
            } catch (_error) {
                showFeedback('Personel kaydi sirasinda baglanti hatasi olustu.', 'error');
            }
        });

        manageStaffList?.addEventListener('click', async (event) => {
            const button = event.target instanceof Element
                ? event.target.closest('.js-manage-delete-staff')
                : null;
            if (!(button instanceof HTMLElement)) {
                return;
            }

            if (!canManagePersonnel) {
                showFeedback('Personel yonetimi icin yetkiniz yok.', 'error');
                return;
            }

            const staffRoleId = Number(button.dataset.staffRoleId || 0);
            if (staffRoleId <= 0) {
                return;
            }

            const confirmed = window.confirm('Bu personel kaydini kaldirmak istiyor musunuz?');
            if (!confirmed) {
                return;
            }

            const requestUrl = buildManageStaffDeleteRoute(staffRoleId);
            try {
                const response = await fetch(requestUrl, {
                    method: 'POST',
                    credentials: 'same-origin',
                    headers: {
                        'Accept': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest',
                        'X-CSRF-TOKEN': csrfToken,
                    },
                });
                const payload = await response.json();
                if (!response.ok || payload?.ok === false) {
                    showFeedback(payload?.message || 'Personel kaydi kaldirilamadi.', 'error');
                    return;
                }

                showFeedback(payload?.message || 'Personel kaydi kaldirildi.', 'success');
                renderManageOverview(payload?.data || {});
            } catch (_error) {
                showFeedback('Personel silinirken baglanti hatasi olustu.', 'error');
            }
        });

        if (input) {
            input.addEventListener('input', () => {
                fetchSuggestions(input.value);
            });

            input.addEventListener('focus', () => {
                if (hasActiveSession) {
                    setScannerState('ready');
                }
            });

            input.addEventListener('blur', () => {
                if (!hasActiveSession) {
                    return;
                }
                setScannerState('paused', 'Tarayici odagi geri aliyor...');
                setTimeout(keepScannerFocus, 20);
            });

            input.addEventListener('keydown', (event) => {
                if (event.key === 'Enter') {
                    if (suggestions.length > 0 && suggestionsBox?.style.display !== 'none' && activeSuggestionIndex >= 0) {
                        event.preventDefault();
                        pickSuggestion(activeSuggestionIndex, true);
                        return;
                    }

                    if (scanForm) {
                        event.preventDefault();
                        submitAction(scanForm);
                    }
                    return;
                }

                if (event.key === 'ArrowDown') {
                    if (suggestions.length === 0 || suggestionsBox?.style.display === 'none') {
                        return;
                    }
                    event.preventDefault();
                    activeSuggestionIndex = Math.min(activeSuggestionIndex + 1, suggestions.length - 1);
                    refreshActiveSuggestion();
                    return;
                }

                if (event.key === 'ArrowUp') {
                    if (suggestions.length === 0 || suggestionsBox?.style.display === 'none') {
                        return;
                    }
                    event.preventDefault();
                    activeSuggestionIndex = Math.max(activeSuggestionIndex - 1, 0);
                    refreshActiveSuggestion();
                    return;
                }

                if (event.key === 'Escape') {
                    hideSuggestions();
                }
            });
        }

        if (suggestionsBox) {
            suggestionsBox.addEventListener('click', (event) => {
                const button = event.target.closest('.barcode-suggestion-item');
                if (!(button instanceof HTMLElement)) {
                    return;
                }

                const index = Number(button.dataset.index ?? -1);
                if (index >= 0) {
                    pickSuggestion(index, true);
                }
            });
        }

        if (recentSalesList) {
            recentSalesList.addEventListener('click', (event) => {
                if (!(event.target instanceof Element)) {
                    return;
                }
                const card = event.target.closest('.recent-sale-item');
                if (!(card instanceof HTMLElement)) {
                    return;
                }
                const saleId = Number(card.dataset.saleId || 0);
                if (saleId > 0) {
                    openSaleReceiptModal(saleId);
                }
            });
        }

        saleReceiptCloseButton?.addEventListener('click', () => {
            closeSaleReceiptModal();
        });

        saleReceiptModal?.addEventListener('click', (event) => {
            if (event.target === saleReceiptModal) {
                closeSaleReceiptModal();
            }
        });

        saleReceiptPrint58Button?.addEventListener('click', () => {
            const url = String(receiptModalState?.print?.paper58PrintUrl || '');
            if (url !== '') {
                window.open(url, '_blank', 'noopener');
            }
        });

        saleReceiptPrint80Button?.addEventListener('click', () => {
            const url = String(receiptModalState?.print?.paper80PrintUrl || '');
            if (url !== '') {
                window.open(url, '_blank', 'noopener');
            }
        });

        saleReceiptPdfA4Button?.addEventListener('click', () => {
            const url = String(receiptModalState?.print?.a4PdfUrl || '');
            if (url !== '') {
                window.open(url, '_blank', 'noopener');
            }
        });

        saleReceiptEditToggleButton?.addEventListener('click', () => {
            if (!receiptModalState) {
                return;
            }
            setReceiptEditMode(!Boolean(receiptModalState.isEditing));
        });

        saleReceiptEditCancelButton?.addEventListener('click', () => {
            setReceiptEditMode(false);
        });

        saleReceiptTable?.addEventListener('input', (event) => {
            if (!receiptModalState?.isEditing) {
                return;
            }

            const target = event.target;
            if (!(target instanceof HTMLElement)) {
                return;
            }

            if (!target.classList.contains('receipt-edit-qty') && !target.classList.contains('receipt-edit-unit')) {
                return;
            }

            const row = target.closest('[data-receipt-item-id]');
            refreshReceiptRowLineTotal(row);
            refreshReceiptEditPreview();
        });

        saleReceiptTable?.addEventListener('change', (event) => {
            if (!receiptModalState?.isEditing) {
                return;
            }

            const target = event.target;
            if (!(target instanceof HTMLInputElement)) {
                return;
            }

            if (target.classList.contains('receipt-edit-qty')) {
                const quantity = Math.max(1, Math.round(Number(target.value || 1)));
                target.value = String(quantity);
            }

            if (target.classList.contains('receipt-edit-unit')) {
                const unitSalePriceKurus = parseTlTextToKurus(target.value || '');
                if (unitSalePriceKurus !== null) {
                    target.value = (unitSalePriceKurus / 100).toLocaleString('tr-TR', {
                        minimumFractionDigits: 2,
                        maximumFractionDigits: 2,
                    });
                }
            }

            const row = target.closest('[data-receipt-item-id]');
            refreshReceiptRowLineTotal(row);
            refreshReceiptEditPreview();
        });

        saleReceiptEditSaveButton?.addEventListener('click', async () => {
            if (!receiptModalState || !saleReceiptTable) {
                return;
            }

            const rows = Array.from(saleReceiptTable.querySelectorAll('[data-receipt-item-id]'));
            if (rows.length === 0) {
                showFeedback('Duzenlenecek satis satiri bulunamadi.', 'error');
                return;
            }

            const items = [];
            for (const row of rows) {
                const itemId = Number(row.getAttribute('data-receipt-item-id') || 0);
                const qtyInput = row.querySelector('.receipt-edit-qty');
                const unitInput = row.querySelector('.receipt-edit-unit');
                const quantity = Number((qtyInput?.value || '').trim());
                const unitSalePriceKurus = parseTlTextToKurus(unitInput?.value || '');

                if (itemId <= 0 || !Number.isFinite(quantity) || quantity < 1 || unitSalePriceKurus === null || unitSalePriceKurus < 1) {
                    showFeedback('Satis duzenleme alanlarinda gecersiz deger var.', 'error');
                    return;
                }

                items.push({
                    id: itemId,
                    quantity: Math.round(quantity),
                    unit_sale_price_kurus: unitSalePriceKurus,
                });
            }

            const updateUrl = buildReceiptRoute(receiptUpdateUrlTemplate, receiptModalState.saleId);
            try {
                const response = await fetch(updateUrl, {
                    method: 'POST',
                    credentials: 'same-origin',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest',
                        'X-CSRF-TOKEN': csrfToken,
                    },
                    body: JSON.stringify({
                        payment_method: String(saleReceiptPaymentMethodSelect?.value || 'cash'),
                        items,
                    }),
                });

                const payload = await response.json();
                if (!response.ok || payload?.ok === false) {
                    showFeedback(payload?.message || 'Satis fis kaydi guncellenemedi.', 'error');
                    return;
                }

                const updatedSale = payload?.data?.sale;
                if (updatedSale && typeof updatedSale === 'object') {
                    const previousEditableItems = resolveReceiptItems(receiptModalState.sale)
                        .filter((item) => Number(item.id || 0) > 0);
                    const previousById = new Map(previousEditableItems.map((item) => [Number(item.id), item]));
                    const nextEditableItems = items.map((editedItem) => {
                        const itemId = Number(editedItem.id || 0);
                        const previous = previousById.get(itemId) || {};
                        const unitSalePriceKurus = Math.max(0, Math.round(Number(editedItem.unit_sale_price_kurus || 0)));

                        return {
                            id: itemId,
                            barcode: String(previous.barcode || ''),
                            name: String(previous.name || '-'),
                            quantity: Math.max(1, Math.round(Number(editedItem.quantity || 1))),
                            unitSalePriceKurus,
                            unitSalePrice: (unitSalePriceKurus / 100).toLocaleString('tr-TR', {
                                minimumFractionDigits: 2,
                                maximumFractionDigits: 2,
                            }),
                        };
                    });

                    receiptModalState.sale = {
                        ...(receiptModalState.sale || {}),
                        ...updatedSale,
                        itemsEditable: nextEditableItems,
                    };
                }
                setReceiptEditMode(false);
                showFeedback(payload?.message || 'Satis fis kaydi guncellendi.', 'success');
                await requestSyncState({ force: true, showSwitchToast: false });
            } catch (_error) {
                showFeedback('Satis fis kaydi guncellenirken baglanti hatasi olustu.', 'error');
            }
        });

        saleReceiptDeleteButton?.addEventListener('click', async () => {
            if (!receiptModalState) {
                return;
            }

            const confirmed = window.confirm('Bu satis kaydi silinsin mi? Islem geri alinamaz.');
            if (!confirmed) {
                return;
            }

            const deleteUrl = buildReceiptRoute(receiptDeleteUrlTemplate, receiptModalState.saleId);
            try {
                const response = await fetch(deleteUrl, {
                    method: 'POST',
                    credentials: 'same-origin',
                    headers: {
                        'Accept': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest',
                        'X-CSRF-TOKEN': csrfToken,
                    },
                });

                const payload = await response.json();
                if (!response.ok || payload?.ok === false) {
                    showFeedback(payload?.message || 'Satis kaydi silinemedi.', 'error');
                    return;
                }

                closeSaleReceiptModal();
                showFeedback(payload?.message || 'Satis kaydi silindi.', 'success');
                await requestSyncState({ force: true, showSwitchToast: false });
            } catch (_error) {
                showFeedback('Satis kaydi silinirken baglanti hatasi olustu.', 'error');
            }
        });

        document.addEventListener('keydown', (event) => {
            if (!hasActiveSession || !input || !scanForm) {
                return;
            }

            if (event.defaultPrevented || isOverlayOpen()) {
                return;
            }

            const target = event.target;
            const targetIsInput = target === input;
            const targetIsEditable = isEditableElement(target);

            if (targetIsInput) {
                return;
            }

            if (targetIsEditable) {
                resetScannerBuffer();
                return;
            }

            if (event.ctrlKey || event.metaKey || event.altKey) {
                return;
            }

            if (event.key === 'Enter') {
                if (scannerBuffer.trim().length === 0) {
                    return;
                }

                event.preventDefault();
                input.value = scannerBuffer.trim();
                hideSuggestions();
                submitAction(scanForm);
                resetScannerBuffer();
                return;
            }

            if (event.key === 'Escape') {
                resetScannerBuffer();
                return;
            }

            if (event.key.length !== 1) {
                return;
            }

            const now = Date.now();
            if (scannerLastKeyAt > 0 && now - scannerLastKeyAt > 320) {
                scannerBuffer = '';
            }
            scannerLastKeyAt = now;
            scannerBuffer += event.key;

            if (scannerBuffer.length >= 3) {
                setScannerState('busy', `Tarama aliniyor (${scannerBuffer.length} karakter)...`);
            }
        });

        if (initialFeedbackJson?.textContent) {
            try {
                const initial = JSON.parse(initialFeedbackJson.textContent);
                if (initial?.success) {
                    showFeedback(String(initial.success), 'success');
                }
                if (initial?.error) {
                    showFeedback(String(initial.error), 'error');
                }
                if (Array.isArray(initial?.errors)) {
                    initial.errors.slice(0, 3).forEach((err) => showFeedback(String(err), 'error'));
                }
            } catch (_error) {
                // ignore malformed bootstrap feedback
            }
        }

        document.addEventListener('click', (event) => {
            if (!(event.target instanceof Element)) {
                return;
            }

            if (menuToggle && menuPanel) {
                const clickedMenuToggle = event.target.closest('#topbar-menu-toggle');
                const clickedInsideMenu = event.target.closest('#topbar-menu-panel');

                if (clickedMenuToggle) {
                    if (menuPanel.classList.contains('open')) {
                        closeMenu();
                    } else {
                        openMenu();
                    }
                    return;
                }

                if (!clickedInsideMenu) {
                    closeMenu();
                }
            }

            if (openCompanyProfileButton && event.target.closest('#open-company-profile')) {
                closeMenu();
                openCompanyProfileModal();
                return;
            }

            if (openReceiptProfileButton && event.target.closest('#open-receipt-profile')) {
                closeMenu();
                openReceiptProfileModal();
                return;
            }

            if (openSupportInboxButton && event.target.closest('#open-support-inbox')) {
                closeMenu();
                openSupportModal({ createMode: false });
                return;
            }

            if (openSupportCreateButton && event.target.closest('#open-support-create')) {
                closeMenu();
                openSupportModal({ createMode: true });
                return;
            }

            if (closeCompanyProfileButton && event.target.closest('#close-company-profile')) {
                closeCompanyProfileModal();
                return;
            }

            if (closeReceiptProfileButton && event.target.closest('#close-receipt-profile')) {
                closeReceiptProfileModal();
                return;
            }

            if (companyProfileModal && event.target === companyProfileModal) {
                closeCompanyProfileModal();
                return;
            }

            if (receiptProfileModal && event.target === receiptProfileModal) {
                closeReceiptProfileModal();
                return;
            }

            if (supportCloseButton && event.target.closest('#support-close')) {
                closeSupportModal();
                return;
            }

            if (supportModal && event.target === supportModal) {
                closeSupportModal();
                return;
            }

            if (event.target.closest('.input-with-suggestions')) {
                return;
            }

            hideSuggestions();
        });

        window.addEventListener('load', () => {
            if (hasActiveSession) {
                setScannerState('ready');
            } else {
                setScannerState('disabled');
            }
            focusInput();
            startSyncPolling();
            requestSyncState({ force: true, showSwitchToast: false });
        });
        if (branchSelect) {
            branchSelect.addEventListener('change', updateRegisterOptions);
        }
        document.addEventListener('visibilitychange', () => {
            if (document.visibilityState === 'visible') {
                if (hasActiveSession) {
                    setScannerState('ready');
                }
                startSyncPolling();
                requestSyncState({ force: true, showSwitchToast: true });
                keepScannerFocus();
                return;
            }

            if (hasActiveSession) {
                setScannerState('paused', 'Tarayici beklemede (sekme arka planda)');
            }
            stopSyncPolling();
        });
        document.addEventListener('mousedown', () => {
            setTimeout(keepScannerFocus, 30);
        });
        focusKeepAliveTimer = window.setInterval(() => {
            keepScannerFocus();
        }, 700);
        window.addEventListener('beforeunload', () => {
            stopSyncPolling();
            if (focusKeepAliveTimer !== null) {
                window.clearInterval(focusKeepAliveTimer);
            }
        });
        updateRegisterOptions();
        setTimeout(keepScannerFocus, 120);
    })();
</script>
</body>
</html>
