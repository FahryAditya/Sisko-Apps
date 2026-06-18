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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.SiskoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementsScreen(
    viewModel: SiskoViewModel,
    onNavigateBack: () -> Unit
) {
    val announcementsList by viewModel.announcements.collectAsState()
    val chatList by viewModel.chatMessages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var activeSubTab by remember { mutableStateOf("notices") } // "notices", "chat"
    var showCreateNoticeDialog by remember { mutableStateOf(false) }
    var activeCommentingNotice by remember { mutableStateOf<AnnouncementEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hub Komunikasi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (activeSubTab == "notices" && (currentUser?.role == "Administrator" || currentUser?.role == "OrgAdmin")) {
                        IconButton(onClick = { showCreateNoticeDialog = true }) {
                            Icon(Icons.Default.NoteAdd, contentDescription = "Siarkan Pengumuman", tint = MaterialTheme.colorScheme.primary)
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
            // Communications toggle
            TabRow(selectedTabIndex = if (activeSubTab == "notices") 0 else 1) {
                Tab(selected = activeSubTab == "notices", onClick = { activeSubTab = "notices" }) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pengumuman Resmi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Tab(selected = activeSubTab == "chat", onClick = { activeSubTab = "chat" }) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Grup Diskusi Siswa", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (activeSubTab == "notices") {
                    NoticesListView(
                        announcements = announcementsList,
                        onOpenComments = { activeCommentingNotice = it }
                    )
                } else {
                    GroupChatView(
                        chatHistory = chatList,
                        currentUser = currentUser,
                        onSendMessage = { txt -> viewModel.sendChatMessage(txt) },
                        onReactMessage = { cid, emo -> viewModel.addChatReaction(cid, emo) },
                        onDeleteMsg = { msg -> viewModel.deleteChatMessage(msg) }
                    )
                }
            }
        }

        // Posting announcement Form popup
        if (showCreateNoticeDialog) {
            AddAnnouncementDialog(
                onDismiss = { showCreateNoticeDialog = false },
                onSave = { title, content, image ->
                    viewModel.publishAnnouncement(title, content, image)
                    showCreateNoticeDialog = false
                }
            )
        }

        // Comment section sheet/dialog popup
        activeCommentingNotice?.let { notice ->
            val commentsFlow = viewModel.getCommentsForAnnouncementFlow(notice.id).collectAsState(initial = emptyList())
            CommentsDialog(
                notice = notice,
                commentsList = commentsFlow.value,
                onDismiss = { activeCommentingNotice = null },
                onSubmitComment = { text ->
                    viewModel.submitAnnouncementComment(notice.id, text)
                }
            )
        }
    }
}

@Composable
fun NoticesListView(
    announcements: List<AnnouncementEntity>,
    onOpenComments: (AnnouncementEntity) -> Unit
) {
    if (announcements.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Belum ada pengumuman disiarkan pembina.", fontSize = 13.sp, color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(announcements) { notice ->
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = notice.authorName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = "Pembina Organisasi", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                            Text(text = notice.date, fontSize = 10.sp, color = Color.LightGray)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = notice.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = notice.content, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Interactions actions bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.clickable { /* mock like toggling */ },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Suka (4)", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            }

                            Row(
                                modifier = Modifier.clickable { onOpenComments(notice) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Comment, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Diskusi Komentar (Tulis)", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupChatView(
    chatHistory: List<ChatMessageEntity>,
    currentUser: UserEntity?,
    onSendMessage: (String) -> Unit,
    onReactMessage: (Int, String) -> Unit,
    onDeleteMsg: (ChatMessageEntity) -> Unit
) {
    var textMessageInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat logs bubble list
        if (chatHistory.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Grup diskusi kosong. Mulai percakapan!", fontSize = 13.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(modifier = Modifier.padding(10.dp)) {
                            Text(text = "🔔 Ini adalah saluran grup internal eskul. Obrolan bersifat rahasia dan transparan terpantau pembina.", fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                items(chatHistory) { msg ->
                    val isMyMsg = msg.senderId == currentUser?.id

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isMyMsg) Alignment.End else Alignment.Start
                    ) {
                        // Sender ID Meta
                        Text(
                            text = "${msg.senderName} (${msg.senderRole})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (msg.senderRole == "Admin") MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )

                        // Bubble Body Card
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMyMsg) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                                bottomStart = if (isMyMsg) 12.dp else 0.dp,
                                bottomEnd = if (isMyMsg) 0.dp else 12.dp
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = msg.messageText, fontSize = 13.sp)
                                
                                // Reactions list line
                                if (msg.reactions.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        msg.reactions.split(",").forEach { emo ->
                                            Box(
                                                modifier = Modifier
                                                    .background(Color.White.copy(alpha = 0.6f), shape = RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(text = emo, fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Hot Emoji Reactions Line
                        Row(
                            modifier = Modifier.padding(top = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf("👍", "❤️", "🔥").forEach { emo ->
                                Text(
                                    text = emo,
                                    fontSize = 11.sp,
                                    modifier = Modifier
                                        .clickable { onReactMessage(msg.id, emo) }
                                        .padding(horizontal = 2.dp)
                                )
                            }
                            if (isMyMsg) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus Pecakapan",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { onDeleteMsg(msg) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

        // Input row bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = textMessageInput,
                onValueChange = { textMessageInput = it },
                placeholder = { Text("Tulis pesan obrolan...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (textMessageInput.isNotBlank()) {
                        onSendMessage(textMessageInput)
                        textMessageInput = ""
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Kirim",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun AddAnnouncementDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Siarkan Pengumuman Resmi", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul Pengumuman") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Isi Diskusi Pengumuman") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("URL Poster Acara (Opsional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank() && content.isNotBlank()) {
                    onSave(title, content, imageUrl)
                }
            }) {
                Text("Siarkan")
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
fun CommentsDialog(
    notice: AnnouncementEntity,
    commentsList: List<AnnouncementCommentEntity>,
    onDismiss: () -> Unit,
    onSubmitComment: (String) -> Unit
) {
    var myCommentInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = "Diskusi Komentar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = notice.title, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Comments logs
                Box(modifier = Modifier.weight(1f)) {
                    if (commentsList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada tanggapan komentar.", fontSize = 12.sp, color = Color.Gray)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(commentsList) { comment ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.LightGray.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(text = comment.authorName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = comment.commentText, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Input writing row
                OutlinedTextField(
                    value = myCommentInput,
                    onValueChange = { myCommentInput = it },
                    placeholder = { Text("Tulis komentar anda...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (myCommentInput.isNotBlank()) {
                                    onSubmitComment(myCommentInput)
                                    myCommentInput = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}
