<x-filament-panels::page>
    <div style="display:flex; flex-direction:column; gap:24px;">
        <x-filament::section>
            <x-slot name="heading">Satış Başlat</x-slot>
            <x-slot name="description">
                Firma seçin, barkodu okutun veya yapıştırın. Barkod okuyucu klavye gibi çalışıyorsa giriş alanına odaklanıp Enter göndermesi yeterlidir.
            </x-slot>

            <div style="display:grid; grid-template-columns:minmax(320px, 0.8fr) minmax(320px, 1.2fr); gap:16px; align-items:end;">
                <div style="display:flex; flex-direction:column; gap:8px;">
                    <label style="font-size:14px; font-weight:600; color:#e5e7eb;">Firma</label>
                    <select wire:model.live="selectedCompanyId" style="width:100%; border-radius:16px; border:1px solid rgba(156,163,175,.50); background:rgba(255,255,255,.06); padding:14px 16px; color:#fff; font-size:16px; line-height:1.5; outline:none;">
                        <option value="">Firma seçin</option>
                        @foreach ($this->companies as $company)
                            <option value="{{ $company['id'] }}">{{ $company['name'] }} / {{ $company['companyCode'] }}</option>
                        @endforeach
                    </select>
                </div>

                <form wire:submit="scanBarcode" style="display:flex; flex-direction:column; gap:8px;">
                    <label style="font-size:14px; font-weight:600; color:#e5e7eb;">Barkod</label>
                    <div style="display:flex; gap:12px; align-items:center;">
                        <input
                            type="text"
                            wire:model.defer="barcodeInput"
                            placeholder="Barkodu okutun veya yazın"
                            style="flex:1; border-radius:16px; border:1px solid rgba(156,163,175,.50); background:rgba(255,255,255,.06); padding:14px 16px; color:#fff; font-size:16px; line-height:1.5; outline:none;"
                        >
                        <x-filament::button type="submit">
                            Sepete Ekle
                        </x-filament::button>
                    </div>
                </form>
            </div>
        </x-filament::section>

        <div style="display:grid; grid-template-columns:minmax(0, 1.6fr) minmax(320px, 0.8fr); gap:24px; align-items:start;">
            <x-filament::section>
                <x-slot name="heading">Sepet</x-slot>
                <x-slot name="description">
                    Satır bazında fiyat, yüzde indirim ve TL indirimi uygulanabilir.
                </x-slot>

                <div style="display:flex; flex-direction:column; gap:16px;">
                    @forelse ($this->cartItems as $item)
                        <div style="border:1px solid rgba(255,255,255,.10); background:rgba(255,255,255,.03); border-radius:16px; padding:18px; display:flex; flex-direction:column; gap:16px;">
                            <div style="display:flex; justify-content:space-between; gap:16px; align-items:flex-start; flex-wrap:wrap;">
                                <div style="display:flex; flex-direction:column; gap:6px; min-width:220px;">
                                    <div style="font-size:18px; font-weight:700; color:#fff; line-height:1.5;">{{ $item['productName'] }}</div>
                                    <div style="font-size:13px; color:#9ca3af; line-height:1.7;">Barkod: {{ $item['barcode'] }}</div>
                                    @if (! empty($item['groupName']))
                                        <div style="font-size:13px; color:#9ca3af; line-height:1.7;">Grup: {{ $item['groupName'] }}</div>
                                    @endif
                                </div>
                                <div style="display:grid; grid-template-columns:repeat(2, minmax(120px, 1fr)); gap:12px; min-width:260px;">
                                    <div style="border:1px solid rgba(255,255,255,.10); border-radius:14px; padding:12px; background:rgba(255,255,255,.02);">
                                        <div style="font-size:12px; color:#9ca3af; margin-bottom:6px;">Birim fiyat</div>
                                        <div style="font-size:20px; font-weight:700; color:#fff; line-height:1.4;">{{ $this->formatMoney($item['salePriceKurus']) }}</div>
                                        @if ($item['hasCustomPrice'])
                                            <div style="font-size:12px; color:#fde68a; margin-top:6px;">Liste dışı fiyat</div>
                                        @endif
                                    </div>
                                    <div style="border:1px solid rgba(255,255,255,.10); border-radius:14px; padding:12px; background:rgba(255,255,255,.02);">
                                        <div style="font-size:12px; color:#9ca3af; margin-bottom:6px;">Satır toplamı</div>
                                        <div style="font-size:20px; font-weight:700; color:#fff; line-height:1.4;">{{ $this->formatMoney($item['lineTotalKurus']) }}</div>
                                    </div>
                                </div>
                            </div>

                            <div style="display:flex; flex-wrap:wrap; gap:10px; align-items:center;">
                                <span style="font-size:14px; font-weight:600; color:#e5e7eb; margin-right:4px;">Adet</span>
                                <x-filament::button size="sm" color="gray" wire:click="decrementQuantity('{{ $item['barcode'] }}')">-</x-filament::button>
                                <span style="min-width:32px; text-align:center; font-size:16px; font-weight:700; color:#fff;">{{ $item['quantity'] }}</span>
                                <x-filament::button size="sm" color="gray" wire:click="incrementQuantity('{{ $item['barcode'] }}')">+</x-filament::button>
                                <x-filament::button size="sm" color="danger" wire:click="removeItem('{{ $item['barcode'] }}')">Kaldır</x-filament::button>
                            </div>

                            <div style="display:grid; grid-template-columns:repeat(auto-fit, minmax(200px, 1fr)); gap:12px; align-items:end;">
                                <div style="display:flex; flex-direction:column; gap:8px;">
                                    <label style="font-size:13px; color:#d1d5db; font-weight:600;">Özel fiyat (TL)</label>
                                    <div style="display:flex; gap:8px;">
                                        <input type="text" wire:model.defer="customPriceInputs.{{ $item['barcode'] }}" style="flex:1; border-radius:14px; border:1px solid rgba(156,163,175,.50); background:rgba(255,255,255,.06); padding:12px 14px; color:#fff; font-size:15px; line-height:1.5; outline:none;">
                                        <x-filament::button size="sm" wire:click="applyCustomPrice('{{ $item['barcode'] }}')">Fiyat</x-filament::button>
                                    </div>
                                </div>
                                <div style="display:flex; flex-direction:column; gap:8px;">
                                    <label style="font-size:13px; color:#d1d5db; font-weight:600;">Yüzde indirim</label>
                                    <div style="display:flex; gap:8px;">
                                        <input type="text" wire:model.defer="percentInputs.{{ $item['barcode'] }}" placeholder="10" style="flex:1; border-radius:14px; border:1px solid rgba(156,163,175,.50); background:rgba(255,255,255,.06); padding:12px 14px; color:#fff; font-size:15px; line-height:1.5; outline:none;">
                                        <x-filament::button size="sm" color="warning" wire:click="applyPercentDiscount('{{ $item['barcode'] }}')">% Uygula</x-filament::button>
                                    </div>
                                </div>
                                <div style="display:flex; flex-direction:column; gap:8px;">
                                    <label style="font-size:13px; color:#d1d5db; font-weight:600;">TL indirim</label>
                                    <div style="display:flex; gap:8px;">
                                        <input type="text" wire:model.defer="fixedDiscountInputs.{{ $item['barcode'] }}" placeholder="25" style="flex:1; border-radius:14px; border:1px solid rgba(156,163,175,.50); background:rgba(255,255,255,.06); padding:12px 14px; color:#fff; font-size:15px; line-height:1.5; outline:none;">
                                        <x-filament::button size="sm" color="warning" wire:click="applyFixedDiscount('{{ $item['barcode'] }}')">TL Uygula</x-filament::button>
                                    </div>
                                </div>
                                <div>
                                    <x-filament::button size="sm" color="gray" wire:click="resetItemPrice('{{ $item['barcode'] }}')">
                                        Liste fiyatına dön
                                    </x-filament::button>
                                </div>
                            </div>
                        </div>
                    @empty
                        <div style="border:1px dashed rgba(255,255,255,.12); border-radius:16px; padding:28px; color:#9ca3af; font-size:15px; line-height:1.9; text-align:center;">
                            Henüz sepette ürün yok. Firma seçip barkod okutun.
                        </div>
                    @endforelse
                </div>
            </x-filament::section>

            <div style="display:flex; flex-direction:column; gap:24px;">
                <x-filament::section>
                    <x-slot name="heading">Satış Özeti</x-slot>

                    <div style="display:flex; flex-direction:column; gap:14px;">
                        <div style="display:flex; justify-content:space-between; gap:16px; font-size:15px; line-height:1.8; color:#e5e7eb;"><span>Toplam ürün</span><strong style="color:#fff;">{{ $this->cartSummary['items'] ?? 0 }}</strong></div>
                        <div style="display:flex; justify-content:space-between; gap:16px; font-size:15px; line-height:1.8; color:#e5e7eb;"><span>Toplam tutar</span><strong style="color:#fff;">{{ $this->formatMoney($this->cartSummary['totalAmountKurus'] ?? 0) }}</strong></div>
                        <div style="display:flex; justify-content:space-between; gap:16px; font-size:15px; line-height:1.8; color:#e5e7eb;"><span>Toplam maliyet</span><strong style="color:#fff;">{{ $this->formatMoney($this->cartSummary['totalCostKurus'] ?? 0) }}</strong></div>
                        <div style="display:flex; justify-content:space-between; gap:16px; font-size:15px; line-height:1.8; color:#e5e7eb;"><span>Kâr</span><strong style="color:#fff;">{{ $this->formatMoney($this->cartSummary['profitKurus'] ?? 0) }}</strong></div>
                    </div>

                    <div style="margin-top:20px;">
                        @if (! empty($this->cartItems))
                            <x-filament::button wire:click="completeSale" style="width:100%; justify-content:center;">
                                Satışı Tamamla
                            </x-filament::button>
                        @else
                            <button type="button" disabled style="width:100%; justify-content:center; display:inline-flex; align-items:center; border-radius:12px; padding:12px 18px; background:rgba(107,114,128,.25); color:#9ca3af; border:1px solid rgba(156,163,175,.18); cursor:not-allowed; font-weight:600;">
                                Satışı Tamamla
                            </button>
                        @endif
                    </div>
                </x-filament::section>

                <x-filament::section>
                    <x-slot name="heading">Son Web Satışları</x-slot>
                    <x-slot name="description">
                        Seçili firmadaki son satışlar burada görünür.
                    </x-slot>

                    <div style="display:flex; flex-direction:column; gap:14px;">
                        @forelse ($this->recentSales as $sale)
                            <div style="border:1px solid rgba(255,255,255,.10); background:rgba(255,255,255,.03); border-radius:16px; padding:16px; display:flex; flex-direction:column; gap:10px;">
                                <div style="display:flex; justify-content:space-between; gap:12px; flex-wrap:wrap; align-items:flex-start;">
                                    <div>
                                        <div style="font-size:16px; font-weight:700; color:#fff; line-height:1.5;">Satış #{{ $sale['id'] }}</div>
                                        <div style="font-size:13px; color:#9ca3af; line-height:1.8;">{{ $sale['completedAt'] }}</div>
                                    </div>
                                    <div style="text-align:right; min-width:140px;">
                                        <div style="font-size:16px; font-weight:700; color:#fff; line-height:1.5;">{{ $sale['total'] }}</div>
                                        <div style="font-size:13px; color:#9ca3af; line-height:1.8;">Kâr: {{ $sale['profit'] }}</div>
                                    </div>
                                </div>
                                <div style="font-size:13px; color:#d1d5db; line-height:1.8;">Ürün sayısı: {{ $sale['items'] }}</div>
                                <div style="font-size:13px; color:#9ca3af; line-height:1.8;">
                                    {{ implode(' / ', array_slice($sale['lines'], 0, 4)) }}
                                </div>
                            </div>
                        @empty
                            <div style="border:1px dashed rgba(255,255,255,.12); border-radius:16px; padding:24px; color:#9ca3af; font-size:14px; line-height:1.9; text-align:center;">
                                Seçili firma için henüz web satışı yok.
                            </div>
                        @endforelse
                    </div>
                </x-filament::section>
            </div>
        </div>
    </div>
</x-filament-panels::page>