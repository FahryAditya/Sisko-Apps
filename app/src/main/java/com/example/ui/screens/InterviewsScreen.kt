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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InterviewEntity
import com.example.ui.SiskoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewsScreen(
    viewModel: SiskoViewModel,
    onNavigateBack: () -> Unit
) {
    val interviewList by viewModel.interviews.collectAsState()
    var showCreateForm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wawancara Penerimaan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateForm = true }) {
                        Icon(Icons.Default.PostAdd, contentDescription = "Tambah Catatan", tint = MaterialTheme.colorScheme.primary)
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
                text = "Log Wawancara Potensi Calon Anggota",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Catat dan evaluasi kecocokan minat bakat calon peserta baru berdasarkan skor kualitatif pembina.",
                fontSize = 11.sp,
                color = Color.Gray
            )

            if (interviewList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Belum ada evaluasi calon peserta eskul.", fontSize = 13.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(interviewList) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = item.memberName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(text = "Di-interview oleh: ${item.recruiterName}", fontSize = 11.sp, color = Color.Gray)
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteInterview(item) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus Catatan", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                
                                // Stars display block
                                StarRatingBar(rating = item.rating, size = 18.dp)

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = item.comment,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    Text(text = "Tanggal: ${item.date}", fontSize = 10.sp, color = Color.LightGray)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showCreateForm) {
            AddInterviewDialog(
                onDismiss = { showCreateForm = false },
                onSave = { name, score, comments ->
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    viewModel.createInterview(name, score, comments, today)
                    showCreateForm = false
                }
            )
        }
    }
}

@Composable
fun StarRatingBar(
    rating: Float,
    size: androidx.compose.ui.unit.Dp = 20.dp,
    clickable: Boolean = false,
    onRatingChanged: (Float) -> Unit = {}
) {
    Row {
        for (i in 1..5) {
            val fillStar = i <= rating
            val starIcon = if (fillStar) Icons.Default.Star else Icons.Default.StarBorder
            Icon(
                imageVector = starIcon,
                contentDescription = null,
                tint = if (fillStar) Color(0xFFFFC107) else Color.LightGray,
                modifier = Modifier
                    .size(size)
                    .clickable(enabled = clickable) {
                        onRatingChanged(i.toFloat())
                    }
            )
        }
    }
}

@Composable
fun AddInterviewDialog(
    onDismiss: () -> Unit,
    onSave: (String, Float, String) -> Unit
) {
    var studentName by remember { mutableStateOf("") }
    var scoreStar by remember { mutableStateOf(4f) }
    var commentText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Evaluasi Wawancara", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = studentName,
                    onValueChange = { studentName = it },
                    label = { Text("Nama Calon Anggota") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "Kompentensi / Skor Bakat:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                StarRatingBar(
                    rating = scoreStar,
                    size = 28.dp,
                    clickable = true,
                    onRatingChanged = { scoreStar = it }
                )

                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("Hasil Observasi Penilai (Catatan)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (studentName.isNotBlank() && commentText.isNotBlank()) {
                    onSave(studentName, scoreStar, commentText)
                }
            }) {
                Text("Simpan Evaluasi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
