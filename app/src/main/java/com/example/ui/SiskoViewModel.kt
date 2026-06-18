package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class SiskoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SiskoDatabase.getDatabase(application, viewModelScope)
    private val repository = SiskoRepository(db.siskoDao())

    // --- AUTHENTICATION STATE ---
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // --- GLOBAL CONTEXTS ---
    val allOrganizations: StateFlow<List<OrganizationEntity>> = repository.allOrganizationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMembers: StateFlow<List<MemberEntity>> = repository.allMembersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendanceGlobally: StateFlow<List<AttendanceEntity>> = repository.allAttendanceFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allKasTransactionsGlobally: StateFlow<List<KasTransactionEntity>> = repository.allKasTransactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAchievements: StateFlow<List<AchievementEntity>> = repository.allAchievementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMemberAchievementsGlobally: StateFlow<List<MemberAchievementEntity>> = repository.allMemberAchievementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrganizationAdminsFlow: StateFlow<List<OrganizationAdminEntity>> = repository.allOrganizationAdminsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Org for scoped views
    private val _selectedOrgId = MutableStateFlow<Int?>(null)
    val selectedOrgId: StateFlow<Int?> = _selectedOrgId.asStateFlow()

    val currentOrganization: StateFlow<OrganizationEntity?> = _selectedOrgId
        .mapLatest { id -> id?.let { repository.getOrganizationById(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- SEARCH / FILTERS ---
    private val _memberSearchQuery = MutableStateFlow("")
    val memberSearchQuery: StateFlow<String> = _memberSearchQuery.asStateFlow()

    private val _memberFilterClass = MutableStateFlow("")
    val memberFilterClass: StateFlow<String> = _memberFilterClass.asStateFlow()

    private val _memberFilterRole = MutableStateFlow("")
    val memberFilterRole: StateFlow<String> = _memberFilterRole.asStateFlow()

    private val _kasFilterType = MutableStateFlow("") // "", "INCOME", "EXPENSE"
    val kasFilterType: StateFlow<String> = _kasFilterType.asStateFlow()

    private val _leaderboardCategory = MutableStateFlow("points") // "points", "attendance"
    val leaderboardCategory: StateFlow<String> = _leaderboardCategory.asStateFlow()

    // --- SCOPED FLOWS (DYNAMIC BY SELECTED ORG) ---
    val members: StateFlow<List<MemberEntity>> = combine(
        _selectedOrgId,
        _memberSearchQuery,
        _memberFilterClass,
        _memberFilterRole
    ) { orgId, query, cl, role ->
        if (orgId == null) return@combine emptyList<MemberEntity>()
        repository.getMembersByOrgIdFlow(orgId).firstOrNull()?.filter {
            (query.isEmpty() || it.name.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true)) &&
            (cl.isEmpty() || it.classGroup == cl) &&
            (role.isEmpty() || it.roleInOrg == role)
        } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendance: StateFlow<List<AttendanceEntity>> = _selectedOrgId
        .flatMapLatest { id -> id?.let { repository.getAttendanceByOrgIdFlow(it) } ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val kasTransactions: StateFlow<List<KasTransactionEntity>> = combine(
        _selectedOrgId,
        _kasFilterType
    ) { id, filter ->
        if (id == null) return@combine emptyList<KasTransactionEntity>()
        repository.getKasTransactionsByOrgIdFlow(id).firstOrNull()?.filter {
            filter.isEmpty() || it.type == filter
        } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcements: StateFlow<List<AnnouncementEntity>> = _selectedOrgId
        .flatMapLatest { id -> id?.let { repository.getAnnouncementsByOrgIdFlow(it) } ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = _selectedOrgId
        .flatMapLatest { id -> id?.let { repository.getChatMessagesByOrgIdFlow(it) } ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val galleryPhotos: StateFlow<List<GalleryPhotoEntity>> = _selectedOrgId
        .flatMapLatest { id -> id?.let { repository.getGalleryPhotosByOrgIdFlow(it) } ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val events: StateFlow<List<EventEntity>> = _selectedOrgId
        .flatMapLatest { id -> id?.let { repository.getEventsByOrgIdFlow(it) } ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val interviews: StateFlow<List<InterviewEntity>> = _selectedOrgId
        .flatMapLatest { id -> id?.let { repository.getInterviewsByOrgIdFlow(it) } ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- LEADERBOARD SCOPED/GLOBAL FLOW ---
    val leaderboardList: StateFlow<List<MemberEntity>> = combine(
        members,
        _leaderboardCategory,
        _selectedOrgId
    ) { orgMembers, category, orgId ->
        if (orgId == null) return@combine emptyList()
        val allAtt = repository.allAttendanceFlow.firstOrNull() ?: emptyList()
        
        when (category) {
            "attendance" -> {
                orgMembers.sortedByDescending { member ->
                    allAtt.count { it.memberId == member.id && it.status == "Hadir" }
                }
            }
            else -> { // "points"
                orgMembers.sortedByDescending { it.points }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Parents dashboard child tracking context
    val parentChildMember: StateFlow<MemberEntity?> = _currentUser
        .filter { it?.role == "Parent" && it.parentOfStudentId != null }
        .map { parent ->
            parent?.parentOfStudentId?.let { studentId ->
                // Look up student User entity details
                val studentUser = repository.getUserById(studentId)
                if (studentUser != null) {
                    val allM = repository.allMembersFlow.first()
                    // Find member row matching email
                    allM.find { it.email.equals(studentUser.email, ignoreCase = true) }
                } else null
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val parentChildOrg: StateFlow<OrganizationEntity?> = parentChildMember
        .mapLatest { child -> child?.organizationId?.let { repository.getOrganizationById(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- AUTH ACTIONS ---
    fun login(email: String, pword: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _loginError.value = null
            val user = repository.getUserByEmail(email.trim())
            if (user != null && user.passwordHash == pword.trim()) {
                _currentUser.value = user
                // Autoselect organization if attached
                if (user.organizationId != null) {
                    _selectedOrgId.value = user.organizationId
                } else if (user.role == "Parent" && user.parentOfStudentId != null) {
                    // Try to resolve student org
                    launch {
                        val studentUser = repository.getUserById(user.parentOfStudentId)
                        if (studentUser?.organizationId != null) {
                            _selectedOrgId.value = studentUser.organizationId
                        }
                    }
                }
                onComplete(true)
            } else {
                _loginError.value = "Email atau password salah"
                onComplete(false)
            }
        }
    }

    fun loginWithBiometrics(onComplete: (Boolean) -> Unit) {
        // Mock Biometric validation using predefined first student
        viewModelScope.launch {
            _loginError.value = null
            // Auto login as Andi (preseeded student)
            val user = repository.getUserById(5) // Andi's user ID is 5 in seed callback
            if (user != null) {
                _currentUser.value = user
                _selectedOrgId.value = user.organizationId
                onComplete(true)
            } else {
                _loginError.value = "Biometric gagal menemukan profile cocok"
                onComplete(false)
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _selectedOrgId.value = null
    }

    fun selectOrg(orgId: Int) {
        _selectedOrgId.value = orgId
    }

    fun setMemberSearchQuery(query: String) {
        _memberSearchQuery.value = query
    }

    fun setMemberFilterClass(cl: String) {
        _memberFilterClass.value = cl
    }

    fun setMemberFilterRole(role: String) {
        _memberFilterRole.value = role
    }

    fun setKasFilterType(type: String) {
        _kasFilterType.value = type
    }

    fun setLeaderboardCategory(cat: String) {
        _leaderboardCategory.value = cat
    }

    // --- ORGANIZATIONS CRUD ---
    fun createOrganization(name: String, desc: String, coach: String, lat: Double, lng: Double, logo: String) {
        viewModelScope.launch {
            val org = OrganizationEntity(
                name = name,
                description = desc,
                coach = coach,
                latitude = lat,
                longitude = lng,
                logoUrl = logo.ifBlank { "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?auto=format&fit=crop&q=80&w=120" }
            )
            repository.insertOrganization(org)
        }
    }

    fun updateOrganization(org: OrganizationEntity) {
        viewModelScope.launch {
            repository.updateOrganization(org)
        }
    }

    fun deleteOrganization(org: OrganizationEntity) {
        viewModelScope.launch {
            repository.deleteOrganization(org)
        }
    }

    // Assign coach user as Admin of Organizaton
    fun assignOrganizationAdmin(orgId: Int, coachName: String, email: String) {
        viewModelScope.launch {
            // Check if user already exists
            var user = repository.getUserByEmail(email)
            if (user == null) {
                // Register User for this OrgAdmin
                val newUser = UserEntity(
                    name = coachName,
                    email = email,
                    role = "OrgAdmin",
                    organizationId = orgId
                )
                val userId = repository.insertUser(newUser).toInt()
                repository.insertOrganizationAdmin(OrganizationAdminEntity(organizationId = orgId, userId = userId))
            } else {
                // Update existing user organization mapping
                val updatedUser = user.copy(role = "OrgAdmin", organizationId = orgId)
                repository.updateUser(updatedUser)
                repository.insertOrganizationAdmin(OrganizationAdminEntity(organizationId = orgId, userId = user.id))
            }
        }
    }

    fun revokeOrganizationAdmin(orgId: Int, userId: Int) {
        viewModelScope.launch {
            repository.deleteOrganizationAdmin(orgId, userId)
            val user = repository.getUserById(userId)
            if (user != null) {
                // Demote back to standard Member or reset role
                val demoted = user.copy(role = "Member")
                repository.updateUser(demoted)
            }
        }
    }

    // --- MEMBER CRUD ---
    fun addMember(name: String, email: String, phone: String, classGroup: String, role: String, photo: String) {
        viewModelScope.launch {
            val orgId = _selectedOrgId.value ?: return@launch
            val member = MemberEntity(
                organizationId = orgId,
                name = name,
                email = email,
                phone = phone,
                classGroup = classGroup,
                roleInOrg = role,
                joinDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                photoUrl = photo.ifBlank { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=150" }
            )
            val memberId = repository.insertMember(member).toInt()
            
            // Also register as a system Member User so they can log in
            val existingUser = repository.getUserByEmail(email)
            if (existingUser == null) {
                repository.insertUser(
                    UserEntity(
                        name = name,
                        email = email,
                        phone = phone,
                        role = "Member",
                        classGroup = classGroup,
                        passwordHash = "123456",
                        organizationId = orgId,
                        photoUrl = member.photoUrl
                    )
                )
            }
        }
    }

    fun updateMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.updateMember(member)
        }
    }

    fun deleteMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.deleteMember(member)
            // Demote corresponding User representation of the member
            val user = repository.getUserByEmail(member.email)
            if (user != null) {
                repository.deleteUser(user)
            }
        }
    }

    fun importMembersCsv(csvText: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val orgId = _selectedOrgId.value ?: return@launch
            val count = repository.importMembersFromCsvText(orgId, csvText)
            onComplete(count)
        }
    }

    // --- ATTENDANCE SYSTEM ---
    fun recordManualAttendance(memberId: Int, date: String, status: String, verifiedBy: String = "Manual") {
        viewModelScope.launch {
            val orgId = _selectedOrgId.value ?: return@launch
            val record = AttendanceEntity(
                organizationId = orgId,
                memberId = memberId,
                date = date,
                status = status,
                timestamp = System.currentTimeMillis(),
                verifiedBy = verifiedBy
            )
            repository.insertAttendance(record)

            // Grant active participants bonus points
            if (status == "Hadir") {
                val member = repository.getMemberById(memberId)
                if (member != null) {
                    repository.updateMember(member.copy(points = member.points + 10))
                }
            }
        }
    }

    fun scanQrAttendance(memberEmail: String, scannedLat: Double, scannedLng: Double, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val orgId = _selectedOrgId.value
            if (orgId == null) {
                onResult(false, "Sesi Eskul tidak aktif")
                return@launch
            }
            val org = repository.getOrganizationById(orgId)
            val allM = repository.allMembersFlow.first()
            val member = allM.find { it.organizationId == orgId && it.email.equals(memberEmail, ignoreCase = true) }
            
            if (org == null || member == null) {
                onResult(false, "Siswa tidak terdaftar di eskul ini")
                return@launch
            }

            // Radius Validation: 100 meters
            val distance = repository.calculateDistanceInMeters(
                org.latitude, org.longitude, scannedLat, scannedLng
            )
            if (distance > 100.0) {
                val fmt = DecimalFormat("#.#")
                onResult(false, "Validasi GPS Gagal: Anda berada ${fmt.format(distance)} meter dari lokasi eskul (Maksimal 100m)")
                return@launch
            }

            // Record attendance for today
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val status = "Hadir"
            val record = AttendanceEntity(
                organizationId = orgId,
                memberId = member.id,
                date = todayStr,
                status = status,
                timestamp = System.currentTimeMillis(),
                latitude = scannedLat,
                longitude = scannedLng,
                verifiedBy = "QR Code (Radius ${DecimalFormat("#.##").format(distance)}m)"
            )
            repository.insertAttendance(record)
            
            // Increment member score for attending via QR Code (bonus award points)
            repository.updateMember(member.copy(points = member.points + 15))

            onResult(true, "Absensi QR Berhasil! Jarak: ${DecimalFormat("#.#").format(distance)}m. Poin +15!")
        }
    }


    // --- KAS TREASURY TRANSACTION ---
    fun addKasTransaction(amount: Double, type: String, desc: String) {
        viewModelScope.launch {
            val orgId = _selectedOrgId.value ?: return@launch
            val recordedBy = _currentUser.value?.name ?: "Unknown"
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val tx = KasTransactionEntity(
                organizationId = orgId,
                amount = amount,
                type = type,
                description = desc,
                date = dateStr,
                timestamp = System.currentTimeMillis(),
                recordedBy = recordedBy
            )
            repository.insertKasTransaction(tx)
        }
    }

    // --- ANNOUNCEMENT ---
    fun publishAnnouncement(title: String, content: String, imgUrl: String) {
        viewModelScope.launch {
            val orgId = _selectedOrgId.value ?: return@launch
            val author = _currentUser.value?.name ?: "Leader"
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val announcement = AnnouncementEntity(
                organizationId = orgId,
                title = title,
                content = content,
                imageUrl = imgUrl,
                date = dateStr,
                timestamp = System.currentTimeMillis(),
                authorName = author
            )
            repository.insertAnnouncement(announcement)
        }
    }

    fun submitAnnouncementComment(annId: Int, commentText: String) {
        viewModelScope.launch {
            val author = _currentUser.value?.name ?: "Member"
            val comment = AnnouncementCommentEntity(
                announcementId = annId,
                authorName = author,
                commentText = commentText,
                timestamp = System.currentTimeMillis()
            )
            repository.insertAnnouncementComment(comment)
        }
    }

    // --- REALTIME GROUP CHAT ---
    fun sendChatMessage(text: String, img: String? = null) {
        viewModelScope.launch {
            val orgId = _selectedOrgId.value ?: return@launch
            val user = _currentUser.value ?: return@launch
            val chat = ChatMessageEntity(
                organizationId = orgId,
                senderId = user.id,
                senderName = user.name,
                senderRole = user.role,
                messageText = text,
                imageUrl = img,
                timestamp = System.currentTimeMillis()
            )
            repository.insertChatMessage(chat)
        }
    }

    fun addChatReaction(chatId: Int, reaction: String) {
        viewModelScope.launch {
            // Find message and append reaction
            val orgId = _selectedOrgId.value ?: return@launch
            val msgs = repository.getChatMessagesByOrgIdFlow(orgId).firstOrNull() ?: emptyList()
            val msg = msgs.find { it.id == chatId }
            if (msg != null) {
                val current = msg.reactions
                val updated = if (current.isBlank()) reaction else {
                    if (current.contains(reaction)) current else "$current,$reaction"
                }
                repository.updateChatMessage(msg.copy(reactions = updated))
            }
        }
    }

    fun deleteChatMessage(msg: ChatMessageEntity) {
        viewModelScope.launch {
            repository.deleteChatMessage(msg)
        }
    }

    // --- PHOTO GALLERY ---
    fun uploadGalleryPhoto(url: String, caption: String) {
        viewModelScope.launch {
            val orgId = _selectedOrgId.value ?: return@launch
            val user = _currentUser.value?.name ?: "Member"
            val photo = GalleryPhotoEntity(
                organizationId = orgId,
                imageUrl = url.ifBlank { "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?auto=format&fit=crop&q=80&w=400" },
                caption = caption,
                uploadedBy = user,
                uploadedAt = System.currentTimeMillis()
            )
            repository.insertGalleryPhoto(photo)
        }
    }

    fun deleteGalleryPhoto(photo: GalleryPhotoEntity) {
        viewModelScope.launch {
            repository.deleteGalleryPhoto(photo)
        }
    }

    // --- EVENTS & RSVPS ---
    fun createEvent(title: String, desc: String, date: String, location: String, poster: String) {
        viewModelScope.launch {
            val orgId = _selectedOrgId.value ?: return@launch
            val event = EventEntity(
                organizationId = orgId,
                title = title,
                description = desc,
                date = date,
                location = location,
                imageUrl = poster.ifBlank { "https://images.unsplash.com/photo-1478131143081-80f7f84ca84d?auto=format&fit=crop&q=80&w=400" },
                status = "Upcoming"
            )
            repository.insertEvent(event)
        }
    }

    fun responseEventRsvp(eventId: Int, status: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val rsvp = EventRsvpEntity(
                eventId = eventId,
                userId = user.id,
                memberName = user.name,
                status = status,
                timestamp = System.currentTimeMillis()
            )
            repository.insertEventRsvp(rsvp)
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    // --- INTERVIEWS ---
    fun createInterview(memberName: String, rating: Float, comment: String, date: String) {
        viewModelScope.launch {
            val orgId = _selectedOrgId.value ?: return@launch
            val recruiter = _currentUser.value?.name ?: "Interviewer"
            val interview = InterviewEntity(
                organizationId = orgId,
                memberName = memberName,
                comment = comment,
                rating = rating,
                date = date,
                recruiterName = recruiter
            )
            repository.insertInterview(interview)
        }
    }

    fun deleteInterview(interview: InterviewEntity) {
        viewModelScope.launch {
            repository.deleteInterview(interview)
        }
    }

    // --- GAMIFICATION / ACHIEVEMENTS ---
    fun awardBadgeToMember(memberId: Int, achievementId: Int) {
        viewModelScope.launch {
            repository.awardAchievement(memberId, achievementId)
            
            // Add bonus points mapping to total points
            val member = repository.getMemberById(memberId)
            val achievement = repository.allAchievementsFlow.firstOrNull()?.find { it.id == achievementId }
            if (member != null && achievement != null) {
                repository.updateMember(member.copy(points = member.points + achievement.points))
            }
        }
    }

    fun getCommentsForAnnouncementFlow(annId: Int): kotlinx.coroutines.flow.Flow<List<AnnouncementCommentEntity>> {
        return repository.getCommentsForAnnouncementFlow(annId)
    }

    fun getRsvpsForEventFlow(eventId: Int): kotlinx.coroutines.flow.Flow<List<EventRsvpEntity>> {
        return repository.getRsvpsForEventFlow(eventId)
    }
}
