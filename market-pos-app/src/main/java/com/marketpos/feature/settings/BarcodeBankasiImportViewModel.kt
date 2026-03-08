package com.marketpos.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.core.util.MoneyUtils
import com.marketpos.domain.model.BarcodeBankasiGroup
import com.marketpos.domain.model.BarcodeBankasiImportItem
import com.marketpos.domain.model.BarcodeBankasiPreviewResult
import com.marketpos.domain.repository.BarcodeBankasiImportRepository
import com.marketpos.domain.repository.ProductRepository
import com.marketpos.domain.usecase.ImportBarcodeBankasiProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val FILTERED_PREVIEW_BATCH_SIZE = 200
private const val FILTERED_PREVIEW_MAX_BATCHES = 12

data class BarcodeBankasiPreviewRowUi(
    val barcode: String,
    val name: String,
    val salePriceLabel: String,
    val sourcePage: Int,
    val lastChangedAt: String?,
    val isExisting: Boolean
)

data class BarcodeBankasiImportUiState(
    val groups: List<BarcodeBankasiGroup> = listOf(BarcodeBankasiGroup("", "Tum Gruplar")),
    val selectedGroup: String = "",
    val query: String = "",
    val startPageInput: String = "1",
    val itemCountInput: String = "100",
    val isGroupsLoading: Boolean = true,
    val isPreviewLoading: Boolean = false,
    val isImporting: Boolean = false,
    val hideExisting: Boolean = false,
    val previewRows: List<BarcodeBankasiPreviewRowUi> = emptyList(),
    val selectedBarcodes: Set<String> = emptySet(),
    val existingBarcodes: Set<String> = emptySet(),
    val previewSummary: String? = null,
    val error: String? = null
) {
    val visiblePreviewRows: List<BarcodeBankasiPreviewRowUi>
        get() = if (hideExisting) previewRows.filterNot { it.isExisting } else previewRows

    val selectedCount: Int
        get() = visiblePreviewRows.count { selectedBarcodes.contains(it.barcode) }

    val existingCount: Int
        get() = previewRows.count { it.isExisting }

    val canImport: Boolean
        get() = visiblePreviewRows.any { selectedBarcodes.contains(it.barcode) }
}

sealed interface BarcodeBankasiImportEvent {
    data class ShowMessage(val message: String) : BarcodeBankasiImportEvent
}

