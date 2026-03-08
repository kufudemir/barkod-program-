package com.marketpos.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.marketpos.BuildConfig
import com.marketpos.core.device.AndroidDeviceIdentityProvider
import com.marketpos.core.device.DeviceIdentityProvider
import com.marketpos.core.premium.AndroidDeviceCodeProvider
import com.marketpos.core.premium.DeviceCodeProvider
import com.marketpos.core.premium.LicenseVerifier
import com.marketpos.core.premium.RsaLicenseVerifier
import com.marketpos.data.db.AppDatabase
import com.marketpos.data.network.BarkodSpaceApiClient
import com.marketpos.data.network.NameSuggestionService
import com.marketpos.data.repository.AccountSessionRepositoryImpl
import com.marketpos.data.repository.ActivationRepositoryImpl
import com.marketpos.data.repository.AppUpdateRepositoryImpl
import com.marketpos.data.repository.BarcodeBankasiImportRepositoryImpl
import com.marketpos.data.repository.CatalogSyncRepositoryImpl
import com.marketpos.data.repository.HeldCartRepositoryImpl
import com.marketpos.data.repository.LegalConsentRepositoryImpl
import com.marketpos.data.repository.NameSuggestionRepositoryImpl
import com.marketpos.data.repository.PremiumRepositoryImpl
import com.marketpos.data.repository.ProductRepositoryImpl
import com.marketpos.data.repository.SaleRepositoryImpl
import com.marketpos.data.repository.SettingsRepositoryImpl
import com.marketpos.data.repository.StockCountRepositoryImpl
import com.marketpos.data.repository.SupportRepositoryImpl
import com.marketpos.data.repository.SyncOutboxRepositoryImpl
import com.marketpos.data.repository.WebBarcodeSearchRepositoryImpl
import com.marketpos.data.repository.WebSaleCompanionRepositoryImpl
import com.marketpos.domain.repository.AccountSessionRepository
import com.marketpos.domain.repository.ActivationRepository
import com.marketpos.domain.repository.AppUpdateRepository
import com.marketpos.domain.repository.BarcodeBankasiImportRepository
import com.marketpos.domain.repository.CatalogSyncRepository
import com.marketpos.domain.repository.HeldCartRepository
import com.marketpos.domain.repository.LegalConsentRepository
import com.marketpos.domain.repository.NameSuggestionRepository
import com.marketpos.domain.repository.PremiumRepository
import com.marketpos.domain.repository.ProductRepository
import com.marketpos.domain.repository.SaleRepository
import com.marketpos.domain.repository.SettingsRepository
import com.marketpos.domain.repository.StockCountRepository
import com.marketpos.domain.repository.SupportRepository
import com.marketpos.domain.repository.SyncOutboxRepository
import com.marketpos.domain.repository.WebBarcodeSearchRepository
import com.marketpos.domain.repository.WebSaleCompanionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object InfrastructureModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "market_pos.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    ensureProductColumns(db)
                    ensureSaleItemColumns(db)
                }
            })
            .build()
    }

    @Provides
    fun provideProductDao(db: AppDatabase) = db.productDao()

    @Provides
    fun provideSaleDao(db: AppDatabase) = db.saleDao()

    @Provides
    fun provideSaleItemDao(db: AppDatabase) = db.saleItemDao()

    @Provides
    fun provideAppSettingDao(db: AppDatabase) = db.appSettingDao()

    @Provides
    fun provideSyncOutboxDao(db: AppDatabase) = db.syncOutboxDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        return OkHttpClient.Builder()
            .callTimeout(8, java.util.concurrent.TimeUnit.SECONDS)
            .connectTimeout(8, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(8, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "MarketPOS/${BuildConfig.VERSION_NAME}")
                    .header("Accept-Language", "tr-TR,tr;q=0.9,en-US;q=0.7,en;q=0.6")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logger)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://duckduckgo.com/")
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    fun provideNameSuggestionService(retrofit: Retrofit): NameSuggestionService {
        return retrofit.create(NameSuggestionService::class.java)
    }

    @Provides
    @Singleton
    fun provideBarkodSpaceApiClient(client: OkHttpClient): BarkodSpaceApiClient {
        return BarkodSpaceApiClient(client)
    }

    @Provides
    @Singleton
    fun provideDeviceCodeProvider(impl: AndroidDeviceCodeProvider): DeviceCodeProvider = impl

    @Provides
    @Singleton
    fun provideDeviceIdentityProvider(impl: AndroidDeviceIdentityProvider): DeviceIdentityProvider = impl

    @Provides
    @Singleton
    fun provideLicenseVerifier(impl: RsaLicenseVerifier): LicenseVerifier = impl
}

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        ensureProductColumns(db)
        ensureSaleItemColumns(db)
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS sync_outbox (
                outboxId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                eventUuid TEXT NOT NULL,
                eventType TEXT NOT NULL,
                payloadJson TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                attemptCount INTEGER NOT NULL,
                lastAttemptAt INTEGER,
                status TEXT NOT NULL,
                errorMessage TEXT
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_sync_outbox_eventUuid ON sync_outbox(eventUuid)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_outbox_status ON sync_outbox(status)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_outbox_createdAt ON sync_outbox(createdAt)")
    }
}

