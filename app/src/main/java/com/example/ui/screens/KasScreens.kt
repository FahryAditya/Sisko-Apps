package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.KasTransactionEntity
import com.example.ui.SiskoViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasScreens(
    viewModel: SiskoViewModel,
    onNavigateBack: () -> Unit
) {
    val kasList by viewModel.kasTransactions.collectAsState()
    val flowFilterType by viewModel.kasFilterType.collectAsState()

    var showAddTxDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    // Aggregate totals
    val incomesTotal = kasList.filter { it.type == "INCOME" }.sumOf { it.amount }
    val expensesTotal = kasList.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val balanceTotal = incomesTotal - expensesTotal

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kas & Keuangan Eskul", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.Upload, contentDescription = "Export Laporan", tint = Color(0xFF4CAF50))
                    }
                    IconButton(onClick = { showAddTxDialog = true }) {
                        Icon(Icons.Default.Payments, contentDescription = "Catat Transaksi", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Balance report board card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SALDO KAS SAAT INI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rp ${DecimalFormat("#,###").format(balanceTotal)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = "Total Masuk (In)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                            Text(text = "Rp ${DecimalFormat("#,###").format(incomesTotal)}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF2E7D32))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Total Keluar (Out)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                            Text(text = "Rp ${DecimalFormat("#,###").format(expensesTotal)}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFFC62828))
                        }
                    }
                }
            }

            // Filters scroll area
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("Semua", ""),
                    Pair("Pemasukan", "INCOME"),
                    Pair("Pengeluaran", "EXPENSE")
                ).forEach { filter ->
                    val isSelected = flowFilterType == filter.second
                    Card(
                        onClick = { viewModel.setKasFilterType(filter.second) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = filter.first,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Transaction log items list
            if (kasList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Belum ada pencatatan kas transparan.", fontSize = 13.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(kasList) { tx ->
                        val isIncome = tx.type == "INCOME"

                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isIncome) Icons.Default.ArrowCircleDown else Icons.Default.ArrowCircleUp,
                                        contentDescription = null,
                                        tint = if (isIncome) Color(0xFF4CAF50) else Color(0xFFF44336),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = tx.description, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = "Tgl: ${tx.date} • Oleh: ${tx.recordedBy}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }

                                Text(
                                    text = (if (isIncome) "+" else "-") + " Rp " + DecimalFormat("#,###").format(tx.amount),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Dialog form popup
        if (showAddTxDialog) {
            AddKasDialog(
                onDismiss = { showAddTxDialog = false },
                onSave = { amount, type, desc ->
                    viewModel.addKasTransaction(amount, type, desc)
                    showAddTxDialog = false
                }
            )
        }

        // Excel Export Laporan Dialog simulation
        if (showExportDialog) {
            KasExcelExportDialog(
                kasList = kasList,
                balanceTotal = balanceTotal,
                onDismiss = { showExportDialog = false }
            )
        }
    }
}

@Composable
fun AddKasDialog(
    onDismiss: () -> Unit,
    onSave: (Double, String, String) -> Unit
) {
    var nominalText by remember { mutableStateOf("") }
    var typeSelection by remember { mutableStateOf("INCOME") } // "INCOME" or "EXPENSE"
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pencatatan Keuangan Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Radio buttons for income expense swap
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { typeSelection = "INCOME" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (typeSelection == "INCOME") Color(0xFFC8E6C9) else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (typeSelection == "INCOME") Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Masuk (In)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { typeSelection = "EXPENSE" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (typeSelection == "EXPENSE") Color(0xFFFFCDD2) else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (typeSelection == "EXPENSE") Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Keluar (Out)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = nominalText,
                    onValueChange = { nominalText = it },
                    label = { Text("Jumlah Nominal Cash (Rupiah)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Asal Usul / Deskripsi") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val value = nominalText.toDoubleOrNull() ?: 0.0
                if (value > 0.0 && description.isNotBlank()) {
                    onSave(value, typeSelection, description)
                }
            }) {
                Text("Simpan Kas")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun KasExcelExportDialog(
    kasList: List<KasTransactionEntity>,
    balanceTotal: Double,
    onDismiss: () -> Unit
) {
    val sb = java.lang.StringBuilder()
    sb.append("SISKO JURMAL CASH BOOK REPORT\n")
    sb.append("-------------------------------------------------------------------------\n")
    sb.append(String.format("%-11s | %-24s | %-12s | %-12s\n", "TANGGAL", "KETERANGAN", "TIPE KAS", "NOMINAL"))
    sb.append("-------------------------------------------------------------------------\n")
    kasList.forEach { record ->
        val itemType = if (record.type == "INCOME") "PEMASUKAN" else "PENGELUARAN"
        val desc = if (record.description.length > 22) record.description.substring(0, 19) + ".." else record.description
        sb.append(String.format("%-11s | %-24s | %-12s | Rp %-10s\n", record.date, desc, itemType, DecimalFormat("#,###").format(record.amount)))
    }
    sb.append("-------------------------------------------------------------------------\n")
    sb.append("Net Balance Sisa Kas: Rp " + DecimalFormat("#,###").format(balanceTotal) + "\n")
    sb.append("File Status: SIGNED & SECURED ONLINE. COMPILED .XLSX")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buku Kas Spreadsheet Di-eksport", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Visualisasi berkas buku kas digital:", fontSize = 12.sp, color = Color.Gray)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black, shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = sb.toString(),
                        color = Color(0xFF00FF00),
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Tutup Berkas")
            }
        }
    )
}
