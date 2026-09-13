package com.studentos.app.data.local.database

import com.studentos.app.data.local.database.entity.*
import com.studentos.app.domain.model.*
import java.time.DayOfWeek

// ── Student ────────────────────────────────────────────────────────────────

fun StudentEntity.toDomain() = Student(
    id = id,
    name = name,
    university = university,
    faculty = faculty,
    course = course,
    group = group,
    semester = semester,
    createdAt = createdAt
)

fun Student.toEntity() = StudentEntity(
    id = id,
    name = name,
    university = university,
    faculty = faculty,
    course = course,
    group = group,
    semester = semester,
    createdAt = createdAt
)

// ── Subject ────────────────────────────────────────────────────────────────

fun SubjectEntity.toDomain() = Subject(
    id = id,
    name = name,
    shortName = shortName,
    teacherId = teacherId,
    color = color,
    type = try { SubjectType.valueOf(type) } catch (_: Exception) { SubjectType.REQUIRED },
    semester = semester,
    notes = notes
)

fun Subject.toEntity() = SubjectEntity(
    id = id,
    name = name,
    shortName = shortName,
    teacherId = teacherId,
    color = color,
    type = type.name,
    semester = semester,
    notes = notes
)

// ── Teacher ────────────────────────────────────────────────────────────────

fun TeacherEntity.toDomain() = Teacher(
    id = id,
    name = name,
    email = email,
    phone = phone,
    department = department,
    notes = notes
)

fun Teacher.toEntity() = TeacherEntity(
    id = id,
    name = name,
    email = email,
    phone = phone,
    department = department,
    notes = notes
)

// ── ScheduleItem ───────────────────────────────────────────────────────────

fun ScheduleItemEntity.toDomain() = ScheduleItem(
    id = id,
    subjectId = subjectId,
    teacherId = teacherId,
    type = try { ClassType.valueOf(type) } catch (_: Exception) { ClassType.LECTURE },
    room = room,
    building = building,
    startTime = startTime,
    endTime = endTime,
    dayOfWeek = DayOfWeek.of(dayOfWeek),
    recurrence = try { RecurrenceType.valueOf(recurrence) } catch (_: Exception) { RecurrenceType.WEEKLY },
    semesterStart = semesterStart,
    semesterEnd = semesterEnd
)

fun ScheduleItem.toEntity() = ScheduleItemEntity(
    id = id,
    subjectId = subjectId,
    teacherId = teacherId,
    type = type.name,
    room = room,
    building = building,
    startTime = startTime,
    endTime = endTime,
    dayOfWeek = dayOfWeek.value,
    recurrence = recurrence.name,
    semesterStart = semesterStart,
    semesterEnd = semesterEnd
)

// ── Assignment ─────────────────────────────────────────────────────────────

fun AssignmentEntity.toDomain(subtasks: List<SubtaskEntity> = emptyList()) = Assignment(
    id = id,
    subjectId = subjectId,
    title = title,
    description = description,
    deadline = deadline,
    priority = try { Priority.valueOf(priority) } catch (_: Exception) { Priority.MEDIUM },
    estimatedDuration = estimatedDuration,
    status = try { AssignmentStatus.valueOf(status) } catch (_: Exception) { AssignmentStatus.TODO },
    createdAt = createdAt,
    completedAt = completedAt,
    subtasks = subtasks.map { it.toDomain() }
)

fun Assignment.toEntity() = AssignmentEntity(
    id = id,
    subjectId = subjectId,
    title = title,
    description = description,
    deadline = deadline,
    priority = priority.name,
    estimatedDuration = estimatedDuration,
    status = status.name,
    createdAt = createdAt,
    completedAt = completedAt
)

fun SubtaskEntity.toDomain() = Subtask(
    id = id,
    assignmentId = assignmentId,
    title = title,
    isCompleted = isCompleted
)

fun Subtask.toEntity() = SubtaskEntity(
    id = id,
    assignmentId = assignmentId,
    title = title,
    isCompleted = isCompleted
)

// ── Grade ──────────────────────────────────────────────────────────────────

fun GradeEntity.toDomain() = Grade(
    id = id,
    subjectId = subjectId,
    type = try { GradeCategory.valueOf(type) } catch (_: Exception) { GradeCategory.OTHER },
    value = value,
    maxValue = maxValue,
    date = date,
    weight = weight,
    notes = notes
)

fun Grade.toEntity() = GradeEntity(
    id = id,
    subjectId = subjectId,
    type = type.name,
    value = value,
    maxValue = maxValue,
    date = date,
    weight = weight,
    notes = notes
)

// ── AttendanceRecord ───────────────────────────────────────────────────────

fun AttendanceRecordEntity.toDomain() = AttendanceRecord(
    id = id,
    scheduleItemId = scheduleItemId,
    subjectId = subjectId,
    date = date,
    status = try { AttendanceStatus.valueOf(status) } catch (_: Exception) { AttendanceStatus.PRESENT },
    reason = reason,
    notes = notes
)

fun AttendanceRecord.toEntity() = AttendanceRecordEntity(
    id = id,
    scheduleItemId = scheduleItemId,
    subjectId = subjectId,
    date = date,
    status = status.name,
    reason = reason,
    notes = notes
)

// ── Exam ───────────────────────────────────────────────────────────────────

fun ExamEntity.toDomain() = Exam(
    id = id,
    subjectId = subjectId,
    type = try { ExamType.valueOf(type) } catch (_: Exception) { ExamType.EXAM },
    date = date,
    time = time,
    room = room,
    teacherId = teacherId,
    notes = notes,
    preparationProgress = preparationProgress
)

fun Exam.toEntity() = ExamEntity(
    id = id,
    subjectId = subjectId,
    type = type.name,
    date = date,
    time = time,
    room = room,
    teacherId = teacherId,
    notes = notes,
    preparationProgress = preparationProgress
)

// ── Note ───────────────────────────────────────────────────────────────────

fun NoteEntity.toDomain() = Note(
    id = id,
    subjectId = subjectId,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Note.toEntity() = NoteEntity(
    id = id,
    subjectId = subjectId,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// ── Attachment ─────────────────────────────────────────────────────────────

fun AttachmentEntity.toDomain() = Attachment(
    id = id,
    relatedEntityType = relatedEntityType,
    relatedEntityId = relatedEntityId,
    localUri = localUri,
    fileName = fileName,
    mimeType = mimeType,
    size = size
)

fun Attachment.toEntity() = AttachmentEntity(
    id = id,
    relatedEntityType = relatedEntityType,
    relatedEntityId = relatedEntityId,
    localUri = localUri,
    fileName = fileName,
    mimeType = mimeType,
    size = size
)