private fun ensureProductColumns(db: SupportSQLiteDatabase) {
    val existingColumns = mutableSetOf<String>()
    db.query("PRAGMA table_info(products)").use { cursor ->
        val nameIndex = cursor.getColumnIndex("name")
        while (cursor.moveToNext()) {
            if (nameIndex >= 0) {
                existingColumns += cursor.getString(nameIndex)
            }
        }
    }

    if ("costPriceKurus" !in existingColumns) {
        db.execSQL("ALTER TABLE products ADD COLUMN costPriceKurus INTEGER NOT NULL DEFAULT 0")
    }
    if ("groupName" !in existingColumns) {
        db.execSQL("ALTER TABLE products ADD COLUMN groupName TEXT")
    }
    if ("stockQty" !in existingColumns) {
        db.execSQL("ALTER TABLE products ADD COLUMN stockQty INTEGER NOT NULL DEFAULT 0")
    }
    if ("minStockQty" !in existingColumns) {
        db.execSQL("ALTER TABLE products ADD COLUMN minStockQty INTEGER NOT NULL DEFAULT 0")
    }
    if ("note" !in existingColumns) {
        db.execSQL("ALTER TABLE products ADD COLUMN note TEXT")
    }
    if ("createdAt" !in existingColumns) {
        db.execSQL("ALTER TABLE products ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
    }
    if ("updatedAt" !in existingColumns) {
        db.execSQL("ALTER TABLE products ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
    }
    if ("isActive" !in existingColumns) {
        db.execSQL("ALTER TABLE products ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
    }
}

private fun ensureSaleItemColumns(db: SupportSQLiteDatabase) {
    val existingColumns = mutableSetOf<String>()
    db.query("PRAGMA table_info(sale_items)").use { cursor ->
        val nameIndex = cursor.getColumnIndex("name")
        while (cursor.moveToNext()) {
            if (nameIndex >= 0) {
                existingColumns += cursor.getString(nameIndex)
            }
        }
    }

    if ("unitBaseSalePriceKurusSnapshot" !in existingColumns) {
        db.execSQL("ALTER TABLE sale_items ADD COLUMN unitBaseSalePriceKurusSnapshot INTEGER NOT NULL DEFAULT 0")
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAccountSessionRepository(impl: AccountSessionRepositoryImpl): AccountSessionRepository

    @Binds
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    abstract fun bindActivationRepository(impl: ActivationRepositoryImpl): ActivationRepository

    @Binds
    abstract fun bindAppUpdateRepository(impl: AppUpdateRepositoryImpl): AppUpdateRepository

    @Binds
    abstract fun bindCatalogSyncRepository(impl: CatalogSyncRepositoryImpl): CatalogSyncRepository

    @Binds
    abstract fun bindSyncOutboxRepository(impl: SyncOutboxRepositoryImpl): SyncOutboxRepository

    @Binds
    abstract fun bindSaleRepository(impl: SaleRepositoryImpl): SaleRepository

    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    abstract fun bindNameSuggestionRepository(impl: NameSuggestionRepositoryImpl): NameSuggestionRepository

    @Binds
    abstract fun bindHeldCartRepository(impl: HeldCartRepositoryImpl): HeldCartRepository

    @Binds
    abstract fun bindLegalConsentRepository(impl: LegalConsentRepositoryImpl): LegalConsentRepository

    @Binds
    abstract fun bindBarcodeBankasiImportRepository(impl: BarcodeBankasiImportRepositoryImpl): BarcodeBankasiImportRepository

    @Binds
    abstract fun bindWebBarcodeSearchRepository(impl: WebBarcodeSearchRepositoryImpl): WebBarcodeSearchRepository

    @Binds
    abstract fun bindWebSaleCompanionRepository(impl: WebSaleCompanionRepositoryImpl): WebSaleCompanionRepository

    @Binds
    abstract fun bindPremiumRepository(impl: PremiumRepositoryImpl): PremiumRepository

    @Binds
    abstract fun bindStockCountRepository(impl: StockCountRepositoryImpl): StockCountRepository

    @Binds
    abstract fun bindSupportRepository(impl: SupportRepositoryImpl): SupportRepository
}
