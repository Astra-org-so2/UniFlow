package com.studentos.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.studentos.app.data.local.database.converter.Converters
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
import com.studentos.app.data.local.database.entity.AssignmentEntity
import com.studentos.app.data.local.database.entity.AttachmentEntity
import com.studentos.app.data.local.database.entity.AttendanceRecordEntity
import com.studentos.app.data.local.database.entity.ExamEntity
import com.studentos.app.data.local.database.entity.GradeEntity
import com.studentos.app.data.local.database.entity.NoteEntity
import com.studentos.app.data.local.database.entity.ScheduleItemEntity
import com.studentos.app.data.local.database.entity.StudentEntity
import com.studentos.app.data.local.database.entity.SubjectEntity
import com.studentos.app.data.local.database.entity.SubtaskEntity
import com.studentos.app.data.local.database.entity.TeacherEntity

@Database(
    entities = [
        StudentEntity::class,
        SubjectEntity::class,
        TeacherEntity::class,
        ScheduleItemEntity::class,
        AssignmentEntity::class,
        SubtaskEntity::class,
        GradeEntity::class,
        AttendanceRecordEntity::class,
        ExamEntity::class,
        NoteEntity::class,
        AttachmentEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class StudentOSDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun subjectDao(): SubjectDao
    abstract fun teacherDao(): TeacherDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun gradeDao(): GradeDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun examDao(): ExamDao
    abstract fun noteDao(): NoteDao
    abstract fun attachmentDao(): AttachmentDao

    companion object {
        const val DATABASE_NAME = "student_os.db"
    }
}
