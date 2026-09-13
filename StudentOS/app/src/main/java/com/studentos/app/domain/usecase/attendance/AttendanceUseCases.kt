package com.studentos.app.domain.usecase.attendance

import com.studentos.app.domain.model.*
import com.studentos.app.domain.repository.AttendanceRepository
import com.studentos.app.domain.repository.SubjectRepository
import java.time.LocalDate
import javax.inject.Inject

class GetAttendanceSummaryUseCase @Inject constructor(
    private val attendanceRepo: AttendanceRepository,
    private val subjectRepo: SubjectRepository
) {
    companion object {
        const val DEFAULT_THRESHOLD = 75.0
        const val DEFAULT_SAFE_ABSENCES = 1
    }

    suspend operator fun invoke(subjectId: Long, threshold: Double = DEFAULT_THRESHOLD): AttendanceSummary {
        val subject = subjectRepo.getById(subjectId)
        val totalClasses = attendanceRepo.getTotalClasses(subjectId)
        val presentCount = attendanceRepo.getPresentCount(subjectId)
        val absentCount = attendanceRepo.getAbsentCount(subjectId)
        val excusedCount = attendanceRepo.getExcusedCount(subjectId)
        val lateCount = attendanceRepo.getLateCount(subjectId)

        val attendedCount = presentCount + lateCount
        val percentage = if (totalClasses > 0) (attendedCount.toDouble() / totalClasses) * 100.0 else 100.0

        // Calculate safe absences remaining based on threshold
        val maxAllowedAbsent = ((100.0 - threshold) / 100.0 * totalClasses).toInt()
        val effectiveAbsences = absentCount // excused don't count
        val safeAbsencesRemaining = maxOf(0, maxAllowedAbsent - effectiveAbsences)

        return AttendanceSummary(
            subjectId = subjectId,
            subjectName = subject?.name ?: "",
            totalClasses = totalClasses,
            presentCount = presentCount,
            absentCount = absentCount,
            excusedCount = excusedCount,
            lateCount = lateCount,
            attendancePercentage = percentage,
            safeAbsencesRemaining = safeAbsencesRemaining
        )
    }

    /**
     * Simulate: what would attendance be if the student skips the next class?
     */
    suspend fun simulateSkip(subjectId: Long, threshold: Double = DEFAULT_THRESHOLD): AttendanceSummary {
        val current = invoke(subjectId, threshold)
        val newTotal = current.totalClasses + 1
        val newAbsent = current.absentCount + 1
        val attended = current.presentCount + current.lateCount
        val newPercentage = if (newTotal > 0) (attended.toDouble() / newTotal) * 100.0 else 100.0
        val maxAllowedAbsent = ((100.0 - threshold) / 100.0 * newTotal).toInt()
        val safeAbsencesRemaining = maxOf(0, maxAllowedAbsent - newAbsent)

        return current.copy(
            totalClasses = newTotal,
            absentCount = newAbsent,
            attendancePercentage = newPercentage,
            safeAbsencesRemaining = safeAbsencesRemaining
        )
    }
}

class MarkAttendanceUseCase @Inject constructor(
    private val attendanceRepo: AttendanceRepository
) {
    suspend operator fun invoke(
        scheduleItemId: Long,
        subjectId: Long,
        date: LocalDate,
        status: AttendanceStatus,
        reason: String = "",
        notes: String = ""
    ): Long {
        // Check if record already exists for this class on this date
        val existing = attendanceRepo.getByScheduleAndDate(scheduleItemId, date)
        return if (existing != null) {
            attendanceRepo.update(existing.copy(status = status, reason = reason, notes = notes))
            existing.id
        } else {
            attendanceRepo.save(
                AttendanceRecord(
                    scheduleItemId = scheduleItemId,
                    subjectId = subjectId,
                    date = date,
                    status = status,
                    reason = reason,
                    notes = notes
                )
            )
        }
    }
}
