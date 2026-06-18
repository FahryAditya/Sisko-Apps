package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SiskoDao {

    // --- USERS & AUTH ---
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'OrgAdmin'")
    fun getOrgAdmins(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    // --- ORGANIZATION ADMINS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganizationAdmin(admin: OrganizationAdminEntity): Long

    @Query("SELECT * FROM organization_admins")
    fun getAllOrganizationAdminsFlow(): Flow<List<OrganizationAdminEntity>>

    @Query("DELETE FROM organization_admins WHERE organizationId = :orgId AND userId = :userId")
    suspend fun deleteOrganizationAdmin(orgId: Int, userId: Int)


    // --- ORGANIZATIONS ---
    @Query("SELECT * FROM organizations")
    fun getAllOrganizationsFlow(): Flow<List<OrganizationEntity>>

    @Query("SELECT * FROM organizations WHERE id = :id LIMIT 1")
    suspend fun getOrganizationById(id: Int): OrganizationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganization(org: OrganizationEntity): Long

    @Update
    suspend fun updateOrganization(org: OrganizationEntity)

    @Delete
    suspend fun deleteOrganization(org: OrganizationEntity)


    // --- MEMBERS ---
    @Query("SELECT * FROM members WHERE organizationId = :orgId")
    fun getMembersByOrgIdFlow(orgId: Int): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members")
    fun getAllMembersFlow(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    suspend fun getMemberById(id: Int): MemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity): Long

    @Update
    suspend fun updateMember(member: MemberEntity)

    @Delete
    suspend fun deleteMember(member: MemberEntity)


    // --- ATTENDANCE ---
    @Query("SELECT * FROM attendance WHERE organizationId = :orgId")
    fun getAttendanceByOrgIdFlow(orgId: Int): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance")
    fun getAllAttendanceFlow(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE memberId = :memberId")
    fun getAttendanceByMemberIdFlow(memberId: Int): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Delete
    suspend fun deleteAttendance(attendance: AttendanceEntity)


    // --- INTERVIEWS ---
    @Query("SELECT * FROM interviews WHERE organizationId = :orgId")
    fun getInterviewsByOrgIdFlow(orgId: Int): Flow<List<InterviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterview(interview: InterviewEntity): Long

    @Update
    suspend fun updateInterview(interview: InterviewEntity)

    @Delete
    suspend fun deleteInterview(interview: InterviewEntity)


    // --- KAS / TREASURY ---
    @Query("SELECT * FROM kas_transactions WHERE organizationId = :orgId ORDER BY timestamp DESC")
    fun getKasTransactionsByOrgIdFlow(orgId: Int): Flow<List<KasTransactionEntity>>

    @Query("SELECT * FROM kas_transactions")
    fun getAllKasTransactionsFlow(): Flow<List<KasTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKasTransaction(kas: KasTransactionEntity): Long

    @Update
    suspend fun updateKasTransaction(kas: KasTransactionEntity)

    @Delete
    suspend fun deleteKasTransaction(kas: KasTransactionEntity)


    // --- ANNOUNCEMENTS & COMMENTS ---
    @Query("SELECT * FROM announcements WHERE organizationId = :orgId ORDER BY timestamp DESC")
    fun getAnnouncementsByOrgIdFlow(orgId: Int): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long

    @Update
    suspend fun updateAnnouncement(announcement: AnnouncementEntity)

    @Delete
    suspend fun deleteAnnouncement(announcement: AnnouncementEntity)

    @Query("SELECT * FROM announcement_comments WHERE announcementId = :annId ORDER BY timestamp ASC")
    fun getCommentsForAnnouncementFlow(annId: Int): Flow<List<AnnouncementCommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncementComment(comment: AnnouncementCommentEntity): Long


    // --- GROUP CHATS ---
    @Query("SELECT * FROM chat_messages WHERE organizationId = :orgId ORDER BY timestamp ASC")
    fun getChatMessagesByOrgIdFlow(orgId: Int): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity): Long

    @Update
    suspend fun updateChatMessage(message: ChatMessageEntity)

    @Delete
    suspend fun deleteChatMessage(message: ChatMessageEntity)


    // --- PHOTO GALLERY ---
    @Query("SELECT * FROM gallery_photos WHERE organizationId = :orgId ORDER BY uploadedAt DESC")
    fun getGalleryPhotosByOrgIdFlow(orgId: Int): Flow<List<GalleryPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGalleryPhoto(photo: GalleryPhotoEntity): Long

    @Delete
    suspend fun deleteGalleryPhoto(photo: GalleryPhotoEntity)


    // --- EVENTS & RSVPS ---
    @Query("SELECT * FROM events WHERE organizationId = :orgId ORDER BY date ASC")
    fun getEventsByOrgIdFlow(orgId: Int): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("SELECT * FROM event_rsvps WHERE eventId = :eventId")
    fun getRsvpsForEventFlow(eventId: Int): Flow<List<EventRsvpEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventRsvp(rsvp: EventRsvpEntity): Long


    // --- ACHIEVEMENTS & LEADERBOARDS ---
    @Query("SELECT * FROM achievements")
    fun getAllAchievementsFlow(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM member_achievements WHERE memberId = :memberId")
    fun getAchievementsByMemberIdFlow(memberId: Int): Flow<List<MemberAchievementEntity>>

    @Query("SELECT * FROM member_achievements")
    fun getAllMemberAchievementsFlow(): Flow<List<MemberAchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun awardAchievement(memberAchievement: MemberAchievementEntity): Long
}
