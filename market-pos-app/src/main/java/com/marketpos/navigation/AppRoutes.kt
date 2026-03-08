package com.marketpos.navigation

import android.net.Uri

object AppRoutes {
    const val BOOTSTRAP = "bootstrap"
    const val SESSION_ENTRY = "session_entry"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val ACTIVATION = "activation"
    const val MODE_SELECTION = "mode_selection"
    const val WEB_COMPANION = "web_companion"
    const val SCAN = "scan"
    const val SERIAL_SCAN = "serial_scan"
    const val NOT_FOUND = "not_found/{barcode}"
    const val PRODUCT_DETAIL = "product_detail/{barcode}"
    const val PRODUCT_EDIT = "product_edit?barcode={barcode}&prefillName={prefillName}&prefillSalePrice={prefillSalePrice}"
    const val SCAN_FOR_PRODUCT = "scan_for_product"
    const val PACKAGE_TEXT_SCAN = "package_text_scan?barcode={barcode}"
    const val CART = "cart"
    const val SALE_SUCCESS = "sale_success/{saleId}"
    const val REPORTS = "reports"
    const val SALES_REPORTS = "sales_reports?section={section}"
    const val STOCK_TRACKING = "stock_tracking"
    const val STOCK_COUNT = "stock_count"
    const val SETTINGS = "settings"
    const val PREMIUM = "premium?feature={feature}"
    const val PRODUCT_LIST = "product_list"
    const val BULK_PRICE_UPDATE = "bulk_price_update"
    const val BULK_STOCK_UPDATE = "bulk_stock_update"
    const val BARKOD_BANKASI_IMPORT = "barkod_bankasi_import"
    const val WEB_BARCODE_SEARCH = "web_barcode_search"
    const val SUPPORT = "support"

    fun notFound(barcode: String): String = "not_found/$barcode"
    fun productDetail(barcode: String): String = "product_detail/$barcode"
    fun productEdit(
        barcode: String? = null,
        prefillName: String? = null,
        prefillSalePrice: Long? = null
    ): String {
        val encodedName = Uri.encode(prefillName.orEmpty())
        return "product_edit?barcode=${barcode.orEmpty()}&prefillName=$encodedName&prefillSalePrice=${prefillSalePrice ?: ""}"
    }

    fun saleSuccess(saleId: Long): String = "sale_success/$saleId"

    fun salesReports(section: String? = null): String {
        return "sales_reports?section=${Uri.encode(section.orEmpty())}"
    }

    fun premium(feature: String? = null): String {
        return "premium?feature=${Uri.encode(feature.orEmpty())}"
    }

    fun packageTextScan(barcode: String? = null): String {
        return "package_text_scan?barcode=${Uri.encode(barcode.orEmpty())}"
    }
}
