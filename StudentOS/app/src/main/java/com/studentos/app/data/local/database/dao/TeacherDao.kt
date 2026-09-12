package com.studentos.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studentos.app.data.local.database.entity.TeacherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {

    @Query("SELECT * FROM teachers ORDER BY name ASC")
    fun getAll(): Flow<List<TeacherEntity>>

    @Query("SELECT * FROM teachers ORDER BY name ASC")
    suspend fun getAllOnce(): List<TeacherEntity>

    @Query("SELECT * FROM teachers WHERE id = :id")
    suspend fun getById(id: Long): TeacherEntity?

    @Query("SELECT * FROM teachers WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<TeacherEntity?>

    @Query("SELECT * FROM teachers WHERE name LIKE '%' || :query || '%' OR department LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<TeacherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(teacher: TeacherEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(teachers: List<TeacherEntity>)

    @Update
    suspend fun update(teacher: TeacherEntity)

    @Delete
    suspend fun delete(teacher: TeacherEntity)

    @Query("DELETE FROM teachers WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM teachers")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM teachers")
    suspend fun count(): Int
}
