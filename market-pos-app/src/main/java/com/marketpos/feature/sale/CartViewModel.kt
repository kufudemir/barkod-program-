package com.marketpos.feature.sale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.core.cart.CartManager
import com.marketpos.core.util.DateUtils
import com.marketpos.core.util.MoneyUtils
import com.marketpos.domain.model.AppMode
import com.marketpos.domain.model.CartItem
import com.marketpos.domain.model.HeldCart
import com.marketpos.domain.model.MobilePosSaleSyncItem
import com.marketpos.domain.model.MobilePosSaleSyncPayload
import com.marketpos.domain.model.PremiumFeature
import com.marketpos.domain.repository.AccountSessionRepository
import com.marketpos.domain.repository.ActivationRepository
import com.marketpos.domain.repository.HeldCartRepository
import com.marketpos.domain.repository.PremiumRepository
import com.marketpos.domain.repository.SettingsRepository
import com.marketpos.domain.repository.SaleRepository
import com.marketpos.domain.repository.WebSaleCompanionRepository
import com.marketpos.domain.usecase.CreateSaleUseCase
import java.math.BigDecimal
import java.math.RoundingMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val heldCarts: List<HeldCart> = emptyList(),
    val mode: AppMode = AppMode.CASHIER,
    val isProcessing: Boolean = false,
    val canUsePremiumPricing: Boolean = false
) {
    val totalAmountKurus: Long get() = items.sumOf { it.lineTotalKurus }
    val totalItemCount: Int get() = items.sumOf { it.quantity }
    val heldCartCount: Int get() = heldCarts.size
}

