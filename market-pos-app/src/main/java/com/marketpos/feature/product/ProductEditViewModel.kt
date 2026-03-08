package com.marketpos.feature.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.core.util.DateUtils
import com.marketpos.core.util.MoneyUtils
import com.marketpos.domain.model.NameSuggestion
import com.marketpos.domain.model.PremiumFeature
import com.marketpos.domain.model.Product
import com.marketpos.domain.repository.NameSuggestionRepository
import com.marketpos.domain.repository.PremiumRepository
import com.marketpos.domain.repository.ProductRepository
import com.marketpos.domain.usecase.CalculateTargetMarginSalePriceUseCase
import com.marketpos.domain.usecase.CreateOrUpdateProductUseCase
import com.marketpos.domain.usecase.SuggestMinStockUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ProductEditField {
    BARCODE,
    NAME,
    SALE_PRICE,
    COST_PRICE,
    STOCK_QTY,
    MIN_STOCK_QTY,
    NOTE
}

data class ProductEditFieldErrors(
    val barcode: String? = null,
    val name: String? = null,
    val salePrice: String? = null,
    val costPrice: String? = null,
    val stockQty: String? = null,
    val minStockQty: String? = null
) {
    fun clear(field: ProductEditField): ProductEditFieldErrors {
        return when (field) {
            ProductEditField.BARCODE -> copy(barcode = null)
            ProductEditField.NAME -> copy(name = null)
            ProductEditField.SALE_PRICE -> copy(salePrice = null)
            ProductEditField.COST_PRICE -> copy(costPrice = null)
            ProductEditField.STOCK_QTY -> copy(stockQty = null)
            ProductEditField.MIN_STOCK_QTY -> copy(minStockQty = null)
            ProductEditField.NOTE -> this
        }
    }

    fun hasAnyError(): Boolean {
        return barcode != null ||
            name != null ||
            salePrice != null ||
            costPrice != null ||
            stockQty != null ||
            minStockQty != null
    }
}

data class ProductEditUiState(
    val barcode: String = "",
    val name: String = "",
    val groupName: String = "",
    val salePriceInput: String = "",
    val costPriceInput: String = "",
    val stockQtyInput: String = "",
    val minStockQtyInput: String = "",
    val targetMarginInput: String = "20",
    val currentMarginLabel: String? = null,
    val minStockSuggestionLabel: String? = null,
    val note: String = "",
    val isEditMode: Boolean = false,
    val isLoadingNameSuggestion: Boolean = false,
    val selectedSuggestionSourceLabel: String? = null,
    val suggestions: List<NameSuggestion> = emptyList(),
    val duplicateProduct: DuplicateProductPreview? = null,
    val canUsePremiumNameTools: Boolean = false,
    val isSaving: Boolean = false,
    val errors: ProductEditFieldErrors = ProductEditFieldErrors(),
    val error: String? = null
)

data class DuplicateProductPreview(
    val barcode: String,
    val name: String,
    val salePriceLabel: String,
    val stockLabel: String
)

sealed interface ProductEditEvent {
    data object ShowSavedDialog : ProductEditEvent
    data object NavigateScanForBarcode : ProductEditEvent
    data class OpenExistingProduct(val barcode: String) : ProductEditEvent
    data class ShowMessage(val message: String) : ProductEditEvent
    data class FocusField(val field: ProductEditField) : ProductEditEvent
}

private data class ProductEditValidationResult(
    val errors: ProductEditFieldErrors,
    val firstInvalidField: ProductEditField?
)

