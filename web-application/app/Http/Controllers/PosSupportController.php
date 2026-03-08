<?php

namespace App\Http\Controllers;

use App\Models\FeedbackMessage;
use App\Models\FeedbackReport;
use App\Models\MobileUser;
use App\Services\PosAuthManager;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class PosSupportController extends Controller
{
    public function inbox(Request $request, PosAuthManager $authManager): JsonResponse
    {
        [$mobileUser, $company, $error] = $this->resolveAuthContext($request, $authManager);
        if ($error instanceof JsonResponse) {
            return $error;
        }

        $status = trim((string) $request->query('status', ''));
        $allowedStatuses = ['new', 'reviewing', 'answered', 'closed'];

        $query = FeedbackReport::query()
            ->with([
                'messages' => fn ($messages) => $messages
                    ->where('is_internal_note', false)
                    ->orderByDesc('id')
                    ->limit(1),
            ])
            ->where(function ($q) use ($mobileUser, $company): void {
                $q->where('mobile_user_id', $mobileUser->id)
                    ->orWhere('company_id', $company->id);
            })
            ->orderByDesc('updated_at')
            ->limit(100);

        if ($status !== '' && in_array($status, $allowedStatuses, true)) {
            $query->where('status', $status);
        }

        $tickets = $query->get()->map(fn (FeedbackReport $report): array => $this->serializeTicketSummary($report))->values();

        return response()->json([
            'ok' => true,
            'tickets' => $tickets,
        ]);
    }

    public function show(
        Request $request,
        int $ticketId,
        PosAuthManager $authManager,
    ): JsonResponse {
        [$mobileUser, $company, $error] = $this->resolveAuthContext($request, $authManager);
        if ($error instanceof JsonResponse) {
            return $error;
        }

        $report = FeedbackReport::query()
            ->with([
                'messages' => fn ($messages) => $messages
                    ->where('is_internal_note', false)
                    ->orderBy('id'),
            ])
            ->find($ticketId);

        if (!$report instanceof FeedbackReport || !$this->canAccess($report, $mobileUser, (int) $company->id)) {
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

    public function create(Request $request, PosAuthManager $authManager): JsonResponse
    {
        [$mobileUser, $company, $error] = $this->resolveAuthContext($request, $authManager);
        if ($error instanceof JsonResponse) {
            return $error;
        }

        $payload = $request->validate([
            'type' => ['required', 'in:bug,feature_request,general'],
            'title' => ['required', 'string', 'min:3', 'max:191'],
            'description' => ['required', 'string', 'min:5', 'max:20000'],
        ]);

        /** @var FeedbackReport $report */
        $report = DB::transaction(function () use ($payload, $mobileUser, $company, $request): FeedbackReport {
            $report = FeedbackReport::query()->create([
                'type' => (string) $payload['type'],
                'source' => 'web_pos',
                'company_id' => $company->id,
                'mobile_user_id' => $mobileUser->id,
                'device_uid' => null,
                'app_version' => null,
                'web_url' => $request->fullUrl(),
                'title' => trim((string) $payload['title']),
                'description' => trim((string) $payload['description']),
                'status' => 'new',
            ]);

            FeedbackMessage::query()->create([
                'feedback_report_id' => $report->id,
                'author_type' => 'user',
                'author_id' => $mobileUser->id,
                'message' => trim((string) $payload['description']),
                'is_internal_note' => false,
            ]);

            return $report;
        });

        $report->load([
            'messages' => fn ($messages) => $messages
                ->where('is_internal_note', false)
                ->orderBy('id'),
        ]);

        return response()->json([
            'ok' => true,
            'message' => 'Ticket olusturuldu.',
            'data' => $this->serializeTicketDetail($report),
        ], 201);
    }

    public function reply(
        Request $request,
        int $ticketId,
        PosAuthManager $authManager,
    ): JsonResponse {
        [$mobileUser, $company, $error] = $this->resolveAuthContext($request, $authManager);
        if ($error instanceof JsonResponse) {
            return $error;
        }

        $payload = $request->validate([
            'message' => ['required', 'string', 'min:2', 'max:20000'],
        ]);

        $report = FeedbackReport::query()->find($ticketId);
        if (!$report instanceof FeedbackReport || !$this->canAccess($report, $mobileUser, (int) $company->id)) {
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

        $report->load([
            'messages' => fn ($messages) => $messages
                ->where('is_internal_note', false)
                ->orderBy('id'),
        ]);

        return response()->json([
            'ok' => true,
            'message' => 'Yanit eklendi.',
            'data' => $this->serializeTicketDetail($report),
        ]);
    }

    public function reopen(
        Request $request,
        int $ticketId,
        PosAuthManager $authManager,
    ): JsonResponse {
        [$mobileUser, $company, $error] = $this->resolveAuthContext($request, $authManager);
        if ($error instanceof JsonResponse) {
            return $error;
        }

        $report = FeedbackReport::query()->find($ticketId);
        if (!$report instanceof FeedbackReport || !$this->canAccess($report, $mobileUser, (int) $company->id)) {
            return response()->json([
                'ok' => false,
                'message' => 'Ticket bulunamadi veya erisim yetkiniz yok.',
            ], 404);
        }

        if ((string) $report->status !== 'closed') {
            return response()->json([
                'ok' => true,
                'message' => 'Ticket zaten acik.',
                'data' => $this->serializeTicketDetail($report),
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

        $report->load([
            'messages' => fn ($messages) => $messages
                ->where('is_internal_note', false)
                ->orderBy('id'),
        ]);

        return response()->json([
            'ok' => true,
            'message' => 'Ticket yeniden acildi.',
            'data' => $this->serializeTicketDetail($report),
        ]);
    }

    /**
     * @return array{0: MobileUser|null, 1: \App\Models\Company|null, 2: JsonResponse|null}
     */
    private function resolveAuthContext(Request $request, PosAuthManager $authManager): array
    {
        $mobileUser = $authManager->resolveAuthenticatedUser($request);
        $company = $authManager->resolveAuthenticatedCompany($request);

        if (!$mobileUser instanceof MobileUser || $company === null) {
            return [null, null, response()->json([
                'ok' => false,
                'message' => 'POS oturumu bulunamadi.',
            ], 401)];
        }

        return [$mobileUser, $company, null];
    }

    private function canAccess(FeedbackReport $report, MobileUser $mobileUser, int $companyId): bool
    {
        if ((int) ($report->mobile_user_id ?? 0) === (int) $mobileUser->id) {
            return true;
        }

        return (int) ($report->company_id ?? 0) === $companyId;
    }

    private function serializeTicketSummary(FeedbackReport $report): array
    {
        /** @var FeedbackMessage|null $lastMessage */
        $lastMessage = $report->messages->first();

        return [
            'ticketId' => (int) $report->id,
            'type' => (string) $report->type,
            'source' => (string) $report->source,
            'status' => (string) $report->status,
            'title' => (string) $report->title,
            'lastMessage' => (string) ($lastMessage?->message ?? ''),
            'lastMessageAt' => $lastMessage?->created_at?->valueOf(),
            'createdAt' => $report->created_at?->valueOf(),
            'updatedAt' => $report->updated_at?->valueOf(),
        ];
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
        ];
    }
}

