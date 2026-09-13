package com.studentos.app.di

import com.studentos.app.data.repository.AssignmentRepositoryImpl
import com.studentos.app.data.repository.AttachmentRepositoryImpl
import com.studentos.app.data.repository.AttendanceRepositoryImpl
import com.studentos.app.data.repository.ExamRepositoryImpl
import com.studentos.app.data.repository.GradeRepositoryImpl
import com.studentos.app.data.repository.NoteRepositoryImpl
import com.studentos.app.data.repository.ScheduleRepositoryImpl
import com.studentos.app.data.repository.StudentRepositoryImpl
import com.studentos.app.data.repository.SubjectRepositoryImpl
import com.studentos.app.data.repository.TeacherRepositoryImpl
import com.studentos.app.domain.repository.AssignmentRepository
import com.studentos.app.domain.repository.AttachmentRepository
import com.studentos.app.domain.repository.AttendanceRepository
import com.studentos.app.domain.repository.ExamRepository
import com.studentos.app.domain.repository.GradeRepository
import com.studentos.app.domain.repository.NoteRepository
import com.studentos.app.domain.repository.ScheduleRepository
import com.studentos.app.domain.repository.StudentRepository
import com.studentos.app.domain.repository.SubjectRepository
import com.studentos.app.domain.repository.TeacherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindStudentRepository(impl: StudentRepositoryImpl): StudentRepository

    @Binds @Singleton
    abstract fun bindSubjectRepository(impl: SubjectRepositoryImpl): SubjectRepository

    @Binds @Singleton
    abstract fun bindTeacherRepository(impl: TeacherRepositoryImpl): TeacherRepository

    @Binds @Singleton
    abstract fun bindScheduleRepository(impl: ScheduleRepositoryImpl): ScheduleRepository

    @Binds @Singleton
    abstract fun bindAssignmentRepository(impl: AssignmentRepositoryImpl): AssignmentRepository

    @Binds @Singleton
    abstract fun bindGradeRepository(impl: GradeRepositoryImpl): GradeRepository

    @Binds @Singleton
    abstract fun bindAttendanceRepository(impl: AttendanceRepositoryImpl): AttendanceRepository

    @Binds @Singleton
    abstract fun bindExamRepository(impl: ExamRepositoryImpl): ExamRepository

    @Binds @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds @Singleton
    abstract fun bindAttachmentRepository(impl: AttachmentRepositoryImpl): AttachmentRepository
}
