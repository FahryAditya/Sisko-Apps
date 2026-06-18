package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
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
fun DashboardScreen(
    viewModel: SiskoViewModel,
    onNavigateToSection: (String) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentOrg by viewModel.currentOrganization.collectAsState()
    val allOrgs by viewModel.allOrganizations.collectAsState()

    var activeTab by remember { mutableStateOf("home") } // "home", "analytics", "reminder"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (currentUser?.role == "Administrator") "Master Admin Portal" else currentOrg?.name ?: "SISKO Mobile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Akses: ${currentUser?.role ?: "Guest"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    if (currentUser?.role == "Administrator" || currentUser?.role == "OrgAdmin") {
                        IconButton(onClick = { onNavigateToSection("admin_panel") }) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Panel Master Admin", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Keluar Sesi", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                )
            )
        },
        bottomBar = {
            if (currentUser?.role != "Parent") {
                NavigationBar(
                    modifier = Modifier.height(64.dp)
                ) {
                    NavigationBarItem(
                        selected = activeTab == "home",
                        onClick = { activeTab = "home" },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Menu", fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == "analytics",
                        onClick = { activeTab = "analytics" },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                        label = { Text("Analitik", fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == "reminder",
                        onClick = { activeTab = "reminder" },
                        icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                        label = { Text("Reminder", fontSize = 11.sp) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                currentUser?.role == "Parent" -> {
                    ParentDashboardView(viewModel = viewModel, onNavigateToSection = onNavigateToSection)
                }
                activeTab == "analytics" -> {
                    AnalyticsDashboardView(viewModel = viewModel)
                }
                activeTab == "reminder" -> {
                    SmartReminderView(viewModel = viewModel)
                }
                else -> {
                    MainDashboardView(
                        viewModel = viewModel,
                        onNavigateToSection = onNavigateToSection
                    )
                }
            }
        }
    }
}

@Composable
fun MainDashboardView(
    viewModel: SiskoViewModel,
    onNavigateToSection: (String) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentOrg by viewModel.currentOrganization.collectAsState()
    val allOrgs by viewModel.allOrganizations.collectAsState()
    val members by viewModel.members.collectAsState()
    val attRecords by viewModel.attendance.collectAsState()
    val kasRecords by viewModel.kasTransactions.collectAsState()

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayAtt = attRecords.filter { it.date == todayStr }
    val presentsCount = todayAtt.count { it.status == "Hadir" }

    // Calculate Kas Treasury balance
    val totalIncome = kasRecords.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = kasRecords.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val kasBalance = totalIncome - totalExpense

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Selamat Datang,",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = currentUser?.name ?: "Siswa",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (currentUser?.role == "Administrator") "Pusat Kontrol Sisko SMAN 1" else currentOrg?.name ?: "SMAN 1 Portal",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.SentimentVerySatisfied,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Multi-Org Switcher for SuperAdmin
        if (currentUser?.role == "Administrator") {
            item {
                Text(
                    text = "PILIH KONTEKS ORGANISASI / ESKUL:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allOrgs.forEach { org ->
                        val isSelected = viewModel.selectedOrgId.collectAsState().value == org.id
                        Card(
                            onClick = { viewModel.selectOrg(org.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = org.name.split(" ")[0],
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Stats row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatMiniCard(
                    title = "Total Anggota",
                    value = "${members.size}",
                    icon = Icons.Default.Group,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    title = "Hadir Hari Ini",
                    value = "$presentsCount siswa",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    title = "Sisa Saldo Kas",
                    value = "Rp ${DecimalFormat("#,###").format(kasBalance)}",
                    icon = Icons.Default.Wallet,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Grid Actions Directory
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MODUL KELOLA EKSTRAKURIKULER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        GridActionItem(
                            title = "Anggota",
                            icon = Icons.Default.PeopleAlt,
                            color = Color(0xFF2196F3),
                            modifier = Modifier.weight(1f)
                        ) { onNavigateToSection("members") }

                        GridActionItem(
                            title = "Absensi",
                            icon = Icons.Default.HowToReg,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        ) { onNavigateToSection("attendance") }

                        GridActionItem(
                            title = "Uang Kas",
                            icon = Icons.Default.AttachMoney,
                            color = Color(0xFFFF9800),
                            modifier = Modifier.weight(1f)
                        ) { onNavigateToSection("kas") }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        GridActionItem(
                            title = "Wawancara",
                            icon = Icons.Default.RateReview,
                            color = Color(0xFF9C27B0),
                            modifier = Modifier.weight(1f)
                        ) { onNavigateToSection("interviews") }

                        GridActionItem(
                            title = "Galeri",
                            icon = Icons.Default.PhotoLibrary,
                            color = Color(0xFFE91E63),
                            modifier = Modifier.weight(1f)
                        ) { onNavigateToSection("gallery") }

                        GridActionItem(
                            title = "Pengumuman",
                            icon = Icons.Default.Campaign,
                            color = Color(0xFF3F51B5),
                            modifier = Modifier.weight(1f)
                        ) { onNavigateToSection("announcements") }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        GridActionItem(
                            title = "Event/Acara",
                            icon = Icons.Default.Event,
                            color = Color(0xFF00BCD4),
                            modifier = Modifier.weight(1f)
                        ) { onNavigateToSection("events") }

                        GridActionItem(
                            title = "Papan Skor",
                            icon = Icons.Default.Leaderboard,
                            color = Color(0xFFFFC107),
                            modifier = Modifier.weight(1f)
                        ) { onNavigateToSection("leaderboard") }

                        // Special conditional block for Super Admin Settings or general Profile
                        GridActionItem(
                            title = "Admin Panel",
                            icon = Icons.Default.SettingsApplications,
                            color = Color(0xFF607D8B),
                            modifier = Modifier.weight(1f)
                        ) { onNavigateToSection("admin_panel") }
                    }
                }
            }
        }
    }
}

@Composable
fun StatMiniCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = title, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun GridActionItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(color.copy(alpha = 0.15f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}

// --- ANALYTICS DASHBOARD VIEW ---
@Composable
fun AnalyticsDashboardView(viewModel: SiskoViewModel) {
    val attendanceRecords by viewModel.attendance.collectAsState()
    val kasRecords by viewModel.kasTransactions.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Title banner
        item {
            Text(
                text = "Dashboard Analitik & Tren",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Grafis visualisasi sebaran data absensi dan perputaran keuangan kas",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Attendance Trend Chart Line Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tren Kehadiran Anggota (Latihan Bulanan)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Line chart drawn directly via Canvas API
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val linePoints = listOf(45f, 60f, 52f, 85f, 70f, 95f) // mock coordinates percents
                            val spacing = size.width / (linePoints.size - 1)
                            val path = Path()

                            // Draw Background grid
                            val gridLinesCount = 4
                            for (i in 0..gridLinesCount) {
                                val y = (size.height / gridLinesCount) * i
                                drawLine(
                                    color = Color.LightGray.copy(alpha = 0.3f),
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            // Build line path
                            linePoints.forEachIndexed { index, percent ->
                                val x = spacing * index
                                val y = size.height - (size.height * (percent / 100f))
                                if (index == 0) {
                                    path.moveTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                }
                                // Draw points circular nodes
                                drawCircle(
                                    color = Color(0xFF2196F3),
                                    radius = 4.dp.toPx(),
                                    center = Offset(x, y)
                                )
                            }

                            // Render Line Stroked
                            drawPath(
                                path = path,
                                color = Color(0xFF2196F3),
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun").forEach { month ->
                            Text(text = month, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Cash flow Bar Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Arus Perputaran Uang Kas (Pemasukan vs Pengeluaran)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFFFF9800)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Drawn bars chart
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val data = listOf(
                                Pair(180f, 40f),  // Jan: Income vs Expense
                                Pair(120f, 90f),  // Feb
                                Pair(240f, 60f),  // Mar
                                Pair(150f, 130f), // Apr
                                Pair(300f, 80f)   // Mei
                            )
                            val barWidth = 14.dp.toPx()
                            val spacing = size.width / data.size

                            data.forEachIndexed { index, pair ->
                                val xCenter = spacing * index + (spacing / 2)
                                
                                // Draw Income Bar (Green representing income assets)
                                val incomeHeight = (pair.first / 350f) * size.height
                                drawRect(
                                    color = Color(0xFF4CAF50),
                                    topLeft = Offset(xCenter - barWidth - 2.dp.toPx(), size.height - incomeHeight),
                                    size = Size(barWidth, incomeHeight)
                                )

                                // Draw Expense Bar (Red representing expenses outcoming)
                                val expenseHeight = (pair.second / 350f) * size.height
                                drawRect(
                                    color = Color(0xFFF44336),
                                    topLeft = Offset(xCenter + 2.dp.toPx(), size.height - expenseHeight),
                                    size = Size(barWidth, expenseHeight)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Januari", "Februari", "Maret", "April", "Mei").forEach { month ->
                            Text(
                                text = month,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.width(55.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                            Box(modifier = Modifier.size(12.dp).background(Color(0xFF4CAF50), shape = RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pemasukan (In)", fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                            Box(modifier = Modifier.size(12.dp).background(Color(0xFFF44336), shape = RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pengeluaran (Out)", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- PARENT DASHBOARD VIEW ---
@Composable
fun ParentDashboardView(
    viewModel: SiskoViewModel,
    onNavigateToSection: (String) -> Unit
) {
    val child by viewModel.parentChildMember.collectAsState()
    val childOrg by viewModel.parentChildOrg.collectAsState()
    val allAttendance by viewModel.allAttendanceGlobally.collectAsState()
    val allAwardedAchievements by viewModel.allMemberAchievementsGlobally.collectAsState()
    val allAchievementsList by viewModel.allAchievements.collectAsState()

    val childAtts = child?.id?.let { cid -> allAttendance.filter { it.memberId == cid } } ?: emptyList()
    val totalChildAtts = childAtts.size
    val childPresents = childAtts.count { it.status == "Hadir" }
    val childAttRate = if (totalChildAtts > 0) (childPresents.toFloat() / totalChildAtts * 100).toInt() else 0

    val childBadges = child?.id?.let { cid ->
        allAwardedAchievements.filter { it.memberId == cid }.mapNotNull { map ->
            allAchievementsList.find { it.id == map.achievementId }
        }
    } ?: emptyList()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Dashboard Monitoring Orang Tua",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Pantau perkembangan presensi, iuran kas, dan piagam prestasi ananda secara transparan",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Child Details Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = child?.name?.firstOrNull()?.toString() ?: "S",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = child?.name ?: "Siswa",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(text = "Kelas: ${child?.classGroup ?: "-"}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        Text(text = "Eskul Terdaftar: ${childOrg?.name ?: "Belum Terdaftar"}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Child Performance summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Persentase Kehadiran", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "$childAttRate%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (childAttRate > 80) Color(0xFF4CAF50) else Color(0xFFFF9800))
                        Text(text = "$childPresents Hadir dari $totalChildAtts Sesi", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }

                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Akumulasi Poin Sisko", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${child?.points ?: 0} B", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                        Text(text = "Gamifikasi Aktif", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }

        // Child's Badges / Achievements list
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Piagam & Penghargaan Ananda",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (childBadges.isEmpty()) {
                        Text(
                            text = "Belum memiliki piagam penghargaan resmi.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        )
                    } else {
                        childBadges.forEach { bad ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFFFC107).copy(alpha = 0.2f), shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFFFFC107))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = bad.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(text = bad.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick shortcut
        item {
            Button(
                onClick = { onNavigateToSection("attendance") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lihat Riwayat Absensi Lengkap")
            }
        }
    }
}

// --- SMART REMINDER VIEW ---
@Composable
fun SmartReminderView(viewModel: SiskoViewModel) {
    val members by viewModel.members.collectAsState()
    val attendanceRecords by viewModel.attendance.collectAsState()
    val kasRecords by viewModel.kasTransactions.collectAsState()

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayAtt = attendanceRecords.filter { it.date == todayStr }

    // Members who have not taken attendance today
    val unCheckedMembers = members.filter { m -> todayAtt.none { it.memberId == m.id } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Pusat Smart Reminder Sisko",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Deteksi otomatis untuk mempercepat rekonsiliasi tugas & data eskul",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Unpressed warning box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationImportant, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Belum Absensi Hari Ini (${unCheckedMembers.size} Siswa)",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Siswa-siswa di bawah terdeteksi belum mengonfirmasi kehadiran hari ini. Klik tombol untuk kirim pengingat:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        if (unCheckedMembers.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("Semua siswa terdaftar sudah check-in hari ini! Mantap 👍", color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(unCheckedMembers) { member ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = member.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Kelas: ${member.classGroup} • ${member.roleInOrg}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }

                        Button(
                            onClick = {
                                // Simulate WhatsApp/Push broadcast action
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // whatsapp green style
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Colek WA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
