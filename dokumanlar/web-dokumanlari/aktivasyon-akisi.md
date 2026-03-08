# Aktivasyon Akışı

1. Uygulama açılır.
2. Oturum seçimi yapılır veya mevcut oturum yüklenir.
3. Firma aktivasyonu yoksa aktivasyon ekranı açılır.
4. Kullanıcı firma ünvanını girer veya geçmiş firmalardan birini seçer.
5. Uygulama `deviceUid`, `deviceName` ve varsa kullanıcı tokeni ile API’ye istek gönderir.
6. Sunucu:
- aynı cihazın daha önce bağlı olduğu firmaları kontrol eder,
- farklı firma denemesinde kullanıcıyı uyarır,
- gerekirse yeni firma oluşturur,
- tek aktif cihaz kuralını uygular,
- aktivasyon tokeni üretir.
7. Aktivasyon tamamlanınca uygulama tarama ekranına geçer.
8. Bulutta katalog varsa geri yükleme seçeneği gösterilir.
