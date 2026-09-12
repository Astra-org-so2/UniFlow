package com.studentos.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studentos.app.data.local.database.entity.GradeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeDao {

    @Query("SELECT * FROM grades ORDER BY date DESC")
    fun getAll(): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grades ORDER BY date DESC")
    suspend fun getAllOnce(): List<GradeEntity>

    @Query("SELECT * FROM grades WHERE id = :id")
    suspend fun getById(id: Long): GradeEntity?

    @Query("SELECT * FROM grades WHERE subject_id = :subjectId ORDER BY date DESC")
    fun getBySubject(subjectId: Long): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grades WHERE subject_id = :subjectId ORDER BY date DESC")
    suspend fun getBySubjectOnce(subjectId: Long): List<GradeEntity>

    @Query("SELECT * FROM grades WHERE subject_id = :subjectId AND type = :type ORDER BY date DESC")
    fun getBySubjectAndType(subjectId: Long, type: String): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grades WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getInRange(start: String, end: String): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grades WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    suspend fun getInRangeOnce(start: String, end: String): List<GradeEntity>

    @Query("SELECT AVG(value * 1.0 / max_value * 100.0) FROM grades WHERE subject_id = :subjectId")
    suspend fun getAverageForSubject(subjectId: Long): Double?

    @Query("""
        SELECT SUM(value * 1.0 / max_value * 100.0 * weight) / SUM(weight) 
        FROM grades WHERE subject_id = :subjectId
    """)
    suspend fun getWeightedAverageForSubject(subjectId: Long): Double?

    @Query("SELECT COUNT(*) FROM grades WHERE subject_id = :subjectId")
    suspend fun getCountForSubject(subjectId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(grade: GradeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(grades: List<GradeEntity>)

    @Update
    suspend fun update(grade: GradeEntity)

    @Delete
    suspend fun delete(grade: GradeEntity)

    @Query("DELETE FROM grades WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM grades")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM grades")
    suspend fun count(): Int
}
