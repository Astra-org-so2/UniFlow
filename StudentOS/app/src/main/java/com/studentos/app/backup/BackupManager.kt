package com.studentos.app.backup

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.studentos.app.data.local.database.*
import com.studentos.app.data.local.database.dao.*
import com.studentos.app.data.local.database.entity.*
import com.studentos.app.domain.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class BackupFile(
    val version: Int = 1,
    val createdAt: String = Instant.now().toString(),
    val deviceInfo: String = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
    val data: BackupContent
)

data class BackupContent(
    val students: List<StudentEntity> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList(),
    val teachers: List<TeacherEntity> = emptyList(),
    val scheduleItems: List<ScheduleItemEntity> = emptyList(),
    val assignments: List<AssignmentEntity> = emptyList(),
    val subtasks: List<SubtaskEntity> = emptyList(),
    val grades: List<GradeEntity> = emptyList(),
    val attendanceRecords: List<AttendanceRecordEntity> = emptyList(),
    val exams: List<ExamEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val attachments: List<AttachmentEntity> = emptyList()
)

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val studentDao: StudentDao,
    private val subjectDao: SubjectDao,
    private val teacherDao: TeacherDao,
    private val scheduleDao: ScheduleDao,
    private val assignmentDao: AssignmentDao,
    private val gradeDao: GradeDao,
    private val attendanceDao: AttendanceDao,
    private val examDao: ExamDao,
    private val noteDao: NoteDao,
    private val attachmentDao: AttachmentDao
) {
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

    suspend fun createBackup(): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val content = BackupContent(
                students = studentDao.getCurrentStudent()?.let { listOf(it) } ?: emptyList(),
                subjects = subjectDao.getAllOnce(),
                teachers = teacherDao.getAllOnce(),
                scheduleItems = scheduleDao.getAllOnce(),
                assignments = assignmentDao.getAllOnce(),
                grades = gradeDao.getAllOnce(),
                attendanceRecords = attendanceDao.getAllOnce(),
                exams = examDao.getAllOnce(),
                notes = noteDao.getAllOnce(),
                attachments = attachmentDao.getAll()
            )

            val backup = BackupFile(
                version = 1,
                data = content
            )

            val fileName = "StudentOS_backup_${LocalDate.now()}.json"
            val file = File(context.cacheDir, fileName)
            file.writeText(gson.toJson(backup))

            // Copy to external storage or return content URI
            val backupDir = File(context.getExternalFilesDir(null), "backups")
            backupDir.mkdirs()
            val destFile = File(backupDir, fileName)
            file.copyTo(destFile, overwrite = true)
            file.delete()

            Result.success(Uri.fromFile(destFile))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFromUri(uri: Uri): Result<BackupFile> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(IOException("Cannot open file"))

            val json = inputStream.bufferedReader().use { it.readText() }
            val backup = gson.fromJson(json, BackupFile::class.java)
                ?: return@withContext Result.failure(IOException("Invalid backup format"))

            if (backup.version > 1) {
                return@withContext Result.failure(IOException("Unsupported backup version: ${backup.version}"))
            }

            // Preview — just return the backup, caller shows confirmation
            Result.success(backup)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun applyRestore(backup: BackupFile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Clear existing data
            noteDao.deleteAll()
            attachmentDao.deleteAll()
            examDao.deleteAll()
            attendanceDao.deleteAll()
            gradeDao.deleteAll()
            assignmentDao.deleteAll()
            scheduleDao.deleteAll()
            subjectDao.deleteAll()
            teacherDao.deleteAll()
            studentDao.deleteAll()

            // Insert backup data
            val data = backup.data
            data.students.forEach { studentDao.insert(it) }
            data.teachers.forEach { teacherDao.insert(it) }
            data.subjects.forEach { subjectDao.insert(it) }
            data.scheduleItems.forEach { scheduleDao.insert(it) }
            data.assignments.forEach { assignmentDao.insert(it) }
            data.grades.forEach { gradeDao.insert(it) }
            data.attendanceRecords.forEach { attendanceDao.insert(it) }
            data.exams.forEach { examDao.insert(it) }
            data.notes.forEach { noteDao.insert(it) }
            data.attachments.forEach { attachmentDao.insert(it) }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getRecordCounts(backup: BackupFile): Map<String, Int> {
        val d = backup.data
        return mapOf(
            "Students" to d.students.size,
            "Subjects" to d.subjects.size,
            "Teachers" to d.teachers.size,
            "Schedule" to d.scheduleItems.size,
            "Assignments" to d.assignments.size,
            "Grades" to d.grades.size,
            "Attendance" to d.attendanceRecords.size,
            "Exams" to d.exams.size,
            "Notes" to d.notes.size
        )
    }
}
