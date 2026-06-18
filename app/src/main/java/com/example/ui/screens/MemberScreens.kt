package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MemberEntity
import com.example.ui.SiskoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberScreens(
    viewModel: SiskoViewModel,
    onNavigateBack: () -> Unit
) {
    val membersList by viewModel.members.collectAsState()
    val searchQuery by viewModel.memberSearchQuery.collectAsState()
    val classFilter by viewModel.memberFilterClass.collectAsState()
    val roleFilter by viewModel.memberFilterRole.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var currentEditingMember by remember { mutableStateOf<MemberEntity?>(null) }

    val classes = listOf("Semua Kelas", "11 MIPA 1", "11 MIPA 2", "11 MIPA 3", "12 IPS 1", "12 IPS 2")
    var classExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Anggota & Siswa", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.DownloadForOffline, contentDescription = "Export Excel", tint = Color(0xFF4CAF50))
                    }
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(Icons.Default.UploadFile, contentDescription = "Import CSV", tint = Color(0xFF2196F3))
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Tambah Anggota", tint = MaterialTheme.colorScheme.primary)
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
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setMemberSearchQuery(it) },
                label = { Text("Cari Anggota (Nama / Email)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setMemberSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Class Filter selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Filter Kelas:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                
                Box {
                    Button(
                        onClick = { classExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text(text = if (classFilter.isEmpty()) "Semua Kelas" else classFilter, fontSize = 12.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(expanded = classExpanded, onDismissRequest = { classExpanded = false }) {
                        classes.forEach { cl ->
                            DropdownMenuItem(
                                text = { Text(cl, fontSize = 13.sp) },
                                onClick = {
                                    viewModel.setMemberFilterClass(if (cl == "Semua Kelas") "" else cl)
                                    classExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Directory List
            if (membersList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tidak menemukan data siswa teregistrasi", fontSize = 13.sp, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(membersList) { member ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { currentEditingMember = member },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = member.name.firstOrNull()?.toString() ?: "S",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = member.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(text = "Kelas: ${member.classGroup} • ${member.roleInOrg}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Text(text = "Kontak: ${member.phone}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFFFF8E1), shape = RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = "${member.points} B", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFF57F17))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    IconButton(
                                        onClick = { viewModel.deleteMember(member) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus Siswa", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Member dialog popup
        if (showAddDialog) {
            MemberFormDialog(
                onDismiss = { showAddDialog = false },
                onSave = { name, email, phone, classGroup, role, photo ->
                    viewModel.addMember(name, email, phone, classGroup, role, photo)
                    showAddDialog = false
                }
            )
        }

        // Edit Member dialog popup
        currentEditingMember?.let { member ->
            MemberFormDialog(
                member = member,
                onDismiss = { currentEditingMember = null },
                onSave = { name, email, phone, classGroup, role, photo ->
                    viewModel.updateMember(
                        member.copy(name = name, email = email, phone = phone, classGroup = classGroup, roleInOrg = role, photoUrl = photo)
                    )
                    currentEditingMember = null
                }
            )
        }

        // CSV Import dialog
        if (showImportDialog) {
            BulkImportDialog(
                onDismiss = { showImportDialog = false },
                onImport = { csvText ->
                    viewModel.importMembersCsv(csvText) { importedCount ->
                        // Simulation reports back successes
                    }
                    showImportDialog = false
                }
            )
        }

        // CSV/Excel formatted Export dialog mockup
        if (showExportDialog) {
            ExcelExportViewDialog(
                members = membersList,
                onDismiss = { showExportDialog = false }
            )
        }
    }
}

@Composable
fun MemberFormDialog(
    member: MemberEntity? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(member?.name ?: "") }
    var email by remember { mutableStateOf(member?.email ?: "") }
    var phone by remember { mutableStateOf(member?.phone ?: "") }
    var classGroup by remember { mutableStateOf(member?.classGroup ?: "11 MIPA 1") }
    var roleInOrg by remember { mutableStateOf(member?.roleInOrg ?: "Anggota") }
    var photoUrl by remember { mutableStateOf(member?.photoUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (member == null) "Tambah Anggota Baru" else "Ubah Profil Siswa", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email (Tautan Akun)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Nomor HP / WA") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = classGroup, onValueChange = { classGroup = it }, label = { Text("Rombel / Kelas") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = roleInOrg, onValueChange = { roleInOrg = it }, label = { Text("Pangkat Organisasi") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = photoUrl, onValueChange = { photoUrl = it }, label = { Text("URL Foto Avatar (Opsional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, email, phone, classGroup, roleInOrg, photoUrl) }) {
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
fun BulkImportDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var csvText by remember {
        mutableStateOf(
            "Rian Mahendra,rian@siswa.id,081223400,11 MIPA 1,Anggota\n" +
            "Gita Kencana,gita@siswa.id,081223401,11 MIPA 3,Bendahara\n" +
            "Hendra Gunawan,hendra@siswa.id,081223402,12 IPS 2,Anggota"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Impor Anggota Massal (CSV)", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Format Baris: Nama,Email,Telp,Kelas,Jabatan", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = csvText,
                    onValueChange = { csvText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    label = { Text("Salin Berkas CSV Di Sini") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onImport(csvText) }) {
                Text("Impor Sekarang")
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
fun ExcelExportViewDialog(
    members: List<MemberEntity>,
    onDismiss: () -> Unit
) {
    // Construct text representation of excel mapping spreadsheet grid
    val sb = StringBuilder()
    sb.append("SISKO SPREADSHEET EXPORT SERVICE\n")
    sb.append("-------------------------------------------------------------------------\n")
    sb.append(String.format("%-4s | %-16s | %-16s | %-8s | %-12s | %-5s\n", "ID", "NAMA SISWA", "EMAIL", "KELAS", "JABATAN", "POIN"))
    sb.append("-------------------------------------------------------------------------\n")
    members.forEachIndexed { idx, m ->
        val truncName = if (m.name.length > 15) m.name.substring(0, 13) + ".." else m.name
        val truncEmail = if (m.email.length > 15) m.email.substring(0, 13) + ".." else m.email
        sb.append(String.format("%-4d | %-16s | %-16s | %-8s | %-12s | %-5d\n", idx + 1, truncName, truncEmail, m.classGroup, m.roleInOrg, m.points))
    }
    sb.append("-------------------------------------------------------------------------\n")
    sb.append("File Status: SUCCESS COMPILED .XLSB. GENERATED LOCALLY.")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Laporan Excel Berhasil", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Visualisasi hasil simpan lembar kerja (Excel .XLSX):", fontSize = 12.sp, color = Color.Gray)
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
