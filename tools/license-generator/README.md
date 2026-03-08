# License Generator

Bu arac `Market POS` icin cihaz bazli premium lisans uretir.
Yeni lisanslar varsayilan olarak daha kisa `ECDSA-P256` imzasi ile uretilir.
Eski `RSA` lisanslari uygulama tarafinda gecerliligini korur.

## 1) Anahtar Olustur

```powershell
cd "C:\Users\Ufuk\Desktop\barkod programı"
powershell -ExecutionPolicy Bypass -File .\tools\license-generator\init-license-keys.ps1
```

Bu komut:
- RSA private key dosyasini `C:\Users\<kullanici>\.marketpos-license\private_key.xml` altina yazar
- ECDSA private key dosyasini `C:\Users\<kullanici>\.marketpos-license\ec_private_key.json` altina yazar
- public key dosyasini uygulamaya ve `tools/license-generator/public_key.json` altina yazar

## 2) Lisans Uret

```powershell
cd "C:\Users\Ufuk\Desktop\barkod programı"
powershell -ExecutionPolicy Bypass -File .\tools\license-generator\generate-license.ps1 -DeviceCode "ABCD-EF12-3456-7890-1234"
```

Sureli lisans icin:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\license-generator\generate-license.ps1 -DeviceCode "ABCD-EF12-3456-7890-1234" -DaysValid 30
```

## Not

- `private_key.xml` dosyasini kimseyle paylasmayin.
- `ec_private_key.json` dosyasini da kimseyle paylasmayin.
- Bu dosyalar sizde kaldigi surece yeni premium lisans uretebilirsiniz.
- Uygulama lisans kodundaki bosluk ve satir sonlarini temizleyerek kabul eder.
