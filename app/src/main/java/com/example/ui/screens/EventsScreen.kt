package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.EventEntity
import com.example.ui.SiskoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    viewModel: SiskoViewModel,
    onNavigateBack: () -> Unit
) {
    val eventsList by viewModel.events.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var expandedEventDetail by remember { mutableStateOf<EventEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agenda & Event Organisasi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (currentUser?.role == "Administrator" || currentUser?.role == "OrgAdmin") {
                        IconButton(onClick = { showCreateDialog = true }) {
                            Icon(Icons.Default.AddAlarm, contentDescription = "Rencana Acara", tint = MaterialTheme.colorScheme.primary)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Kalender Agenda Menarik",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Simak rencana kegiatan menarik yang akan diselenggarakan dan konfirmasi kehadiran RSVP anda dengan mandiri.",
                fontSize = 11.sp,
                color = Color.Gray
            )

            if (eventsList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Belum ada agenda terencana terdaftar.", fontSize = 13.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(eventsList) { agenda ->
                        val rsvpsFlow = viewModel.getRsvpsForEventFlow(agenda.id).collectAsState(initial = emptyList())
                        val rsvpList = rsvpsFlow.value
                        val goingCount = rsvpList.count { it.status == "Going" }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedEventDetail = agenda }
                        ) {
                            Column {
                                AsyncImage(
                                    model = agenda.imageUrl,
                                    contentDescription = agenda.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                )

                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = agenda.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                        IconButton(onClick = { viewModel.deleteEvent(agenda) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Hapus Agenda", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = agenda.date, fontSize = 12.sp, color = Color.Gray)
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = agenda.location, fontSize = 12.sp, color = Color.Gray)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(text = agenda.description, fontSize = 13.sp, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // RSVP submission actions area
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "RSVP: $goingCount Hadir", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Button(
                                                onClick = { viewModel.responseEventRsvp(agenda.id, "Going") },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text("Going", fontSize = 10.sp)
                                            }

                                            OutlinedButton(
                                                onClick = { viewModel.responseEventRsvp(agenda.id, "Not Going") },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text("Skip", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Create Dialog
        if (showCreateDialog) {
            AddEventDialog(
                onDismiss = { showCreateDialog = false },
                onSave = { title, desc, date, loc, image ->
                    viewModel.createEvent(title, desc, date, loc, image)
                    showCreateDialog = false
                }
            )
        }

        // Detail Popup
        expandedEventDetail?.let { detail ->
            AlertDialog(
                onDismissRequest = { expandedEventDetail = null },
                title = { Text(detail.title, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Rincian Deskripsi: ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = detail.description, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Tempat diselenggarakan: ${detail.location}", fontSize = 11.sp, color = Color.Gray)
                        Text(text = "Waktu Agenda: ${detail.date}", fontSize = 11.sp, color = Color.Gray)
                    }
                },
                confirmButton = {
                    Button(onClick = { expandedEventDetail = null }) {
                        Text("Tutup Detail")
                    }
                }
            )
        }
    }
}

@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var imagePoster by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daftarkan Agenda Eskul", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Nama Rencana Acara") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Uraian Singkat Acara") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Tgl (Tahun-Bl-H)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Tempat") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = imagePoster, onValueChange = { imagePoster = it }, label = { Text("Poster URL (Opsional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank() && location.isNotBlank()) {
                    onSave(title, desc, date, location, imagePoster)
                }
            }) {
                Text("Daftarkan Rencana")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
