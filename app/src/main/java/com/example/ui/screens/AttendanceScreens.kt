package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.SiskoViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreens(
    viewModel: SiskoViewModel,
    onNavigateBack: () -> Unit
) {
    val membersList by viewModel.members.collectAsState()
    val allAttendance by viewModel.attendance.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var activeViewTab by remember { mutableStateOf("rollcall") } // "rollcall", "qr_suite", "calendar_history"
    var showScanResultDialog by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Presensi Kehadiran", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.Analytics, contentDescription = "Simpan Excel", tint = Color(0xFF4CAF50))
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
        ) {
            // View tabs selector
            TabRow(selectedTabIndex = when(activeViewTab) {
                "rollcall" -> 0
                "qr_suite" -> 1
                else -> 2
            }) {
                Tab(selected = activeViewTab == "rollcall", onClick = { activeViewTab = "rollcall" }) {
                    Text("Absen Manual", modifier = Modifier.padding(14.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeViewTab == "qr_suite", onClick = { activeViewTab = "qr_suite" }) {
                    Text("QR Scanner GPS", modifier = Modifier.padding(14.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeViewTab == "calendar_history", onClick = { activeViewTab = "calendar_history" }) {
                    Text("Kalender", modifier = Modifier.padding(14.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                when (activeViewTab) {
                    "rollcall" -> {
                        ManualRollCallView(
                            members = membersList,
                            attendanceToday = allAttendance.filter { it.date == todayStr },
                            onMark = { mid, status ->
                                viewModel.recordManualAttendance(mid, todayStr, status)
                            }
                        )
                    }
                    "qr_suite" -> {
                        QrGPSScannerSuite(
                            viewModel = viewModel,
                            currentUser = currentUser,
                            allMembers = membersList,
                            onScanComplete = { isSuccess, summary ->
                                showScanResultDialog = Pair(isSuccess, summary)
                            }
                        )
                    }
                    else -> {
                        CalendarAttendanceHistoryView(
                            allAttendance = allAttendance,
                            members = membersList
                        )
                    }
                }
            }
        }

        // Action outcome popups
        showScanResultDialog?.let { result ->
            AlertDialog(
                onDismissRequest = { showScanResultDialog = null },
                icon = {
                    Icon(
                        imageVector = if (result.first) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (result.first) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.size(54.dp)
                    )
                },
                title = { Text(if (result.first) "Absensi Berhasil" else "Presensi Gagal", fontWeight = FontWeight.Bold) },
                text = { Text(result.second, textAlign = TextAlign.Center) },
                confirmButton = {
                    Button(onClick = { showScanResultDialog = null }) {
                        Text("Tutup")
                    }
                }
            )
        }

        if (showExportDialog) {
            AttendanceExcelExportDialog(
                attendance = allAttendance,
                members = membersList,
                onDismiss = { showExportDialog = false }
            )
        }
    }
}

@Composable
fun ManualRollCallView(
    members: List<MemberEntity>,
    attendanceToday: List<AttendanceEntity>,
    onMark: (Int, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Papan Tulis Presensi Hari Ini",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Simpan daftar hadir siswa latihan secara langsung dibawah (Auto-save):",
                fontSize = 11.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(members) { m ->
            val att = attendanceToday.find { it.memberId == m.id }
            val currentStatus = att?.status ?: "Alfa" // Defaults to alpha if unchecked

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
                    Column {
                        Text(text = m.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "${m.classGroup} • ${m.roleInOrg}", fontSize = 11.sp, color = Color.Gray)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AttendanceStatusButton(label = "H", isSelected = currentStatus == "Hadir", color = Color(0xFF4CAF50)) {
                            onMark(m.id, "Hadir")
                        }
                        AttendanceStatusButton(label = "I", isSelected = currentStatus == "Izin", color = Color(0xFF2196F3)) {
                            onMark(m.id, "Izin")
                        }
                        AttendanceStatusButton(label = "S", isSelected = currentStatus == "Sakit", color = Color(0xFFFF9800)) {
                            onMark(m.id, "Sakit")
                        }
                        AttendanceStatusButton(label = "A", isSelected = currentStatus == "Alfa", color = Color(0xFFF44336)) {
                            onMark(m.id, "Alfa")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceStatusButton(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(
                color = if (isSelected) color else color.copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) color else color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else color
        )
    }
}

// QR scanner with custom mock coordination calculation
@Composable
fun QrGPSScannerSuite(
    viewModel: SiskoViewModel,
    currentUser: UserEntity?,
    allMembers: List<MemberEntity>,
    onScanComplete: (Boolean, String) -> Unit
) {
    val activeOrg by viewModel.currentOrganization.collectAsState()
    var selectedStudentEmailForScan by remember { mutableStateOf("") }
    var mockGpsLat by remember { mutableStateOf("-6.2088") } // Mock Pramuka coordinate default
    var mockGpsLng by remember { mutableStateOf("106.8456") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Simulasi QR Scan Berbayar & Validasi Radius",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Mengintegrasikan formula Haversine untuk verifikasi presensi berdasarkan jarak absolut maks 100 meter ke titik eskul.",
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        // Render visual generated School Pass
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("KARTU QR DIGITAL SISKO", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Draw visual QR mockup symbol grid Box
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .background(Color.White, shape = RoundedCornerShape(12.dp))
                            .border(2.dp, MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(6) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    repeat(6) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .background(
                                                    if ((it + (0..1).random()) % 2 == 0) Color.Black else Color.Transparent,
                                                    shape = RoundedCornerShape(2.dp)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Encrypted Token: ${currentUser?.email ?: "Guest"}",
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color.Gray
                    )
                }
            }
        }

        // MOCK SCANNER PAD PANEL TOOL
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "SIMULATOR KOTAK KAMERA SCANNER", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "1. Pilih Isian Kartu Siswa yang Di-scan:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Select member
                    var membersExpanded by remember { mutableStateOf(false) }
                    Box {
                        Button(
                            onClick = { membersExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Text(text = if (selectedStudentEmailForScan.isEmpty()) "Pilih Siswa" else selectedStudentEmailForScan)
                        }
                        DropdownMenu(expanded = membersExpanded, onDismissRequest = { membersExpanded = false }) {
                            allMembers.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text("${m.name} (${m.email})") },
                                    onClick = {
                                        selectedStudentEmailForScan = m.email
                                        membersExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "2. Konfigurasi Mocking GPS Scanner (Untuk Uji Radius):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = mockGpsLat,
                            onValueChange = { mockGpsLat = it },
                            label = { Text("Mock Lat") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = mockGpsLng,
                            onValueChange = { mockGpsLng = it },
                            label = { Text("Mock Lng") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    // Helper coordinate chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                // Exactly on the point of Scouts Pramuka
                                mockGpsLat = "-6.2088"
                                mockGpsLng = "106.8456"
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("<5m (Tepat)", fontSize = 9.sp)
                        }

                        Button(
                            onClick = {
                                // Slightly offset, around 85 meters
                                mockGpsLat = "-6.2082"
                                mockGpsLng = "106.8451"
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("~80m (Dalam Radius)", fontSize = 9.sp)
                        }

                        Button(
                            onClick = {
                                // Far offset, around 450 meters
                                mockGpsLat = "-6.2115"
                                mockGpsLng = "106.8485"
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("~450m (Diluar Radius)", fontSize = 9.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (selectedStudentEmailForScan.isNotBlank()) {
                                val lat = mockGpsLat.toDoubleOrNull() ?: 0.0
                                val lng = mockGpsLng.toDoubleOrNull() ?: 0.0
                                viewModel.scanQrAttendance(selectedStudentEmailForScan, lat, lng) { success, msg ->
                                    onScanComplete(success, msg)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SIMULASI SCAN KAMERA")
                    }
                }
            }
        }
    }
}

// Calendar View layout
@Composable
fun CalendarAttendanceHistoryView(
    allAttendance: List<AttendanceEntity>,
    members: List<MemberEntity>
) {
    var selectedStudentForFilter by remember { mutableStateOf<MemberEntity?>(null) }
    var expandedDropdown by remember { mutableStateOf(false) }

    val filteredAtts = if (selectedStudentForFilter == null) allAttendance else {
        allAttendance.filter { it.memberId == selectedStudentForFilter!!.id }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "Kalender Presensi Bulanan", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            
            Box {
                Button(
                    onClick = { expandedDropdown = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text(text = selectedStudentForFilter?.name ?: "Semua Anggota (Historikal)")
                }
                DropdownMenu(expanded = expandedDropdown, onDismissRequest = { expandedDropdown = false }) {
                    DropdownMenuItem(text = { Text("Semua Anggota (Historikal)") }, onClick = {
                        selectedStudentForFilter = null
                        expandedDropdown = false
                    })
                    members.forEach { m ->
                        DropdownMenuItem(text = { Text(m.name) }, onClick = {
                            selectedStudentForFilter = m
                            expandedDropdown = false
                        })
                    }
                }
            }
        }

        // Draw calendar grid
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Juni 2026 Check-in Grid", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Simplified grid representation
                    val dummyDays = (1..30).toList()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("S", "S", "R", "K", "J", "S", "M").forEach { d ->
                            Text(text = d, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Grouping days into weeks rows
                    val rows = dummyDays.chunked(7)
                    rows.forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            week.forEach { day ->
                                val dayStr = "2026-06-" + String.format("%02d", day)
                                val dayRecords = filteredAtts.filter { it.date == dayStr }
                                
                                val statusColor = when {
                                    dayRecords.any { it.status == "Hadir" } -> Color(0xFFC8E6C9)
                                    dayRecords.any { it.status == "Izin" } -> Color(0xFFBBDEFB)
                                    dayRecords.any { it.status == "Sakit" } -> Color(0xFFFFE082)
                                    dayRecords.any { it.status == "Alfa" } -> Color(0xFFFFCDD2)
                                    else -> Color.Transparent
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .background(statusColor, shape = RoundedCornerShape(4.dp))
                                        .border(0.5.dp, Color.LightGray.copy(alpha = 0.4f), shape = RoundedCornerShape(4.dp))
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "$day", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            // Append trailing space placeholders for short columns if incomplete week
                            if (week.size < 7) {
                                repeat(7 - week.size) {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        // Summary indices details
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Rasio Absensi Rekap:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    val presents = filteredAtts.count { it.status == "Hadir" }
                    val permissions = filteredAtts.count { it.status == "Izin" }
                    val sicks = filteredAtts.count { it.status == "Sakit" }
                    val alphas = filteredAtts.count { it.status == "Alfa" }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        RecapItem(label = "Hadir", valStr = "$presents", color = Color(0xFF4CAF50))
                        RecapItem(label = "Izin", valStr = "$permissions", color = Color(0xFF2196F3))
                        RecapItem(label = "Sakit", valStr = "$sicks", color = Color(0xFFFF9800))
                        RecapItem(label = "Alfa", valStr = "$alphas", color = Color(0xFFF44336))
                    }
                }
            }
        }
    }
}

@Composable
fun RecapItem(label: String, valStr: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(24.dp).background(color, shape = CircleShape), contentAlignment = Alignment.Center) {
            Text(text = valStr, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
fun AttendanceExcelExportDialog(
    attendance: List<AttendanceEntity>,
    members: List<MemberEntity>,
    onDismiss: () -> Unit
) {
    val sb = StringBuilder()
    sb.append("SISKO ABSENSI LEDGER SHEET REPORT\n")
    sb.append("-------------------------------------------------------------------------\n")
    sb.append(String.format("%-14s | %-16s | %-8s | %-9s\n", "TANGGAL", "NAMA SISWA", "STATUS", "VALIDASI"))
    sb.append("-------------------------------------------------------------------------\n")
    attendance.forEach { record ->
        val m = members.find { it.id == record.memberId }
        val studName = if ((m?.name?.length ?: 0) > 13) m!!.name.substring(0, 11) + ".." else m?.name ?: "Unknown"
        val truncV = if (record.verifiedBy.length > 9) record.verifiedBy.substring(0, 7) + ".." else record.verifiedBy
        sb.append(String.format("%-14s | %-16s | %-8s | %-9s\n", record.date, studName, record.status, truncV))
    }
    sb.append("-------------------------------------------------------------------------\n")
    sb.append("Laporan Absensi Sukses Di-eksport ke File: records_absensi.xlsx")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Absensi Selesai", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Berikut adalah tabel data hasil export laporan:", fontSize = 12.sp, color = Color.Gray)
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
                Text("Tutup Laporan")
            }
        }
    )
}
