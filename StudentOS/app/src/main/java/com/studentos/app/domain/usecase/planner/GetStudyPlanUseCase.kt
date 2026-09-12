package com.studentos.app.domain.usecase.planner

import com.studentos.app.domain.model.*
import com.studentos.app.domain.repository.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetStudyPlanUseCase @Inject constructor(
    private val assignmentRepo: AssignmentRepository,
    private val examRepo: ExamRepository,
    private val subjectRepo: SubjectRepository,
    private val attendanceRepo: AttendanceRepository,
    private val gradeRepo: GradeRepository
) {
    suspend operator fun invoke(): StudyPlan {
        val recommendations = mutableListOf<StudyRecommendation>()
        val today = LocalDate.now()

        // 1. Overdue tasks — highest priority
        val overdueTasks = assignmentRepo.getOverdueOnce()
        for (task in overdueTasks.sortedBy { it.deadline }) {
            val subject = subjectRepo.getById(task.subjectId)
            recommendations.add(
                StudyRecommendation(
                    title = task.title,
                    reason = "Overdue since ${task.deadline}",
                    estimatedMinutes = task.estimatedDuration.coerceAtLeast(30),
                    priority = Priority.URGENT,
                    assignmentId = task.id,
                    subjectId = task.subjectId
                )
            )
        }

        // 2. Tasks due today
        val todayTasks = assignmentRepo.getForDateOnce(today)
        for (task in todayTasks) {
            if (task.status != AssignmentStatus.DONE) {
                recommendations.add(
                    StudyRecommendation(
                        title = task.title,
                        reason = "Due today",
                        estimatedMinutes = task.estimatedDuration.coerceAtLeast(30),
                        priority = Priority.HIGH,
                        assignmentId = task.id,
                        subjectId = task.subjectId
                    )
                )
            }
        }

        // 3. Tasks due tomorrow
        val tomorrow = today.plusDays(1)
        val tomorrowTasks = assignmentRepo.getForDateOnce(tomorrow)
        for (task in tomorrowTasks) {
            if (task.status != AssignmentStatus.DONE) {
                recommendations.add(
                    StudyRecommendation(
                        title = task.title,
                        reason = "Due tomorrow · ~${task.estimatedDuration.coerceAtLeast(30)} min",
                        estimatedMinutes = task.estimatedDuration.coerceAtLeast(30),
                        priority = Priority.HIGH,
                        assignmentId = task.id,
                        subjectId = task.subjectId
                    )
                )
            }
        }

        // 4. Upcoming exams (within 14 days)
        val upcomingExams = examRepo.getUpcomingOnce()
        for (exam in upcomingExams) {
            val daysUntil = ChronoUnit.DAYS.between(today, exam.date)
            if (daysUntil in 0..14) {
                val subject = subjectRepo.getById(exam.subjectId)
                val progress = exam.preparationProgress
                val urgency = when {
                    daysUntil <= 3 -> Priority.URGENT
                    daysUntil <= 7 -> Priority.HIGH
                    else -> Priority.MEDIUM
                }
                recommendations.add(
                    StudyRecommendation(
                        title = "Prepare for ${subject?.name ?: "Exam"}",
                        reason = "${daysUntil}d left · ${progress}% prepared",
                        estimatedMinutes = ((100 - progress) * 3).coerceIn(30, 360),
                        priority = urgency,
                        examId = exam.id,
                        subjectId = exam.subjectId
                    )
                )
            }
        }

        // 5. Tasks due this week (after tomorrow)
        val endOfWeek = today.plusDays(5)
        val weekTasks = assignmentRepo.getInRangeOnce(tomorrow.plusDays(1), endOfWeek)
        for (task in weekTasks.take(3)) {
            if (task.status != AssignmentStatus.DONE) {
                val daysUntil = ChronoUnit.DAYS.between(today, task.deadline!!)
                recommendations.add(
                    StudyRecommendation(
                        title = task.title,
                        reason = "Due in ${daysUntil} days · ~${task.estimatedDuration.coerceAtLeast(30)} min",
                        estimatedMinutes = task.estimatedDuration.coerceAtLeast(30),
                        priority = task.priority,
                        assignmentId = task.id,
                        subjectId = task.subjectId
                    )
                )
            }
        }

        // 6. Subjects with attendance below threshold
        val subjects = subjectRepo.getAllOnce()
        for (subject in subjects) {
            val totalClasses = attendanceRepo.getTotalClasses(subject.id)
            if (totalClasses < 3) continue
            val attended = attendanceRepo.getAttendedCount(subject.id)
            val percentage = (attended.toDouble() / totalClasses) * 100.0
            if (percentage < 80.0) {
                recommendations.add(
                    StudyRecommendation(
                        title = "Attend ${subject.name}",
                        reason = "Attendance at ${percentage.toInt()}% — risk zone",
                        estimatedMinutes = 0,
                        priority = if (percentage < 70.0) Priority.URGENT else Priority.MEDIUM,
                        subjectId = subject.id
                    )
                )
            }
        }

        // Sort by priority, then by urgency
        val sorted = recommendations.sortedWith(
            compareByDescending<StudyRecommendation> { it.priority.ordinal }
                .thenBy { it.estimatedMinutes }
        )

        return StudyPlan(recommendations = sorted.distinctBy { it.title }.take(10))
    }
}
