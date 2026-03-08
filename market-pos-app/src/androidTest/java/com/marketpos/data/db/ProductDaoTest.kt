package com.marketpos.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.marketpos.data.db.entity.ProductEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_and_read_product() = runBlocking {
        val entity = ProductEntity(
            barcode = "8690000000011",
            name = "Test Urun",
            salePriceKurus = 1700,
            costPriceKurus = 1000,
            stockQty = 5,
            minStockQty = 1,
            note = "Raf 1",
            createdAt = 1L,
            updatedAt = 1L,
            isActive = true
        )
        db.productDao().upsert(entity)
        val fetched = db.productDao().getByBarcode(entity.barcode)
        assertEquals(entity.name, fetched?.name)
        assertEquals(entity.salePriceKurus, fetched?.salePriceKurus)
        assertEquals(entity.note, fetched?.note)
    }
}
