# Documentation Priority

Source summaries:
- `ai_docs/sources.md`
- `ai_docs/rules.md`

Use this file when documents disagree or when deciding what must be updated after a change.

## Priority order

1. `dokumanlar/uygulama-dokumanlari/program ozellikleri.md`
2. `dokumanlar/uygulama-dokumanlari/teknik-mimari.md`
3. `dokumanlar/uygulama-dokumanlari/room-entity-plani.md`
4. `dokumanlar/web-dokumanlari/web-veri-modeli.md`
5. `dokumanlar/web-dokumanlari/web-api-sozlesmesi.md`
6. `dokumanlar/uygulama-dokumanlari/gelistirme-fazlari-v4-web-pos-parity.md`
7. Other phase, changelog, and patch docs

## Update triggers

Update relevant docs when:

- an endpoint changes
- API request or response behavior changes
- data model behavior changes
- feature status materially changes
- release packaging or patch contents change

## AI docs usage

- Treat `ai_docs` as an operational memory layer, not the ultimate source of product truth
- Use `ai_docs` to accelerate navigation and summarize constraints
- If source docs and `ai_docs` diverge, reconcile `ai_docs`
