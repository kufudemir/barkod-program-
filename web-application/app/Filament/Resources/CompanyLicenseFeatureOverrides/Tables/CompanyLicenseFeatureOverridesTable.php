<?php

namespace App\Filament\Resources\CompanyLicenseFeatureOverrides\Tables;

use App\Models\Company;
use App\Models\LicensePackage;
use Illuminate\Database\Eloquent\Builder;
use Filament\Actions\DeleteAction;
use Filament\Actions\EditAction;
use Filament\Tables\Columns\IconColumn;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Filters\SelectFilter;
use Filament\Tables\Table;

class CompanyLicenseFeatureOverridesTable
{
    public static function configure(Table $table): Table
    {
        return $table
            ->defaultSort('updated_at', 'desc')
            ->columns([
                TextColumn::make('companyLicense.id')->label('Lisans ID')->sortable(),
                TextColumn::make('companyLicense.company.name')->label('Firma')->searchable()->sortable(),
                TextColumn::make('companyLicense.package.code')->label('Paket')->badge(),
                TextColumn::make('featureFlag.key')->label('Feature Key')->copyable()->searchable(),
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
                IconColumn::make('is_enabled')->label('Açık')->boolean(),
                TextColumn::make('reason')->label('Gerekçe')->limit(60)->placeholder('-')->toggleable(isToggledHiddenByDefault: true),
                TextColumn::make('updated_at')->label('Güncellendi')->since(),
            ])
            ->filters([
                SelectFilter::make('company_id')
                    ->label('Firma')
                    ->options(fn (): array => Company::query()->orderBy('name')->pluck('name', 'id')->all())
                    ->query(function (Builder $query, array $data): Builder {
                        $companyId = $data['value'] ?? null;
                        if (! filled($companyId)) {
                            return $query;
                        }

                        return $query->whereHas('companyLicense', fn (Builder $licenseQuery): Builder => $licenseQuery->where('company_id', $companyId));
                    }),
                SelectFilter::make('package_id')
                    ->label('Paket')
                    ->options(fn (): array => LicensePackage::query()->orderBy('sort_order')->pluck('name', 'id')->all())
                    ->query(function (Builder $query, array $data): Builder {
                        $packageId = $data['value'] ?? null;
                        if (! filled($packageId)) {
                            return $query;
                        }

                        return $query->whereHas('companyLicense', fn (Builder $licenseQuery): Builder => $licenseQuery->where('package_id', $packageId));
                    }),
                SelectFilter::make('feature_flag_id')
                    ->label('Feature')
                    ->relationship('featureFlag', 'title'),
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
