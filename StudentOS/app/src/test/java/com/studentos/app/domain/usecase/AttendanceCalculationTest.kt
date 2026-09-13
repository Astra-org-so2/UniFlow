package com.studentos.app.domain.usecase

import com.studentos.app.domain.model.*
import com.studentos.app.domain.repository.AttendanceRepository
import com.studentos.app.domain.repository.SubjectRepository
import com.studentos.app.domain.usecase.attendance.GetAttendanceSummaryUseCase
import com.studentos.app.domain.usecase.attendance.MarkAttendanceUseCase
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AttendanceCalculationTest {

    private lateinit var attendanceRepo: AttendanceRepository
    private lateinit var subjectRepo: SubjectRepository
    private lateinit var getSummary: GetAttendanceSummaryUseCase

    @Before
    fun setup() {
        attendanceRepo = mockk()
        subjectRepo = mockk()
        getSummary = GetAttendanceSummaryUseCase(attendanceRepo, subjectRepo)
    }

    @Test
    fun `calculate attendance with all present`() = runTest {
        coEvery { subjectRepo.getById(1) } returns Subject(id = 1, name = "Physics")
        coEvery { attendanceRepo.getTotalClasses(1) } returns 10
        coEvery { attendanceRepo.getPresentCount(1) } returns 10
        coEvery { attendanceRepo.getAbsentCount(1) } returns 0
        coEvery { attendanceRepo.getExcusedCount(1) } returns 0
        coEvery { attendanceRepo.getLateCount(1) } returns 0

        val result = getSummary(1)

        assertEquals(100.0, result.attendancePercentage, 0.01)
        assertEquals(0, result.absentCount)
        assertEquals(10, result.presentCount)
    }

    @Test
    fun `calculate attendance with mixed statuses`() = runTest {
        coEvery { subjectRepo.getById(1) } returns Subject(id = 1, name = "Math")
        coEvery { attendanceRepo.getTotalClasses(1) } returns 20
        coEvery { attendanceRepo.getPresentCount(1) } returns 14
        coEvery { attendanceRepo.getAbsentCount(1) } returns 3
        coEvery { attendanceRepo.getExcusedCount(1) } returns 1
        coEvery { attendanceRepo.getLateCount(1) } returns 2

        val result = getSummary(1)

        // attended = present(14) + late(2) = 16
        // percentage = 16/20 * 100 = 80%
        assertEquals(80.0, result.attendancePercentage, 0.01)
        assertEquals(16, result.presentCount + result.lateCount)
    }

    @Test
    fun `calculate safe absences remaining`() = runTest {
        coEvery { subjectRepo.getById(1) } returns Subject(id = 1, name = "Physics")
        coEvery { attendanceRepo.getTotalClasses(1) } returns 20
        coEvery { attendanceRepo.getPresentCount(1) } returns 16
        coEvery { attendanceRepo.getAbsentCount(1) } returns 3
        coEvery { attendanceRepo.getExcusedCount(1) } returns 0
        coEvery { attendanceRepo.getLateCount(1) } returns 1

        // Threshold 75%
        // Max allowed absent = (100-75)/100 * 20 = 5
        // Current absent = 3
        // Safe remaining = 5 - 3 = 2
        val result = getSummary(1, threshold = 75.0)

        assertEquals(2, result.safeAbsencesRemaining)
    }

    @Test
    fun `zero safe absences when at threshold`() = runTest {
        coEvery { subjectRepo.getById(1) } returns Subject(id = 1, name = "Physics")
        coEvery { attendanceRepo.getTotalClasses(1) } returns 20
        coEvery { attendanceRepo.getPresentCount(1) } returns 15
        coEvery { attendanceRepo.getAbsentCount(1) } returns 5
        coEvery { attendanceRepo.getExcusedCount(1) } returns 0
        coEvery { attendanceRepo.getLateCount(1) } returns 0

        // Threshold 75%
        // Max allowed absent = (100-75)/100 * 20 = 5
        // Current absent = 5
        // Safe remaining = 5 - 5 = 0
        val result = getSummary(1, threshold = 75.0)

        assertEquals(0, result.safeAbsencesRemaining)
    }

    @Test
    fun `excused absences do not count against student`() = runTest {
        coEvery { subjectRepo.getById(1) } returns Subject(id = 1, name = "Physics")
        coEvery { attendanceRepo.getTotalClasses(1) } returns 20
        coEvery { attendanceRepo.getPresentCount(1) } returns 14
        coEvery { attendanceRepo.getAbsentCount(1) } returns 2
        coEvery { attendanceRepo.getExcusedCount(1) } returns 4
        coEvery { attendanceRepo.getLateCount(1) } returns 0

        // attended = 14 + 0 = 14
        // percentage = 14/20 * 100 = 70%
        // Max absent = 5 (for 75% threshold)
        // Effective absences = 2 (excused don't count)
        // Safe remaining = 5 - 2 = 3
        val result = getSummary(1, threshold = 75.0)

        assertEquals(70.0, result.attendancePercentage, 0.01)
        assertEquals(3, result.safeAbsencesRemaining)
    }

    @Test
    fun `simulate skip reduces attendance`() = runTest {
        coEvery { subjectRepo.getById(1) } returns Subject(id = 1, name = "Physics")
        coEvery { attendanceRepo.getTotalClasses(1) } returns 10
        coEvery { attendanceRepo.getPresentCount(1) } returns 8
        coEvery { attendanceRepo.getAbsentCount(1) } returns 2
        coEvery { attendanceRepo.getExcusedCount(1) } returns 0
        coEvery { attendanceRepo.getLateCount(1) } returns 0

        val result = getSummary.simulateSkip(1, threshold = 75.0)

        // New total = 11, new absent = 3
        // attended = 8, percentage = 8/11 * 100 = 72.7%
        assertEquals(72.7, result.attendancePercentage, 0.1)
        assertEquals(11, result.totalClasses)
        assertEquals(3, result.absentCount)
    }

    @Test
    fun `late counts as attended`() = runTest {
        coEvery { subjectRepo.getById(1) } returns Subject(id = 1, name = "Physics")
        coEvery { attendanceRepo.getTotalClasses(1) } returns 10
        coEvery { attendanceRepo.getPresentCount(1) } returns 5
        coEvery { attendanceRepo.getAbsentCount(1) } returns 0
        coEvery { attendanceRepo.getExcusedCount(1) } returns 0
        coEvery { attendanceRepo.getLateCount(1) } returns 5

        val result = getSummary(1)

        // attended = 5 + 5 = 10 → 100%
        assertEquals(100.0, result.attendancePercentage, 0.01)
    }

    @Test
    fun `empty attendance returns 100 percent`() = runTest {
        coEvery { subjectRepo.getById(1) } returns Subject(id = 1, name = "Physics")
        coEvery { attendanceRepo.getTotalClasses(1) } returns 0
        coEvery { attendanceRepo.getPresentCount(1) } returns 0
        coEvery { attendanceRepo.getAbsentCount(1) } returns 0
        coEvery { attendanceRepo.getExcusedCount(1) } returns 0
        coEvery { attendanceRepo.getLateCount(1) } returns 0

        val result = getSummary(1)

        assertEquals(100.0, result.attendancePercentage, 0.01)
    }
}
