package com.marketpos.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.marketpos.data.db.dao.AppSettingDao
import com.marketpos.data.db.dao.ProductDao
import com.marketpos.data.db.dao.SaleDao
import com.marketpos.data.db.dao.SaleItemDao
import com.marketpos.data.db.dao.SyncOutboxDao
import com.marketpos.data.db.entity.AppSettingEntity
import com.marketpos.data.db.entity.ProductEntity
import com.marketpos.data.db.entity.SaleEntity
import com.marketpos.data.db.entity.SaleItemEntity
import com.marketpos.data.db.entity.SyncOutboxEntity

@Database(
    entities = [
        ProductEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        AppSettingEntity::class,
        SyncOutboxEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun saleItemDao(): SaleItemDao
    abstract fun appSettingDao(): AppSettingDao
    abstract fun syncOutboxDao(): SyncOutboxDao
}
