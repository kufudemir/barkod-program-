package com.marketpos.feature.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.core.cart.CartManager
import com.marketpos.core.util.MoneyUtils
import com.marketpos.domain.model.AppMode
import com.marketpos.domain.model.Product
import com.marketpos.domain.repository.ProductRepository
import com.marketpos.domain.repository.SettingsRepository
import com.marketpos.domain.usecase.ApplyPercentPriceChangeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val mode: AppMode = AppMode.CASHIER,
    val product: Product? = null,
    val pendingCartSalePriceKurus: Long? = null
) {
    val pendingCartEffectivePriceKurus: Long?
        get() = pendingCartSalePriceKurus ?: product?.salePriceKurus

    val pendingCartPriceLabel: String?
        get() = pendingCartEffectivePriceKurus?.let(MoneyUtils::formatKurus)

    val hasPendingCartOverride: Boolean
        get() = pendingCartSalePriceKurus != null && pendingCartSalePriceKurus != product?.salePriceKurus
}

sealed interface ProductDetailEvent {
    data class ShowMessage(val message: String) : ProductDetailEvent
    data class NavigateEdit(val barcode: String) : ProductDetailEvent
    data object NavigateScan : ProductDetailEvent
}

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val settingsRepository: SettingsRepository,
    private val cartManager: CartManager,
    private val applyPercentPriceChangeUseCase: ApplyPercentPriceChangeUseCase
) : ViewModel() {

    private val barcode = savedStateHandle.get<String>("barcode").orEmpty()

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProductDetailEvent>()
    val events: SharedFlow<ProductDetailEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            productRepository.observeByBarcode(barcode).collect { product ->
                _uiState.value = _uiState.value.copy(product = product)
            }
        }
        viewModelScope.launch {
            settingsRepository.observeMode().collect { mode ->
                _uiState.value = _uiState.value.copy(mode = mode)
            }
        }
    }

    fun addToCart() {
        val state = _uiState.value
        val product = state.product ?: return
        cartManager.addProduct(product, state.pendingCartEffectivePriceKurus)
        _uiState.value = state.copy(pendingCartSalePriceKurus = null)
        viewModelScope.launch { _events.emit(ProductDetailEvent.NavigateScan) }
    }

    fun requestEdit(pinIfCashier: String?) {
        viewModelScope.launch {
            if (!isAuthorized(pinIfCashier)) return@launch
            _events.emit(ProductDetailEvent.NavigateEdit(barcode))
        }
    }

    fun updatePriceManual(priceInput: String, pinIfCashier: String?) {
        val priceKurus = MoneyUtils.parseTlInputToKurus(priceInput)
        if (priceKurus == null) {
            viewModelScope.launch { _events.emit(ProductDetailEvent.ShowMessage("Geçerli fiyat giriniz")) }
            return
        }
        viewModelScope.launch {
            if (!isAuthorized(pinIfCashier)) return@launch
            val roundedPrice = MoneyUtils.roundUpToWholeTL(priceKurus)
            _uiState.value = _uiState.value.copy(pendingCartSalePriceKurus = roundedPrice)
            _events.emit(ProductDetailEvent.ShowMessage("Sepete eklenecek fiyat güncellendi"))
        }
    }

    fun updatePricePercent(percentInput: String, increase: Boolean, pinIfCashier: String?) {
        val percent = percentInput.replace(",", ".").toDoubleOrNull()
        if (percent == null || percent <= 0.0) {
            viewModelScope.launch { _events.emit(ProductDetailEvent.ShowMessage("Geçerli yüzde giriniz")) }
            return
        }
        val product = _uiState.value.product ?: return
        val signedPercent = if (increase) percent else -percent
        viewModelScope.launch {
            if (!isAuthorized(pinIfCashier)) return@launch
            val basePrice = _uiState.value.pendingCartEffectivePriceKurus ?: product.salePriceKurus
            val newPrice = applyPercentPriceChangeUseCase(basePrice, signedPercent)
            _uiState.value = _uiState.value.copy(pendingCartSalePriceKurus = newPrice)
            _events.emit(ProductDetailEvent.ShowMessage("Sepete özel yüzde işlemi uygulandı"))
        }
    }

    fun resetPendingCartPrice() {
        _uiState.value = _uiState.value.copy(pendingCartSalePriceKurus = null)
    }

    fun increaseStock(amountInput: String, pinIfCashier: String?) {
        val amount = amountInput.toIntOrNull()
        if (amount == null || amount <= 0) {
            viewModelScope.launch { _events.emit(ProductDetailEvent.ShowMessage("Geçerli stok miktarı giriniz")) }
            return
        }
        val product = _uiState.value.product ?: return
        viewModelScope.launch {
            if (!isAuthorized(pinIfCashier)) return@launch
            productRepository.updateStock(product.barcode, product.stockQty + amount)
            _events.emit(ProductDetailEvent.ShowMessage("Stok güncellendi"))
        }
    }

    private suspend fun isAuthorized(pinIfCashier: String?): Boolean {
        if (_uiState.value.mode == AppMode.ADMIN) return true
        val valid = !pinIfCashier.isNullOrBlank() && settingsRepository.verifyPin(pinIfCashier)
        if (!valid) _events.emit(ProductDetailEvent.ShowMessage("PIN hatalı"))
        return valid
    }
}