sealed interface CartEvent {
    data class ShowMessage(val message: String) : CartEvent
    data class NavigateSaleSuccess(val saleId: Long) : CartEvent
    data object NavigateScan : CartEvent
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartManager: CartManager,
    private val settingsRepository: SettingsRepository,
    private val createSaleUseCase: CreateSaleUseCase,
    private val saleRepository: SaleRepository,
    private val activationRepository: ActivationRepository,
    private val accountSessionRepository: AccountSessionRepository,
    private val webSaleCompanionRepository: WebSaleCompanionRepository,
    private val premiumRepository: PremiumRepository,
    private val heldCartRepository: HeldCartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CartEvent>()
    val events: SharedFlow<CartEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            cartManager.items.collect { items ->
                _uiState.value = _uiState.value.copy(items = items)
            }
        }
        viewModelScope.launch {
            settingsRepository.observeMode().collect { mode ->
                _uiState.value = _uiState.value.copy(mode = mode)
            }
        }
        viewModelScope.launch {
            premiumRepository.observeState().collect { premiumState ->
                _uiState.value = _uiState.value.copy(canUsePremiumPricing = premiumState.isPro)
            }
        }
        viewModelScope.launch {
            heldCartRepository.observeHeldCarts().collect { heldCarts ->
                _uiState.value = _uiState.value.copy(heldCarts = heldCarts)
            }
        }
    }

    fun increase(barcode: String) = cartManager.increase(barcode)

    fun decrease(barcode: String) = cartManager.decrease(barcode)

    fun increaseBy(barcode: String, step: Int) {
        repeat(step.coerceAtLeast(1)) {
            cartManager.increase(barcode)
        }
    }

    fun remove(barcode: String) = cartManager.remove(barcode)

    fun setQuantity(barcode: String, quantityInput: String) {
        val quantity = quantityInput.toIntOrNull()
        if (quantity == null || quantity < 0) {
            viewModelScope.launch { _events.emit(CartEvent.ShowMessage("Geçerli adet giriniz")) }
            return
        }
        cartManager.setQuantity(barcode, quantity)
    }

    fun applyCustomPrice(barcode: String, priceInput: String, pinIfCashier: String?) {
        if (!ensurePremiumPricing()) return
        val priceKurus = MoneyUtils.parseTlInputToKurus(priceInput)
        if (priceKurus == null || priceKurus <= 0L) {
            viewModelScope.launch { _events.emit(CartEvent.ShowMessage("Geçerli fiyat giriniz")) }
            return
        }
        viewModelScope.launch {
            if (_uiState.value.mode == AppMode.CASHIER) {
                val valid = !pinIfCashier.isNullOrBlank() && settingsRepository.verifyPin(pinIfCashier)
                if (!valid) {
                    _events.emit(CartEvent.ShowMessage("PIN hatali"))
                    return@launch
                }
            }
            cartManager.setCustomPrice(barcode, MoneyUtils.roundUpToWholeTL(priceKurus))
            _events.emit(CartEvent.ShowMessage("Özel fiyat uygulandı"))
        }
    }

    fun applyPercentDiscount(barcode: String, percentInput: String, pinIfCashier: String?) {
        if (!ensurePremiumPricing()) return
        val percent = percentInput.replace(",", ".").toBigDecimalOrNull()
        if (percent == null || percent <= BigDecimal.ZERO || percent >= BigDecimal(100)) {
            viewModelScope.launch { _events.emit(CartEvent.ShowMessage("Geçerli indirim yüzdesi giriniz")) }
            return
        }
        viewModelScope.launch {
            if (!isPriceChangeAuthorized(pinIfCashier)) return@launch
            val item = _uiState.value.items.firstOrNull { it.barcode == barcode } ?: return@launch
            val discountedRaw = BigDecimal(item.salePriceKurus)
                .multiply(BigDecimal(100).subtract(percent))
                .divide(BigDecimal(100), 0, RoundingMode.HALF_UP)
                .longValueExact()
                .coerceAtLeast(1L)
            val discounted = MoneyUtils.roundUpToWholeTL(discountedRaw)
            cartManager.setCustomPrice(barcode, discounted)
            _events.emit(CartEvent.ShowMessage("Yüzde indirimi uygulandı"))
        }
    }

    fun applyFixedDiscount(barcode: String, amountInput: String, pinIfCashier: String?) {
        if (!ensurePremiumPricing()) return
        val discountKurus = MoneyUtils.parseTlInputToKurus(amountInput)
        if (discountKurus == null || discountKurus <= 0L) {
            viewModelScope.launch { _events.emit(CartEvent.ShowMessage("Geçerli indirim tutarı giriniz")) }
            return
        }
        viewModelScope.launch {
            if (!isPriceChangeAuthorized(pinIfCashier)) return@launch
            val item = _uiState.value.items.firstOrNull { it.barcode == barcode } ?: return@launch
            val discounted = MoneyUtils.roundUpToWholeTL(
                (item.salePriceKurus - discountKurus).coerceAtLeast(1L)
            )
            cartManager.setCustomPrice(barcode, discounted)
            _events.emit(CartEvent.ShowMessage("TL indirimi uygulandi"))
        }
    }

    fun resetPrice(barcode: String) {
        if (!ensurePremiumPricing()) return
        cartManager.resetPrice(barcode)
    }

    private fun ensurePremiumPricing(): Boolean {
        if (_uiState.value.canUsePremiumPricing) return true
        viewModelScope.launch {
            _events.emit(CartEvent.ShowMessage("${PremiumFeature.LINE_PRICE_OVERRIDE.title} PRO sürümde"))
        }
        return false
    }

    private suspend fun isPriceChangeAuthorized(pinIfCashier: String?): Boolean {
        if (_uiState.value.mode == AppMode.ADMIN) return true
        val valid = !pinIfCashier.isNullOrBlank() && settingsRepository.verifyPin(pinIfCashier)
        if (!valid) _events.emit(CartEvent.ShowMessage("PIN hatali"))
        return valid
    }

    fun cancel() {
        viewModelScope.launch { _events.emit(CartEvent.NavigateScan) }
    }

    fun checkout() {
        if (_uiState.value.isProcessing) {
            viewModelScope.launch { _events.emit(CartEvent.ShowMessage("Satış islemi devam ediyor")) }
            return
        }
        val items = _uiState.value.items
        if (items.isEmpty()) {
            viewModelScope.launch { _events.emit(CartEvent.ShowMessage("Sepet bos")) }
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            val result = createSaleUseCase(items)
            _uiState.value = _uiState.value.copy(isProcessing = false)
            result
                .onSuccess { saleId ->
                    cartManager.clear()
                    syncLocalSaleToCloud(saleId)
                    _events.emit(CartEvent.NavigateSaleSuccess(saleId))
                }
                .onFailure { err ->
                    _events.emit(CartEvent.ShowMessage(err.message ?: "Satış tamamlanamadı"))
                }
        }
    }

    fun parkCurrentCart(label: String?) {
        val items = cartManager.snapshot()
        if (items.isEmpty()) {
            viewModelScope.launch { _events.emit(CartEvent.ShowMessage("Bekletilecek sepet bos")) }
            return
        }
        viewModelScope.launch {
            heldCartRepository.saveHeldCart(label, items)
                .onSuccess { heldCart ->
                    cartManager.clear()
                    _events.emit(CartEvent.ShowMessage("${heldCart.label} beklemeye alindi"))
                }
                .onFailure { error ->
                    _events.emit(CartEvent.ShowMessage(error.message ?: "Sepet beklemeye alınamadı"))
                }
        }
    }

    fun restoreHeldCart(cartId: String) {
        viewModelScope.launch {
            val heldCart = heldCartRepository.getHeldCart(cartId)
            if (heldCart == null) {
                _events.emit(CartEvent.ShowMessage("Bekleyen sepet bulunamadı"))
                return@launch
            }

            val currentItems = cartManager.snapshot()
            if (currentItems.isNotEmpty()) {
                heldCartRepository.saveHeldCart(
                    label = "Otomatik Bekletilen ${DateUtils.formatDateTime(DateUtils.now())}",
                    items = currentItems
                ).onFailure { error ->
                    _events.emit(CartEvent.ShowMessage(error.message ?: "Aktif sepet beklemeye alınamadı"))
                    return@launch
                }
            }

            cartManager.replaceAll(heldCart.items)
            heldCartRepository.deleteHeldCart(cartId)
                .onSuccess {
                    val info = if (currentItems.isNotEmpty()) {
                        "Aktif sepet otomatik bekletildi. ${heldCart.label} geri getirildi"
                    } else {
                        "${heldCart.label} geri getirildi"
                    }
                    _events.emit(CartEvent.ShowMessage(info))
                }
                .onFailure { error ->
                    _events.emit(CartEvent.ShowMessage(error.message ?: "Bekleyen sepet silinemedi"))
                }
        }
    }

    fun deleteHeldCart(cartId: String) {
        viewModelScope.launch {
            heldCartRepository.deleteHeldCart(cartId)
                .onSuccess { _events.emit(CartEvent.ShowMessage("Bekleyen sepet silindi")) }
                .onFailure { error ->
                    _events.emit(CartEvent.ShowMessage(error.message ?: "Bekleyen sepet silinemedi"))
                }
        }
    }

    private fun syncLocalSaleToCloud(saleId: Long) {
        viewModelScope.launch {
            val accessToken = accountSessionRepository.getAccessToken() ?: return@launch
            val activation = activationRepository.getActivationState()
            val companyCode = activation.companyCode?.takeIf { it.isNotBlank() } ?: return@launch
            val deviceUid = activation.deviceUid?.takeIf { it.isNotBlank() } ?: activationRepository.getDeviceUid()
            val deviceName = activation.deviceName?.takeIf { it.isNotBlank() } ?: activationRepository.getDeviceName()

            val sale = saleRepository.getSaleById(saleId) ?: return@launch
            val saleItems = saleRepository.getSaleItems(saleId)
            if (saleItems.isEmpty()) return@launch

            val payload = MobilePosSaleSyncPayload(
                localSaleId = sale.saleId,
                createdAt = sale.createdAt,
                totalItems = sale.itemCount,
                totalAmountKurus = sale.totalAmountKurus,
                totalCostKurus = sale.totalCostKurus,
                profitKurus = sale.profitKurus,
                paymentMethod = "cash",
                items = saleItems.map { item ->
                    MobilePosSaleSyncItem(
                        barcode = item.productBarcode,
                        productName = item.productNameSnapshot,
                        quantity = item.quantity,
                        unitSalePriceKurus = item.unitSalePriceKurusSnapshot,
                        unitCostPriceKurus = item.unitCostPriceKurusSnapshot,
                        lineTotalKurus = item.lineTotalKurus,
                        lineProfitKurus = item.lineTotalKurus - item.lineCostKurus
                    )
                }
            )

            webSaleCompanionRepository.publishMobilePosSale(
                accessToken = accessToken,
                companyCode = companyCode,
                deviceUid = deviceUid,
                deviceName = deviceName,
                payload = payload
            )
        }
    }
}

