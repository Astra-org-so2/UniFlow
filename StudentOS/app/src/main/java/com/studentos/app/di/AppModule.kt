package com.studentos.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.studentos.app.ai.AIGateway
import com.studentos.app.ai.NoOpAIProvider
import com.studentos.app.domain.repository.*
import com.studentos.app.domain.usecase.analytics.GetAnalyticsUseCase
import com.studentos.app.domain.usecase.analytics.GetSubjectAnalyticsUseCase
import com.studentos.app.domain.usecase.attendance.GetAttendanceSummaryUseCase
import com.studentos.app.domain.usecase.attendance.MarkAttendanceUseCase
import com.studentos.app.domain.usecase.grade.CalculateGradeUseCase
import com.studentos.app.domain.usecase.grade.GetRequiredExamGradeUseCase
import com.studentos.app.domain.usecase.planner.GetStudyPlanUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore

    @Provides
    @Singleton
    fun provideAIGateway(): AIGateway = NoOpAIProvider()

    // Use Cases
    @Provides
    fun provideGetAttendanceSummaryUseCase(
        attendanceRepo: AttendanceRepository,
        subjectRepo: SubjectRepository
    ) = GetAttendanceSummaryUseCase(attendanceRepo, subjectRepo)

    @Provides
    fun provideMarkAttendanceUseCase(
        attendanceRepo: AttendanceRepository
    ) = MarkAttendanceUseCase(attendanceRepo)

    @Provides
    fun provideCalculateGradeUseCase(
        gradeRepo: GradeRepository,
        subjectRepo: SubjectRepository
    ) = CalculateGradeUseCase(gradeRepo, subjectRepo)

    @Provides
    fun provideGetRequiredExamGradeUseCase(
        gradeRepo: GradeRepository
    ) = GetRequiredExamGradeUseCase(gradeRepo)

    @Provides
    fun provideGetStudyPlanUseCase(
        assignmentRepo: AssignmentRepository,
        examRepo: ExamRepository,
        subjectRepo: SubjectRepository,
        attendanceRepo: AttendanceRepository,
        gradeRepo: GradeRepository
    ) = GetStudyPlanUseCase(assignmentRepo, examRepo, subjectRepo, attendanceRepo, gradeRepo)

    @Provides
    fun provideGetAnalyticsUseCase(
        assignmentRepo: AssignmentRepository,
        gradeRepo: GradeRepository,
        attendanceRepo: AttendanceRepository,
        subjectRepo: SubjectRepository,
        examRepo: ExamRepository
    ) = GetAnalyticsUseCase(assignmentRepo, gradeRepo, attendanceRepo, subjectRepo, examRepo)

    @Provides
    fun provideGetSubjectAnalyticsUseCase(
        assignmentRepo: AssignmentRepository,
        gradeRepo: GradeRepository,
        attendanceRepo: AttendanceRepository,
        examRepo: ExamRepository
    ) = GetSubjectAnalyticsUseCase(assignmentRepo, gradeRepo, attendanceRepo, examRepo)
}
