package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.GalleryPhotoEntity
import com.example.ui.SiskoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: SiskoViewModel,
    onNavigateBack: () -> Unit
) {
    val photoGrid by viewModel.galleryPhotos.collectAsState()
    var showUploadDialog by remember { mutableStateOf(false) }
    var activeZoomPhoto by remember { mutableStateOf<GalleryPhotoEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Galeri Foto Kegiatan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { showUploadDialog = true }) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Unggah Foto", tint = MaterialTheme.colorScheme.primary)
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
                text = "Koleksi Kenangan & Dokumentasi Acara",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Dokumentasikan momen-momen seru dan berharga selama latihan gabungan eskul langsung di satu berkas bersama.",
                fontSize = 11.sp,
                color = Color.Gray
            )

            if (photoGrid.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Belum ada unggahan foto dokumentasi.", fontSize = 13.sp, color = Color.Gray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(photoGrid) { photo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.85f)
                                .clickable { activeZoomPhoto = photo }
                        ) {
                            Column {
                                // Load image via Coil AsyncImage
                                AsyncImage(
                                    model = photo.imageUrl,
                                    contentDescription = photo.caption,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1.2f)
                                )

                                Column(modifier = Modifier.padding(8.dp).weight(0.8f), verticalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        text = photo.caption,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "Oleh: ${photo.uploadedBy}", fontSize = 9.sp, color = Color.Gray)
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Hapus Foto",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { viewModel.deleteGalleryPhoto(photo) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Gallery photo dialog
        if (showUploadDialog) {
            UploadPhotoDialog(
                onDismiss = { showUploadDialog = false },
                onSave = { link, caption ->
                    viewModel.uploadGalleryPhoto(link, caption)
                    showUploadDialog = false
                }
            )
        }

        // Fullscreen Lightbox Zoom view Dialog
        activeZoomPhoto?.let { photo ->
            AlertDialog(
                onDismissRequest = { activeZoomPhoto = null },
                text = {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column {
                            AsyncImage(
                                model = photo.imageUrl,
                                contentDescription = photo.caption,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            )
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = photo.caption, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "Diupload oleh: ${photo.uploadedBy}", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { activeZoomPhoto = null }) {
                        Text("Tutup")
                    }
                }
            )
        }
    }
}

@Composable
fun UploadPhotoDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var photoLinkInput by remember { mutableStateOf("") }
    var captionText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Momen Kegiatan Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = photoLinkInput,
                    onValueChange = { photoLinkInput = it },
                    label = { Text("URL Link Foto (Cloudinary/Unsplash)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    label = { Text("Keterangan Caption") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (photoLinkInput.isNotBlank() && captionText.isNotBlank()) {
                    onSave(photoLinkInput, captionText)
                }
            }) {
                Text("Bagikan Foto")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
