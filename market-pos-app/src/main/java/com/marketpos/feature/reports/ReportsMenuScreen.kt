package com.marketpos.feature.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable

@Composable
fun ReportsMenuScreen(
    onOpenSalesSummary: () -> Unit,
    onOpenRecentSales: () -> Unit,
    onOpenTopSelling: () -> Unit,
    onOpenStockTracking: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Raporlar", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Aşağıdaki rapor türlerinden birini seçin.",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(onClick = onOpenSalesSummary, modifier = Modifier.fillMaxWidth()) {
                Text("Satış Raporları")
            }

            Button(onClick = onOpenRecentSales, modifier = Modifier.fillMaxWidth()) {
                Text("Son Satışlar")
            }

            Button(onClick = onOpenTopSelling, modifier = Modifier.fillMaxWidth()) {
                Text("En Çok Satılan Ürünler")
            }

            Button(onClick = onOpenStockTracking, modifier = Modifier.fillMaxWidth()) {
                Text("Stok Takibi")
            }

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Geri")
            }
        }
    }
}

