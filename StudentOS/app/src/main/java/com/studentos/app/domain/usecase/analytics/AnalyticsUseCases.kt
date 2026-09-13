package com.studentos.app.domain.usecase.analytics

import com.studentos.app.domain.model.*
import com.studentos.app.domain.repository.*
import java.time.LocalDate
import javax.inject.Inject

class GetAnalyticsUseCase @Inject constructor(
    private val assignmentRepo: AssignmentRepository,
    private val gradeRepo: GradeRepository,
    private val attendanceRepo: AttendanceRepository,
    private val subjectRepo: SubjectRepository,
    private val examRepo: ExamRepository
) {
    suspend operator fun invoke(): List<SubjectAnalytics> {
        val subjects = subjectRepo.getAllOnce()
        return subjects.map { subject ->
            GetSubjectAnalyticsUseCase(
                assignmentRepo, gradeRepo, attendanceRepo, examRepo
            )(subject.id)
        }
    }
}

class GetSubjectAnalyticsUseCase @Inject constructor(
    private val assignmentRepo: AssignmentRepository,
    private val gradeRepo: GradeRepository,
    private val attendanceRepo: AttendanceRepository,
    private val examRepo: ExamRepository
) {
    suspend operator fun invoke(subjectId: Long): SubjectAnalytics {
        val totalClasses = attendanceRepo.getTotalClasses(subjectId)
        val attended = attendanceRepo.getAttendedCount(subjectId)
        val attendancePercentage = if (totalClasses > 0) (attended.toDouble() / totalClasses) * 100.0 else 100.0

        val average = gradeRepo.getAverageForSubject(subjectId) ?: 0.0
        val weightedAverage = gradeRepo.getWeightedAverageForSubject(subjectId) ?: 0.0

        val allAssignments = assignmentRepo.getAllOnce().filter { it.subjectId == subjectId }
        val completed = allAssignments.count { it.status == AssignmentStatus.DONE }
        val total = allAssignments.size
        val overdue = allAssignments.count {
            it.subjectId == subjectId &&
            it.status != AssignmentStatus.DONE &&
            it.deadline != null &&
            it.deadline.isBefore(LocalDate.now())
        }

        val upcomingExams = examRepo.getUpcomingOnce().count { it.subjectId == subjectId }

        return SubjectAnalytics(
            subjectId = subjectId,
            subjectName = "", // Will be filled by caller
            attendancePercentage = attendancePercentage,
            averageGrade = average,
            weightedAverage = weightedAverage,
            completedAssignments = completed,
            totalAssignments = total,
            overdueAssignments = overdue,
            upcomingExams = upcomingExams
        )
    }
}
