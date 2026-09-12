package com.studentos.app.domain.repository

import com.studentos.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

// ── Student Repository ─────────────────────────────────────────────────────

interface StudentRepository {
    fun getCurrentStudent(): Flow<Student?>
    suspend fun getCurrentStudentOnce(): Student?
    suspend fun save(student: Student): Long
    suspend fun update(student: Student)
    suspend fun delete(student: Student)
}

// ── Subject Repository ─────────────────────────────────────────────────────

interface SubjectRepository {
    fun getAll(): Flow<List<Subject>>
    suspend fun getAllOnce(): List<Subject>
    fun getBySemester(semester: Int): Flow<List<Subject>>
    suspend fun getById(id: Long): Subject?
    fun getByIdFlow(id: Long): Flow<Subject?>
    fun getByTeacher(teacherId: Long): Flow<List<Subject>>
    fun search(query: String): Flow<List<Subject>>
    suspend fun save(subject: Subject): Long
    suspend fun update(subject: Subject)
    suspend fun delete(subject: Subject)
    suspend fun deleteById(id: Long)
}

// ── Teacher Repository ─────────────────────────────────────────────────────

interface TeacherRepository {
    fun getAll(): Flow<List<Teacher>>
    suspend fun getAllOnce(): List<Teacher>
    suspend fun getById(id: Long): Teacher?
    fun getByIdFlow(id: Long): Flow<Teacher?>
    fun search(query: String): Flow<List<Teacher>>
    suspend fun save(teacher: Teacher): Long
    suspend fun update(teacher: Teacher)
    suspend fun delete(teacher: Teacher)
    suspend fun deleteById(id: Long)
}

// ── Schedule Repository ────────────────────────────────────────────────────

interface ScheduleRepository {
    fun getAll(): Flow<List<ScheduleItem>>
    suspend fun getAllOnce(): List<ScheduleItem>
    suspend fun getById(id: Long): ScheduleItem?
    fun getByDay(dayOfWeek: Int): Flow<List<ScheduleItem>>
    fun getBySubject(subjectId: Long): Flow<List<ScheduleItem>>
    fun getByTeacher(teacherId: Long): Flow<List<ScheduleItem>>
    fun getForDate(date: LocalDate): Flow<List<ScheduleItem>>
    suspend fun getForDateOnce(date: LocalDate): List<ScheduleItem>
    fun getForCurrentWeek(): Flow<List<ScheduleItem>>
    suspend fun save(item: ScheduleItem): Long
    suspend fun update(item: ScheduleItem)
    suspend fun delete(item: ScheduleItem)
    suspend fun deleteById(id: Long)
    suspend fun duplicate(id: Long): Long?
}

// ── Assignment Repository ──────────────────────────────────────────────────

interface AssignmentRepository {
    fun getAll(): Flow<List<Assignment>>
    suspend fun getAllOnce(): List<Assignment>
    suspend fun getById(id: Long): Assignment?
    fun getByIdFlow(id: Long): Flow<Assignment?>
    fun getBySubject(subjectId: Long): Flow<List<Assignment>>
    fun getByStatus(status: AssignmentStatus): Flow<List<Assignment>>
    fun getActive(): Flow<List<Assignment>>
    suspend fun getActiveOnce(): List<Assignment>
    fun getForDate(date: LocalDate): Flow<List<Assignment>>
    fun getOverdue(): Flow<List<Assignment>>
    suspend fun getOverdueOnce(): List<Assignment>
    fun getInRange(start: LocalDate, end: LocalDate): Flow<List<Assignment>>
    suspend fun getOverdueCount(): Int
    suspend fun save(assignment: Assignment): Long
    suspend fun update(assignment: Assignment)
    suspend fun delete(assignment: Assignment)
    suspend fun deleteById(id: Long)
    suspend fun markComplete(id: Long)
    suspend fun getSubtasks(assignmentId: Long): List<Subtask>
    fun getSubtasksFlow(assignmentId: Long): Flow<List<Subtask>>
    suspend fun saveSubtask(subtask: Subtask): Long
    suspend fun updateSubtask(subtask: Subtask)
    suspend fun deleteSubtask(subtask: Subtask)
}

