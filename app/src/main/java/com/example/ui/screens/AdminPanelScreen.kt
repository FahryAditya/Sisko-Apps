package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.SiskoViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: SiskoViewModel,
    onNavigateBack: () -> Unit,
    onOpenOrgData: () -> Unit
) {
    val allOrgs by viewModel.allOrganizations.collectAsState()
    val allMems by viewModel.allMembers.collectAsState()
    val allAtts by viewModel.allAttendanceGlobally.collectAsState()
    val allKases by viewModel.allKasTransactionsGlobally.collectAsState()
    val usersList by viewModel.allUsers.collectAsState()
    val orgAdminsMap by viewModel.allOrganizationAdminsFlow.collectAsState(emptyList())

    var showAddOrgDialog by remember { mutableStateOf(false) }
    var currentOrgForAdminManage by remember { mutableStateOf<OrganizationEntity?>(null) }

    // Aggregate values
    val totalOrgs = allOrgs.size
    val totalGlobalMembers = allMems.size
    val totalGlobalAttsToday = allAtts.filter { it.date == java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) }.count { it.status == "Hadir" }
    
    val totalGlobalIncomes = allKases.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalGlobalExpenses = allKases.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val totalGlobalBalance = totalGlobalIncomes - totalGlobalExpenses

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Kontrol Master Admin", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddOrgDialog = true }) {
                        Icon(Icons.Default.AddHomeWork, contentDescription = "Tambah Eskul", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Aggregate Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "RINGKASAN INFRASTRUKTUR SISKO",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                GlobalSummaryWidget(title = "Organisasi", value = "$totalOrgs", modifier = Modifier.weight(1f))
                                GlobalSummaryWidget(title = "Total Siswa", value = "$totalGlobalMembers", modifier = Modifier.weight(1f))
                                GlobalSummaryWidget(title = "Hadir Hari Ini", value = "$totalGlobalAttsToday", modifier = Modifier.weight(1f))
                                GlobalSummaryWidget(title = "Kas Gabungan", value = "Rp ${DecimalFormat("#,###").format(totalGlobalBalance)}", modifier = Modifier.weight(1.5f))
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DAFTAR EKSTRAKURIKULER & ORGANISASI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { /* Refresh mock sync */ }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Sinkronisasi")
                        }
                    }
                }

                // List organizations
                items(allOrgs) { org ->
                    val orgMembersCount = allMems.count { it.organizationId == org.id }
                    val orgAttToday = allAtts.filter { it.organizationId == org.id && it.date == java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) }.count { it.status == "Hadir" }
                    val orgIncomes = allKases.filter { it.organizationId == org.id && it.type == "INCOME" }.sumOf { it.amount }
                    val orgExpenses = allKases.filter { it.organizationId == org.id && it.type == "EXPENSE" }.sumOf { it.amount }
                    val orgKas = orgIncomes - orgExpenses
                    val adminsCount = orgAdminsMap.count { it.organizationId == org.id }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = org.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(text = "Pembina: ${org.coach}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (org.status == "Active") Color(0xFFC8E6C9) else Color(0xFFFFCDD2),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = org.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (org.status == "Active") Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Mini Statistics row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                MiniMetric(label = "Anggota", valStr = "$orgMembersCount", modifier = Modifier.weight(1f))
                                MiniMetric(label = "Hadir", valStr = "$orgAttToday", modifier = Modifier.weight(1f))
                                MiniMetric(label = "Saldo Kas", valStr = "Rp ${DecimalFormat("#,###").format(orgKas)}", modifier = Modifier.weight(1.5f))
                                MiniMetric(label = "Admin Org", valStr = "$adminsCount", modifier = Modifier.weight(1f))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { currentOrgForAdminManage = org },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.ManageAccounts, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Kelola Admin", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        viewModel.selectOrg(org.id)
                                        onOpenOrgData()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Buka Sesi", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Dialog ADD ORGANIZATION
            if (showAddOrgDialog) {
                AddOrgDialog(
                    onDismiss = { showAddOrgDialog = false },
                    onConfirm = { name, desc, coach, lat, lng, logo ->
                        viewModel.createOrganization(name, desc, coach, lat, lng, logo)
                        showAddOrgDialog = false
                    }
                )
            }

            // Dialog MANAGE ADMINS PER ORG
            currentOrgForAdminManage?.let { org ->
                ManageOrgAdminsDialog(
                    org = org,
                    viewModel = viewModel,
                    onDismiss = { currentOrgForAdminManage = null }
                )
            }
        }
    }
}

@Composable
fun GlobalSummaryWidget(title: String, value: String, modifier: Modifier) {
    Column(modifier = modifier) {
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(text = title, fontSize = 9.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
    }
}

@Composable
fun MiniMetric(label: String, valStr: String, modifier: Modifier) {
    Column(modifier = modifier) {
        Text(text = valStr, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
    }
}

@Composable
fun AddOrgDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Double, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var coach by remember { mutableStateOf("") }
    var latText by remember { mutableStateOf("-6.2088") }
    var lngText by remember { mutableStateOf("106.8456") }
    var logoUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Organisasi/Eskul Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Organisasi") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Deskripsi Singkat") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = coach, onValueChange = { coach = it }, label = { Text("Nama Pembina (Coach)") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = latText, onValueChange = { latText = it }, label = { Text("GPS Lat") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = lngText, onValueChange = { lngText = it }, label = { Text("GPS Lng") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = logoUrl, onValueChange = { logoUrl = it }, label = { Text("Logo URL (Opsional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                val lat = latText.toDoubleOrNull() ?: -6.2088
                val lng = lngText.toDoubleOrNull() ?: 106.8456
                onConfirm(name, desc, coach, lat, lng, logoUrl)
            }) {
                Text("Simpan")
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
fun ManageOrgAdminsDialog(
    org: OrganizationEntity,
    viewModel: SiskoViewModel,
    onDismiss: () -> Unit
) {
    val usersList by viewModel.allUsers.collectAsState()
    val orgAdminsMap by viewModel.allOrganizationAdminsFlow.collectAsState(emptyList())

    // Filter users who are OrgAdmins of this organization
    val assignedAdminIds = orgAdminsMap.filter { it.organizationId == org.id }.map { it.userId }
    val assignedAdmins = usersList.filter { it.id in assignedAdminIds }

    var newAdminEmail by remember { mutableStateOf("") }
    var newAdminName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = "Kelola Admin Eskul", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(text = org.name, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "ADMIN AKTIF SEKARANG:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                
                if (assignedAdmins.isEmpty()) {
                    Text(
                        text = "Belum ada admin khusus terdaftar.",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    assignedAdmins.forEach { adm ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = adm.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(text = adm.email, fontSize = 11.sp, color = Color.Gray)
                            }
                            IconButton(
                                onClick = { viewModel.revokeOrganizationAdmin(org.id, adm.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus Akses", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                Text(text = "DAFTARKAN ADMIN PEMBINA BARU:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(value = newAdminName, onValueChange = { newAdminName = it }, label = { Text("Nama Pembina") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = newAdminEmail, onValueChange = { newAdminEmail = it }, label = { Text("Email Pembina") }, modifier = Modifier.fillMaxWidth())
                
                Button(
                    onClick = {
                        if (newAdminEmail.isNotBlank() && newAdminName.isNotBlank()) {
                            viewModel.assignOrganizationAdmin(org.id, newAdminName, newAdminEmail)
                            newAdminEmail = ""
                            newAdminName = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tunjuk Jadi Admin")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}
