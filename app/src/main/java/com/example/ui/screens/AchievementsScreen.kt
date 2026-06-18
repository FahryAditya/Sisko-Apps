package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    viewModel: SiskoViewModel,
    onNavigateBack: () -> Unit
) {
    val leaderboard by viewModel.leaderboardList.collectAsState()
    val activeCategory by viewModel.leaderboardCategory.collectAsState()
    val allBadgesList by viewModel.allAchievements.collectAsState()
    val membersList by viewModel.members.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var activeSubTab by remember { mutableStateOf("ranks") } // "ranks", "badges"
    var showAwardDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gamifikasi & Prestasi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (activeSubTab == "badges" && (currentUser?.role == "Administrator" || currentUser?.role == "OrgAdmin")) {
                        IconButton(onClick = { showAwardDialog = true }) {
                            Icon(Icons.Default.MilitaryTech, contentDescription = "Berikan Lencana", tint = Color(0xFFFFC107))
                        }
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
            // Gamification toggler
            TabRow(selectedTabIndex = if (activeSubTab == "ranks") 0 else 1) {
                Tab(selected = activeSubTab == "ranks", onClick = { activeSubTab = "ranks" }) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Stars, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFFF9800))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Leaderboard Siswa", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Tab(selected = activeSubTab == "badges", onClick = { activeSubTab = "badges" }) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFFFC107))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Lencana & Piagam", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (activeSubTab == "ranks") {
                    LeaderboardView(
                        activeCategory = activeCategory,
                        leaderboard = leaderboard,
                        onCategorySelected = { viewModel.setLeaderboardCategory(it) }
                    )
                } else {
                    BadgesCatalogView(
                        allBadgesList = allBadgesList
                    )
                }
            }
        }

        // Award badge dialogpopup
        if (showAwardDialog) {
            AwardBadgeDialog(
                members = membersList,
                badges = allBadgesList,
                onDismiss = { showAwardDialog = false },
                onAward = { mid, bid ->
                    viewModel.awardBadgeToMember(mid, bid)
                    showAwardDialog = false
                }
            )
        }
    }
}

@Composable
fun LeaderboardView(
    activeCategory: String,
    leaderboard: List<MemberEntity>,
    onCategorySelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toggle categories
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onCategorySelected("points") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeCategory == "points") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (activeCategory == "points") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.OfflineBolt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Berdasar Poin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onCategorySelected("attendance") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeCategory == "attendance") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (activeCategory == "attendance") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.EventAvailable, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Berdasar Hadir", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Leaderboard listings
        if (leaderboard.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Klasemen kosong. Isi dengan data siswa pembina.", fontSize = 13.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(leaderboard) { idx, student ->
                    val isTop3 = idx < 3
                    val placementColor = when (idx) {
                        0 -> Color(0xFFFFD700) // Gold medals color
                        1 -> Color(0xFFC0C0C0) // Silver medals color
                        2 -> Color(0xFFCD7F32) // Bronze medals color
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

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
                                // Index position round badge
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(
                                            placementColor.copy(alpha = if (isTop3) 1f else 0.1f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${idx + 1}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isTop3) Color.Black else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(text = student.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "${student.classGroup} • ${student.roleInOrg}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }

                            // Metric value
                            Text(
                                text = if (activeCategory == "points") "${student.points} pts" else "Aktif Sesi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (idx == 0) Color(0xFFF57F17) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgesCatalogView(
    allBadgesList: List<AchievementEntity>
) {
    if (allBadgesList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Katalog Piagam Kosong.", fontSize = 13.sp, color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allBadgesList) { badge ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFFFC107).copy(alpha = 0.15f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (badge.badgeType) {
                                    "STAR" -> Icons.Default.Stars
                                    "GOLD_MEDAL" -> Icons.Default.WorkspacePremium
                                    "CROWN" -> Icons.Default.EmojiEvents
                                    else -> Icons.Default.MilitaryTech
                                },
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = badge.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = badge.description, fontSize = 12.sp, color = Color.Gray)
                        }

                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE3F2FD), shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = "+${badge.points} B", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1E88E5))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AwardBadgeDialog(
    members: List<MemberEntity>,
    badges: List<AchievementEntity>,
    onDismiss: () -> Unit,
    onAward: (Int, Int) -> Unit
) {
    var selectedMemberEmail by remember { mutableStateOf("") }
    var selectedBadgeTitle by remember { mutableStateOf("") }

    var memberPickId by remember { mutableStateOf<Int?>(null) }
    var badgePickId by remember { mutableStateOf<Int?>(null) }

    var memExpanded by remember { mutableStateOf(false) }
    var badExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Anugerah Piagam Kehormatan", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Anugerah penghargaan prestasi resmi kepada siswa terpilih untuk meningkatkan poin secara transparan offline-first:", fontSize = 12.sp, color = Color.Gray)
                
                // Member dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { memExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text(text = if (selectedMemberEmail.isEmpty()) "Pilih Siswa Penerima" else selectedMemberEmail)
                    }
                    DropdownMenu(expanded = memExpanded, onDismissRequest = { memExpanded = false }) {
                        members.forEach { m ->
                            DropdownMenuItem(text = { Text("${m.name} (${m.classGroup})") }, onClick = {
                                selectedMemberEmail = m.name
                                memberPickId = m.id
                                memExpanded = false
                            })
                        }
                    }
                }

                // Badge dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { badExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text(text = if (selectedBadgeTitle.isEmpty()) "Pilih Jenis Piagam" else selectedBadgeTitle)
                    }
                    DropdownMenu(expanded = badExpanded, onDismissRequest = { badExpanded = false }) {
                        badges.forEach { b ->
                            DropdownMenuItem(text = { Text("${b.title} (+${b.points} Pts)") }, onClick = {
                                selectedBadgeTitle = b.title
                                badgePickId = b.id
                                badExpanded = false
                            })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val mid = memberPickId
                val bid = badgePickId
                if (mid != null && bid != null) {
                    onAward(mid, bid)
                }
            }) {
                Text("Anugerahkan Lencana")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
