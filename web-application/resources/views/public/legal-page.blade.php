<!doctype html>
<html lang="tr">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>{{ $title }} - barkod.space</title>
</head>
<body style="margin:0; background:#09090b; color:#f4f4f5; font-family:Arial,Helvetica,sans-serif;">
<div style="max-width:960px; margin:0 auto; padding:48px 20px;">
    <div style="display:flex; flex-direction:column; gap:20px;">
        <div style="display:flex; justify-content:space-between; gap:16px; align-items:flex-start; flex-wrap:wrap;">
            <div>
                <div style="font-size:16px; color:#a1a1aa; margin-bottom:10px;">barkod.space</div>
                <h1 style="margin:0; font-size:36px; line-height:1.2;">{{ $title }}</h1>
                <div style="margin-top:12px; color:#a1a1aa; font-size:16px; line-height:1.8;">
                    {{ $summary }}
                </div>
            </div>
            <a href="/" style="text-decoration:none; color:#09090b; background:#f59e0b; border-radius:14px; padding:14px 18px; font-weight:700; white-space:nowrap;">Ana Sayfa</a>
        </div>

        <div style="border:1px solid rgba(255,255,255,.10); background:#18181b; border-radius:22px; padding:24px;">
            <div style="display:flex; flex-direction:column; gap:16px; color:#e4e4e7; font-size:16px; line-height:1.9;">
                @foreach ($body as $paragraph)
                    <p style="margin:0;">{{ $paragraph }}</p>
                @endforeach
            </div>
        </div>

        <div style="color:#71717a; font-size:14px;">
            Politika sürümü: {{ $version }}
        </div>
    </div>
</div>
</body>
</html>
