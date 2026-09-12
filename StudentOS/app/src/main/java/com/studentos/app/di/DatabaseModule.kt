package com.studentos.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.studentos.app.data.local.database.StudentOSDatabase
import com.studentos.app.data.local.database.dao.AssignmentDao
import com.studentos.app.data.local.database.dao.AttachmentDao
import com.studentos.app.data.local.database.dao.AttendanceDao
import com.studentos.app.data.local.database.dao.ExamDao
import com.studentos.app.data.local.database.dao.GradeDao
import com.studentos.app.data.local.database.dao.NoteDao
import com.studentos.app.data.local.database.dao.ScheduleDao
import com.studentos.app.data.local.database.dao.StudentDao
import com.studentos.app.data.local.database.dao.SubjectDao
import com.studentos.app.data.local.database.dao.TeacherDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StudentOSDatabase {
        return Room.databaseBuilder(
            context,
            StudentOSDatabase::class.java,
            StudentOSDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Enable WAL mode for better concurrent read/write performance
                    db.execSQL("PRAGMA journal_mode=WAL")
                    db.execSQL("PRAGMA foreign_keys=ON")
                }
            })
            .build()
    }

    @Provides fun provideStudentDao(db: StudentOSDatabase): StudentDao = db.studentDao()
    @Provides fun provideSubjectDao(db: StudentOSDatabase): SubjectDao = db.subjectDao()
    @Provides fun provideTeacherDao(db: StudentOSDatabase): TeacherDao = db.teacherDao()
    @Provides fun provideScheduleDao(db: StudentOSDatabase): ScheduleDao = db.scheduleDao()
    @Provides fun provideAssignmentDao(db: StudentOSDatabase): AssignmentDao = db.assignmentDao()
    @Provides fun provideGradeDao(db: StudentOSDatabase): GradeDao = db.gradeDao()
    @Provides fun provideAttendanceDao(db: StudentOSDatabase): AttendanceDao = db.attendanceDao()
    @Provides fun provideExamDao(db: StudentOSDatabase): ExamDao = db.examDao()
    @Provides fun provideNoteDao(db: StudentOSDatabase): NoteDao = db.noteDao()
    @Provides fun provideAttachmentDao(db: StudentOSDatabase): AttachmentDao = db.attachmentDao()
}
