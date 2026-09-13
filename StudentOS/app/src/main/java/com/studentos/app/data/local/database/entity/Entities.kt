package com.studentos.app.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

// ── Student ────────────────────────────────────────────────────────────────

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val university: String = "",
    val faculty: String = "",
    val course: Int = 1,
    @ColumnInfo(name = "group_name") val group: String = "",
    val semester: Int = 1,
    @ColumnInfo(name = "created_at") val createdAt: Instant = Instant.now()
)

// ── Subject ────────────────────────────────────────────────────────────────

@Entity(
    tableName = "subjects",
    foreignKeys = [
        ForeignKey(
            entity = TeacherEntity::class,
            parentColumns = ["id"],
            childColumns = ["teacher_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("teacher_id"),
        Index("semester")
    ]
)
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "short_name") val shortName: String = "",
    @ColumnInfo(name = "teacher_id") val teacherId: Long? = null,
    val color: Int = 0xFF6750A4.toInt(),
    val type: String = "REQUIRED", // SubjectType
    val semester: Int = 1,
    val notes: String = ""
)

// ── Teacher ────────────────────────────────────────────────────────────────

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String = "",
    val phone: String = "",
    val department: String = "",
    val notes: String = ""
)

// ── ScheduleItem ───────────────────────────────────────────────────────────

@Entity(
    tableName = "schedule_items",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeacherEntity::class,
            parentColumns = ["id"],
            childColumns = ["teacher_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("subject_id"),
        Index("teacher_id"),
        Index("day_of_week"),
        Index("day_of_week", "start_time")
    ]
)
data class ScheduleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "subject_id") val subjectId: Long,
    @ColumnInfo(name = "teacher_id") val teacherId: Long? = null,
    val type: String = "LECTURE", // ClassType
    val room: String = "",
    val building: String = "",
    @ColumnInfo(name = "start_time") val startTime: LocalTime,
    @ColumnInfo(name = "end_time") val endTime: LocalTime,
    @ColumnInfo(name = "day_of_week") val dayOfWeek: Int, // 1=Monday..7=Sunday
    val recurrence: String = "WEEKLY", // RecurrenceType
    @ColumnInfo(name = "semester_start") val semesterStart: LocalDate,
    @ColumnInfo(name = "semester_end") val semesterEnd: LocalDate
)

// ── Assignment ─────────────────────────────────────────────────────────────

@Entity(
    tableName = "assignments",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("subject_id"),
        Index("deadline"),
        Index("status"),
        Index("priority"),
        Index("deadline", "status")
    ]
)
data class AssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "subject_id") val subjectId: Long,
    val title: String,
    val description: String = "",
    val deadline: LocalDate? = null,
    val priority: String = "MEDIUM", // Priority
    @ColumnInfo(name = "estimated_duration") val estimatedDuration: Int = 0,
    val status: String = "TODO", // AssignmentStatus
    @ColumnInfo(name = "created_at") val createdAt: Instant = Instant.now(),
    @ColumnInfo(name = "completed_at") val completedAt: Instant? = null
)

// ── Subtask ────────────────────────────────────────────────────────────────

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = AssignmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["assignment_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("assignment_id")]
)
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "assignment_id") val assignmentId: Long,
    val title: String,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false
)

// ── Grade ──────────────────────────────────────────────────────────────────

@Entity(
    tableName = "grades",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("subject_id"),
        Index("date"),
        Index("subject_id", "type")
    ]
)
data class GradeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "subject_id") val subjectId: Long,
    val type: String = "OTHER", // GradeCategory
    val value: Double,
    @ColumnInfo(name = "max_value") val maxValue: Double = 100.0,
    val date: LocalDate = LocalDate.now(),
    val weight: Double = 1.0,
    val notes: String = ""
)

// ── AttendanceRecord ───────────────────────────────────────────────────────

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["schedule_item_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("schedule_item_id"),
        Index("subject_id"),
        Index("date"),
        Index("subject_id", "date"),
        Index("schedule_item_id", "date", unique = true) // one record per class per day
    ]
)
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "schedule_item_id") val scheduleItemId: Long,
    @ColumnInfo(name = "subject_id") val subjectId: Long,
    val date: LocalDate,
    val status: String = "PRESENT", // AttendanceStatus
    val reason: String = "",
    val notes: String = ""
)

// ── Exam ───────────────────────────────────────────────────────────────────

@Entity(
    tableName = "exams",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeacherEntity::class,
            parentColumns = ["id"],
            childColumns = ["teacher_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("subject_id"),
        Index("teacher_id"),
        Index("date")
    ]
)
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "subject_id") val subjectId: Long,
    val type: String = "EXAM", // ExamType
    val date: LocalDate,
    val time: LocalTime? = null,
    val room: String = "",
    @ColumnInfo(name = "teacher_id") val teacherId: Long? = null,
    val notes: String = "",
    @ColumnInfo(name = "preparation_progress") val preparationProgress: Int = 0
)

// ── Note ───────────────────────────────────────────────────────────────────

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("subject_id"),
        Index("created_at"),
        Index("updated_at")
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "subject_id") val subjectId: Long? = null,
    val title: String,
    val content: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Instant = Instant.now(),
    @ColumnInfo(name = "updated_at") val updatedAt: Instant = Instant.now()
)

// ── Attachment ─────────────────────────────────────────────────────────────

@Entity(
    tableName = "attachments",
    indices = [
        Index("related_entity_type", "related_entity_id")
    ]
)
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "related_entity_type") val relatedEntityType: String,
    @ColumnInfo(name = "related_entity_id") val relatedEntityId: Long,
    @ColumnInfo(name = "local_uri") val localUri: String,
    @ColumnInfo(name = "file_name") val fileName: String,
    @ColumnInfo(name = "mime_type") val mimeType: String = "",
    val size: Long = 0
)
