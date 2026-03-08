<!doctype html>
<html lang="tr">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>{{ $appName }} Kurulum</title>
    <style>
        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f5f1e8;
            color: #1f2937;
        }
        .wrapper {
            max-width: 760px;
            margin: 40px auto;
            padding: 0 16px;
        }
        .card {
            background: #ffffff;
            border: 1px solid #d6d3d1;
            border-radius: 18px;
            padding: 24px;
            box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
        }
        h1 {
            margin: 0 0 12px;
            font-size: 28px;
        }
        p, li { line-height: 1.55; }
        .muted { color: #57534e; }
        .notice, .error, .success {
            border-radius: 12px;
            padding: 14px 16px;
            margin: 16px 0;
        }
        .notice { background: #fef3c7; color: #92400e; }
        .error { background: #fee2e2; color: #991b1b; }
        .success { background: #dcfce7; color: #166534; }
        .grid {
            display: grid;
            gap: 14px;
            grid-template-columns: 1fr 1fr;
        }
        .field {
            display: flex;
            flex-direction: column;
            gap: 6px;
        }
        .field.full { grid-column: 1 / -1; }
        label {
            font-weight: 700;
            font-size: 14px;
        }
        input, select {
            padding: 12px 14px;
            border: 1px solid #cbd5e1;
            border-radius: 10px;
            font-size: 15px;
            background: #fff;
            color: #1f2937;
        }
        button {
            width: 100%;
            border: 0;
            border-radius: 12px;
            background: #111827;
            color: #ffffff;
            padding: 14px 16px;
            font-size: 16px;
            font-weight: 700;
            cursor: pointer;
        }
        pre {
            white-space: pre-wrap;
            word-break: break-word;
            background: #0f172a;
            color: #e2e8f0;
            border-radius: 12px;
            padding: 14px;
            font-size: 13px;
        }
        @media (max-width: 640px) {
            .grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
<div class="wrapper">
    <div class="card">
        <h1>{{ $appName }} Kurulum</h1>
        <p class="muted">
            Bu ekran terminal erişimi olmayan sunucularda migration ve cache temizleme işlemlerini
            güvenli şekilde çalıştırmak için kullanılır.
        </p>

        <div class="notice">
            Bu ekran sadece gizli kurulum anahtarı ile açılır.
            İşlem tamamlandıktan sonra <strong>APP_SETUP_ENABLED=false</strong> yapın.
        </div>

        @if (session('setup_success'))
            <div class="success">İşlem başarıyla tamamlandı.</div>
        @endif

        @if ($locked)
            <div class="success">
                Kurulum daha önce tamamlanmış. Bu ekrandan sadece bekleyen migration ve cache temizleme çalıştırabilirsiniz.
            </div>
        @endif

        @if (session('setup_error'))
            <div class="error">{{ session('setup_error') }}</div>
        @endif

        @if ($errors->any())
            <div class="error">
                <ul>
                    @foreach ($errors->all() as $error)
                        <li>{{ $error }}</li>
                    @endforeach
                </ul>
            </div>
        @endif

        @if (session('artisan_output'))
            <pre>{{ session('artisan_output') }}</pre>
        @endif

        <form method="post" action="{{ route('setup.run') }}">
            @csrf
            <div class="grid">
                <div class="field full">
                    <label for="secret">Kurulum Anahtarı</label>
                    <input id="secret" name="secret" type="text" value="{{ old('secret', $secret) }}" autocomplete="off" required>
                </div>

                <div class="field full">
                    <label for="action">İşlem Türü</label>
                    <select id="action" name="action" required>
                        <option value="migrate_cache_clear" {{ old('action', 'migrate_cache_clear') === 'migrate_cache_clear' ? 'selected' : '' }}>
                            Migration + Cache Temizle (Önerilen)
                        </option>
                        <option value="migrate_only" {{ old('action') === 'migrate_only' ? 'selected' : '' }}>
                            Sadece Migration
                        </option>
                        <option value="cache_clear_only" {{ old('action') === 'cache_clear_only' ? 'selected' : '' }}>
                            Sadece Cache Temizle
                        </option>
                    </select>
                </div>

                @if (! $locked)
                    <div class="field">
                        <label for="name">Admin Adı</label>
                        <input id="name" name="name" type="text" value="{{ old('name', 'Admin') }}" required>
                    </div>

                    <div class="field">
                        <label for="email">Admin E-Posta</label>
                        <input id="email" name="email" type="email" value="{{ old('email', 'admin@barkod.space') }}" required>
                    </div>

                    <div class="field full">
                        <label for="password">Admin Şifresi</label>
                        <input id="password" name="password" type="password" required>
                    </div>
                @endif
            </div>

            <div style="margin-top: 18px;">
                <button type="submit">İşlemi Çalıştır</button>
            </div>
        </form>
    </div>
</div>
</body>
</html>
