package com.studentos.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studentos.app.data.local.database.entity.ScheduleItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedule_items ORDER BY day_of_week ASC, start_time ASC")
    fun getAll(): Flow<List<ScheduleItemEntity>>

    @Query("SELECT * FROM schedule_items ORDER BY day_of_week ASC, start_time ASC")
    suspend fun getAllOnce(): List<ScheduleItemEntity>

    @Query("SELECT * FROM schedule_items WHERE id = :id")
    suspend fun getById(id: Long): ScheduleItemEntity?

    @Query("SELECT * FROM schedule_items WHERE day_of_week = :dayOfWeek ORDER BY start_time ASC")
    fun getByDay(dayOfWeek: Int): Flow<List<ScheduleItemEntity>>

    @Query("SELECT * FROM schedule_items WHERE day_of_week = :dayOfWeek ORDER BY start_time ASC")
    suspend fun getByDayOnce(dayOfWeek: Int): List<ScheduleItemEntity>

    @Query("SELECT * FROM schedule_items WHERE subject_id = :subjectId ORDER BY day_of_week ASC, start_time ASC")
    fun getBySubject(subjectId: Long): Flow<List<ScheduleItemEntity>>

    @Query("SELECT * FROM schedule_items WHERE subject_id = :subjectId ORDER BY day_of_week ASC, start_time ASC")
    suspend fun getBySubjectOnce(subjectId: Long): List<ScheduleItemEntity>

    @Query("SELECT * FROM schedule_items WHERE teacher_id = :teacherId ORDER BY day_of_week ASC, start_time ASC")
    fun getByTeacher(teacherId: Long): Flow<List<ScheduleItemEntity>>

    @Query("""
        SELECT * FROM schedule_items 
        WHERE day_of_week = :dayOfWeek 
        AND :date BETWEEN semester_start AND semester_end
        ORDER BY start_time ASC
    """)
    fun getForDate(dayOfWeek: Int, date: String): Flow<List<ScheduleItemEntity>>

    @Query("""
        SELECT * FROM schedule_items 
        WHERE day_of_week = :dayOfWeek 
        AND :date BETWEEN semester_start AND semester_end
        ORDER BY start_time ASC
    """)
    suspend fun getForDateOnce(dayOfWeek: Int, date: String): List<ScheduleItemEntity>

    @Query("""
        SELECT * FROM schedule_items 
        WHERE :date BETWEEN semester_start AND semester_end
        ORDER BY day_of_week ASC, start_time ASC
    """)
    fun getForWeek(date: String): Flow<List<ScheduleItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ScheduleItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ScheduleItemEntity>)

    @Update
    suspend fun update(item: ScheduleItemEntity)

    @Delete
    suspend fun delete(item: ScheduleItemEntity)

    @Query("DELETE FROM schedule_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM schedule_items")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM schedule_items")
    suspend fun count(): Int
}
