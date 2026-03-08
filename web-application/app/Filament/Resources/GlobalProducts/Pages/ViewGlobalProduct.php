<?php

namespace App\Filament\Resources\GlobalProducts\Pages;

use App\Filament\Resources\GlobalProducts\GlobalProductResource;
use Filament\Actions\Action;
use Filament\Actions\EditAction;
use Filament\Forms\Components\Select;
use Filament\Notifications\Notification;
use Filament\Resources\Pages\ViewRecord;

class ViewGlobalProduct extends ViewRecord
{
    protected static string $resource = GlobalProductResource::class;

    protected function getHeaderActions(): array
    {
        return [
            Action::make('useCandidateName')
                ->label('Bu ismi kullan')
                ->icon('heroicon-o-check-circle')
                ->visible(fn (): bool => $this->record->candidates()->exists())
                ->form([
                    Select::make('candidate_name')
                        ->label('İsim adayı')
                        ->options(fn (): array => $this->record->candidates()
                            ->orderByDesc('last_seen_at')
                            ->pluck('candidate_name', 'candidate_name')
                            ->all())
                        ->required(),
                ])
                ->action(function (array $data): void {
                    $this->record->update([
                        'canonical_name' => $data['candidate_name'],
                    ]);

                    Notification::make()
                        ->title('Ana ürün adı güncellendi')
                        ->success()
                        ->send();
                }),
            EditAction::make(),
        ];
    }
}
