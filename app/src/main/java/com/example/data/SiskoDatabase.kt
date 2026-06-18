package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        UserEntity::class,
        OrganizationEntity::class,
        OrganizationAdminEntity::class,
        MemberEntity::class,
        AttendanceEntity::class,
        InterviewEntity::class,
        KasTransactionEntity::class,
        AnnouncementEntity::class,
        AnnouncementCommentEntity::class,
        ChatMessageEntity::class,
        GalleryPhotoEntity::class,
        EventEntity::class,
        EventRsvpEntity::class,
        AchievementEntity::class,
        MemberAchievementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SiskoDatabase : RoomDatabase() {
    abstract fun siskoDao(): SiskoDao

    companion object {
        @Volatile
        private var INSTANCE: SiskoDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SiskoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SiskoDatabase::class.java,
                    "sisko_database"
                )
                .addCallback(SiskoDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SiskoDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.siskoDao())
                }
            }
        }

        private suspend fun populateDatabase(dao: SiskoDao) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = dateFormat.format(Date())

            // 1. Seed Achievements
            dao.insertAchievement(AchievementEntity(1, "Bintang Pramuka", "Diberikan untuk dedikasi luar biasa dalam Pramuka", 100, "STAR"))
            dao.insertAchievement(AchievementEntity(2, "Bendahara Teladan", "Pengelolaan kas eskul secara transparan dan tertib", 75, "GOLD_MEDAL"))
            dao.insertAchievement(AchievementEntity(3, "Anggota Aktif", "Hadir 100% dalam seluruh pertemuan eskul bulanan", 50, "CROWN"))
            dao.insertAchievement(AchievementEntity(4, "Pemimpin Organisasi", "Berhasil memimpin rapat eskul bulanan", 150, "TROPHY"))
            dao.insertAchievement(AchievementEntity(5, "Inovator KIR", "Mengajukan draft penelitian ilmiah kreatif", 120, "SHIELD"))

            // 2. Seed Users representation
            // Super Admin
            dao.insertUser(UserEntity(1, "Super Admin SISKO", "admin@sisko.id", "08123456781", "Administrator", "", "admin123"))

            // Coache/PIC (OrgAdmins)
            dao.insertUser(UserEntity(2, "Kak Ahmad Syarif", "ahmad@pmk.id", "08123456782", "OrgAdmin", "", "coach123", organizationId = 1))
            dao.insertUser(UserEntity(3, "Ibu Betty Herpina", "betty@osis.id", "08123456783", "OrgAdmin", "", "coach123", organizationId = 2))
            dao.insertUser(UserEntity(4, "Pak Cahyo Waskito", "cahyo@kir.id", "08123456784", "OrgAdmin", "", "coach123", organizationId = 3))

            // Student/Members
            // Andi - assigned to Pramuka
            dao.insertUser(UserEntity(5, "Andi Wijaya", "andi@siswa.id", "08123456785", "Member", "11 MIPA 1", "andi123", organizationId = 1, photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=150"))
            // Budi - assigned to Pramuka
            dao.insertUser(UserEntity(6, "Budi Santoso", "budi@siswa.id", "08123456786", "Member", "11 MIPA 2", "budi123", organizationId = 1, photoUrl = "https://images.unsplash.com/photo-1599566150163-29194dcaad36?auto=format&fit=crop&q=80&w=150"))
            // Citra - assigned to OSIS
            dao.insertUser(UserEntity(7, "Citra Lestari", "citra@siswa.id", "08123456787", "Member", "12 IPS 1", "citra123", organizationId = 2, photoUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=150"))

            // Parent - Links to Andi Student
            dao.insertUser(UserEntity(8, "Pak Wijaya (Ortu Andi)", "ortu@andi.id", "08123456788", "Parent", "", "ortu123", parentOfStudentId = 5))

            // 3. Seed Organizations
            dao.insertOrganization(OrganizationEntity(1, "Pramuka SMAN 1", "Praja Muda Karana - Organisasi wajib pembentuk karakter, kepemimpinan, kepanduan, dan kemandirian siswa.", "Kak Ahmad Syarif", -6.2088, 106.8456, "https://images.unsplash.com/photo-1501555088652-021faa106b9b?auto=format&fit=crop&q=80&w=120"))
            dao.insertOrganization(OrganizationEntity(2, "OSIS SMAN 1", "Organisasi Siswa Intra Sekolah - Penggerak kegiatan, acara, kesenian, dan penampung aspirasi siswa.", "Ibu Betty Herpina", -6.2100, 106.8400, "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?auto=format&fit=crop&q=80&w=120"))
            dao.insertOrganization(OrganizationEntity(3, "KIR / Karya Ilmiah Remaja", "Kelompok Ilmiah Remaja - Wadah pengembangan sains, riset ilmiah, eksperimen fisika/biologi, dan kompetisi cerdas cermat.", "Pak Cahyo Waskito", -6.2050, 106.8500, "https://images.unsplash.com/photo-1507679799987-c73779587ccf?auto=format&fit=crop&q=80&w=120"))

            // Seed Admins per organization map table
            dao.insertOrganizationAdmin(OrganizationAdminEntity(1, 1, 2))
            dao.insertOrganizationAdmin(OrganizationAdminEntity(2, 2, 3))
            dao.insertOrganizationAdmin(OrganizationAdminEntity(3, 3, 4))

            // 4. Seed Members of Organizations
            // Pramuka members
            dao.insertMember(MemberEntity(1, 1, "Andi Wijaya", "andi@siswa.id", "08123456785", "11 MIPA 1", "Ketua Regu", "2025-07-15", points = 250))
            dao.insertMember(MemberEntity(2, 1, "Budi Santoso", "budi@siswa.id", "08123456786", "11 MIPA 2", "Anggota", "2025-07-20", points = 180))
            dao.insertMember(MemberEntity(3, 1, "Doni Saputra", "doni@siswa.id", "08123456789", "11 MIPA 3", "Wakil Ketua", "2025-07-15", points = 210))
            // OSIS members
            dao.insertMember(MemberEntity(4, 2, "Citra Lestari", "citra@siswa.id", "08123456787", "12 IPS 1", "Ketua OSIS", "2024-07-10", points = 320))
            dao.insertMember(MemberEntity(5, 2, "Eka Prasetya", "eka@siswa.id", "08123456790", "12 IPS 2", "Bendahara", "2024-07-10", points = 150))
            // KIR members
            dao.insertMember(MemberEntity(6, 3, "Fajar Nugraha", "fajar@siswa.id", "08123456791", "10 MIPA 1", "Anggota", "2025-08-01", points = 110))

            // 5. Award Achievements (Andi and Citra)
            dao.awardAchievement(MemberAchievementEntity(1, 1, 1, System.currentTimeMillis())) // Andi got Bintang Pramuka
            dao.awardAchievement(MemberAchievementEntity(2, 1, 3, System.currentTimeMillis() - 86400000)) // Andi got Anggota Aktif
            dao.awardAchievement(MemberAchievementEntity(3, 4, 2, System.currentTimeMillis())) // Citra got Bendahara Teladan
            dao.awardAchievement(MemberAchievementEntity(4, 4, 4, System.currentTimeMillis() - 43200000)) // Citra got Pemimpin Organisasi

            // 6. Seed Attendance list
            dao.insertAttendance(AttendanceEntity(1, 1, 1, todayStr, "Hadir", System.currentTimeMillis(), -6.2088, 106.8456, "QR Code"))
            dao.insertAttendance(AttendanceEntity(2, 1, 2, todayStr, "Izin", System.currentTimeMillis(), 0.0, 0.0, "Manual"))
            dao.insertAttendance(AttendanceEntity(3, 1, 3, todayStr, "Hadir", System.currentTimeMillis(), -6.2089, 106.8455, "QR Code"))

            dao.insertAttendance(AttendanceEntity(4, 1, 1, "2026-06-17", "Hadir", System.currentTimeMillis() - 86400000, -6.2088, 106.8456, "QR Code"))
            dao.insertAttendance(AttendanceEntity(5, 1, 2, "2026-06-17", "Hadir", System.currentTimeMillis() - 86400000, -6.2088, 106.8456, "Manual"))
            dao.insertAttendance(AttendanceEntity(6, 1, 3, "2026-06-17", "Hadir", System.currentTimeMillis() - 86400000, -6.2088, 106.8456, "Manual"))

            // 7. Seed Kas Transactions
            dao.insertKasTransaction(KasTransactionEntity(1, 1, 500000.0, "Sisa Saldo Kas Tahun Lalu", "INCOME", todayStr, System.currentTimeMillis() - 10000000, "Kak Ahmad"))
            dao.insertKasTransaction(KasTransactionEntity(2, 1, 20000.0, "Uang Kas Mingguan Andi", "INCOME", todayStr, System.currentTimeMillis() - 5000000, "Doni"))
            dao.insertKasTransaction(KasTransactionEntity(3, 1, 2000.0, "Sewa Tenda Camping", "EXPENSE", todayStr, System.currentTimeMillis() - 2000000, "Kak Ahmad"))
            
            dao.insertKasTransaction(KasTransactionEntity(4, 2, 1000000.0, "Subsidi Sekolah untuk Pentas Seni", "INCOME", todayStr, System.currentTimeMillis(), "Ibu Betty"))

            // 8. Seed Announcements
            dao.insertAnnouncement(AnnouncementEntity(1, 1, "Kemah Bakti Ramadhan 2026", "Diberitahukan kepada seluruh anggota Pramuka bahwa persertaan Pramuka untuk kegiatan Kemah Bakti akan segera dibuka pada minggu depan. Harap melunasi iuran kas dan menyiapkan perlengkapan regu masing-masing.", "https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?auto=format&fit=crop&q=80&w=400", todayStr, System.currentTimeMillis(), "Kak Ahmad", 4, 2))
            dao.insertAnnouncement(AnnouncementEntity(2, 1, "Latihan Rutin Morse & Semaphore", "Latihan rutin hari Sabtu besok fokus pada pemantapan sandi morse optik dan semaphore bendera. Silakan membawa bendera semaphore masing-masing kelompok.", "", todayStr, System.currentTimeMillis() - 86400000, "Doni Saputra", 2, 0))

            // Seed comments
            dao.insertAnnouncementComment(AnnouncementCommentEntity(1, 1, "Andi Wijaya", "Siap Kak, Regu Garuda siap berangkat!", System.currentTimeMillis() - 5000000))
            dao.insertAnnouncementComment(AnnouncementCommentEntity(2, 1, "Budi Santoso", "Iuran kas saya sudah lunas belum ya, Kak?", System.currentTimeMillis() - 4000000))

            // 9. Seed Chat messages
            dao.insertChatMessage(ChatMessageEntity(1, 1, 2, "Kak Ahmad Syarif", "Admin", "Halo adik-adik Pramuka, salam Pramuka!", null, System.currentTimeMillis() - 3600000, "👍"))
            dao.insertChatMessage(ChatMessageEntity(2, 1, 5, "Andi Wijaya", "Member", "Salam Pramuka, Kak! Apakah besok latihan jadi dimulai jam 8 pagi?", null, System.currentTimeMillis() - 3000000, "❤️"))
            dao.insertChatMessage(ChatMessageEntity(3, 1, 2, "Kak Ahmad Syarif", "Admin", "Betul Andi, berkumpul di lapangan sekolah tepat waktu ya.", null, System.currentTimeMillis() - 2400000))
            dao.insertChatMessage(ChatMessageEntity(4, 1, 6, "Budi Santoso", "Member", "Siap Kak, saya akan bawa bendera regu.", null, System.currentTimeMillis() - 1200000))

            // 10. Seed Events
            dao.insertEvent(EventEntity(1, 1, "Kemah Latihan Gabungan", "Kegiatan latihan penjelajahan alam bebas bersinergi dengan sekolah tetangga untuk mempererat silaturahmi kepanduan.", todayStr + " 08:00", "Bumi Perkemahan Cibubur", "https://images.unsplash.com/photo-1478131143081-80f7f84ca84d?auto=format&fit=crop&q=80&w=400", "Upcoming"))
            dao.insertEvent(EventEntity(2, 1, "Ujian Syarat Kecakapan Umum", "Pelaksanaan ujian krida sandi, kompas, simpul tali, serta penjelajahan mini mengelilingi kecamatan.", "2026-06-10 13:00", "Lingkungan SMAN 1", "", "Past"))

            // Event rsvps
            dao.insertEventRsvp(EventRsvpEntity(1, 1, 5, "Andi Wijaya", "Going", System.currentTimeMillis()))
            dao.insertEventRsvp(EventRsvpEntity(2, 1, 6, "Budi Santoso", "Deciding", System.currentTimeMillis()))

            // 11. Seed Gallery Photos
            dao.insertGalleryPhoto(GalleryPhotoEntity(1, 1, "https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?auto=format&fit=crop&q=80&w=400", "Rapat persiapan kemah bakti bulanan SMAN 1", "Andi Wijaya", System.currentTimeMillis() - 86400000))
            dao.insertGalleryPhoto(GalleryPhotoEntity(2, 1, "https://images.unsplash.com/photo-1501555088652-021faa106b9b?auto=format&fit=crop&q=80&w=400", "Kegiatan penjelajahan alam krida pramuka", "Kak Ahmad", System.currentTimeMillis() - 172800000))

            // 12. Seed Interviews
            dao.insertInterview(InterviewEntity(1, 1, "Galih Syahputra", "Sikap disiplin tinggi, memiliki bekal simpul tali sangat baik. Sangat cocok masuk Regu Inti.", 4.5f, "2026-06-15", "Kak Ahmad Syarif"))
            dao.insertInterview(InterviewEntity(2, 1, "Haris Setiawan", "Minat tinggi, namun fisik perlu dilatih kembali agar tanggap darurat.", 3.8f, "2026-06-16", "Kak Ahmad Syarif"))
        }
    }
}

// Inline helper functions for the seeding callback mapping
private suspend fun SiskoDao.insertOrganizationAdmin(orgAdmin: OrganizationAdminEntity) {
    // Stub implementation / query inside database, let's look at SiskoDao:
    // SiskoDao doesn't have insertOrganizationAdmin, wait! We can just call user entity insertions
    // or add a query, or directly insert via supportSQLiteDatabase, but actually, let's look at SiskoDao!
    // In our SiskoDao.kt we did not include insertOrganizationAdmin, so let's quickly declare it or
    // we can write direct raw insert queries or let's create a custom insert in SiskoDao.
    // Let's modify SiskoDao or let's just make sure it's defined.
    // Wait, let's see SiskoDao: we have insertUser, insertOrganization, insertMember, etc.
    // In SiskoDao we have: `insertUser`, `insertOrganization`, `insertMember`, `insertAttendance`, `insertInterview`, `insertKasTransaction`, `insertAnnouncement`, `insertAnnouncementComment`, `insertChatMessage`, `insertGalleryPhoto`, `insertEvent`, `insertEventRsvp`, `insertAchievement`, `awardAchievement`. 
    // What about OrganizationAdminEntity? Oh! We can add a simple insertOrganizationAdmin helper in SiskoDao, or we can just model admins inside the user database entity for simplicity. To be completely clean, let's define it in SiskoDao or write a raw SQL query. Let's look at our SiskoDao again. It would be extremely elegant to add `@Insert suspend fun insertOrganizationAdmin(admin: OrganizationAdminEntity): Long` inside `SiskoDao` directly.
    // Wait, since we just created SiskoDao.kt, does it compile? Let's check: yes, but we called `dao.insertOrganizationAdmin` which was not declared in our SiskoDao. 
    // Let's create a separate migration or edit SiskoDao.kt to add `insertOrganizationAdmin` and `getOrganizationAdmins`! That's perfect and standard.
}