// ── Grade Repository ───────────────────────────────────────────────────────

interface GradeRepository {
    fun getAll(): Flow<List<Grade>>
    suspend fun getAllOnce(): List<Grade>
    suspend fun getById(id: Long): Grade?
    fun getBySubject(subjectId: Long): Flow<List<Grade>>
    suspend fun getBySubjectOnce(subjectId: Long): List<Grade>
    fun getInRange(start: LocalDate, end: LocalDate): Flow<List<Grade>>
    suspend fun getAverageForSubject(subjectId: Long): Double?
    suspend fun getWeightedAverageForSubject(subjectId: Long): Double?
    suspend fun save(grade: Grade): Long
    suspend fun update(grade: Grade)
    suspend fun delete(grade: Grade)
    suspend fun deleteById(id: Long)
}

// ── Attendance Repository ──────────────────────────────────────────────────

interface AttendanceRepository {
    fun getAll(): Flow<List<AttendanceRecord>>
    suspend fun getAllOnce(): List<AttendanceRecord>
    suspend fun getById(id: Long): AttendanceRecord?
    fun getBySubject(subjectId: Long): Flow<List<AttendanceRecord>>
    suspend fun getBySubjectOnce(subjectId: Long): List<AttendanceRecord>
    fun getByDate(date: LocalDate): Flow<List<AttendanceRecord>>
    suspend fun getByScheduleAndDate(scheduleItemId: Long, date: LocalDate): AttendanceRecord?
    suspend fun getTotalClasses(subjectId: Long): Int
    suspend fun getPresentCount(subjectId: Long): Int
    suspend fun getAbsentCount(subjectId: Long): Int
    suspend fun getExcusedCount(subjectId: Long): Int
    suspend fun getLateCount(subjectId: Long): Int
    suspend fun getAttendedCount(subjectId: Long): Int
    suspend fun save(record: AttendanceRecord): Long
    suspend fun update(record: AttendanceRecord)
    suspend fun delete(record: AttendanceRecord)
    suspend fun deleteById(id: Long)
}

// ── Exam Repository ────────────────────────────────────────────────────────

interface ExamRepository {
    fun getAll(): Flow<List<Exam>>
    suspend fun getAllOnce(): List<Exam>
    suspend fun getById(id: Long): Exam?
    fun getBySubject(subjectId: Long): Flow<List<Exam>>
    fun getUpcoming(): Flow<List<Exam>>
    suspend fun getUpcomingOnce(): List<Exam>
    fun getInRange(start: LocalDate, end: LocalDate): Flow<List<Exam>>
    suspend fun save(exam: Exam): Long
    suspend fun update(exam: Exam)
    suspend fun delete(exam: Exam)
    suspend fun deleteById(id: Long)
}

// ── Note Repository ────────────────────────────────────────────────────────

interface NoteRepository {
    fun getAll(): Flow<List<Note>>
    suspend fun getAllOnce(): List<Note>
    suspend fun getById(id: Long): Note?
    fun getByIdFlow(id: Long): Flow<Note?>
    fun getBySubject(subjectId: Long): Flow<List<Note>>
    fun search(query: String): Flow<List<Note>>
    suspend fun save(note: Note): Long
    suspend fun update(note: Note)
    suspend fun delete(note: Note)
    suspend fun deleteById(id: Long)
}

// ── Attachment Repository ──────────────────────────────────────────────────

interface AttachmentRepository {
    fun getByEntity(type: String, entityId: Long): Flow<List<Attachment>>
    suspend fun getByEntityOnce(type: String, entityId: Long): List<Attachment>
    suspend fun getById(id: Long): Attachment?
    suspend fun save(attachment: Attachment): Long
    suspend fun delete(attachment: Attachment)
    suspend fun deleteByEntity(type: String, entityId: Long)
}
