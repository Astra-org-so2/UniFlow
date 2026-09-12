package com.studentos.app.domain.usecase

import com.studentos.app.domain.model.*
import com.studentos.app.domain.repository.*
import com.studentos.app.domain.usecase.planner.GetStudyPlanUseCase
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class StudyPlannerTest {

    private lateinit var assignmentRepo: AssignmentRepository
    private lateinit var examRepo: ExamRepository
    private lateinit var subjectRepo: SubjectRepository
    private lateinit var attendanceRepo: AttendanceRepository
    private lateinit var gradeRepo: GradeRepository
    private lateinit var planner: GetStudyPlanUseCase

    @Before
    fun setup() {
        assignmentRepo = mockk()
        examRepo = mockk()
        subjectRepo = mockk()
        attendanceRepo = mockk()
        gradeRepo = mockk()
        planner = GetStudyPlanUseCase(assignmentRepo, examRepo, subjectRepo, attendanceRepo, gradeRepo)
    }

    @Test
    fun `overdue tasks get highest priority`() = runTest {
        val overdue = Assignment(
            id = 1,
            subjectId = 1,
            title = "Overdue Lab",
            deadline = LocalDate.now().minusDays(2),
            status = AssignmentStatus.TODO,
            priority = Priority.MEDIUM,
            estimatedDuration = 60
        )

        coEvery { assignmentRepo.getOverdueOnce() } returns listOf(overdue)
        coEvery { assignmentRepo.getForDateOnce(any()) } returns emptyList()
        coEvery { assignmentRepo.getInRangeOnce(any(), any()) } returns emptyList()
        coEvery { examRepo.getUpcomingOnce() } returns emptyList()
        coEvery { subjectRepo.getAllOnce() } returns emptyList()

        val plan = planner()

        assertTrue(plan.recommendations.isNotEmpty())
        assertEquals(Priority.URGENT, plan.recommendations.first().priority)
    }

    @Test
    fun `today tasks are prioritized`() = runTest {
        val todayTask = Assignment(
            id = 1,
            subjectId = 1,
            title = "Due Today",
            deadline = LocalDate.now(),
            status = AssignmentStatus.TODO,
            priority = Priority.HIGH,
            estimatedDuration = 45
        )

        coEvery { assignmentRepo.getOverdueOnce() } returns emptyList()
        coEvery { assignmentRepo.getForDateOnce(LocalDate.now()) } returns listOf(todayTask)
        coEvery { assignmentRepo.getForDateOnce(LocalDate.now().plusDays(1)) } returns emptyList()
        coEvery { assignmentRepo.getInRangeOnce(any(), any()) } returns emptyList()
        coEvery { examRepo.getUpcomingOnce() } returns emptyList()
        coEvery { subjectRepo.getAllOnce() } returns emptyList()

        val plan = planner()

        assertTrue(plan.recommendations.any { it.assignmentId == 1L })
    }

    @Test
    fun `exams within 14 days appear in plan`() = runTest {
        val exam = Exam(
            id = 1,
            subjectId = 1,
            type = ExamType.EXAM,
            date = LocalDate.now().plusDays(5),
            preparationProgress = 30
        )

        coEvery { assignmentRepo.getOverdueOnce() } returns emptyList()
        coEvery { assignmentRepo.getForDateOnce(any()) } returns emptyList()
        coEvery { assignmentRepo.getInRangeOnce(any(), any()) } returns emptyList()
        coEvery { examRepo.getUpcomingOnce() } returns listOf(exam)
        coEvery { subjectRepo.getAllOnce() } returns listOf(
            Subject(id = 1, name = "Physics")
        )
        coEvery { attendanceRepo.getTotalClasses(any()) } returns 0

        val plan = planner()

        assertTrue(plan.recommendations.any { it.examId == 1L })
        assertTrue(plan.recommendations.any { it.title.contains("Prepare") || it.title.contains("Physics") })
    }

    @Test
    fun `empty data produces empty plan`() = runTest {
        coEvery { assignmentRepo.getOverdueOnce() } returns emptyList()
        coEvery { assignmentRepo.getForDateOnce(any()) } returns emptyList()
        coEvery { assignmentRepo.getInRangeOnce(any(), any()) } returns emptyList()
        coEvery { examRepo.getUpcomingOnce() } returns emptyList()
        coEvery { subjectRepo.getAllOnce() } returns emptyList()

        val plan = planner()

        assertTrue(plan.recommendations.isEmpty())
    }

    @Test
    fun `plan recommendations are deduplicated`() = runTest {
        val task = Assignment(
            id = 1,
            subjectId = 1,
            title = "Same Task",
            deadline = LocalDate.now(),
            status = AssignmentStatus.TODO,
            priority = Priority.HIGH,
            estimatedDuration = 30
        )

        coEvery { assignmentRepo.getOverdueOnce() } returns listOf(task)
        coEvery { assignmentRepo.getForDateOnce(LocalDate.now()) } returns listOf(task)
        coEvery { assignmentRepo.getForDateOnce(any()) } returns emptyList()
        coEvery { assignmentRepo.getInRangeOnce(any(), any()) } returns emptyList()
        coEvery { examRepo.getUpcomingOnce() } returns emptyList()
        coEvery { subjectRepo.getAllOnce() } returns emptyList()

        val plan = planner()

        val sameTaskCount = plan.recommendations.count { it.title == "Same Task" }
        assertEquals(1, sameTaskCount)
    }

    @Test
    fun `low attendance subjects appear in recommendations`() = runTest {
        val subject = Subject(id = 1, name = "Physics")

        coEvery { assignmentRepo.getOverdueOnce() } returns emptyList()
        coEvery { assignmentRepo.getForDateOnce(any()) } returns emptyList()
        coEvery { assignmentRepo.getInRangeOnce(any(), any()) } returns emptyList()
        coEvery { examRepo.getUpcomingOnce() } returns emptyList()
        coEvery { subjectRepo.getAllOnce() } returns listOf(subject)
        coEvery { attendanceRepo.getTotalClasses(1) } returns 10
        coEvery { attendanceRepo.getAttendedCount(1) } returns 6 // 60% attendance

        val plan = planner()

        assertTrue(plan.recommendations.any { it.title.contains("Attend") || it.title.contains("Physics") })
    }
}
