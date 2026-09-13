package com.studentos.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studentos.app.data.local.database.entity.ExamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {

    @Query("SELECT * FROM exams ORDER BY date ASC")
    fun getAll(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams ORDER BY date ASC")
    suspend fun getAllOnce(): List<ExamEntity>

    @Query("SELECT * FROM exams WHERE id = :id")
    suspend fun getById(id: Long): ExamEntity?

    @Query("SELECT * FROM exams WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<ExamEntity?>

    @Query("SELECT * FROM exams WHERE subject_id = :subjectId ORDER BY date ASC")
    fun getBySubject(subjectId: Long): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE subject_id = :subjectId ORDER BY date ASC")
    suspend fun getBySubjectOnce(subjectId: Long): List<ExamEntity>

    @Query("SELECT * FROM exams WHERE date >= :today ORDER BY date ASC")
    fun getUpcoming(today: String): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE date >= :today ORDER BY date ASC")
    suspend fun getUpcomingOnce(today: String): List<ExamEntity>

    @Query("SELECT * FROM exams WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    fun getInRange(start: String, end: String): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    suspend fun getInRangeOnce(start: String, end: String): List<ExamEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exam: ExamEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exams: List<ExamEntity>)

    @Update
    suspend fun update(exam: ExamEntity)

    @Delete
    suspend fun delete(exam: ExamEntity)

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM exams")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM exams")
    suspend fun count(): Int
}
