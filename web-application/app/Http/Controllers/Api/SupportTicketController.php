<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Company;
use App\Models\FeedbackAttachment;
use App\Models\FeedbackMessage;
use App\Models\FeedbackReport;
use App\Models\MobileUser;
use App\Services\MobileUserTokenManager;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Storage;

class SupportTicketController extends Controller
{
    public function inbox(Request $request, MobileUserTokenManager $tokenManager): JsonResponse
    {
        $mobileUser = $tokenManager->findActiveUserByToken($request->bearerToken());
        if (!$mobileUser instanceof MobileUser) {
            return response()->json([
                'ok' => false,
                'message' => 'Giris oturumu bulunamadi.',
            ], 401);
        }

        $companyIds = $mobileUser->companies()->pluck('id');
        $status = trim((string) $request->query('status', ''));

        $query = FeedbackReport::query()
            ->with([
                'company:id,name,company_code',
                'messages' => fn ($messages) => $messages
                    ->where('is_internal_note', false)
                    ->orderByDesc('id')
                    ->limit(1),
            ])
            ->where(function ($q) use ($mobileUser, $companyIds): void {
                $q->where('mobile_user_id', $mobileUser->id);
                if ($companyIds->isNotEmpty()) {
                    $q->orWhereIn('company_id', $companyIds->all());
                }
            })
            ->orderByDesc('updated_at')
            ->limit(100);

        if ($status !== '' && in_array($status, ['new', 'reviewing', 'answered', 'closed'], true)) {
            $query->where('status', $status);
        }

        $tickets = $query->get()->map(function (FeedbackReport $report): array {
            $lastMessage = $report->messages->first();

            return [
                'ticketId' => (int) $report->id,
                'type' => (string) $report->type,
                'source' => (string) $report->source,
                'status' => (string) $report->status,
                'title' => (string) $report->title,
                'companyName' => (string) ($report->company?->name ?? 'Firma bagli degil'),
                'companyCode' => (string) ($report->company?->company_code ?? ''),
                'lastMessage' => (string) ($lastMessage?->message ?? ''),
                'lastMessageAt' => $lastMessage?->created_at?->valueOf(),
                'createdAt' => $report->created_at?->valueOf(),
                'updatedAt' => $report->updated_at?->valueOf(),
            ];
        })->values();

        return response()->json([
            'ok' => true,
            'tickets' => $tickets,
        ]);
    }

    public function create(Request $request, MobileUserTokenManager $tokenManager): JsonResponse
    {
        $mobileUser = $tokenManager->findActiveUserByToken($request->bearerToken());

        $payload = $request->validate([
            'type' => ['required', 'in:bug,feature_request,general'],
            'source' => ['required', 'in:mobile,web_pos'],
            'title' => ['required', 'string', 'min:3', 'max:191'],
            'description' => ['required', 'string', 'min:5', 'max:20000'],
            'companyCode' => ['nullable', 'string', 'max:64'],
            'deviceUid' => ['nullable', 'string', 'max:191'],
            'appVersion' => ['nullable', 'string', 'max:64'],
            'webUrl' => ['nullable', 'string', 'max:512'],
        ]);

        $companyCode = trim((string) ($payload['companyCode'] ?? ''));
        $deviceUid = trim((string) ($payload['deviceUid'] ?? ''));
        $company = $this->resolveCompanyForCreate($mobileUser, $companyCode);
        if ($companyCode !== '' && !$company instanceof Company) {
            return response()->json([
                'ok' => false,
                'message' => 'Firma kodu bulunamadi veya erisim yetkiniz yok.',
            ], 422);
        }

        if (!$mobileUser instanceof MobileUser && ($companyCode === '' || $deviceUid === '')) {
            return response()->json([
                'ok' => false,
                'message' => 'Misafir ticket icin firma kodu ve cihaz kimligi zorunludur.',
            ], 422);
        }

        /** @var FeedbackReport $report */
        $report = DB::transaction(function () use ($payload, $mobileUser, $company, $deviceUid): FeedbackReport {
            $report = FeedbackReport::query()->create([
                'type' => (string) $payload['type'],
                'source' => (string) $payload['source'],
                'company_id' => $company?->id,
                'mobile_user_id' => $mobileUser?->id,
                'device_uid' => $deviceUid !== '' ? $deviceUid : null,
                'app_version' => trim((string) ($payload['appVersion'] ?? '')) ?: null,
                'web_url' => trim((string) ($payload['webUrl'] ?? '')) ?: null,
                'title' => trim((string) $payload['title']),
                'description' => trim((string) $payload['description']),
                'status' => 'new',
            ]);

            FeedbackMessage::query()->create([
                'feedback_report_id' => $report->id,
                'author_type' => 'user',
                'author_id' => $mobileUser?->id,
                'message' => trim((string) $payload['description']),
                'is_internal_note' => false,
            ]);

            return $report;
        });

        return response()->json([
            'ok' => true,
            'message' => 'Ticket olusturuldu.',
            'data' => [
                'ticketId' => (int) $report->id,
                'status' => (string) $report->status,
                'createdAt' => $report->created_at?->valueOf(),
            ],
        ], 201);
    }

