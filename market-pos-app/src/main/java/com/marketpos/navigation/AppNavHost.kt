package com.marketpos.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.marketpos.feature.activation.ActivationScreen
import com.marketpos.feature.activation.AppBootstrapScreen
import com.marketpos.feature.auth.ForgotPasswordScreen
import com.marketpos.feature.auth.LoginScreen
import com.marketpos.feature.auth.RegisterScreen
import com.marketpos.feature.auth.SessionEntryScreen
import com.marketpos.feature.companion.WebCompanionScreen
import com.marketpos.feature.product.ProductDetailScreen
import com.marketpos.feature.product.ProductEditScreen
import com.marketpos.feature.product.ProductListScreen
import com.marketpos.feature.product.PackageTextScanScreen
import com.marketpos.feature.product.ScanForProductScreen
import com.marketpos.feature.reports.ReportsMenuScreen
import com.marketpos.feature.reports.ReportsScreen
import com.marketpos.feature.reports.StockCountScreen
import com.marketpos.feature.reports.StockTrackingScreen
import com.marketpos.feature.scan.NotFoundScreen
import com.marketpos.feature.scan.ScanScreen
import com.marketpos.feature.scan.SerialScanScreen
import com.marketpos.feature.sale.CartScreen
import com.marketpos.feature.sale.SaleSuccessScreen
import com.marketpos.feature.settings.BulkPriceUpdateScreen
import com.marketpos.feature.settings.BulkStockUpdateScreen
import com.marketpos.feature.settings.BarcodeBankasiImportScreen
import com.marketpos.feature.settings.ModeSelectionScreen
import com.marketpos.feature.settings.PremiumScreen
import com.marketpos.feature.settings.SettingsScreen
import com.marketpos.feature.settings.SupportScreen
import com.marketpos.feature.settings.WebBarcodeSearchScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppRoutes.BOOTSTRAP) {
        composable(AppRoutes.BOOTSTRAP) {
            AppBootstrapScreen(
                onNavigateSessionEntry = {
                    navController.navigate(AppRoutes.SESSION_ENTRY) {
                        popUpTo(AppRoutes.BOOTSTRAP) { inclusive = true }
                    }
                },
                onNavigateActivation = {
                    navController.navigate(AppRoutes.ACTIVATION) {
                        popUpTo(AppRoutes.BOOTSTRAP) { inclusive = true }
                    }
                },
                onNavigateModeSelection = {
                    navController.navigate(AppRoutes.MODE_SELECTION) {
                        popUpTo(AppRoutes.BOOTSTRAP) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.SESSION_ENTRY) {
            SessionEntryScreen(
                onNavigateActivation = {
                    navController.navigate(AppRoutes.ACTIVATION) {
                        popUpTo(AppRoutes.SESSION_ENTRY) { inclusive = true }
                    }
                },
                onNavigateLogin = { navController.navigate(AppRoutes.LOGIN) },
                onNavigateRegister = { navController.navigate(AppRoutes.REGISTER) }
            )
        }

        composable(AppRoutes.LOGIN) {
            LoginScreen(
                onAuthenticated = {
                    navController.navigate(AppRoutes.BOOTSTRAP) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onNavigateRegister = { navController.navigate(AppRoutes.REGISTER) },
                onNavigateForgotPassword = { navController.navigate(AppRoutes.FORGOT_PASSWORD) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.REGISTER) {
            RegisterScreen(
                onAuthenticated = {
                    navController.navigate(AppRoutes.BOOTSTRAP) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onNavigateLogin = { navController.navigate(AppRoutes.LOGIN) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.ACTIVATION) {
            ActivationScreen(
                onActivated = {
                    navController.navigate(AppRoutes.MODE_SELECTION) {
                        popUpTo(AppRoutes.ACTIVATION) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.MODE_SELECTION) {
            ModeSelectionScreen(
                viewModel = hiltViewModel(),
                onNavigateMobileScan = {
                    navController.navigate(AppRoutes.SCAN) {
                        popUpTo(AppRoutes.MODE_SELECTION) { inclusive = true }
                    }
                },
                onNavigateWebCompanion = {
                    navController.navigate(AppRoutes.WEB_COMPANION) {
                        popUpTo(AppRoutes.MODE_SELECTION) { inclusive = true }
                    }
                },
                onNavigateLogin = { navController.navigate(AppRoutes.LOGIN) }
            )
        }

        composable(AppRoutes.WEB_COMPANION) {
            WebCompanionScreen(
                viewModel = hiltViewModel(),
                onNavigateMobileScan = {
                    navController.navigate(AppRoutes.SCAN) {
                        popUpTo(AppRoutes.WEB_COMPANION) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.SCAN) {
            ScanScreen(
                viewModel = hiltViewModel(),
                onNavigateDetail = { navController.navigate(AppRoutes.productDetail(it)) },
                onNavigateNotFound = { navController.navigate(AppRoutes.notFound(it)) },
                onNavigateSerialScan = { navController.navigate(AppRoutes.SERIAL_SCAN) },
                onNavigateStockTracking = { navController.navigate(AppRoutes.STOCK_TRACKING) },
                onNavigatePremium = { navController.navigate(AppRoutes.premium(it.name)) },
                onNavigateCart = { navController.navigate(AppRoutes.CART) },
                onNavigateSettings = { navController.navigate(AppRoutes.SETTINGS) }
            )
        }

        composable(AppRoutes.SERIAL_SCAN) {
            SerialScanScreen(
                viewModel = hiltViewModel(),
                onNavigateCart = { navController.navigate(AppRoutes.CART) },
                onNavigatePremium = { navController.navigate(AppRoutes.premium(it.name)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.NOT_FOUND,
            arguments = listOf(navArgument("barcode") { type = NavType.StringType })
        ) {
            NotFoundScreen(
                viewModel = hiltViewModel(),
                onNavigateScan = { navController.popBackStack(AppRoutes.SCAN, false) },
                onNavigateAddProduct = { navController.navigate(AppRoutes.productEdit(it)) }
            )
        }

        composable(
            route = AppRoutes.PRODUCT_DETAIL,
            arguments = listOf(navArgument("barcode") { type = NavType.StringType })
        ) {
            ProductDetailScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
                onNavigateEdit = { navController.navigate(AppRoutes.productEdit(it)) },
                onNavigateScan = {
                    navController.navigate(AppRoutes.SCAN) {
                        popUpTo(AppRoutes.SCAN) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = AppRoutes.PRODUCT_EDIT,
            arguments = listOf(
                navArgument("barcode") { type = NavType.StringType; defaultValue = "" },
                navArgument("prefillName") { type = NavType.StringType; defaultValue = "" },
                navArgument("prefillSalePrice") { type = NavType.StringType; defaultValue = "" }
            )
        ) {
            val entry = navController.currentBackStackEntry ?: return@composable
            val scannedBarcode by entry.savedStateHandle.getStateFlow<String?>("scanned_barcode", null).collectAsState()
            val ocrSuggestedNames by entry.savedStateHandle.getStateFlow<ArrayList<String>?>("ocr_name_suggestions", null).collectAsState()
            ProductEditScreen(
                viewModel = hiltViewModel(),
                scannedBarcode = scannedBarcode,
                onConsumeScannedBarcode = { entry.savedStateHandle["scanned_barcode"] = null },
                ocrSuggestedNames = ocrSuggestedNames?.toList(),
                onConsumeOcrSuggestedNames = { entry.savedStateHandle["ocr_name_suggestions"] = null },
                onBack = { navController.popBackStack() },
                onNavigateScanAfterSave = {
                    navController.navigate(AppRoutes.SCAN) {
                        popUpTo(AppRoutes.SCAN) { inclusive = true }
                    }
                },
                onNavigateScanForProduct = { navController.navigate(AppRoutes.SCAN_FOR_PRODUCT) },
                onOpenExistingProduct = { barcode -> navController.navigate(AppRoutes.productEdit(barcode)) },
                onNavigatePremium = { feature ->
                    navController.navigate(AppRoutes.premium(feature.name))
                },
                onNavigatePackageTextScan = { barcode ->
                    navController.navigate(AppRoutes.packageTextScan(barcode))
                }
            )
        }

        composable(AppRoutes.SCAN_FOR_PRODUCT) {
            ScanForProductScreen(
                onScanned = { barcode ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("scanned_barcode", barcode)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.PACKAGE_TEXT_SCAN,
            arguments = listOf(navArgument("barcode") { type = NavType.StringType; defaultValue = "" })
        ) { backStackEntry ->
            PackageTextScanScreen(
                barcode = backStackEntry.arguments?.getString("barcode").orEmpty(),
                onSuggestionsReady = { suggestions ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("ocr_name_suggestions", ArrayList(suggestions))
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.CART) {
            CartScreen(
                viewModel = hiltViewModel(),
                onNavigateScan = { navController.popBackStack(AppRoutes.SCAN, false) },
                onNavigatePremium = { feature -> navController.navigate(AppRoutes.premium(feature.name)) },
                onNavigateSaleSuccess = { navController.navigate(AppRoutes.saleSuccess(it)) }
            )
        }

        composable(
            route = AppRoutes.SALE_SUCCESS,
            arguments = listOf(navArgument("saleId") { type = NavType.LongType })
        ) { backStackEntry ->
            val saleId = backStackEntry.arguments?.getLong("saleId") ?: 0L
            SaleSuccessScreen(
                saleId = saleId,
                onNewSale = {
                    navController.navigate(AppRoutes.SCAN) {
                        popUpTo(AppRoutes.SCAN) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.REPORTS) {
            ReportsMenuScreen(
                onOpenSalesSummary = { navController.navigate(AppRoutes.salesReports("summary")) },
                onOpenRecentSales = { navController.navigate(AppRoutes.salesReports("recent_sales")) },
                onOpenTopSelling = { navController.navigate(AppRoutes.salesReports("top_products")) },
                onOpenStockTracking = { navController.navigate(AppRoutes.STOCK_TRACKING) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.SALES_REPORTS,
            arguments = listOf(navArgument("section") { type = NavType.StringType; defaultValue = "summary" })
        ) { backStackEntry ->
            ReportsScreen(
                viewModel = hiltViewModel(),
                initialSection = backStackEntry.arguments?.getString("section"),
                onOpenStockTracking = { navController.navigate(AppRoutes.STOCK_TRACKING) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.STOCK_TRACKING) {
            StockTrackingScreen(
                viewModel = hiltViewModel(),
                onNavigateStockCount = { navController.navigate(AppRoutes.STOCK_COUNT) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.STOCK_COUNT) {
            StockCountScreen(
                viewModel = hiltViewModel(),
                onNavigatePremium = { navController.navigate(AppRoutes.premium(it.name)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.SETTINGS) {
            SettingsScreen(
                viewModel = hiltViewModel(),
                onOpenPremium = { feature -> navController.navigate(AppRoutes.premium(feature?.name)) },
                onOpenManualAdd = { navController.navigate(AppRoutes.productEdit()) },
                onOpenBarcodeBankasiImport = { navController.navigate(AppRoutes.BARKOD_BANKASI_IMPORT) },
                onOpenWebBarcodeSearch = { navController.navigate(AppRoutes.WEB_BARCODE_SEARCH) },
                onOpenProductList = { navController.navigate(AppRoutes.PRODUCT_LIST) },
                onOpenBulkPriceUpdate = { navController.navigate(AppRoutes.BULK_PRICE_UPDATE) },
                onOpenBulkStockUpdate = { navController.navigate(AppRoutes.BULK_STOCK_UPDATE) },
                onOpenReports = { navController.navigate(AppRoutes.REPORTS) },
                onOpenStockTracking = { navController.navigate(AppRoutes.STOCK_TRACKING) },
                onOpenStockCount = { navController.navigate(AppRoutes.STOCK_COUNT) },
                onOpenSupport = { navController.navigate(AppRoutes.SUPPORT) },
                onOpenLogin = { navController.navigate(AppRoutes.LOGIN) },
                onOpenRegister = { navController.navigate(AppRoutes.REGISTER) },
                onConnectionCleared = {
                    navController.navigate(AppRoutes.BOOTSTRAP) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onBackToScan = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.SUPPORT) {
            SupportScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.PREMIUM,
            arguments = listOf(navArgument("feature") { type = NavType.StringType; defaultValue = "" })
        ) {
            PremiumScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.BARKOD_BANKASI_IMPORT) {
            BarcodeBankasiImportScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.WEB_BARCODE_SEARCH) {
            val entry = navController.currentBackStackEntry ?: return@composable
            val scannedBarcode by entry.savedStateHandle.getStateFlow<String?>("scanned_barcode", null).collectAsState()
            WebBarcodeSearchScreen(
                viewModel = hiltViewModel(),
                scannedBarcode = scannedBarcode,
                onConsumeScannedBarcode = { entry.savedStateHandle["scanned_barcode"] = null },
                onBack = { navController.popBackStack() },
                onNavigateScanForBarcode = { navController.navigate(AppRoutes.SCAN_FOR_PRODUCT) },
                onOpenProductAdd = { result ->
                    navController.navigate(
                        AppRoutes.productEdit(
                            barcode = result.barcode,
                            prefillName = result.name,
                            prefillSalePrice = result.salePriceKurus
                        )
                    )
                }
            )
        }

        composable(AppRoutes.PRODUCT_LIST) {
            ProductListScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
                onOpenEdit = { navController.navigate(AppRoutes.productEdit(it)) }
            )
        }

        composable(AppRoutes.BULK_PRICE_UPDATE) {
            BulkPriceUpdateScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.BULK_STOCK_UPDATE) {
            BulkStockUpdateScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() }
            )
        }
    }
}
