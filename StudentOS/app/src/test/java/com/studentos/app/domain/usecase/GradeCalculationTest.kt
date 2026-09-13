package com.studentos.app.domain.usecase

import com.studentos.app.domain.model.*
import com.studentos.app.domain.repository.GradeRepository
import com.studentos.app.domain.repository.SubjectRepository
import com.studentos.app.domain.usecase.grade.CalculateGradeUseCase
import com.studentos.app.domain.usecase.grade.GetRequiredExamGradeUseCase
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GradeCalculationTest {

    private lateinit var gradeRepo: GradeRepository
    private lateinit var subjectRepo: SubjectRepository
    private lateinit var calculateGrade: CalculateGradeUseCase
    private lateinit var getRequiredExam: GetRequiredExamGradeUseCase

    @Before
    fun setup() {
        gradeRepo = mockk()
        subjectRepo = mockk()
        calculateGrade = CalculateGradeUseCase(gradeRepo, subjectRepo)
        getRequiredExam = GetRequiredExamGradeUseCase(gradeRepo)
    }

    @Test
    fun `calculate weighted average correctly`() = runTest {
        val grades = listOf(
            Grade(subjectId = 1, type = GradeCategory.HOMEWORK, value = 80.0, maxValue = 100.0, weight = 1.0),
            Grade(subjectId = 1, type = GradeCategory.TEST, value = 90.0, maxValue = 100.0, weight = 2.0),
            Grade(subjectId = 1, type = GradeCategory.LAB, value = 70.0, maxValue = 100.0, weight = 1.0)
        )

        coEvery { subjectRepo.getById(1) } returns Subject(id = 1, name = "Physics")
        coEvery { gradeRepo.getBySubjectOnce(1) } returns grades
        coEvery { gradeRepo.getAverageForSubject(1) } returns 80.0
        coEvery { gradeRepo.getWeightedAverageForSubject(1) } returns 82.5

        val result = calculateGrade(1)

        assertEquals(82.5, result.weightedAverage, 0.01)
        assertEquals("4.0", result.currentGrade) // 82.5 → 4.0
    }

    @Test
    fun `grade 5 for 90 percent plus`() = runTest {
        coEvery { subjectRepo.getById(1) } returns Subject(id = 1, name = "Math")
        coEvery { gradeRepo.getBySubjectOnce(1) } returns listOf(
            Grade(subjectId = 1, value = 95.0, maxValue = 100.0, weight = 1.0)
        )
        coEvery { gradeRepo.getAverageForSubject(1) } returns 95.0
        coEvery { gradeRepo.getWeightedAverageForSubject(1) } returns 95.0

        val result = calculateGrade(1)

        assertEquals("5.0", result.currentGrade)
    }

    @Test
    fun `grade 3 for 60 percent`() = runTest {
        coEvery { subjectRepo.getById(1) } returns Subject(id = 1, name = "Physics")
        coEvery { gradeRepo.getBySubjectOnce(1) } returns listOf(
            Grade(subjectId = 1, value = 65.0, maxValue = 100.0, weight = 1.0)
        )
        coEvery { gradeRepo.getAverageForSubject(1) } returns 65.0
        coEvery { gradeRepo.getWeightedAverageForSubject(1) } returns 65.0

        val result = calculateGrade(1)

        assertEquals("3.0", result.currentGrade)
    }

    @Test
    fun `grade 2 for below 60 percent`() = runTest {
        coEvery { subjectRepo.getById(1) } returns Subject(id = 1, name = "Physics")
        coEvery { gradeRepo.getBySubjectOnce(1) } returns listOf(
            Grade(subjectId = 1, value = 45.0, maxValue = 100.0, weight = 1.0)
        )
        coEvery { gradeRepo.getAverageForSubject(1) } returns 45.0
        coEvery { gradeRepo.getWeightedAverageForSubject(1) } returns 45.0

        val result = calculateGrade(1)

        assertEquals("2.0", result.currentGrade)
    }

    @Test
    fun `required exam grade calculation`() = runTest {
        val grades = listOf(
            Grade(subjectId = 1, type = GradeCategory.HOMEWORK, value = 80.0, maxValue = 100.0, weight = 1.0),
            Grade(subjectId = 1, type = GradeCategory.TEST, value = 90.0, maxValue = 100.0, weight = 1.0)
        )
        // Coursework average = (80 + 90) / 2 = 85%
        // For 90% final with 40% exam weight:
        // 90 = 85 * 0.6 + exam * 0.4
        // exam = (90 - 51) / 0.4 = 97.5%
        coEvery { gradeRepo.getBySubjectOnce(1) } returns grades

        val result = getRequiredExam(1, targetGrade = 90.0, examWeight = 0.4)

        assertNotNull(result)
        assertEquals(97.5, result!!, 0.1)
    }

    @Test
    fun `required exam returns null when impossible`() = runTest {
        val grades = listOf(
            Grade(subjectId = 1, type = GradeCategory.HOMEWORK, value = 30.0, maxValue = 100.0, weight = 1.0)
        )
        // Coursework average = 30%
        // For 90% final with 40% exam weight:
        // 90 = 30 * 0.6 + exam * 0.4
        // exam = (90 - 18) / 0.4 = 180% — impossible
        coEvery { gradeRepo.getBySubjectOnce(1) } returns grades

        val result = getRequiredExam(1, targetGrade = 90.0, examWeight = 0.4)

        assertNull(result) // impossible
    }

    @Test
    fun `grade percentage calculation`() {
        val grade = Grade(subjectId = 1, value = 75.0, maxValue = 100.0)
        assertEquals(75.0, grade.percentage, 0.01)

        val grade2 = Grade(subjectId = 1, value = 15.0, maxValue = 20.0)
        assertEquals(75.0, grade2.percentage, 0.01)

        val grade3 = Grade(subjectId = 1, value = 0.0, maxValue = 0.0)
        assertEquals(0.0, grade3.percentage, 0.01)
    }
}
