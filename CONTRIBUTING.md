# Contributing

KolayKasa reposuna katki verirken asagidaki adimlari izle.

## 1) Issue ve kapsam
- Once issue ac veya mevcut issue sec.
- Kapsami netlestir: hangi dosyalar, hangi davranis degisikligi.

## 2) Branch stratejisi
- Yeni branch kullan:
  - `feature/<kisa-ad>`
  - `fix/<kisa-ad>`
  - `docs/<kisa-ad>`

## 3) Kod ve dokuman disiplini
- Ilgisiz dosyalara dokunma.
- Yeni davranis varsa ilgili dokumani guncelle.
- Mimarikurallara (`dokumanlar/proje-kurallari.md`) uy.

## 4) Test ve dogrulama
Mumkun olan kontrolleri calistir:
- Android unit test: `./gradlew :app:testDebugUnitTest`
- Web test: `cd web-application && php artisan test`
- Gerekiyorsa manuel rota/ekran dogrulamalari

## 5) Pull Request
PR acarken su basliklari yaz:
- Problem
- Cozum
- Etkilenen moduller
- Test kaniti
- Olasi riskler

## 6) Commit mesaji
Acik ve izlenebilir commit mesaji kullan:
- `feat: ...`
- `fix: ...`
- `docs: ...`
- `refactor: ...`
