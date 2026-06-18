package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SiskoRepository(private val dao: SiskoDao) {

    // --- USERS & AUTH ---
    val allUsersFlow: Flow<List<UserEntity>> = dao.getAllUsersFlow()
    val orgAdminsFlow: Flow<List<UserEntity>> = dao.getOrgAdmins()
    val allOrganizationAdminsFlow: Flow<List<OrganizationAdminEntity>> = dao.getAllOrganizationAdminsFlow()

    suspend fun getUserByEmail(email: String): UserEntity? {
        return dao.getUserByEmail(email)
    }

    suspend fun getUserById(id: Int): UserEntity? {
        return dao.getUserById(id)
    }

    suspend fun insertUser(user: UserEntity): Long {
        return dao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) {
        dao.updateUser(user)
    }

    suspend fun deleteUser(user: UserEntity) {
        dao.deleteUser(user)
    }


    // --- ORGANIZATIONS ---
    val allOrganizationsFlow: Flow<List<OrganizationEntity>> = dao.getAllOrganizationsFlow()

    suspend fun getOrganizationById(id: Int): OrganizationEntity? {
        return dao.getOrganizationById(id)
    }

    suspend fun insertOrganization(org: OrganizationEntity): Long {
        return dao.insertOrganization(org)
    }

    suspend fun updateOrganization(org: OrganizationEntity) {
        dao.updateOrganization(org)
    }

    suspend fun deleteOrganization(org: OrganizationEntity) {
        dao.deleteOrganization(org)
    }

    suspend fun insertOrganizationAdmin(orgAdmin: OrganizationAdminEntity): Long {
        return dao.insertOrganizationAdmin(orgAdmin)
    }

    suspend fun deleteOrganizationAdmin(orgId: Int, userId: Int) {
        dao.deleteOrganizationAdmin(orgId, userId)
    }


    // --- MEMBERS ---
    val allMembersFlow: Flow<List<MemberEntity>> = dao.getAllMembersFlow()

    fun getMembersByOrgIdFlow(orgId: Int): Flow<List<MemberEntity>> {
        return dao.getMembersByOrgIdFlow(orgId)
    }

    suspend fun getMemberById(id: Int): MemberEntity? {
        return dao.getMemberById(id)
    }

    suspend fun insertMember(member: MemberEntity): Long {
        return dao.insertMember(member)
    }

    suspend fun updateMember(member: MemberEntity) {
        dao.updateMember(member)
    }

    suspend fun deleteMember(member: MemberEntity) {
        dao.deleteMember(member)
    }


    // --- ATTENDANCE ---
    val allAttendanceFlow: Flow<List<AttendanceEntity>> = dao.getAllAttendanceFlow()

    fun getAttendanceByOrgIdFlow(orgId: Int): Flow<List<AttendanceEntity>> {
        return dao.getAttendanceByOrgIdFlow(orgId)
    }

    fun getAttendanceByMemberIdFlow(memberId: Int): Flow<List<AttendanceEntity>> {
        return dao.getAttendanceByMemberIdFlow(memberId)
    }

    suspend fun insertAttendance(attendance: AttendanceEntity): Long {
        return dao.insertAttendance(attendance)
    }

    suspend fun updateAttendance(attendance: AttendanceEntity) {
        dao.updateAttendance(attendance)
    }

    suspend fun deleteAttendance(attendance: AttendanceEntity) {
        dao.deleteAttendance(attendance)
    }


    // --- INTERVIEWS ---
    fun getInterviewsByOrgIdFlow(orgId: Int): Flow<List<InterviewEntity>> {
        return dao.getInterviewsByOrgIdFlow(orgId)
    }

    suspend fun insertInterview(interview: InterviewEntity): Long {
        return dao.insertInterview(interview)
    }

    suspend fun updateInterview(interview: InterviewEntity) {
        dao.updateInterview(interview)
    }

    suspend fun deleteInterview(interview: InterviewEntity) {
        dao.deleteInterview(interview)
    }


    // --- KAS / TREASURY ---
    val allKasTransactionsFlow: Flow<List<KasTransactionEntity>> = dao.getAllKasTransactionsFlow()

    fun getKasTransactionsByOrgIdFlow(orgId: Int): Flow<List<KasTransactionEntity>> {
        return dao.getKasTransactionsByOrgIdFlow(orgId)
    }

    suspend fun insertKasTransaction(kas: KasTransactionEntity): Long {
        return dao.insertKasTransaction(kas)
    }

    suspend fun updateKasTransaction(kas: KasTransactionEntity) {
        dao.updateKasTransaction(kas)
    }

    suspend fun deleteKasTransaction(kas: KasTransactionEntity) {
        dao.deleteKasTransaction(kas)
    }


    // --- ANNOUNCEMENTS ---
    fun getAnnouncementsByOrgIdFlow(orgId: Int): Flow<List<AnnouncementEntity>> {
        return dao.getAnnouncementsByOrgIdFlow(orgId)
    }

    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long {
        return dao.insertAnnouncement(announcement)
    }

    suspend fun updateAnnouncement(announcement: AnnouncementEntity) {
        dao.updateAnnouncement(announcement)
    }

    suspend fun deleteAnnouncement(announcement: AnnouncementEntity) {
        dao.deleteAnnouncement(announcement)
    }

    fun getCommentsForAnnouncementFlow(annId: Int): Flow<List<AnnouncementCommentEntity>> {
        return dao.getCommentsForAnnouncementFlow(annId)
    }

    suspend fun insertAnnouncementComment(comment: AnnouncementCommentEntity): Long {
        return dao.insertAnnouncementComment(comment)
    }


    // --- CHAT MESSAGES ---
    fun getChatMessagesByOrgIdFlow(orgId: Int): Flow<List<ChatMessageEntity>> {
        return dao.getChatMessagesByOrgIdFlow(orgId)
    }

    suspend fun insertChatMessage(message: ChatMessageEntity): Long {
        return dao.insertChatMessage(message)
    }

    suspend fun updateChatMessage(message: ChatMessageEntity) {
        dao.updateChatMessage(message)
    }

    suspend fun deleteChatMessage(message: ChatMessageEntity) {
        dao.deleteChatMessage(message)
    }


    // --- GALLERY PHOTOS ---
    fun getGalleryPhotosByOrgIdFlow(orgId: Int): Flow<List<GalleryPhotoEntity>> {
        return dao.getGalleryPhotosByOrgIdFlow(orgId)
    }

    suspend fun insertGalleryPhoto(photo: GalleryPhotoEntity): Long {
        return dao.insertGalleryPhoto(photo)
    }

    suspend fun deleteGalleryPhoto(photo: GalleryPhotoEntity) {
        dao.deleteGalleryPhoto(photo)
    }


    // --- EVENTS & RSVPS ---
    fun getEventsByOrgIdFlow(orgId: Int): Flow<List<EventEntity>> {
        return dao.getEventsByOrgIdFlow(orgId)
    }

    suspend fun insertEvent(event: EventEntity): Long {
        return dao.insertEvent(event)
    }

    suspend fun updateEvent(event: EventEntity) {
        dao.updateEvent(event)
    }

    suspend fun deleteEvent(event: EventEntity) {
        dao.deleteEvent(event)
    }

    fun getRsvpsForEventFlow(eventId: Int): Flow<List<EventRsvpEntity>> {
        return dao.getRsvpsForEventFlow(eventId)
    }

    suspend fun insertEventRsvp(rsvp: EventRsvpEntity): Long {
        return dao.insertEventRsvp(rsvp)
    }


    // --- ACHIEVEMENTS & LEADERBOARDS ---
    val allAchievementsFlow: Flow<List<AchievementEntity>> = dao.getAllAchievementsFlow()
    val allMemberAchievementsFlow: Flow<List<MemberAchievementEntity>> = dao.getAllMemberAchievementsFlow()

    fun getAchievementsByMemberIdFlow(memberId: Int): Flow<List<MemberAchievementEntity>> {
        return dao.getAchievementsByMemberIdFlow(memberId)
    }

    suspend fun insertAchievement(achievement: AchievementEntity): Long {
        return dao.insertAchievement(achievement)
    }

    suspend fun awardAchievement(memberId: Int, achievementId: Int): Long {
        return dao.awardAchievement(MemberAchievementEntity(memberId = memberId, achievementId = achievementId, awardedAt = System.currentTimeMillis()))
    }

    // --- MOCK SERVICE LOGICS ---
    // Mock CSV Import for students
    suspend fun importMembersFromCsvText(orgId: Int, csvText: String): Int {
        var count = 0
        try {
            val lines = csvText.lines()
            for (line in lines) {
                if (line.isBlank() || line.startsWith("Nama") || line.startsWith("name")) continue
                val parts = line.split(",")
                if (parts.size >= 4) {
                    val name = parts[0].trim()
                    val email = parts[1].trim()
                    val phone = parts[2].trim()
                    val classGroup = parts[3].trim()
                    val roleInOrg = if (parts.size >= 5) parts[4].trim() else "Anggota"
                    
                    val member = MemberEntity(
                        organizationId = orgId,
                        name = name,
                        email = email,
                        phone = phone,
                        classGroup = classGroup,
                        roleInOrg = roleInOrg,
                        joinDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                        points = 100
                    )
                    dao.insertMember(member)
                    count++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return count
    }

    // Calculates distance between coordinate points to validate 100-meter radius
    fun calculateDistanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371e3 // Earth radius in meters
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) *
                Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return r * c // returns distance in meters
    }
}