@HiltViewModel
class BarcodeBankasiImportViewModel @Inject constructor(
    private val importRepository: BarcodeBankasiImportRepository,
    private val productRepository: ProductRepository,
    private val importBarcodeBankasiProductsUseCase: ImportBarcodeBankasiProductsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BarcodeBankasiImportUiState())
    val uiState: StateFlow<BarcodeBankasiImportUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<BarcodeBankasiImportEvent>()
    val events: SharedFlow<BarcodeBankasiImportEvent> = _events.asSharedFlow()

    private var lastPreviewItems: List<BarcodeBankasiImportItem> = emptyList()

    private data class PreparedPreview(
        val items: List<BarcodeBankasiImportItem>,
        val rows: List<BarcodeBankasiPreviewRowUi>,
        val existingBarcodes: Set<String>,
        val fetchedPages: Int,
        val totalAvailable: Int?
    )

    init {
        loadGroups()
    }

    fun updateQuery(value: String) {
        _uiState.value = _uiState.value.copy(query = value, error = null)
    }

    fun updateSelectedGroup(value: String) {
        _uiState.value = _uiState.value.copy(selectedGroup = value, error = null)
    }

    fun updateStartPage(value: String) {
        _uiState.value = _uiState.value.copy(
            startPageInput = value.filter(Char::isDigit).ifBlank { "" },
            error = null
        )
    }

    fun updateItemCount(value: String) {
        _uiState.value = _uiState.value.copy(
            itemCountInput = value.filter(Char::isDigit).ifBlank { "" },
            error = null
        )
    }

    fun selectItemCount(count: Int) {
        _uiState.value = _uiState.value.copy(itemCountInput = count.toString(), error = null)
    }

    fun toggleHideExisting() {
        val state = _uiState.value
        val hideExisting = !state.hideExisting
        val selected = if (hideExisting) {
            state.selectedBarcodes - state.existingBarcodes
        } else {
            state.selectedBarcodes
        }
        _uiState.value = state.copy(
            hideExisting = hideExisting,
            selectedBarcodes = selected
        )
        val requestedVisibleCount = state.itemCountInput.toIntOrNull() ?: return
        if (hideExisting && _uiState.value.visiblePreviewRows.size < requestedVisibleCount) {
            loadPreview()
        }
    }

    fun toggleSelection(barcode: String) {
        val current = _uiState.value.selectedBarcodes.toMutableSet()
        if (!current.add(barcode)) current.remove(barcode)
        _uiState.value = _uiState.value.copy(selectedBarcodes = current)
    }

    fun selectAll() {
        val all = _uiState.value.visiblePreviewRows.map { it.barcode }.toSet()
        _uiState.value = _uiState.value.copy(selectedBarcodes = all)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedBarcodes = emptySet())
    }

    fun loadPreview() {
        val state = _uiState.value
        val query = state.query.trim().ifBlank { null }
        val group = state.selectedGroup.ifBlank { null }
        val startPage = state.startPageInput.toIntOrNull()
        val itemCount = state.itemCountInput.toIntOrNull()

        if (group == null && query == null) {
            _uiState.value = state.copy(error = "En az bir grup secin veya arama metni girin")
            return
        }
        if (startPage == null || startPage < 1) {
            _uiState.value = state.copy(error = "Başlangıç sayfası 1 veya büyük olmalı")
            return
        }
        if (itemCount == null || itemCount !in 1..1000) {
            _uiState.value = state.copy(error = "Adet 1 ile 1000 arasında olmalı")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(
                isPreviewLoading = true,
                error = null,
                previewSummary = null,
                previewRows = emptyList(),
                selectedBarcodes = emptySet(),
                existingBarcodes = emptySet()
            )
            preparePreview(
                query = query,
                group = group,
                startPage = startPage,
                itemCount = itemCount,
                hideExisting = state.hideExisting
            ).onSuccess { preview ->
                lastPreviewItems = preview.items
                val selected = preview.rows.map { it.barcode }.toSet()
                    .minus(if (state.hideExisting) preview.existingBarcodes else emptySet())

                _uiState.value = _uiState.value.copy(
                    isPreviewLoading = false,
                    previewRows = preview.rows,
                    selectedBarcodes = selected,
                    existingBarcodes = preview.existingBarcodes,
                    previewSummary = preview.toSummaryText(
                        group = group,
                        hideExisting = state.hideExisting,
                        requestedVisibleCount = itemCount
                    )
                )
                if (preview.rows.isEmpty() || preview.rows.none { !it.isExisting }) {
                        _events.emit(BarcodeBankasiImportEvent.ShowMessage("Ürün bulunamadı"))
                    }
                }
                .onFailure { error ->
                    lastPreviewItems = emptyList()
                    _uiState.value = _uiState.value.copy(
                        isPreviewLoading = false,
                        previewRows = emptyList(),
                        selectedBarcodes = emptySet(),
                        existingBarcodes = emptySet(),
                        previewSummary = null,
                        error = error.message ?: "Önizleme getirilemedi"
                    )
                }
        }
    }

    private suspend fun preparePreview(
        query: String?,
        group: String?,
        startPage: Int,
        itemCount: Int,
        hideExisting: Boolean
    ): Result<PreparedPreview> {
        return if (hideExisting) {
            prepareFilteredPreview(query, group, startPage, itemCount)
        } else {
            prepareRegularPreview(query, group, startPage, itemCount)
        }
    }

    private suspend fun prepareRegularPreview(
        query: String?,
        group: String?,
        startPage: Int,
        itemCount: Int
    ): Result<PreparedPreview> {
        return importRepository.fetchPreview(query, group, startPage, itemCount)
            .mapCatching { preview ->
                val existingBarcodes = productRepository
                    .listByBarcodes(preview.items.map { it.barcode })
                    .map { it.barcode }
                    .toSet()
                PreparedPreview(
                    items = preview.items,
                    rows = preview.items.map { item ->
                        item.toUi(isExisting = existingBarcodes.contains(item.barcode))
                    },
                    existingBarcodes = existingBarcodes,
                    fetchedPages = preview.fetchedPages,
                    totalAvailable = preview.totalAvailable
                )
            }
    }

    private suspend fun prepareFilteredPreview(
        query: String?,
        group: String?,
        startPage: Int,
        itemCount: Int
    ): Result<PreparedPreview> = runCatching {
        val preparedItems = mutableListOf<BarcodeBankasiImportItem>()
        val preparedRows = mutableListOf<BarcodeBankasiPreviewRowUi>()
        val existingBarcodes = linkedSetOf<String>()
        val seenBarcodes = linkedSetOf<String>()
        var totalAvailable: Int? = null
        var totalFetchedPages = 0
        var currentPage = startPage
        var batchCount = 0
        val requestedVisibleCount = itemCount

        while (preparedRows.count { !it.isExisting } < requestedVisibleCount && batchCount < FILTERED_PREVIEW_MAX_BATCHES) {
            val batchPreview = importRepository.fetchPreview(
                query = query,
                group = group,
                startPage = currentPage,
                requestedItemCount = FILTERED_PREVIEW_BATCH_SIZE
            ).getOrThrow()

            if (totalAvailable == null) {
                totalAvailable = batchPreview.totalAvailable
            }
            if (batchPreview.items.isEmpty() || batchPreview.fetchedPages <= 0) {
                break
            }

            val uniqueBatchItems = batchPreview.items.filter { seenBarcodes.add(it.barcode) }
            if (uniqueBatchItems.isNotEmpty()) {
                val existingBatchBarcodes = productRepository
                    .listByBarcodes(uniqueBatchItems.map { it.barcode })
                    .map { it.barcode }
                    .toSet()

                existingBarcodes += existingBatchBarcodes
                uniqueBatchItems.forEach { item ->
                    preparedItems += item
                    preparedRows += item.toUi(isExisting = existingBatchBarcodes.contains(item.barcode))
                }
            }

            totalFetchedPages += batchPreview.fetchedPages
            currentPage += batchPreview.fetchedPages
            batchCount++
        }

        PreparedPreview(
            items = preparedItems,
            rows = preparedRows,
            existingBarcodes = existingBarcodes,
            fetchedPages = totalFetchedPages,
            totalAvailable = totalAvailable
        )
    }

    fun importPreview() {
        val state = _uiState.value
        val selected = state.selectedBarcodes
        val hiddenExisting = if (state.hideExisting) state.existingBarcodes else emptySet()
        val itemsToImport = lastPreviewItems.filter {
            selected.contains(it.barcode) && !hiddenExisting.contains(it.barcode)
        }
        if (itemsToImport.isEmpty()) {
            viewModelScope.launch {
                _events.emit(BarcodeBankasiImportEvent.ShowMessage("En az bir urun secin"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isImporting = true, error = null)
            importBarcodeBankasiProductsUseCase(itemsToImport)
                .onSuccess { summary ->
                    _uiState.value = _uiState.value.copy(isImporting = false)
                    _events.emit(
                        BarcodeBankasiImportEvent.ShowMessage(
                            "${summary.importedCount} urun iceri aktarildi. " +
                                "${summary.createdCount} yeni, ${summary.updatedCount} güncellendi."
                        )
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        error = error.message ?: "İçe aktarma başarısız"
                    )
                }
        }
    }

    private fun loadGroups() {
        viewModelScope.launch {
            importRepository.fetchGroups()
                .onSuccess { groups ->
                    _uiState.value = _uiState.value.copy(
                        groups = if (groups.isEmpty()) _uiState.value.groups else groups,
                        isGroupsLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isGroupsLoading = false)
                }
        }
    }

    private fun BarcodeBankasiImportItem.toUi(isExisting: Boolean): BarcodeBankasiPreviewRowUi {
        return BarcodeBankasiPreviewRowUi(
            barcode = barcode,
            name = name,
            salePriceLabel = MoneyUtils.formatKurus(salePriceKurus),
            sourcePage = sourcePage,
            lastChangedAt = lastChangedAt,
            isExisting = isExisting
        )
    }

    private fun PreparedPreview.toSummaryText(
        group: String?,
        hideExisting: Boolean,
        requestedVisibleCount: Int
    ): String {
        val totalText = totalAvailable?.let { "Kaynakta yaklasik $it urun var." } ?: "Kaynak toplami okunamadi."
        val groupText = group?.takeIf { it.isNotBlank() }?.let { "Grup: $it. " }.orEmpty()
        val existingCount = rows.count { it.isExisting }
        val visibleCount = rows.count { !it.isExisting }
        val existingText = if (existingCount > 0) " Gizlenen/mevcut: $existingCount." else ""
        val visibleText = if (hideExisting) {
            "Gorunen yeni urun: ${visibleCount.coerceAtMost(requestedVisibleCount)}."
        } else {
            "Listelenen urun: ${rows.size}."
        }
        return "$visibleText ${groupText}$fetchedPages sayfa tarandi. $totalText$existingText"
    }
}


