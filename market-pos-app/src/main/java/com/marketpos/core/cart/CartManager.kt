package com.marketpos.core.cart

import com.marketpos.domain.model.CartItem
import com.marketpos.domain.model.Product
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class CartManager @Inject constructor() {

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    fun addProduct(product: Product, overrideSalePriceKurus: Long? = null) {
        val current = _items.value.toMutableList()
        val index = current.indexOfFirst { it.barcode == product.barcode }
        val appliedPrice = overrideSalePriceKurus ?: product.salePriceKurus
        if (index >= 0) {
            val old = current[index]
            current[index] = old.copy(
                quantity = old.quantity + 1,
                salePriceKurus = overrideSalePriceKurus ?: old.salePriceKurus
            )
        } else {
            current.add(
                CartItem(
                    barcode = product.barcode,
                    name = product.name,
                    baseSalePriceKurus = product.salePriceKurus,
                    salePriceKurus = appliedPrice,
                    costPriceKurus = product.costPriceKurus,
                    quantity = 1,
                    stockQty = product.stockQty
                )
            )
        }
        _items.value = current
    }

    fun increase(barcode: String) {
        update(barcode) { it.copy(quantity = it.quantity + 1) }
    }

    fun decrease(barcode: String) {
        val list = _items.value.toMutableList()
        val index = list.indexOfFirst { it.barcode == barcode }
        if (index < 0) return
        val old = list[index]
        if (old.quantity <= 1) {
            list.removeAt(index)
        } else {
            list[index] = old.copy(quantity = old.quantity - 1)
        }
        _items.value = list
    }

    fun remove(barcode: String) {
        _items.value = _items.value.filterNot { it.barcode == barcode }
    }

    fun setQuantity(barcode: String, quantity: Int) {
        if (quantity <= 0) {
            remove(barcode)
            return
        }
        update(barcode) { it.copy(quantity = quantity) }
    }

    fun setCustomPrice(barcode: String, newSalePriceKurus: Long) {
        if (newSalePriceKurus <= 0L) return
        update(barcode) { it.copy(salePriceKurus = newSalePriceKurus) }
    }

    fun resetPrice(barcode: String) {
        update(barcode) { it.copy(salePriceKurus = it.baseSalePriceKurus) }
    }

    fun quantityOf(barcode: String): Int {
        return _items.value.firstOrNull { it.barcode == barcode }?.quantity ?: 0
    }

    fun snapshot(): List<CartItem> = _items.value

    fun replaceAll(items: List<CartItem>) {
        _items.value = items
    }

    fun clear() {
        _items.value = emptyList()
    }

    private fun update(barcode: String, transform: (CartItem) -> CartItem) {
        val list = _items.value.toMutableList()
        val index = list.indexOfFirst { it.barcode == barcode }
        if (index >= 0) {
            list[index] = transform(list[index])
            _items.value = list
        }
    }
}