@HiltViewModel
class ProductEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val createOrUpdateProductUseCase: CreateOrUpdateProductUseCase,
    private val nameSuggestionRepository: NameSuggestionRepository,
    private val premiumRepository: PremiumRepository,
    private val suggestMinStockUseCase: SuggestMinStockUseCase,
    private val calculateTargetMarginSalePriceUseCase: CalculateTargetMarginSalePriceUseCase
) : ViewModel() {

    private val initialBarcode = savedStateHandle.get<String>("barcode").orEmpty()
    private val prefillName = savedStateHandle.get<String>("prefillName").orEmpty().trim()
    private val prefillSalePriceKurus = savedStateHandle.get<String>("prefillSalePrice").orEmpty().toLongOrNull()
    private var originalBarcode: String? = null

    private val _uiState = MutableStateFlow(
        ProductEditUiState(
            barcode = initialBarcode,
            name = prefillName,
            salePriceInput = prefillSalePriceKurus?.toTlInput().orEmpty()
        )
    )
    val uiState: StateFlow<ProductEditUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProductEditEvent>()
    val events: SharedFlow<ProductEditEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            premiumRepository.observeState().collect { premiumState ->
                _uiState.value = _uiState.value.copy(canUsePremiumNameTools = premiumState.isPro)
            }
        }
        viewModelScope.launch {
            when {
                initialBarcode.isBlank() -> {
                    val firstField = when {
                        prefillName.isNotBlank() && prefillSalePriceKurus != null -> ProductEditField.COST_PRICE
                        prefillName.isNotBlank() -> ProductEditField.SALE_PRICE
                        else -> ProductEditField.BARCODE
                    }
                    _events.emit(ProductEditEvent.FocusField(firstField))
                }

                else -> {
                    val existing = productRepository.getByBarcode(initialBarcode)
                    if (existing != null) {
                        originalBarcode = existing.barcode
                        _uiState.value = existing.toUiState()
                        _events.emit(ProductEditEvent.FocusField(ProductEditField.SALE_PRICE))
                    } else {
                        when {
                            prefillName.isNotBlank() && prefillSalePriceKurus != null -> {
                                _events.emit(ProductEditEvent.FocusField(ProductEditField.COST_PRICE))
                            }

                            prefillName.isNotBlank() -> {
                                _events.emit(ProductEditEvent.FocusField(ProductEditField.SALE_PRICE))
                            }

                            else -> {
                                tryNameSuggestion()
                            }
                        }
                    }
                }
            }
        }
    }

    fun updateBarcode(value: String) {
        _uiState.value = _uiState.value.copy(
            barcode = value.filter(Char::isDigit).take(32),
            selectedSuggestionSourceLabel = null,
            suggestions = emptyList(),
            duplicateProduct = null,
            errors = _uiState.value.errors.clear(ProductEditField.BARCODE),
            error = null
        )
    }

    fun updateName(value: String) {
        _uiState.value = _uiState.value.copy(
            name = value,
            errors = _uiState.value.errors.clear(ProductEditField.NAME),
            error = null
        )
    }

    fun updateGroupName(value: String) {
        _uiState.value = _uiState.value.copy(groupName = value.take(40), error = null)
    }

    fun updateSalePrice(value: String) {
        _uiState.value = _uiState.value.copy(
            salePriceInput = sanitizePriceInput(value),
            errors = _uiState.value.errors.clear(ProductEditField.SALE_PRICE),
            error = null
        ).withCurrentMargin()
    }

    fun updateCostPrice(value: String) {
        _uiState.value = _uiState.value.copy(
            costPriceInput = sanitizePriceInput(value),
            errors = _uiState.value.errors.clear(ProductEditField.COST_PRICE),
            error = null
        ).withCurrentMargin()
    }

    fun updateStockQty(value: String) {
        _uiState.value = _uiState.value.copy(
            stockQtyInput = value.filter(Char::isDigit),
            errors = _uiState.value.errors.clear(ProductEditField.STOCK_QTY),
            error = null
        )
    }

    fun updateMinStockQty(value: String) {
        _uiState.value = _uiState.value.copy(
            minStockQtyInput = value.filter(Char::isDigit),
            errors = _uiState.value.errors.clear(ProductEditField.MIN_STOCK_QTY),
            error = null
        )
    }

    fun updateTargetMargin(value: String) {
        _uiState.value = _uiState.value.copy(
            targetMarginInput = value.filter { it.isDigit() || it == ',' || it == '.' },
            error = null
        )
    }

    fun updateNote(value: String) {
        _uiState.value = _uiState.value.copy(note = value, error = null)
    }

    fun requestBarcodeScan() {
        viewModelScope.launch { _events.emit(ProductEditEvent.NavigateScanForBarcode) }
    }

    fun onBarcodeScanned(barcode: String) {
        val normalizedBarcode = barcode.filter(Char::isDigit).take(32)
        _uiState.value = _uiState.value.copy(
            barcode = normalizedBarcode,
            selectedSuggestionSourceLabel = null,
            suggestions = emptyList(),
            duplicateProduct = null,
            errors = _uiState.value.errors.clear(ProductEditField.BARCODE),
            error = null
        )
        viewModelScope.launch {
            val existing = productRepository.getByBarcode(normalizedBarcode)
            if (existing != null && (originalBarcode == null || existing.barcode != originalBarcode)) {
                _uiState.value = _uiState.value.copy(duplicateProduct = existing.toDuplicatePreview())
                return@launch
            }
            tryNameSuggestion()
        }
    }

    fun dismissDuplicateProduct() {
        _uiState.value = _uiState.value.copy(duplicateProduct = null)
    }

    fun editDuplicateProduct() {
        val duplicateBarcode = _uiState.value.duplicateProduct?.barcode ?: return
        dismissDuplicateProduct()
        viewModelScope.launch {
            _events.emit(ProductEditEvent.OpenExistingProduct(duplicateBarcode))
        }
    }

    fun rescanAfterDuplicate() {
        dismissDuplicateProduct()
        requestBarcodeScan()
    }

    fun applySuggestion(name: String) {
        val source = _uiState.value.suggestions.firstOrNull { it.name == name }?.sourceLabel
        _uiState.value = _uiState.value.copy(
            name = name,
            selectedSuggestionSourceLabel = source,
            errors = _uiState.value.errors.clear(ProductEditField.NAME),
            error = null
        )
        viewModelScope.launch {
            _events.emit(ProductEditEvent.FocusField(ProductEditField.SALE_PRICE))
        }
    }

    fun onOcrSuggestions(names: List<String>) {
        val suggestions = names
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .mapIndexed { index, name ->
                NameSuggestion(
                    name = name,
                    sourceLabel = if (index == 0) "Ambalaj OCR (Secilen)" else "Ambalaj OCR"
                )
            }

        if (suggestions.isEmpty()) return

        mergeSuggestions(
            incoming = suggestions,
            autoFillIfEmpty = false
        )

        val preferred = suggestions.first()
        _uiState.value = _uiState.value.copy(
            name = preferred.name,
            selectedSuggestionSourceLabel = preferred.sourceLabel,
            errors = _uiState.value.errors.clear(ProductEditField.NAME),
            error = null
        )
    }

    fun applyTargetMarginPreset(percent: Int) {
        _uiState.value = _uiState.value.copy(targetMarginInput = percent.toString())
        applyTargetMargin()
    }

    fun applyTargetMargin() {
        val costPriceKurus = MoneyUtils.parseTlInputToKurus(_uiState.value.costPriceInput)
        val marginPercent = _uiState.value.targetMarginInput.replace(",", ".").toDoubleOrNull()
        if (costPriceKurus == null || costPriceKurus < 0L) {
            viewModelScope.launch {
                _events.emit(ProductEditEvent.ShowMessage("Önce geçerli bir alış fiyatı girin"))
                _events.emit(ProductEditEvent.FocusField(ProductEditField.COST_PRICE))
            }
            return
        }
        if (marginPercent == null) {
            viewModelScope.launch {
                _events.emit(ProductEditEvent.ShowMessage("Geçerli bir kâr marjı girin"))
            }
            return
        }

        calculateTargetMarginSalePriceUseCase(costPriceKurus, marginPercent)
            .onSuccess { salePrice ->
                _uiState.value = _uiState.value.copy(
                    salePriceInput = salePrice.toTlInput(),
                    errors = _uiState.value.errors.clear(ProductEditField.SALE_PRICE),
                    error = null
                ).withCurrentMargin()
            }
            .onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message ?: "Hedef kâr hesabı yapılamadı")
            }
    }

    fun suggestMinStock() {
        val barcode = _uiState.value.barcode.trim()
        if (barcode.isBlank()) {
            viewModelScope.launch {
                _events.emit(ProductEditEvent.ShowMessage("Min stok önerisi için önce barkod girin"))
                _events.emit(ProductEditEvent.FocusField(ProductEditField.BARCODE))
            }
            return
        }

        viewModelScope.launch {
            suggestMinStockUseCase(barcode)
                .onSuccess { suggestion ->
                    _uiState.value = _uiState.value.copy(
                        minStockQtyInput = suggestion.suggestedMinStock.toString(),
                        minStockSuggestionLabel = if (suggestion.usedFallback) {
                            "Son 30 günde satış verisi yok. Varsayılan minimum stok önerisi: ${suggestion.suggestedMinStock}"
                        } else {
                            "Son 30 günde ${suggestion.soldQuantityLast30Days} adet satıldı. Öneri: ${suggestion.suggestedMinStock}"
                        },
                        errors = _uiState.value.errors.clear(ProductEditField.MIN_STOCK_QTY),
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        minStockSuggestionLabel = null,
                        error = error.message ?: "Min stok önerisi alınamadı"
                    )
                }
        }
    }

    fun tryNameSuggestion() {
        if (!_uiState.value.canUsePremiumNameTools) {
            viewModelScope.launch { _events.emit(ProductEditEvent.FocusField(ProductEditField.NAME)) }
            return
        }
        val rawBarcode = _uiState.value.barcode
        val barcode = rawBarcode.filter(Char::isDigit).ifBlank { rawBarcode.trim() }
        if (barcode.isBlank()) {
            viewModelScope.launch {
                _events.emit(ProductEditEvent.ShowMessage("Önce barkod girin veya tarayın"))
                _events.emit(ProductEditEvent.FocusField(ProductEditField.BARCODE))
            }
            return
        }
        if (_uiState.value.name.isNotBlank()) {
            viewModelScope.launch { _events.emit(ProductEditEvent.FocusField(ProductEditField.SALE_PRICE)) }
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                barcode = barcode,
                isLoadingNameSuggestion = true,
                error = null
            )
            val suggestions = nameSuggestionRepository.suggestNames(barcode)
            _uiState.value = _uiState.value.copy(isLoadingNameSuggestion = false)
            mergeSuggestions(
                incoming = suggestions,
                autoFillIfEmpty = _uiState.value.name.isBlank()
            )
            val bestSuggestion = _uiState.value.suggestions.firstOrNull()
            if (!bestSuggestion?.name.isNullOrBlank()) {
                val sourceLabel = bestSuggestion?.sourceLabel.orEmpty()
                _events.emit(ProductEditEvent.ShowMessage("İsim önerisi: $sourceLabel"))
                _events.emit(ProductEditEvent.FocusField(ProductEditField.SALE_PRICE))
            } else {
                _events.emit(ProductEditEvent.FocusField(ProductEditField.NAME))
            }
        }
    }

    fun save() {
        val state = _uiState.value
        val barcode = state.barcode.trim()
        val salePrice = MoneyUtils.parseTlInputToKurus(state.salePriceInput)
        val costPrice = MoneyUtils.parseTlInputToKurus(state.costPriceInput)
        val stockQty = state.stockQtyInput.toIntOrNull()
        val minStock = state.minStockQtyInput.toIntOrNull()
        val validation = validate(
            barcode = barcode,
            name = state.name,
            salePrice = salePrice,
            costPrice = costPrice,
            stockQty = stockQty,
            minStock = minStock
        )

        if (validation.errors.hasAnyError()) {
            _uiState.value = state.copy(
                errors = validation.errors,
                error = "Lütfen eksik veya hatalı alanları düzeltin"
            )
            viewModelScope.launch {
                validation.firstInvalidField?.let { _events.emit(ProductEditEvent.FocusField(it)) }
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            val existing = when {
                !originalBarcode.isNullOrBlank() -> productRepository.getByBarcode(originalBarcode!!)
                else -> productRepository.getByBarcode(barcode)
            }
            val now = DateUtils.now()
            val product = Product(
                barcode = barcode,
                name = state.name.trim(),
                groupName = state.groupName.trim().ifBlank { null },
                salePriceKurus = salePrice!!,
                costPriceKurus = costPrice!!,
                stockQty = stockQty!!,
                minStockQty = minStock!!,
                note = state.note.trim().ifBlank { null },
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
                isActive = true
            )
            createOrUpdateProductUseCase(product, originalBarcode)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    _events.emit(ProductEditEvent.ShowSavedDialog)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = mapSaveError(error)
                    )
                }
        }
    }

    private fun mapSaveError(error: Throwable): String {
        val message = error.message.orEmpty()
        return when {
            message.contains("FOREIGN KEY", ignoreCase = true) ->
                "Bu ürün daha önce satışlarda kullanıldığı için kayıt güncellenemedi. Sorun düzeltildi; yeniden deneyin."
            message.isNotBlank() -> message
            else -> "Ürün kaydedilemedi"
        }
    }

    private fun validate(
        barcode: String,
        name: String,
        salePrice: Long?,
        costPrice: Long?,
        stockQty: Int?,
        minStock: Int?
    ): ProductEditValidationResult {
        var errors = ProductEditFieldErrors()
        var firstInvalid: ProductEditField? = null

        fun register(field: ProductEditField, message: String) {
            if (firstInvalid == null) firstInvalid = field
            errors = when (field) {
                ProductEditField.BARCODE -> errors.copy(barcode = message)
                ProductEditField.NAME -> errors.copy(name = message)
                ProductEditField.SALE_PRICE -> errors.copy(salePrice = message)
                ProductEditField.COST_PRICE -> errors.copy(costPrice = message)
                ProductEditField.STOCK_QTY -> errors.copy(stockQty = message)
                ProductEditField.MIN_STOCK_QTY -> errors.copy(minStockQty = message)
                ProductEditField.NOTE -> errors
            }
        }

        if (barcode.isBlank()) register(ProductEditField.BARCODE, "Barkod zorunludur")
        if (name.isBlank()) register(ProductEditField.NAME, "Ürün adı zorunludur")
        if (salePrice == null || salePrice <= 0L) register(ProductEditField.SALE_PRICE, "Geçerli bir satış fiyatı girin")
        if (costPrice == null || costPrice < 0L) register(ProductEditField.COST_PRICE, "Geçerli bir alış fiyatı girin")
        if (stockQty == null) register(ProductEditField.STOCK_QTY, "Stok miktarı zorunludur")
        if (minStock == null) register(ProductEditField.MIN_STOCK_QTY, "Minimum stok zorunludur")
        if (stockQty != null && stockQty < 0) register(ProductEditField.STOCK_QTY, "Stok negatif olamaz")
        if (minStock != null && minStock < 0) register(ProductEditField.MIN_STOCK_QTY, "Minimum stok negatif olamaz")

        return ProductEditValidationResult(errors, firstInvalid)
    }

    private fun sanitizePriceInput(value: String): String {
        val output = StringBuilder()
        var separatorAdded = false
        var decimals = 0

        value.forEach { char ->
            when {
                char.isDigit() && (!separatorAdded || decimals < 2) -> {
                    output.append(char)
                    if (separatorAdded) decimals++
                }

                (char == ',' || char == '.') && !separatorAdded -> {
                    if (output.isEmpty()) output.append('0')
                    output.append(',')
                    separatorAdded = true
                }
            }
        }

        return output.toString()
    }

    private fun Product.toUiState(): ProductEditUiState = ProductEditUiState(
        barcode = barcode,
        name = name,
        groupName = groupName.orEmpty(),
        salePriceInput = salePriceKurus.toTlInput(),
        costPriceInput = costPriceKurus.toTlInput(),
        stockQtyInput = stockQty.toString(),
        minStockQtyInput = minStockQty.toString(),
        note = note.orEmpty(),
        isEditMode = true,
        selectedSuggestionSourceLabel = null,
        suggestions = emptyList()
    ).withCurrentMargin()

    private fun Product.toDuplicatePreview(): DuplicateProductPreview {
        return DuplicateProductPreview(
            barcode = barcode,
            name = name,
            salePriceLabel = MoneyUtils.formatKurus(salePriceKurus),
            stockLabel = "$stockQty adet"
        )
    }

    private fun mergeSuggestions(
        incoming: List<NameSuggestion>,
        autoFillIfEmpty: Boolean
    ) {
        if (incoming.isEmpty()) return

        val merged = (_uiState.value.suggestions + incoming)
            .filter { it.name.isNotBlank() }
            .distinctBy { it.name.uppercase() }
            .sortedWith(
                compareByDescending<NameSuggestion> { sourcePriority(it.sourceLabel) }
                    .thenBy { it.name.length }
            )
            .take(5)

        val autoSelected = when {
            autoFillIfEmpty -> merged.firstOrNull()
            else -> merged.firstOrNull { it.name.equals(_uiState.value.name, ignoreCase = true) }
        }

        _uiState.value = _uiState.value.copy(
            suggestions = merged,
            name = if (autoFillIfEmpty) autoSelected?.name.orEmpty() else _uiState.value.name,
            selectedSuggestionSourceLabel = autoSelected?.sourceLabel ?: _uiState.value.selectedSuggestionSourceLabel
        )
    }

    private fun sourcePriority(sourceLabel: String): Int {
        return when (sourceLabel) {
            "barkod.space Katalog" -> 400
            "BarkodBankası" -> 300
            "Ambalaj OCR (Secilen)" -> 280
            "Ambalaj OCR" -> 260
            "Web Analiz" -> 200
            else -> 0
        }
    }

    private fun Long.toTlInput(): String {
        return BigDecimal(this)
            .divide(BigDecimal(100))
            .stripTrailingZeros()
            .toPlainString()
            .replace('.', ',')
    }

    private fun ProductEditUiState.withCurrentMargin(): ProductEditUiState {
        return copy(currentMarginLabel = calculateCurrentMarginLabel(salePriceInput, costPriceInput))
    }

    private fun calculateCurrentMarginLabel(
        salePriceInput: String,
        costPriceInput: String
    ): String? {
        val salePriceKurus = MoneyUtils.parseTlInputToKurus(salePriceInput)
        val costPriceKurus = MoneyUtils.parseTlInputToKurus(costPriceInput)
        if (salePriceKurus == null || costPriceKurus == null || salePriceKurus <= 0L) return null

        val sale = BigDecimal.valueOf(salePriceKurus)
        val cost = BigDecimal.valueOf(costPriceKurus)
        val profit = sale.subtract(cost)
        val marginPercent = profit
            .multiply(BigDecimal(100))
            .divide(sale, 2, RoundingMode.HALF_UP)

        return "%${marginPercent.toPlainString()}"
    }
}