    public function show(
        Request $request,
        int $ticketId,
        MobileUserTokenManager $tokenManager,
    ): JsonResponse {
        $mobileUser = $tokenManager->findActiveUserByToken($request->bearerToken());
        if (!$mobileUser instanceof MobileUser) {
            return response()->json([
                'ok' => false,
                'message' => 'Giris oturumu bulunamadi.',
            ], 401);
        }

        $report = FeedbackReport::query()
            ->with([
                'company:id,name,company_code',
                'attachments:id,feedback_report_id,feedback_message_id,file_path,mime_type,created_at',
                'messages' => fn ($messages) => $messages
                    ->where('is_internal_note', false)
                    ->orderBy('id'),
            ])
            ->find($ticketId);

        if (!$report instanceof FeedbackReport || !$this->canAccessReport($mobileUser, $report)) {
            return response()->json([
                'ok' => false,
                'message' => 'Ticket bulunamadi veya erisim yetkiniz yok.',
            ], 404);
        }

        return response()->json([
            'ok' => true,
            'data' => $this->serializeTicketDetail($report),
        ]);
    }

    public function reply(
        Request $request,
        int $ticketId,
        MobileUserTokenManager $tokenManager,
    ): JsonResponse {
        $mobileUser = $tokenManager->findActiveUserByToken($request->bearerToken());
        if (!$mobileUser instanceof MobileUser) {
            return response()->json([
                'ok' => false,
                'message' => 'Giris oturumu bulunamadi.',
            ], 401);
        }

        $payload = $request->validate([
            'message' => ['required', 'string', 'min:2', 'max:20000'],
        ]);

        $report = FeedbackReport::query()->find($ticketId);
        if (!$report instanceof FeedbackReport || !$this->canAccessReport($mobileUser, $report)) {
            return response()->json([
                'ok' => false,
                'message' => 'Ticket bulunamadi veya erisim yetkiniz yok.',
            ], 404);
        }

        if ((string) $report->status === 'closed') {
            return response()->json([
                'ok' => false,
                'message' => 'Kapatilmis ticket icin once yeniden acma islemi yapin.',
            ], 422);
        }

        FeedbackMessage::query()->create([
            'feedback_report_id' => $report->id,
            'author_type' => 'user',
            'author_id' => $mobileUser->id,
            'message' => trim((string) $payload['message']),
            'is_internal_note' => false,
        ]);

        if ((string) $report->status !== 'new') {
            $report->status = 'reviewing';
        }
        $report->touch();
        $report->save();

        return response()->json([
            'ok' => true,
            'message' => 'Yanıt eklendi.',
        ]);
    }

    public function reopen(
        Request $request,
        int $ticketId,
        MobileUserTokenManager $tokenManager,
    ): JsonResponse {
        $mobileUser = $tokenManager->findActiveUserByToken($request->bearerToken());
        if (!$mobileUser instanceof MobileUser) {
            return response()->json([
                'ok' => false,
                'message' => 'Giris oturumu bulunamadi.',
            ], 401);
        }

        $report = FeedbackReport::query()->find($ticketId);
        if (!$report instanceof FeedbackReport || !$this->canAccessReport($mobileUser, $report)) {
            return response()->json([
                'ok' => false,
                'message' => 'Ticket bulunamadi veya erisim yetkiniz yok.',
            ], 404);
        }

        if ((string) $report->status !== 'closed') {
            return response()->json([
                'ok' => true,
                'message' => 'Ticket zaten acik.',
                'data' => [
                    'status' => (string) $report->status,
                ],
            ]);
        }

        $report->status = 'reviewing';
        $report->save();

        FeedbackMessage::query()->create([
            'feedback_report_id' => $report->id,
            'author_type' => 'user',
            'author_id' => $mobileUser->id,
            'message' => 'Ticket kullanici tarafindan yeniden acildi.',
            'is_internal_note' => false,
        ]);

        return response()->json([
            'ok' => true,
            'message' => 'Ticket yeniden acildi.',
            'data' => [
                'status' => (string) $report->status,
            ],
        ]);
    }

