<?php

namespace App\Http\Controllers;

use App\Services\LicenseRequestWorkflowService;
use Illuminate\Contracts\View\View;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\File;
use Illuminate\Support\Facades\URL;

class PublicLegalPagesController extends Controller
{
    private const STATUS_READY = 'ready';
    private const STATUS_ACTIVE = 'active';
    private const STATUS_SOON = 'soon';

    /**
     * @var array<string, string>
     */
    private const STATUS_LABELS = [
        self::STATUS_READY => 'Hazır',
        self::STATUS_ACTIVE => 'Aktif geliştiriliyor',
        self::STATUS_SOON => 'Yakında',
    ];

    /**
     * @var array<string, string>
     */
    private const STATUS_CLASSES = [
        self::STATUS_READY => 'state-ok',
        self::STATUS_ACTIVE => 'state-work',
        self::STATUS_SOON => 'state-soon',
    ];

    public function home(): View
    {
        $heroStatusItems = [
            ['name' => 'Android uygulama', 'status' => self::STATUS_READY],
            ['name' => 'Bulut senkron', 'status' => self::STATUS_READY],
            ['name' => 'Web yönetim paneli', 'status' => self::STATUS_READY],
            ['name' => 'Web POS', 'status' => self::STATUS_ACTIVE],
            ['name' => 'Gelişmiş fiş profilleri', 'status' => self::STATUS_SOON],
        ];
        $featureHighlights = [
            ['name' => 'Barkod okutma ve hızlı satış', 'status' => self::STATUS_READY],
            ['name' => 'Stok ve satış raporları', 'status' => self::STATUS_READY],
            ['name' => 'Firma bazlı bulut senkron', 'status' => self::STATUS_READY],
            ['name' => 'Paket ve lisans yönetimi', 'status' => self::STATUS_ACTIVE],
            ['name' => 'Tam web POS parity', 'status' => self::STATUS_ACTIVE],
            ['name' => 'Gelişmiş ticket merkezi', 'status' => self::STATUS_SOON],
        ];
        $faqItems = [
            [
                'q' => 'Uygulama internetsiz çalışır mı?',
                'a' => 'Evet. Mobil POS tarafı offline-first çalışır, internet geldiğinde senkron tamamlanır.',
            ],
            [
                'q' => 'Web POS şu an kullanılabilir mi?',
                'a' => 'Web POS çekirdeği aktif geliştirme aşamasında. Faz çıktıları tamamlandıkça kapsam artar.',
            ],
            [
                'q' => 'Lisans satın alma nasıl olacak?',
                'a' => 'İlk model banka transferi + admin onayıdır. Lisans firma bazlı tanımlanır.',
            ],
            [
                'q' => 'Hazır olmayan özellikleri nereden anlarım?',
                'a' => 'Landing sayfalarında her özellik "Hazır / Aktif geliştiriliyor / Yakında" etiketiyle gösterilir.',
            ],
        ];
        $this->assertStatusItemsAreValid($heroStatusItems);
        $this->assertStatusItemsAreValid($featureHighlights);

        return view('public.home', [
            'version' => config('legal.version'),
            'heroStatusItems' => $this->decorateStatusItems($heroStatusItems),
            'featureHighlights' => $this->decorateStatusItems($featureHighlights),
            'faqItems' => $faqItems,
        ]);
    }

    public function packages(): View
    {
        $packageCards = [
            [
                'title' => 'Ücretsiz',
                'subtitle' => 'Başlangıç',
                'status' => self::STATUS_READY,
                'features' => [
                    'Temel mobil POS akışı',
                    'Ürün ve sepet yönetimi',
                    'Misafir kullanım',
                    'Cihaz içi çalışma',
                ],
            ],
            [
                'title' => 'Gümüş',
                'subtitle' => 'Mobil Pro',
                'status' => self::STATUS_ACTIVE,
                'features' => [
                    'Kullanıcı hesabı ve bulut senkron',
                    'Raporlar, stok takibi, toplu güncelleme',
                    'OCR ve gelişmiş isim önerme',
                    'Ticket ve hesap kurtarma katmanı',
                ],
            ],
            [
                'title' => 'Altın',
                'subtitle' => 'Web POS',
                'status' => self::STATUS_SOON,
                'features' => [
                    'Web POS ve çoklu cihaz sekmeleri',
                    'HID barkod okuyucu desteği',
                    'Bekleyen satış ve ödeme türleri',
                    'Fiş profilleri ve yazdırma akışları',
                ],
            ],
        ];
        $this->assertStatusItemsAreValid($packageCards);

        return view('public.packages', [
            'packageCards' => $this->decorateStatusItems($packageCards),
        ]);
    }

