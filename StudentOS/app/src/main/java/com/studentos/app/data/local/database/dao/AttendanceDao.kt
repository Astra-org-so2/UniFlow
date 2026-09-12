package com.studentos.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studentos.app.data.local.database.entity.AttendanceRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Query("SELECT * FROM attendance_records ORDER BY date DESC")
    fun getAll(): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records ORDER BY date DESC")
    suspend fun getAllOnce(): List<AttendanceRecordEntity>

    @Query("SELECT * FROM attendance_records WHERE id = :id")
    suspend fun getById(id: Long): AttendanceRecordEntity?

    @Query("SELECT * FROM attendance_records WHERE subject_id = :subjectId ORDER BY date DESC")
    fun getBySubject(subjectId: Long): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE subject_id = :subjectId ORDER BY date DESC")
    suspend fun getBySubjectOnce(subjectId: Long): List<AttendanceRecordEntity>

    @Query("SELECT * FROM attendance_records WHERE date = :date ORDER BY id ASC")
    fun getByDate(date: String): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE date = :date ORDER BY id ASC")
    suspend fun getByDateOnce(date: String): List<AttendanceRecordEntity>

    @Query("SELECT * FROM attendance_records WHERE schedule_item_id = :scheduleItemId AND date = :date LIMIT 1")
    suspend fun getByScheduleAndDate(scheduleItemId: Long, date: String): AttendanceRecordEntity?

    @Query("SELECT COUNT(*) FROM attendance_records WHERE subject_id = :subjectId")
    suspend fun getTotalClasses(subjectId: Long): Int

    @Query("SELECT COUNT(*) FROM attendance_records WHERE subject_id = :subjectId AND status = 'PRESENT'")
    suspend fun getPresentCount(subjectId: Long): Int

    @Query("SELECT COUNT(*) FROM attendance_records WHERE subject_id = :subjectId AND status = 'ABSENT'")
    suspend fun getAbsentCount(subjectId: Long): Int

    @Query("SELECT COUNT(*) FROM attendance_records WHERE subject_id = :subjectId AND status = 'EXCUSED'")
    suspend fun getExcusedCount(subjectId: Long): Int

    @Query("SELECT COUNT(*) FROM attendance_records WHERE subject_id = :subjectId AND status = 'LATE'")
    suspend fun getLateCount(subjectId: Long): Int

    @Query("SELECT COUNT(*) FROM attendance_records WHERE subject_id = :subjectId AND status = 'PRESENT' OR status = 'LATE'")
    suspend fun getAttendedCount(subjectId: Long): Int

    @Query("SELECT * FROM attendance_records WHERE subject_id = :subjectId AND date BETWEEN :start AND :end ORDER BY date DESC")
    fun getBySubjectInRange(subjectId: Long, start: String, end: String): Flow<List<AttendanceRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AttendanceRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<AttendanceRecordEntity>)

    @Update
    suspend fun update(record: AttendanceRecordEntity)

    @Delete
    suspend fun delete(record: AttendanceRecordEntity)

    @Query("DELETE FROM attendance_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM attendance_records")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM attendance_records")
    suspend fun count(): Int
}
