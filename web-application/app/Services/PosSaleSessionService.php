<?php

namespace App\Services;

use App\Models\PosSession;
use App\Models\SaleSession;
use Illuminate\Http\Request;
use Illuminate\Support\Collection;
use RuntimeException;

class PosSaleSessionService
{
    private const DEFAULT_SOURCE_LABEL = 'Web Manuel';

    public function resolveContext(Request $request, PosSession $posSession, ?int $mobileUserId = null): array
    {
        $sessions = $this->listActiveByPosSession($posSession->id);

        if ($sessions->isEmpty()) {
            $this->create($posSession, self::DEFAULT_SOURCE_LABEL, null, $mobileUserId);
            $sessions = $this->listActiveByPosSession($posSession->id);
        }

        $selectedId = (int) $request->session()->get('pos.auth.sale_session_id', 0);
        $activeSession = $sessions->firstWhere('id', $selectedId) ?? $sessions->first();

        if (! $activeSession instanceof SaleSession) {
            throw new RuntimeException('Aktif satis sekmesi bulunamadi.');
        }

        $this->writeSelection($request, $activeSession->id);

        return [
            'sessions' => $sessions,
            'activeSession' => $activeSession,
        ];
    }

    public function createManualTab(Request $request, PosSession $posSession, ?int $mobileUserId = null): SaleSession
    {
        $label = $this->nextManualLabel($posSession->id);
        $session = $this->create($posSession, $label, null, $mobileUserId);
        $this->writeSelection($request, $session->id);

        return $session;
    }

    public function switchTab(Request $request, PosSession $posSession, int $saleSessionId): SaleSession
    {
        $session = SaleSession::query()
            ->where('id', $saleSessionId)
            ->where('pos_session_id', $posSession->id)
            ->where('status', 'active')
            ->first();

        if (! $session instanceof SaleSession) {
            throw new RuntimeException('Secilen satis sekmesi bulunamadi.');
        }

        $this->writeSelection($request, $session->id);

        return $session;
    }

    public function rotateAfterCheckout(
        Request $request,
        PosSession $posSession,
        SaleSession $saleSession,
        ?int $mobileUserId = null,
    ): SaleSession {
        if ($saleSession->status === 'active') {
            $saleSession->update([
                'status' => 'completed',
            ]);
        }

        $existingActive = SaleSession::query()
            ->where('pos_session_id', $posSession->id)
            ->where('status', 'active')
            ->orderBy('id')
            ->first();

        if ($existingActive instanceof SaleSession) {
            $this->writeSelection($request, $existingActive->id);

            return $existingActive;
        }

        $newSession = $this->create(
            $posSession,
            $saleSession->source_label,
            $saleSession->source_device_uid,
            $mobileUserId ?? $saleSession->created_by_mobile_user_id,
        );

        $this->writeSelection($request, $newSession->id);

        return $newSession;
    }

    public function holdAndRotate(
        Request $request,
        PosSession $posSession,
        SaleSession $saleSession,
        ?int $mobileUserId = null,
    ): SaleSession {
        if ((int) $saleSession->pos_session_id !== (int) $posSession->id || $saleSession->status !== 'active') {
            throw new RuntimeException('Bekletilecek satis sekmesi bulunamadi.');
        }

        $saleSession->update([
            'status' => 'held',
        ]);

        $fallback = SaleSession::query()
            ->where('pos_session_id', $posSession->id)
            ->where('status', 'active')
            ->orderBy('id')
            ->first();

        if ($fallback instanceof SaleSession) {
            $this->writeSelection($request, $fallback->id);

            return $fallback;
        }

        $newSession = $this->createManualTab($request, $posSession, $mobileUserId);

        return $newSession;
    }

