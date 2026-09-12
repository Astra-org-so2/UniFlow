package com.studentos.app.domain.usecase.grade

import com.studentos.app.domain.model.*
import com.studentos.app.domain.repository.GradeRepository
import com.studentos.app.domain.repository.SubjectRepository
import javax.inject.Inject

class CalculateGradeUseCase @Inject constructor(
    private val gradeRepo: GradeRepository,
    private val subjectRepo: SubjectRepository
) {
    suspend operator fun invoke(subjectId: Long): GradeSummary {
        val subject = subjectRepo.getById(subjectId)
        val grades = gradeRepo.getBySubjectOnce(subjectId)
        val average = gradeRepo.getAverageForSubject(subjectId) ?: 0.0
        val weightedAverage = gradeRepo.getWeightedAverageForSubject(subjectId) ?: 0.0

        val currentGrade = when {
            weightedAverage >= 90.0 -> "5.0"
            weightedAverage >= 75.0 -> "4.0"
            weightedAverage >= 60.0 -> "3.0"
            else -> "2.0"
        }

        return GradeSummary(
            subjectId = subjectId,
            subjectName = subject?.name ?: "",
            grades = grades,
            average = average,
            weightedAverage = weightedAverage,
            currentGrade = currentGrade,
            requiredExamGrade = null
        )
    }
}

class GetRequiredExamGradeUseCase @Inject constructor(
    private val gradeRepo: GradeRepository
) {
    /**
     * Calculate the minimum exam grade needed to achieve a target final grade.
     *
     * @param subjectId The subject to calculate for
     * @param targetGrade The target final percentage (e.g., 90.0 for "5.0")
     * @param examWeight The weight of the exam (e.g., 0.4 for 40%)
     * @return Required exam percentage, or null if impossible
     */
    suspend operator fun invoke(
        subjectId: Long,
        targetGrade: Double = 90.0,
        examWeight: Double = 0.4
    ): Double? {
        val grades = gradeRepo.getBySubjectOnce(subjectId)
        if (grades.isEmpty()) return targetGrade

        // Calculate current coursework average (excluding exam category)
        val courseworkGrades = grades.filter { it.type != GradeCategory.EXAM }
        if (courseworkGrades.isEmpty()) return targetGrade

        val totalWeight = courseworkGrades.sumOf { it.weight }
        if (totalWeight <= 0) return targetGrade

        val courseworkAverage = courseworkGrades.sumOf { (it.value / it.maxValue * 100.0) * it.weight } / totalWeight
        val courseworkWeight = 1.0 - examWeight

        // targetGrade = courseworkAverage * courseworkWeight + examGrade * examWeight
        // examGrade = (targetGrade - courseworkAverage * courseworkWeight) / examWeight
        val requiredExam = (targetGrade - courseworkAverage * courseworkWeight) / examWeight

        return if (requiredExam in 0.0..100.0) requiredExam else null
    }
}