    public function userCreateGuide(): View
    {
        return view('public.guide-user');
    }

    public function guestGuide(): View
    {
        return view('public.guide-guest');
    }

    public function licenseRequest(): View
    {
        return view('public.license-request', [
            'defaultPackage' => 'SILVER',
            'bankTransfer' => config('license.bank_transfer'),
        ]);
    }

    public function submitLicenseRequest(
        Request $request,
        LicenseRequestWorkflowService $workflow
    ): RedirectResponse {
        $payload = $request->validate([
            'requester_name' => ['required', 'string', 'max:255'],
            'requester_email' => ['required', 'email', 'max:255'],
            'requester_phone' => ['nullable', 'string', 'max:64'],
            'company_code' => ['nullable', 'string', 'max:64'],
            'requested_package_code' => ['required', 'string', 'in:SILVER,GOLD,PRO'],
            'bank_reference_note' => ['nullable', 'string', 'max:255'],
        ]);

        $licenseRequest = $workflow->createRequest(
            requesterName: $payload['requester_name'],
            requesterEmail: $payload['requester_email'],
            requesterPhone: $payload['requester_phone'] ?? null,
            requestedPackageCode: $payload['requested_package_code'],
            bankReferenceNote: $payload['bank_reference_note'] ?? null,
            companyCode: $payload['company_code'] ?? null,
            mobileUser: null,
        );

        return redirect()
            ->route('public.license-request')
            ->with('success', sprintf('Lisans talebiniz alındı. Talep No: #%d', $licenseRequest->id));
    }

    public function apk(): RedirectResponse
    {
        $latestJsonPath = public_path('app-updates/android/latest.json');

        if (! File::exists($latestJsonPath)) {
            return redirect('/app-updates/android/');
        }

        $payload = json_decode((string) File::get($latestJsonPath), true);
        $apkUrl = is_array($payload) ? ($payload['apkUrl'] ?? null) : null;

        if (! is_string($apkUrl) || trim($apkUrl) === '') {
            return redirect('/app-updates/android/');
        }

        if (str_starts_with($apkUrl, 'http://') || str_starts_with($apkUrl, 'https://')) {
            return redirect()->away($apkUrl);
        }

        return redirect(URL::to($apkUrl));
    }

    public function disclosure(): View
    {
        return $this->renderPage('disclosure');
    }

    public function dataUsage(): View
    {
        return $this->renderPage('data_usage');
    }

    private function renderPage(string $key): View
    {
        $page = config("legal.$key");

        abort_if(! is_array($page), 404);

        return view('public.legal-page', [
            'version' => config('legal.version'),
            'title' => $page['title'] ?? '',
            'summary' => $page['summary'] ?? '',
            'body' => $page['body'] ?? [],
        ]);
    }

    /**
     * @param list<array<string, mixed>> $items
     * @return list<array<string, mixed>>
     */
    private function decorateStatusItems(array $items): array
    {
        foreach ($items as $index => $item) {
            $status = (string) ($item['status'] ?? '');
            $items[$index]['statusLabel'] = self::STATUS_LABELS[$status] ?? self::STATUS_LABELS[self::STATUS_SOON];
            $items[$index]['statusClass'] = self::STATUS_CLASSES[$status] ?? self::STATUS_CLASSES[self::STATUS_SOON];
        }

        return $items;
    }

    /**
     * @param list<array<string, mixed>> $items
     */
    private function assertStatusItemsAreValid(array $items): void
    {
        $allowed = array_keys(self::STATUS_LABELS);

        foreach ($items as $item) {
            $status = (string) ($item['status'] ?? '');

            abort_unless(in_array($status, $allowed, true), 500, "Geçersiz durum etiketi: {$status}");
        }
    }
}