    public function closeTab(Request $request, PosSession $posSession, int $saleSessionId, ?int $mobileUserId = null): SaleSession
    {
        $target = SaleSession::query()
            ->where('id', $saleSessionId)
            ->where('pos_session_id', $posSession->id)
            ->where('status', 'active')
            ->first();

        if (! $target instanceof SaleSession) {
            throw new RuntimeException('Kapatilacak satis sekmesi bulunamadi.');
        }

        $target->update([
            'status' => 'cancelled',
        ]);

        $fallback = SaleSession::query()
            ->where('pos_session_id', $posSession->id)
            ->where('status', 'active')
            ->orderBy('id')
            ->first();

        if ($fallback instanceof SaleSession) {
            $this->writeSelection($request, $fallback->id);

            return $fallback;
        }

        $newSession = $this->create($posSession, self::DEFAULT_SOURCE_LABEL, null, $mobileUserId);
        $this->writeSelection($request, $newSession->id);

        return $newSession;
    }

    /**
     * @return Collection<int, SaleSession>
     */
    public function listHeldByPosSession(int $posSessionId): Collection
    {
        return SaleSession::query()
            ->where('pos_session_id', $posSessionId)
            ->where('status', 'held')
            ->orderByDesc('updated_at')
            ->orderByDesc('id')
            ->get();
    }

    public function resumeHeldTab(Request $request, PosSession $posSession, int $saleSessionId): SaleSession
    {
        $session = SaleSession::query()
            ->where('id', $saleSessionId)
            ->where('pos_session_id', $posSession->id)
            ->where('status', 'held')
            ->first();

        if (! $session instanceof SaleSession) {
            throw new RuntimeException('Geri acilacak bekleyen satis bulunamadi.');
        }

        $session->update([
            'status' => 'active',
        ]);

        $this->writeSelection($request, $session->id);

        return $session;
    }

    public function discardHeldTab(PosSession $posSession, int $saleSessionId): void
    {
        $session = SaleSession::query()
            ->where('id', $saleSessionId)
            ->where('pos_session_id', $posSession->id)
            ->where('status', 'held')
            ->first();

        if (! $session instanceof SaleSession) {
            throw new RuntimeException('Silinecek bekleyen satis bulunamadi.');
        }

        $session->items()->delete();
        $session->update([
            'status' => 'cancelled',
        ]);
    }

    public function clearSelection(Request $request): void
    {
        $auth = (array) $request->session()->get('pos.auth', []);
        $auth['sale_session_id'] = null;
        $request->session()->put('pos.auth', $auth);
        $request->session()->save();
    }

    /**
     * @return Collection<int, SaleSession>
     */
    private function listActiveByPosSession(int $posSessionId): Collection
    {
        return SaleSession::query()
            ->where('pos_session_id', $posSessionId)
            ->where('status', 'active')
            ->orderBy('id')
            ->get();
    }

    private function nextManualLabel(int $posSessionId): string
    {
        $existingLabels = SaleSession::query()
            ->where('pos_session_id', $posSessionId)
            ->pluck('source_label')
            ->map(fn (string $label): string => mb_strtolower(trim($label), 'UTF-8'))
            ->all();

        $base = self::DEFAULT_SOURCE_LABEL;
        $seed = 1;

        do {
            $candidate = $seed === 1 ? $base : $base . ' ' . $seed;
            $exists = in_array(mb_strtolower($candidate, 'UTF-8'), $existingLabels, true);
            $seed++;
        } while ($exists);

        return $candidate;
    }

    private function create(
        PosSession $posSession,
        string $sourceLabel,
        ?string $sourceDeviceUid,
        ?int $mobileUserId = null,
    ): SaleSession {
        return SaleSession::query()->create([
            'pos_session_id' => $posSession->id,
            'source_device_uid' => $sourceDeviceUid,
            'source_label' => $sourceLabel,
            'created_by_mobile_user_id' => $mobileUserId,
            'status' => 'active',
        ]);
    }

    private function writeSelection(Request $request, int $saleSessionId): void
    {
        $auth = (array) $request->session()->get('pos.auth', []);
        $auth['sale_session_id'] = $saleSessionId;
        $request->session()->put('pos.auth', $auth);
        $request->session()->save();
    }
}
