<?php

namespace App\Filament\Resources\LicensePackageFeatures\Tables;

use Illuminate\Database\Eloquent\Builder;
use Filament\Actions\DeleteAction;
use Filament\Actions\EditAction;
use Filament\Tables\Columns\IconColumn;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Filters\SelectFilter;
use Filament\Tables\Table;

class LicensePackageFeaturesTable
{
    public static function configure(Table $table): Table
    {
        return $table
            ->defaultSort('id', 'desc')
            ->columns([
                TextColumn::make('package.code')->label('Paket')->badge()->sortable(),
                TextColumn::make('package.name')->label('Paket Adı')->searchable()->sortable(),
                TextColumn::make('featureFlag.key')->label('Feature Key')->searchable()->copyable(),
                TextColumn::make('featureFlag.title')->label('Feature')->searchable()->sortable(),
                TextColumn::make('featureFlag.scope')
                    ->label('Kapsam')
                    ->badge()
                    ->formatStateUsing(fn (string $state): string => match ($state) {
                        'mobile' => 'Mobil',
                        'web_pos' => 'Web POS',
                        'admin' => 'Admin',
                        default => 'Ortak',
                    }),
                IconColumn::make('featureFlag.is_core')->label('Core')->boolean(),
                IconColumn::make('is_enabled')->label('Açık')->boolean(),
                TextColumn::make('updated_at')->label('Güncellendi')->since(),
            ])
            ->filters([
                SelectFilter::make('package_id')
                    ->label('Paket')
                    ->relationship('package', 'name'),
                SelectFilter::make('scope')
                    ->label('Kapsam')
                    ->options([
                        'mobile' => 'Mobil',
                        'web_pos' => 'Web POS',
                        'admin' => 'Admin',
                        'shared' => 'Ortak',
                    ])
                    ->query(function (Builder $query, array $data): Builder {
                        $scope = $data['value'] ?? null;
                        if (! filled($scope)) {
                            return $query;
                        }

                        return $query->whereHas('featureFlag', fn (Builder $featureQuery): Builder => $featureQuery->where('scope', $scope));
                    }),
                SelectFilter::make('is_enabled')
                    ->label('Durum')
                    ->options([
                        '1' => 'Açık',
                        '0' => 'Kapalı',
                    ]),
            ])
            ->recordActions([
                EditAction::make(),
                DeleteAction::make(),
            ])
            ->toolbarActions([]);
    }
}
