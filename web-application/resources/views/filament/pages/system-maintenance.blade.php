<x-filament-panels::page>
    <div style="display: flex; flex-direction: column; gap: 24px;">
        <x-filament::section>
            <x-slot name="heading">Sistem Sıfırlama</x-slot>
            <x-slot name="description">
                Bu işlem sistemin çalışmasını bozmaz ancak içerik verilerini temizler.
            </x-slot>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px;">
                <div style="border: 1px solid rgba(255,255,255,.10); background: rgba(255,255,255,.03); border-radius: 16px; padding: 20px;"><div style="font-size:14px; color:#9ca3af; margin-bottom:8px;">Mobil kullanıcı</div><div style="font-size:32px; line-height:1.3; font-weight:700; color:#fff;">{{ $this->summary['mobile_users'] ?? 0 }}</div></div>
                <div style="border: 1px solid rgba(255,255,255,.10); background: rgba(255,255,255,.03); border-radius: 16px; padding: 20px;"><div style="font-size:14px; color:#9ca3af; margin-bottom:8px;">Personel rol kaydı</div><div style="font-size:32px; line-height:1.3; font-weight:700; color:#fff;">{{ $this->summary['company_staff_roles'] ?? 0 }}</div></div>
                <div style="border: 1px solid rgba(255,255,255,.10); background: rgba(255,255,255,.03); border-radius: 16px; padding: 20px;"><div style="font-size:14px; color:#9ca3af; margin-bottom:8px;">Firma</div><div style="font-size:32px; line-height:1.3; font-weight:700; color:#fff;">{{ $this->summary['companies'] ?? 0 }}</div></div>
                <div style="border: 1px solid rgba(255,255,255,.10); background: rgba(255,255,255,.03); border-radius: 16px; padding: 20px;"><div style="font-size:14px; color:#9ca3af; margin-bottom:8px;">Cihaz</div><div style="font-size:32px; line-height:1.3; font-weight:700; color:#fff;">{{ $this->summary['devices'] ?? 0 }}</div></div>
                <div style="border: 1px solid rgba(255,255,255,.10); background: rgba(255,255,255,.03); border-radius: 16px; padding: 20px;"><div style="font-size:14px; color:#9ca3af; margin-bottom:8px;">Global barkod</div><div style="font-size:32px; line-height:1.3; font-weight:700; color:#fff;">{{ $this->summary['global_products'] ?? 0 }}</div></div>
                <div style="border: 1px solid rgba(255,255,255,.10); background: rgba(255,255,255,.03); border-radius: 16px; padding: 20px;"><div style="font-size:14px; color:#9ca3af; margin-bottom:8px;">Firma fiyat kaydı</div><div style="font-size:32px; line-height:1.3; font-weight:700; color:#fff;">{{ $this->summary['company_product_offers'] ?? 0 }}</div></div>
                <div style="border: 1px solid rgba(255,255,255,.10); background: rgba(255,255,255,.03); border-radius: 16px; padding: 20px;"><div style="font-size:14px; color:#9ca3af; margin-bottom:8px;">Senkron kaydı</div><div style="font-size:32px; line-height:1.3; font-weight:700; color:#fff;">{{ $this->summary['sync_batches'] ?? 0 }}</div></div>
                <div style="border: 1px solid rgba(255,255,255,.10); background: rgba(255,255,255,.03); border-radius: 16px; padding: 20px;"><div style="font-size:14px; color:#9ca3af; margin-bottom:8px;">Web satışı</div><div style="font-size:32px; line-height:1.3; font-weight:700; color:#fff;">{{ $this->summary['web_sales'] ?? 0 }}</div></div>
            </div>
        </x-filament::section>

        <x-filament::section>
            <x-slot name="heading">Tehlikeli İşlem</x-slot>

            <div style="display: flex; flex-direction: column; gap: 20px;">
                <div style="border: 1px solid rgba(239,68,68,.30); background: rgba(239,68,68,.10); border-radius: 16px; padding: 20px; color: #fee2e2; font-size: 15px; line-height: 1.9;">
                    <div style="font-weight: 700; margin-bottom: 10px;">Bu işlem neleri temizler?</div>
                    <div>Firmalar ve bağlı cihazlar</div>
                    <div>Global barkod kataloğu ve isim adayları</div>
                    <div>Firma bazlı fiyat kayıtları</div>
                    <div>Personel rol kayıtları</div>
                    <div>Mobil kullanıcılar, erişim tokenları ve şifre sıfırlama kodları</div>
                    <div>Senkron kayıtları ve tekrar kontrol kayıtları</div>
                    <div>Web satışları ve web satış satırları</div>
                    <div style="margin-top: 12px; font-weight: 700;">Admin panel kullanıcıları, migration kayıtları ve uygulama dosyaları korunur.</div>
                    <div style="margin-top: 8px;">Sıfırlama sonrası mobil kullanıcıların uygulamada yeniden giriş yapıp firmayı tekrar aktive etmesi gerekir.</div>
                </div>

                <form wire:submit="resetSystem" style="display: flex; flex-direction: column; gap: 14px; max-width: 760px;">
                    <label style="font-size: 15px; font-weight: 600; color: #e5e7eb; line-height: 1.8;">
                        Devam etmek için <span style="color: #fff; font-weight: 700;">tüm sistemi sıfırla</span> yazın
                    </label>
                    <textarea
                        wire:model.defer="confirmationText"
                        rows="3"
                        placeholder="tüm sistemi sıfırla"
                        style="width: 100%; border-radius: 16px; border: 1px solid rgba(156,163,175,.50); background: rgba(255,255,255,.04); padding: 14px 16px; color: #fff; font-size: 16px; line-height: 1.7; outline: none;"
                    ></textarea>

                    <div>
                        <x-filament::button type="submit" color="danger">
                            Sistemi Sıfırla
                        </x-filament::button>
                    </div>
                </form>
            </div>
        </x-filament::section>
    </div>
</x-filament-panels::page>
