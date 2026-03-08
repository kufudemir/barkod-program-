<!doctype html>
<html lang="tr">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>KolayKasa | barkod.space</title>
    <style>
        :root {
            --bg: #070c16;
            --bg-soft: #0f172a;
            --card: #111b2e;
            --text: #f8fafc;
            --muted: #cbd5e1;
            --line: rgba(255, 255, 255, .16);
            --ok: #10b981;
            --work: #f59e0b;
            --soon: #64748b;
            --cta: #22c55e;
            --cta-text: #0f172a;
        }
        * { box-sizing: border-box; }
        body {
            margin: 0;
            font-family: "Segoe UI", "Inter", Arial, sans-serif;
            color: var(--text);
            background: radial-gradient(1200px 500px at 12% -5%, #1e293b 0%, #0b1220 48%, #070c16 100%);
        }
        .container { max-width: 1120px; margin: 0 auto; padding: 28px 18px 60px; }
        .top {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 14px;
            margin-bottom: 24px;
            flex-wrap: wrap;
        }
        .brand { font-size: 24px; font-weight: 800; letter-spacing: .3px; }
        .mini-nav { display: flex; gap: 10px; flex-wrap: wrap; }
        .mini-nav a {
            color: var(--muted);
            text-decoration: none;
            border: 1px solid var(--line);
            border-radius: 999px;
            padding: 8px 12px;
            font-size: 13px;
        }
        .hero {
            border: 1px solid var(--line);
            border-radius: 24px;
            background: rgba(10, 20, 35, .78);
            padding: 26px;
        }
        h1 { margin: 0; font-size: clamp(28px, 4.6vw, 48px); line-height: 1.15; }
        .subtitle {
            margin-top: 14px;
            color: var(--muted);
            font-size: 17px;
            line-height: 1.65;
            max-width: 920px;
        }
        .cta-grid {
            margin-top: 24px;
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
            gap: 12px;
        }
        .btn {
            display: inline-block;
            text-decoration: none;
            border-radius: 14px;
            padding: 13px 14px;
            text-align: center;
            font-weight: 700;
            border: 1px solid var(--line);
            color: var(--text);
            background: #0f1b30;
        }
        .btn-primary {
            background: linear-gradient(135deg, var(--cta), #34d399);
            color: var(--cta-text);
            border-color: transparent;
        }
        .section {
            margin-top: 18px;
            border: 1px solid var(--line);
            border-radius: 20px;
            background: rgba(10, 18, 32, .7);
            padding: 20px;
        }
        .section h2 { margin: 0 0 10px; font-size: 24px; }
        .section .sub { color: var(--muted); line-height: 1.7; margin: 0 0 14px; }
        .grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 10px;
        }
        .item {
            border: 1px solid var(--line);
            border-radius: 12px;
            padding: 11px 12px;
            background: var(--card);
            font-size: 14px;
            line-height: 1.6;
        }
        .state { font-weight: 700; font-size: 12px; letter-spacing: .2px; }
        .state-ok { color: var(--ok); }
        .state-work { color: var(--work); }
        .state-soon { color: var(--soon); }
        .faq-list {
            margin: 0;
            padding: 0;
            list-style: none;
            display: grid;
            gap: 10px;
        }
        .faq {
            border: 1px solid var(--line);
            border-radius: 12px;
            padding: 12px;
            background: #111827;
        }
        .faq-q { margin: 0 0 6px; font-weight: 700; }
        .faq-a { margin: 0; color: var(--muted); line-height: 1.7; }
        .foot {
            margin-top: 20px;
            display: grid;
            gap: 10px;
        }
        .foot-links {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }
        .foot-links a {
            color: var(--muted);
            text-decoration: none;
            border: 1px solid var(--line);
            border-radius: 999px;
            padding: 8px 12px;
            font-size: 13px;
        }
        .foot-note { color: #94a3b8; font-size: 13px; }
    </style>
</head>
<body>
<div class="container">
    <div class="top">
        <div class="brand">KolayKasa <span style="color:#94a3b8;">| barkod.space</span></div>
        <div class="mini-nav">
            <a href="/paketler">Paketler</a>
            <a href="/lisans-talebi">Lisans Talebi</a>
            <a href="/admin">Admin Girişi</a>
        </div>
    </div>

    <section class="hero">
        <h1>KolayKasa ile barkodlu satışa telefondan başlayın</h1>
        <p class="subtitle">
            Market, tekel, büfe ve küçük işletmeler için geliştirilen KolayKasa; barkod okutma,
            ürün yönetimi, stok takibi, raporlar ve bulut destekli senkron altyapısını tek uygulamada sunar.
        </p>

        <div class="cta-grid">
            <a class="btn btn-primary" href="/apk">APK İndir</a>
            <a class="btn" href="/kullanici-olustur">Kullanıcı Oluştur</a>
            <a class="btn" href="/misafir-basla">Misafir Olarak Başla</a>
            <a class="btn" href="/admin">Giriş Yap</a>
        </div>
    </section>

    <section class="section">
        <h2>Durum Özeti</h2>
        <p class="sub">Hazır olmayan özellikler “Hazır” etiketiyle gösterilmez. Her madde tek durum etiketi taşır.</p>
        <div class="grid">
            @foreach($heroStatusItems as $item)
                <div class="item">
                    <span class="state {{ $item['statusClass'] }}">{{ $item['statusLabel'] }}</span><br>
                    {{ $item['name'] }}
                </div>
            @endforeach
        </div>
    </section>

    <section class="section">
        <h2>Özellik Görünürlüğü</h2>
        <p class="sub">Ticari vitrin etiketleri V4 planına göre yönetilir.</p>
        <div class="grid">
            @foreach($featureHighlights as $item)
                <div class="item">
                    <span class="state {{ $item['statusClass'] }}">{{ $item['statusLabel'] }}</span><br>
                    {{ $item['name'] }}
                </div>
            @endforeach
        </div>
    </section>

    <section class="section">
        <h2>Sık Sorulan Sorular</h2>
        <ul class="faq-list">
            @foreach($faqItems as $faq)
                <li class="faq">
                    <p class="faq-q">{{ $faq['q'] }}</p>
                    <p class="faq-a">{{ $faq['a'] }}</p>
                </li>
            @endforeach
        </ul>
    </section>

    <footer class="foot">
        <div class="foot-links">
            <a href="/paketler">Paketler</a>
            <a href="/lisans-talebi">Lisans Başvurusu</a>
            <a href="/aydinlatma-metni">Aydınlatma Metni</a>
            <a href="/veri-kullanimi">Veri Kullanımı</a>
        </div>
        <div class="foot-note">Politika sürümü: {{ $version }}</div>
    </footer>
</div>
</body>
</html>
