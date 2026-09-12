package com.studentos.app.domain.model

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

// ── Enums ──────────────────────────────────────────────────────────────────

enum class ClassType {
    LECTURE, PRACTICE, LAB, SEMINAR, OTHER
}

enum class RecurrenceType {
    WEEKLY, BIWEEKLY, NONE
}

enum class AssignmentStatus {
    TODO, IN_PROGRESS, DONE, OVERDUE
}

enum class Priority {
    LOW, MEDIUM, HIGH, URGENT
}

enum class GradeCategory {
    HOMEWORK, TEST, LAB, EXAM, PROJECT, OTHER
}

enum class AttendanceStatus {
    PRESENT, ABSENT, EXCUSED, LATE
}

enum class ExamType {
    EXAM, CREDIT, DIFFERENTIATED_CREDIT, COURSEWORK_DEFENSE
}

enum class SubjectType {
    REQUIRED, ELECTIVE, OPTIONAL
}

// ── Domain Models ──────────────────────────────────────────────────────────

data class Student(
    val id: Long = 0,
    val name: String,
    val university: String = "",
    val faculty: String = "",
    val course: Int = 1,
    val group: String = "",
    val semester: Int = 1,
    val createdAt: Instant = Instant.now()
)

data class Subject(
    val id: Long = 0,
    val name: String,
    val shortName: String = "",
    val teacherId: Long? = null,
    val color: Int = 0xFF6750A4.toInt(),
    val type: SubjectType = SubjectType.REQUIRED,
    val semester: Int = 1,
    val notes: String = ""
)

data class Teacher(
    val id: Long = 0,
    val name: String,
    val email: String = "",
    val phone: String = "",
    val department: String = "",
    val notes: String = ""
)

data class ScheduleItem(
    val id: Long = 0,
    val subjectId: Long,
    val teacherId: Long? = null,
    val type: ClassType = ClassType.LECTURE,
    val room: String = "",
    val building: String = "",
    val startTime: LocalTime,
    val endTime: LocalTime,
    val dayOfWeek: DayOfWeek,
    val recurrence: RecurrenceType = RecurrenceType.WEEKLY,
    val semesterStart: LocalDate,
    val semesterEnd: LocalDate
)

data class Assignment(
    val id: Long = 0,
    val subjectId: Long,
    val title: String,
    val description: String = "",
    val deadline: LocalDate? = null,
    val priority: Priority = Priority.MEDIUM,
    val estimatedDuration: Int = 0, // minutes
    val status: AssignmentStatus = AssignmentStatus.TODO,
    val createdAt: Instant = Instant.now(),
    val completedAt: Instant? = null,
    val subtasks: List<Subtask> = emptyList()
)

data class Subtask(
    val id: Long = 0,
    val assignmentId: Long,
    val title: String,
    val isCompleted: Boolean = false
)

data class Grade(
    val id: Long = 0,
    val subjectId: Long,
    val type: GradeCategory = GradeCategory.OTHER,
    val value: Double,
    val maxValue: Double = 100.0,
    val date: LocalDate = LocalDate.now(),
    val weight: Double = 1.0,
    val notes: String = ""
) {
    val percentage: Double get() = if (maxValue > 0) (value / maxValue) * 100.0 else 0.0
}

data class AttendanceRecord(
    val id: Long = 0,
    val scheduleItemId: Long,
    val subjectId: Long,
    val date: LocalDate,
    val status: AttendanceStatus = AttendanceStatus.PRESENT,
    val reason: String = "",
    val notes: String = ""
)

data class Exam(
    val id: Long = 0,
    val subjectId: Long,
    val type: ExamType = ExamType.EXAM,
    val date: LocalDate,
    val time: LocalTime? = null,
    val room: String = "",
    val teacherId: Long? = null,
    val notes: String = "",
    val preparationProgress: Int = 0 // 0-100
) {
    val daysUntil: Long get() = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), date)
}

data class Note(
    val id: Long = 0,
    val subjectId: Long? = null,
    val title: String,
    val content: String = "",
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)

data class Attachment(
    val id: Long = 0,
    val relatedEntityType: String, // "assignment", "note", "exam", "subject"
    val relatedEntityId: Long,
    val localUri: String,
    val fileName: String,
    val mimeType: String = "",
    val size: Long = 0
)

// ── Analytics Models ───────────────────────────────────────────────────────

data class SubjectAnalytics(
    val subjectId: Long,
    val subjectName: String,
    val attendancePercentage: Double,
    val averageGrade: Double,
    val weightedAverage: Double,
    val completedAssignments: Int,
    val totalAssignments: Int,
    val overdueAssignments: Int,
    val upcomingExams: Int
)

data class AttendanceSummary(
    val subjectId: Long,
    val subjectName: String,
    val totalClasses: Int,
    val presentCount: Int,
    val absentCount: Int,
    val excusedCount: Int,
    val lateCount: Int,
    val attendancePercentage: Double,
    val safeAbsencesRemaining: Int
)

data class GradeSummary(
    val subjectId: Long,
    val subjectName: String,
    val grades: List<Grade>,
    val average: Double,
    val weightedAverage: Double,
    val currentGrade: String,
    val requiredExamGrade: Double? // to achieve target
)

data class StudyPlan(
    val recommendations: List<StudyRecommendation>,
    val generatedAt: Instant = Instant.now()
)

data class StudyRecommendation(
    val title: String,
    val reason: String,
    val estimatedMinutes: Int,
    val priority: Priority,
    val assignmentId: Long? = null,
    val examId: Long? = null,
    val subjectId: Long? = null
)

// ── Dashboard Models ───────────────────────────────────────────────────────

data class TodayDashboard(
    val greeting: String,
    val nextClass: ScheduleItem?,
    val nextClassSubject: Subject?,
    val nextClassTeacher: Teacher?,
    val todaySchedule: List<TodayScheduleItem>,
    val upcomingTasks: List<Assignment>,
    val upcomingExams: List<Exam>,
    val attendanceWarnings: List<AttendanceWarning>,
    val recommendation: StudyRecommendation?
)

data class TodayScheduleItem(
    val scheduleItem: ScheduleItem,
    val subject: Subject,
    val teacher: Teacher?,
    val isCompleted: Boolean = false,
    val isNow: Boolean = false,
    val attendanceRecord: AttendanceRecord? = null
)

data class AttendanceWarning(
    val subjectId: Long,
    val subjectName: String,
    val percentage: Double,
    val safeAbsencesRemaining: Int
)

// ── Backup Models ──────────────────────────────────────────────────────────

data class BackupMetadata(
    val version: Int = 1,
    val createdAt: Instant = Instant.now(),
    val studentName: String = "",
    val deviceInfo: String = "",
    val recordCounts: Map<String, Int> = emptyMap()
)

data class BackupData(
    val metadata: BackupMetadata,
    val students: List<Student>,
    val subjects: List<Subject>,
    val teachers: List<Teacher>,
    val scheduleItems: List<ScheduleItem>,
    val assignments: List<Assignment>,
    val grades: List<Grade>,
    val attendanceRecords: List<AttendanceRecord>,
    val exams: List<Exam>,
    val notes: List<Note>
)
