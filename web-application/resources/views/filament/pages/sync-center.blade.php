<x-filament-panels::page>
    <div style="display: flex; flex-direction: column; gap: 24px;">
        <x-filament::section>
            <x-slot name="heading">Senkron Özeti</x-slot>
            <x-slot name="description">
                Firma ve cihaz bazında genel senkron durumu burada gösterilir.
            </x-slot>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px;">
                <div style="border: 1px solid rgba(255,255,255,.10); background: rgba(255,255,255,.03); border-radius: 16px; padding: 20px;">
                    <div style="font-size: 14px; color: #9ca3af; margin-bottom: 8px;">Aktif cihaz</div>
                    <div style="font-size: 32px; font-weight: 700; color: #fff; line-height: 1.2;">{{ $this->summary['activeDeviceCount'] ?? 0 }}</div>
                </div>
                <div style="border: 1px solid rgba(255,255,255,.10); background: rgba(255,255,255,.03); border-radius: 16px; padding: 20px;">
                    <div style="font-size: 14px; color: #9ca3af; margin-bottom: 8px;">Bugün senkron yapan firma</div>
                    <div style="font-size: 32px; font-weight: 700; color: #fff; line-height: 1.2;">{{ $this->summary['companiesSyncedToday'] ?? 0 }}</div>
                </div>
                <div style="border: 1px solid rgba(239,68,68,.30); background: rgba(239,68,68,.10); border-radius: 16px; padding: 20px;">
                    <div style="font-size: 14px; color: #fecaca; margin-bottom: 8px;">Hatalı cihaz</div>
                    <div style="font-size: 32px; font-weight: 700; color: #fff; line-height: 1.2;">{{ $this->summary['latestFailedDeviceCount'] ?? 0 }}</div>
                </div>
                <div style="border: 1px solid rgba(245,158,11,.30); background: rgba(245,158,11,.10); border-radius: 16px; padding: 20px;">
                    <div style="font-size: 14px; color: #fde68a; margin-bottom: 8px;">Bekleyen senkron</div>
                    <div style="font-size: 32px; font-weight: 700; color: #fff; line-height: 1.2;">{{ $this->summary['pendingDeviceCount'] ?? 0 }}</div>
                </div>
            </div>
        </x-filament::section>

        <x-filament::section>
            <x-slot name="heading">Firma ve Cihaz Durumları</x-slot>
            <x-slot name="description">
                En güncel batch bilgisine göre son durum listelenir.
            </x-slot>

            <div style="overflow-x: auto; border: 1px solid rgba(255,255,255,.10); border-radius: 16px;">
                <table style="width: 100%; border-collapse: collapse; min-width: 980px;">
                    <thead style="background: rgba(255,255,255,.03);">
                        <tr>
                            <th style="padding: 16px; text-align: left; font-size: 14px; color: #d1d5db;">Firma</th>
                            <th style="padding: 16px; text-align: left; font-size: 14px; color: #d1d5db;">Cihaz</th>
                            <th style="padding: 16px; text-align: left; font-size: 14px; color: #d1d5db;">Durum</th>
                            <th style="padding: 16px; text-align: left; font-size: 14px; color: #d1d5db;">Son başarılı</th>
                            <th style="padding: 16px; text-align: left; font-size: 14px; color: #d1d5db;">Son batch</th>
                            <th style="padding: 16px; text-align: left; font-size: 14px; color: #d1d5db;">Kayıt</th>
                            <th style="padding: 16px; text-align: left; font-size: 14px; color: #d1d5db;">Son hata</th>
                            <th style="padding: 16px; text-align: left; font-size: 14px; color: #d1d5db;">İşlem</th>
                        </tr>
                    </thead>
                    <tbody>
                        @forelse ($this->rows as $row)
                            @php
                                $statusStyle = match ($row['statusColor']) {
                                    'success' => 'border:1px solid rgba(34,197,94,.30); background: rgba(34,197,94,.10); color:#bbf7d0;',
                                    'warning' => 'border:1px solid rgba(245,158,11,.30); background: rgba(245,158,11,.10); color:#fde68a;',
                                    'danger' => 'border:1px solid rgba(239,68,68,.30); background: rgba(239,68,68,.10); color:#fecaca;',
                                    default => 'border:1px solid rgba(255,255,255,.10); background: rgba(255,255,255,.03); color:#d1d5db;',
                                };
                            @endphp
                            <tr style="border-top: 1px solid rgba(255,255,255,.08); vertical-align: top;">
                                <td style="padding: 18px 16px; color: #fff; font-weight: 600; line-height: 1.6;">{{ $row['companyName'] }}</td>
                                <td style="padding: 18px 16px; color: #e5e7eb; line-height: 1.6;">{{ $row['deviceName'] }}</td>
                                <td style="padding: 18px 16px; line-height: 1.8;">
                                    <span style="display:inline-block; padding: 5px 12px; border-radius: 999px; font-size: 12px; font-weight: 700; {{ $statusStyle }}">
                                        {{ $row['statusLabel'] }}
                                    </span>
                                    <div style="margin-top: 8px; font-size: 12px; color: #9ca3af;">{{ $row['isActive'] ? 'Aktif cihaz' : 'Pasif cihaz' }}</div>
                                </td>
                                <td style="padding: 18px 16px; color: #fff; line-height: 1.6;">{{ $row['lastSuccessfulSync'] }}</td>
                                <td style="padding: 18px 16px; color: #fff; line-height: 1.6;">{{ $row['lastBatchAt'] }}</td>
                                <td style="padding: 18px 16px; color: #fff; line-height: 1.6;">{{ $row['receivedCount'] }} / {{ $row['processedCount'] }} / {{ $row['pendingCount'] }}</td>
                                <td style="padding: 18px 16px; line-height: 1.6;">
                                    @if ($row['lastError'] !== 'Yok')
                                        <span style="color: #fecaca;">{{ $row['lastError'] }}</span>
                                    @else
                                        <span style="color: #9ca3af;">Yok</span>
                                    @endif
                                </td>
                                <td style="padding: 18px 16px;">
                                    <div style="display: flex; flex-wrap: wrap; gap: 8px;">
                                        @if ($row['companyUrl'])
                                            <a href="{{ $row['companyUrl'] }}" style="display:inline-flex; align-items:center; padding:8px 12px; border-radius: 12px; border:1px solid rgba(255,255,255,.10); background: rgba(255,255,255,.04); color:#fff; font-size:12px; font-weight:600; text-decoration:none;">Firma</a>
                                        @endif
                                        <a href="{{ $row['deviceUrl'] }}" style="display:inline-flex; align-items:center; padding:8px 12px; border-radius: 12px; border:1px solid rgba(255,255,255,.10); background: rgba(255,255,255,.04); color:#fff; font-size:12px; font-weight:600; text-decoration:none;">Cihaz</a>
                                        @if ($row['batchUrl'])
                                            <a href="{{ $row['batchUrl'] }}" style="display:inline-flex; align-items:center; padding:8px 12px; border-radius: 12px; border:1px solid rgba(245,158,11,.30); background: rgba(245,158,11,.10); color:#fde68a; font-size:12px; font-weight:600; text-decoration:none;">Batch</a>
                                        @endif
                                    </div>
                                </td>
                            </tr>
                        @empty
                            <tr>
                                <td colspan="8" style="padding: 28px 16px; text-align: center; color: #9ca3af;">Henüz senkron özeti gösterecek cihaz bulunmuyor.</td>
                            </tr>
                        @endforelse
                    </tbody>
                </table>
            </div>
        </x-filament::section>
    </div>
</x-filament-panels::page>
