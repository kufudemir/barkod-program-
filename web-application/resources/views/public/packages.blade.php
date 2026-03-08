<!doctype html>
<html lang="tr">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Paketler | KolayKasa</title>
    <style>
        :root {
            --bg: #0b1220;
            --card: #111b2e;
            --text: #f8fafc;
            --muted: #cbd5e1;
            --line: rgba(255, 255, 255, .18);
            --ok: #10b981;
            --work: #f59e0b;
            --soon: #64748b;
        }
        * { box-sizing: border-box; }
        body { margin: 0; font-family: "Segoe UI", "Inter", Arial, sans-serif; color: var(--text); background: var(--bg); }
        .container { max-width: 1080px; margin: 0 auto; padding: 28px 18px 48px; }
        .top { display: flex; justify-content: space-between; align-items: center; gap: 12px; margin-bottom: 20px; flex-wrap: wrap; }
        .top a { color: var(--muted); text-decoration: none; border: 1px solid var(--line); padding: 8px 12px; border-radius: 999px; font-size: 13px; }
        h1 { margin: 0; font-size: 34px; }
        .sub { margin-top: 10px; color: var(--muted); line-height: 1.7; }
        .grid {
            margin-top: 24px;
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
            gap: 14px;
        }
        .card { border: 1px solid var(--line); border-radius: 16px; background: var(--card); padding: 16px; }
        .name { font-size: 22px; font-weight: 800; }
        .sub-name { margin-top: 4px; color: #94a3b8; font-size: 13px; font-weight: 700; }
        .tag { margin-top: 10px; font-size: 12px; font-weight: 700; letter-spacing: .3px; }
        .state-ok { color: var(--ok); }
        .state-work { color: var(--work); }
        .state-soon { color: var(--soon); }
        ul { margin: 14px 0 0; padding-left: 18px; color: var(--muted); line-height: 1.7; }
        .rule {
            margin-top: 18px;
            padding: 12px;
            border-radius: 12px;
            border: 1px solid var(--line);
            color: #cbd5e1;
            background: #0f172a;
            font-size: 13px;
            line-height: 1.7;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="top">
        <h1>Paketler</h1>
        <div style="display:flex; gap:10px; flex-wrap:wrap;">
            <a href="/lisans-talebi">Lisans Talebi</a>
            <a href="/">Ana Sayfa</a>
        </div>
    </div>

    <p class="sub">
        Paketler V4 planına göre faz bazlı açılır. Hazır olmayan alanlar “Hazır” olarak işaretlenmez.
    </p>

    <div class="grid">
        @foreach($packageCards as $card)
            <div class="card">
                <div class="name">{{ $card['title'] }}</div>
                <div class="sub-name">{{ $card['subtitle'] }}</div>
                <div class="tag {{ $card['statusClass'] }}">{{ $card['statusLabel'] }}</div>
                <ul>
                    @foreach($card['features'] as $feature)
                        <li>{{ $feature }}</li>
                    @endforeach
                </ul>
            </div>
        @endforeach
    </div>

    <div class="rule">
        Etiket politikası: <strong>Hazır</strong> yalnız aktif üretim kullanımı içindir.
        Geliştirilen alanlar <strong>Aktif geliştiriliyor</strong>, faza alınmamış alanlar
        <strong>Yakında</strong> olarak gösterilir.
    </div>
</div>
</body>
</html>
