package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val phone: String = "",
    val role: String, // "Administrator", "OrgAdmin", "Member", "Parent"
    val classGroup: String = "", // "11 MIPA 1", etc.
    val passwordHash: String = "123456", // simplified for seed/demo authentication
    val organizationId: Int? = null, // for Member/OrgAdmin role
    val parentOfStudentId: Int? = null, // for Parent role, links to a Member student's User ID
    val photoUrl: String = ""
)

@Entity(tableName = "organizations")
data class OrganizationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val coach: String,
    val latitude: Double,
    val longitude: Double,
    val logoUrl: String,
    val status: String = "Active" // "Active", "Inactive"
)

@Entity(tableName = "organization_admins")
data class OrganizationAdminEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val organizationId: Int,
    val userId: Int
)

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val organizationId: Int,
    val name: String,
    val email: String,
    val phone: String = "",
    val classGroup: String = "",
    val roleInOrg: String = "Anggota", // "Ketua", "Wakil", "Sekretaris", "Anggota"
    val joinDate: String,
    val photoUrl: String = "",
    val points: Int = 100,
    val verified: Boolean = true
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val organizationId: Int,
    val memberId: Int,
    val date: String, // YYYY-MM-DD
    val status: String, // "Hadir", "Izin", "Sakit", "Alfa"
    val timestamp: Long,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val verifiedBy: String = "Manual" // "Manual", "QR Code"
)

@Entity(tableName = "interviews")
data class InterviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val organizationId: Int,
    val memberName: String,
    val comment: String,
    val rating: Float, // 1.0 to 5.0
    val date: String,
    val recruiterName: String
)

@Entity(tableName = "kas_transactions")
data class KasTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val organizationId: Int,
    val amount: Double,
    val description: String,
    val type: String, // "INCOME" or "EXPENSE"
    val date: String, // YYYY-MM-DD
    val timestamp: Long,
    val recordedBy: String
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val organizationId: Int,
    val title: String,
    val content: String,
    val imageUrl: String = "",
    val date: String,
    val timestamp: Long,
    val authorName: String,
    val likesCount: Int = 0,
    val commentCount: Int = 0
)

@Entity(tableName = "announcement_comments")
data class AnnouncementCommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val announcementId: Int,
    val authorName: String,
    val commentText: String,
    val timestamp: Long
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val organizationId: Int,
    val senderId: Int,
    val senderName: String,
    val senderRole: String, // "Admin", "Member", etc.
    val messageText: String,
    val imageUrl: String? = null,
    val timestamp: Long,
    val reactions: String = "" // comma-separated reactions like "👍,❤️"
)

@Entity(tableName = "gallery_photos")
data class GalleryPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val organizationId: Int,
    val imageUrl: String,
    val caption: String,
    val uploadedBy: String,
    val uploadedAt: Long
)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val organizationId: Int,
    val title: String,
    val description: String,
    val date: String, // YYYY-MM-DD HH:mm
    val location: String,
    val imageUrl: String = "",
    val status: String = "Upcoming" // "Upcoming", "Past"
)

@Entity(tableName = "event_rsvps")
data class EventRsvpEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventId: Int,
    val userId: Int,
    val memberName: String,
    val status: String, // "Going", "Deciding", "Not Going"
    val timestamp: Long
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val points: Int,
    val badgeType: String // "GOLD_MEDAL", "SILVER_MEDAL", "BRONZE_MEDAL", "STAR", "SHIELD", "CROWN", "TROPHY"
)

@Entity(tableName = "member_achievements")
data class MemberAchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memberId: Int,
    val achievementId: Int,
    val awardedAt: Long
)