    public function attach(
        Request $request,
        int $ticketId,
        MobileUserTokenManager $tokenManager,
    ): JsonResponse {
        $mobileUser = $tokenManager->findActiveUserByToken($request->bearerToken());
        if (!$mobileUser instanceof MobileUser) {
            return response()->json([
                'ok' => false,
                'message' => 'Giris oturumu bulunamadi.',
            ], 401);
        }

        $report = FeedbackReport::query()->find($ticketId);
        if (!$report instanceof FeedbackReport || !$this->canAccessReport($mobileUser, $report)) {
            return response()->json([
                'ok' => false,
                'message' => 'Ticket bulunamadi veya erisim yetkiniz yok.',
            ], 404);
        }

        $payload = $request->validate([
            'file' => ['required', 'file', 'max:5120'],
            'messageId' => ['nullable', 'integer', 'min:1'],
        ]);

        $messageId = (int) ($payload['messageId'] ?? 0);
        if ($messageId > 0) {
            $messageBelongs = FeedbackMessage::query()
                ->where('id', $messageId)
                ->where('feedback_report_id', $report->id)
                ->exists();
            if (!$messageBelongs) {
                return response()->json([
                    'ok' => false,
                    'message' => 'Secilen mesaj ticket ile eslesmiyor.',
                ], 422);
            }
        }

        $file = $request->file('file');
        $storedPath = $file->store('support-attachments', 'public');

        $attachment = FeedbackAttachment::query()->create([
            'feedback_report_id' => $report->id,
            'feedback_message_id' => $messageId > 0 ? $messageId : null,
            'file_path' => (string) $storedPath,
            'mime_type' => $file->getClientMimeType(),
        ]);

        return response()->json([
            'ok' => true,
            'message' => 'Ek dosya yüklendi.',
            'data' => [
                'attachmentId' => (int) $attachment->id,
                'path' => (string) $attachment->file_path,
                'url' => Storage::disk('public')->url((string) $attachment->file_path),
                'mimeType' => (string) ($attachment->mime_type ?? ''),
                'createdAt' => $attachment->created_at?->valueOf(),
            ],
        ], 201);
    }

    private function resolveCompanyForCreate(?MobileUser $mobileUser, string $companyCode): ?Company
    {
        if ($companyCode !== '') {
            $company = Company::query()
                ->where('company_code', $companyCode)
                ->where('status', 'active')
                ->first();
            if (!$company instanceof Company) {
                return null;
            }

            if ($mobileUser instanceof MobileUser && (int) $company->owner_mobile_user_id !== (int) $mobileUser->id) {
                return null;
            }

            return $company;
        }

        if (!$mobileUser instanceof MobileUser) {
            return null;
        }

        return $mobileUser->companies()
            ->where('status', 'active')
            ->latest('updated_at')
            ->first();
    }

    private function canAccessReport(MobileUser $mobileUser, FeedbackReport $report): bool
    {
        if ((int) ($report->mobile_user_id ?? 0) === (int) $mobileUser->id) {
            return true;
        }

        $companyId = (int) ($report->company_id ?? 0);
        if ($companyId <= 0) {
            return false;
        }

        return $mobileUser->companies()
            ->where('id', $companyId)
            ->where('status', 'active')
            ->exists();
    }

    private function serializeTicketDetail(FeedbackReport $report): array
    {
        return [
            'ticketId' => (int) $report->id,
            'type' => (string) $report->type,
            'source' => (string) $report->source,
            'status' => (string) $report->status,
            'title' => (string) $report->title,
            'description' => (string) $report->description,
            'companyName' => (string) ($report->company?->name ?? 'Firma bagli degil'),
            'companyCode' => (string) ($report->company?->company_code ?? ''),
            'createdAt' => $report->created_at?->valueOf(),
            'updatedAt' => $report->updated_at?->valueOf(),
            'messages' => $report->messages
                ->where('is_internal_note', false)
                ->values()
                ->map(fn (FeedbackMessage $message): array => [
                    'messageId' => (int) $message->id,
                    'authorType' => (string) $message->author_type,
                    'authorId' => $message->author_id !== null ? (int) $message->author_id : null,
                    'message' => (string) $message->message,
                    'createdAt' => $message->created_at?->valueOf(),
                ]),
            'attachments' => $report->attachments
                ->values()
                ->map(fn (FeedbackAttachment $attachment): array => [
                    'attachmentId' => (int) $attachment->id,
                    'messageId' => $attachment->feedback_message_id !== null ? (int) $attachment->feedback_message_id : null,
                    'path' => (string) $attachment->file_path,
                    'url' => Storage::disk('public')->url((string) $attachment->file_path),
                    'mimeType' => (string) ($attachment->mime_type ?? ''),
                    'createdAt' => $attachment->created_at?->valueOf(),
                ]),
        ];
    }
}
