<!doctype html>
<html lang="tr">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Web POS Giriş | KolayKasa</title>
    <style>
        :root {
            --bg: #0b1020;
            --card: #121a30;
            --line: rgba(148, 163, 184, .25);
            --text: #f8fafc;
            --muted: #cbd5e1;
            --primary: #0ea5e9;
            --danger: #ef4444;
        }
        * { box-sizing: border-box; }
        body {
            margin: 0;
            min-height: 100vh;
            display: grid;
            place-items: center;
            background: radial-gradient(circle at 20% 15%, #1e3a8a 0%, transparent 35%), radial-gradient(circle at 80% 10%, #0f766e 0%, transparent 32%), var(--bg);
            font-family: "Segoe UI", "Inter", Arial, sans-serif;
            color: var(--text);
            padding: 16px;
        }
        .card {
            width: min(520px, 100%);
            border: 1px solid var(--line);
            border-radius: 20px;
            background: rgba(18, 26, 48, .92);
            padding: 22px;
            box-shadow: 0 18px 40px rgba(0, 0, 0, .25);
        }
        h1 { margin: 0 0 8px; font-size: 28px; }
        p { margin: 0 0 18px; color: var(--muted); line-height: 1.7; }
        .field { margin-bottom: 12px; }
        .field label { display: block; margin-bottom: 6px; font-size: 13px; font-weight: 700; color: #dbeafe; }
        .field input {
            width: 100%;
            border: 1px solid var(--line);
            border-radius: 12px;
            background: rgba(15, 23, 42, .75);
            color: var(--text);
            padding: 11px 12px;
            outline: none;
        }
        .field input:focus { border-color: rgba(14, 165, 233, .7); box-shadow: 0 0 0 2px rgba(14, 165, 233, .15); }
        .btn {
            width: 100%;
            border: none;
            border-radius: 12px;
            background: linear-gradient(90deg, #0284c7, #0ea5e9);
            color: #fff;
            padding: 12px;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
            margin-top: 4px;
        }
        .error {
            border: 1px solid rgba(239, 68, 68, .55);
            background: rgba(239, 68, 68, .12);
            border-radius: 12px;
            padding: 10px 12px;
            margin-bottom: 12px;
            color: #fecaca;
            font-size: 13px;
            line-height: 1.7;
        }
        .sub {
            margin-top: 14px;
            border: 1px dashed rgba(148, 163, 184, .35);
            border-radius: 12px;
            padding: 10px 12px;
            color: #cbd5e1;
            font-size: 13px;
            line-height: 1.7;
        }
        .chips { margin-top: 8px; display: flex; flex-wrap: wrap; gap: 8px; }
        .chip {
            border: 1px solid rgba(148, 163, 184, .25);
            border-radius: 999px;
            padding: 5px 10px;
            font-size: 12px;
            color: #e2e8f0;
            background: rgba(255, 255, 255, .04);
        }
    </style>
</head>
<body>
<div class="card">
    <h1>Web POS Giriş</h1>
    <p>
        Bu ekran admin panelinden bağımsızdır. Giriş yaptıktan sonra satış shell ekranı açılır.
    </p>

    @if($errors->any())
        <div class="error">
            @foreach($errors->all() as $error)
                <div>{{ $error }}</div>
            @endforeach
        </div>
    @endif
    @if(!empty($reasonMessage))
        <div class="error">
            <div>{{ $reasonMessage }}</div>
        </div>
    @endif

    <form method="post" action="{{ route('pos.login.submit', [], false) }}">
        @csrf
        <div class="field">
            <label for="email">E-posta</label>
            <input id="email" name="email" type="email" value="{{ old('email') }}" placeholder="ornek@firma.com" required>
        </div>
        <div class="field">
            <label for="password">Şifre</label>
            <input id="password" name="password" type="password" placeholder="********" required>
        </div>
        <div class="field">
            <label for="company_code">Firma Kodu (Opsiyonel)</label>
            <input id="company_code" name="company_code" type="text" value="{{ old('company_code') }}" placeholder="Örn: BALYAN-001">
        </div>
        <button class="btn" type="submit">POS'a Giriş Yap</button>
    </form>

    @if(!empty($ownedCompanies))
        <div class="sub">
            Bu hesapla ilişkili firmalar:
            <div class="chips">
                @foreach($ownedCompanies as $company)
                    <span class="chip">{{ $company['name'] }} / {{ $company['companyCode'] }} · {{ $company['roleLabel'] ?? '-' }}</span>
                @endforeach
            </div>
        </div>
    @endif
</div>
</body>
</html>
