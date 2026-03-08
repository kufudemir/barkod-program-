package com.marketpos.domain.usecase

import com.marketpos.domain.model.Product
import com.marketpos.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateOrUpdateProductUseCaseTest {

    @Test
    fun `new product with duplicate barcode returns failure`() = runTest {
        val repository = FakeProductRepository().apply {
            products["8690000000001"] = sampleProduct(barcode = "8690000000001")
        }
        val useCase = CreateOrUpdateProductUseCase(repository)

        val result = useCase(sampleProduct(barcode = "8690000000001"), originalBarcode = null)

        assertFalse(result.isSuccess)
        assertEquals("Bu barkod zaten kayitli", result.exceptionOrNull()?.message)
    }

    @Test
    fun `barcode change keeps old product inactive and writes new barcode`() = runTest {
        val repository = FakeProductRepository().apply {
            products["111"] = sampleProduct(barcode = "111", createdAt = 5L)
        }
        val useCase = CreateOrUpdateProductUseCase(repository)

        val result = useCase(
            product = sampleProduct(barcode = "222", salePriceKurus = 1650L),
            originalBarcode = "111"
        )

        assertTrue(result.isSuccess)
        assertEquals("111", repository.replacedPreviousBarcode)
        assertEquals("222", repository.replacedProduct?.barcode)
        assertEquals(1700L, repository.replacedProduct?.salePriceKurus)
        assertEquals(5L, repository.replacedProduct?.createdAt)
    }

    private fun sampleProduct(
        barcode: String,
        salePriceKurus: Long = 1500L,
        createdAt: Long = 1L
    ) = Product(
        barcode = barcode,
        name = "Test Urun",
        salePriceKurus = salePriceKurus,
        costPriceKurus = 1000L,
        stockQty = 3,
        minStockQty = 1,
        note = "not",
        createdAt = createdAt,
        updatedAt = createdAt,
        isActive = true
    )
}

private class FakeProductRepository : ProductRepository {
    val products = linkedMapOf<String, Product>()
    var replacedPreviousBarcode: String? = null
    var replacedProduct: Product? = null

    override fun observeAllActive(): Flow<List<Product>> = emptyFlow()

    override fun observeSearch(query: String): Flow<List<Product>> = emptyFlow()

    override fun observeByBarcode(barcode: String): Flow<Product?> = emptyFlow()

    override suspend fun getByBarcode(barcode: String): Product? = products[barcode]

    override suspend fun upsert(product: Product) {
        products[product.barcode] = product
    }

    override suspend fun upsertReplacingBarcode(previousBarcode: String, product: Product) {
        replacedPreviousBarcode = previousBarcode
        replacedProduct = product
        products[previousBarcode] = products.getValue(previousBarcode).copy(isActive = false)
        products[product.barcode] = product
    }

    override suspend fun deactivate(barcode: String) = Unit

    override suspend fun updateStock(barcode: String, newQty: Int) = Unit

    override suspend fun updateSalePrice(barcode: String, newSalePriceKurus: Long) = Unit

    override suspend fun listByBarcodes(barcodes: List<String>): List<Product> = emptyList()

    override suspend fun listAllActiveOnce(): List<Product> = emptyList()
}
