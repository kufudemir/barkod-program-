<!doctype html>
<html lang="tr">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Lisans Talebi | KolayKasa</title>
    <style>
        :root {
            --bg: #0f172a;
            --card: #111827;
            --line: rgba(255, 255, 255, .18);
            --text: #f8fafc;
            --muted: #cbd5e1;
            --work: #f59e0b;
            --btn: #2563eb;
            --ok-bg: rgba(16, 185, 129, .14);
            --ok-line: rgba(16, 185, 129, .5);
            --ok-text: #a7f3d0;
            --err-bg: rgba(239, 68, 68, .14);
            --err-line: rgba(239, 68, 68, .5);
            --err-text: #fecaca;
        }
        * { box-sizing: border-box; }
        body { margin: 0; font-family: "Segoe UI", "Inter", Arial, sans-serif; background: var(--bg); color: var(--text); }
        .wrap { max-width: 920px; margin: 0 auto; padding: 28px 16px 42px; }
        .top { display: flex; justify-content: space-between; align-items: center; gap: 10px; flex-wrap: wrap; margin-bottom: 14px; }
        .top a { color: #93c5fd; text-decoration: none; border: 1px solid var(--line); border-radius: 999px; padding: 8px 12px; font-size: 13px; }
        .box { border: 1px solid var(--line); border-radius: 16px; background: var(--card); padding: 18px; }
        h1 { margin: 0 0 10px; }
        p { color: var(--muted); line-height: 1.8; }
        .label {
            display: inline-block;
            margin-top: 4px;
            background: #1e293b;
            border: 1px solid var(--line);
            border-radius: 999px;
            padding: 6px 10px;
            font-size: 12px;
            font-weight: 700;
            color: var(--work);
        }
        .alert {
            margin-top: 14px;
            border-radius: 10px;
            padding: 10px 12px;
            font-size: 13px;
            line-height: 1.7;
        }
        .alert.success {
            border: 1px solid var(--ok-line);
            background: var(--ok-bg);
            color: var(--ok-text);
        }
        .alert.error {
            border: 1px solid var(--err-line);
            background: var(--err-bg);
            color: var(--err-text);
        }
        .grid {
            margin-top: 16px;
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 12px;
        }
        .field label {
            display: block;
            margin-bottom: 6px;
            font-size: 13px;
            color: #e2e8f0;
            font-weight: 700;
        }
        .field input, .field select, .field textarea {
            width: 100%;
            border: 1px solid var(--line);
            border-radius: 10px;
            background: #0f172a;
            color: var(--text);
            padding: 10px 12px;
            font-size: 14px;
            outline: none;
        }
        .field textarea { min-height: 88px; resize: vertical; }
        .actions {
            margin-top: 14px;
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
            align-items: center;
        }
        button {
            border: none;
            border-radius: 10px;
            background: var(--btn);
            color: #fff;
            padding: 10px 14px;
            font-weight: 700;
            cursor: pointer;
        }
        .note { color: #94a3b8; font-size: 13px; line-height: 1.7; }
        .bank {
            margin-top: 12px;
            border: 1px dashed var(--line);
            border-radius: 10px;
            padding: 10px;
            color: #cbd5e1;
            font-size: 13px;
            line-height: 1.8;
        }
        .bank div strong { color: #f8fafc; }
    </style>
</head>
<body>
<div class="wrap">
    <div class="top">
        <a href="/">Ana Sayfa</a>
        <a href="/paketler">Paketleri Gör</a>
    </div>

    <div class="box">
        <h1>Lisans Talebi</h1>
        <p>
            Lisans talebinizi bu formdan oluşturabilirsiniz. İlk aşamada süreç banka transferi ve admin onayı ile ilerler.
        </p>
        <div class="label">Aktif geliştiriliyor</div>

        @if(session('success'))
            <div class="alert success">{{ session('success') }}</div>
        @endif

        @if($errors->any())
            <div class="alert error">
                @foreach($errors->all() as $error)
                    <div>{{ $error }}</div>
                @endforeach
            </div>
        @endif

        <form method="post" action="{{ route('public.license-request.submit') }}">
            @csrf
            <div class="grid">
                <div class="field">
                    <label for="requester_name">Ad Soyad</label>
                    <input id="requester_name" name="requester_name" type="text" value="{{ old('requester_name') }}" placeholder="Örn: Ufuk Demir" required>
                </div>
                <div class="field">
                    <label for="requester_email">E-posta</label>
                    <input id="requester_email" name="requester_email" type="email" value="{{ old('requester_email') }}" placeholder="ornek@firma.com" required>
                </div>
                <div class="field">
                    <label for="requester_phone">Telefon</label>
                    <input id="requester_phone" name="requester_phone" type="text" value="{{ old('requester_phone') }}" placeholder="05xx xxx xx xx">
                </div>
                <div class="field">
                    <label for="requested_package_code">Talep Edilen Paket</label>
                    <select id="requested_package_code" name="requested_package_code" required>
                        @php $selectedPackage = old('requested_package_code', $defaultPackage ?? 'SILVER'); @endphp
                        <option value="SILVER" {{ $selectedPackage === 'SILVER' ? 'selected' : '' }}>Gümüş (Mobil Pro)</option>
                        <option value="GOLD" {{ $selectedPackage === 'GOLD' ? 'selected' : '' }}>Altın (Web POS)</option>
                    </select>
                </div>
                <div class="field">
                    <label for="company_code">Firma Kodu (Opsiyonel)</label>
                    <input id="company_code" name="company_code" type="text" value="{{ old('company_code') }}" placeholder="Örn: BALYAN-001">
                </div>
            </div>
            <div class="field" style="margin-top:12px;">
                <label for="bank_reference_note">Banka Referans Notu (Opsiyonel)</label>
                <textarea id="bank_reference_note" name="bank_reference_note" placeholder="Ödeme sonrası referans notunu buraya girebilirsiniz.">{{ old('bank_reference_note') }}</textarea>
            </div>

            <div class="bank">
                <div><strong>Hesap Adı:</strong> {{ data_get($bankTransfer, 'account_name', '-') }}</div>
                <div><strong>IBAN:</strong> {{ data_get($bankTransfer, 'iban', '-') }}</div>
                <div><strong>Banka:</strong> {{ data_get($bankTransfer, 'bank_name', '-') }}</div>
                <div><strong>Açıklama Önerisi:</strong> {{ data_get($bankTransfer, 'description_hint', '-') }}</div>
            </div>

            <div class="actions">
                <button type="submit">Talep Gönder</button>
                <span class="note">Talebiniz alındığında admin panelinde inceleme akışına düşer.</span>
            </div>
        </form>
    </div>
</div>
</body>
</html>
