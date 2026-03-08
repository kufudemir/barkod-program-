<x-filament-panels::page>
    <div style="display: flex; flex-direction: column; gap: 24px;">
        <x-filament::section>
            <x-slot name="heading">Etkin Olmayan Firma Temizliği</x-slot>
            <x-slot name="description">
                Misafir kaynaklı ve uzun süre hareket görmeyen firma kayıtlarını temizlemek için kullanılır.
            </x-slot>

            <form wire:submit="save" style="display: flex; flex-direction: column; gap: 20px;">
                <div style="display: grid; grid-template-columns: minmax(320px, 1.3fr) minmax(240px, 0.7fr); gap: 16px; align-items: start;">
                    <div style="border: 1px solid rgba(255,255,255,.10); background: rgba(255,255,255,.03); border-radius: 16px; padding: 20px;">
                        <label style="display: flex; align-items: flex-start; gap: 12px; color: #fff; line-height: 1.8;">
                            <input type="checkbox" wire:model="cleanupEnabled" style="margin-top: 4px; width: 18px; height: 18px;">
                            <span>
                                <span style="display: block; font-size: 15px; font-weight: 700;">Etkin olmayan firma silme aktif</span>
                                <span style="display: block; margin-top: 6px; font-size: 14px; color: #d1d5db;">
                                    Açık olduğunda sistem, belirlediğiniz gün sınırını geçen sahipsiz misafir firmaları temizlemeye uygun hale getirir.
                                </span>
                            </span>
                        </label>

                        <div style="margin-top: 22px; display: flex; flex-direction: column; gap: 8px;">
                            <label style="font-size: 14px; font-weight: 600; color: #e5e7eb;">Eşik gün</label>
                            <input
                                type="number"
                                min="1"
                                max="365"
                                wire:model="cleanupDays"
                                wire:change="refreshPreview"
                                style="width: 160px; border-radius: 14px; border: 1px solid rgba(156,163,175,.50); background: rgba(255,255,255,.04); padding: 12px 14px; color: #fff; font-size: 16px; line-height: 1.5; outline: none;"
                            >
                            <p style="font-size: 13px; line-height: 1.8; color: #9ca3af; margin: 0;">Örnek: 30 gün boyunca hiç hareket olmayan uygun firmalar önizlemeye dahil edilir.</p>
                        </div>
                    </div>

                    <div style="display: grid; gap: 16px;">
                        <div style="border: 1px solid rgba(255,255,255,.10); background: rgba(255,255,255,.03); border-radius: 16px; padding: 20px;">
                            <div style="font-size: 14px; color: #9ca3af; margin-bottom: 8px;">Temizlenecek firma</div>
                            <div style="font-size: 32px; line-height: 1.3; font-weight: 700; color: #fff;">{{ $this->previewCount }}</div>
                            <div style="margin-top: 8px; font-size: 13px; line-height: 1.8; color: #9ca3af;">Şu anki ayara göre temizlenmeye aday kayıt sayısı</div>
                        </div>

                        <div style="border: 1px solid rgba(255,255,255,.10); background: rgba(255,255,255,.03); border-radius: 16px; padding: 20px;">
                            <div style="font-size: 14px; color: #9ca3af; margin-bottom: 8px;">Son çalışma</div>
                            <div style="font-size: 24px; line-height: 1.4; font-weight: 700; color: #fff;">{{ $this->lastRunAt ?? '-' }}</div>
                            <div style="margin-top: 8px; font-size: 13px; line-height: 1.8; color: #9ca3af;">Elle veya otomatik tetiklenen son temizleme zamanı</div>
                        </div>
                    </div>
                </div>

                <div style="border: 1px solid rgba(245,158,11,.30); background: rgba(245,158,11,.10); border-radius: 16px; padding: 18px; color: #fef3c7; font-size: 14px; line-height: 1.9;">
                    Bu araç sadece misafir kaynaklı, sahipsiz ve eşik süre boyunca hareket görmemiş firmaları temizler.
                    Kayıtlı kullanıcıya bağlı veya yönetici tarafından açılmış firmalar korunur.
                </div>

                <div style="display: flex; flex-wrap: wrap; gap: 12px;">
                    <x-filament::button type="button" color="gray" wire:click="refreshPreview">
                        Önizlemeyi Yenile
                    </x-filament::button>
                    <x-filament::button type="button" color="danger" wire:click="runCleanupNow">
                        Şimdi Temizle
                    </x-filament::button>
                    <x-filament::button type="submit">
                        Ayarları Kaydet
                    </x-filament::button>
                </div>
            </form>
        </x-filament::section>
    </div>
</x-filament-panels::page>
