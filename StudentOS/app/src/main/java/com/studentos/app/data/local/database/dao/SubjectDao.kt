package com.studentos.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studentos.app.data.local.database.entity.SubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {

    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAll(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects ORDER BY name ASC")
    suspend fun getAllOnce(): List<SubjectEntity>

    @Query("SELECT * FROM subjects WHERE semester = :semester ORDER BY name ASC")
    fun getBySemester(semester: Int): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getById(id: Long): SubjectEntity?

    @Query("SELECT * FROM subjects WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<SubjectEntity?>

    @Query("SELECT * FROM subjects WHERE teacher_id = :teacherId")
    fun getByTeacher(teacherId: Long): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE teacher_id = :teacherId")
    suspend fun getByTeacherOnce(teacherId: Long): List<SubjectEntity>

    @Query("SELECT * FROM subjects WHERE name LIKE '%' || :query || '%' OR short_name LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subject: SubjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subjects: List<SubjectEntity>)

    @Update
    suspend fun update(subject: SubjectEntity)

    @Delete
    suspend fun delete(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM subjects")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun count(): Int
}
