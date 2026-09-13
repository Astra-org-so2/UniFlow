package com.studentos.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studentos.app.data.local.database.entity.AssignmentEntity
import com.studentos.app.data.local.database.entity.SubtaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {

    @Query("SELECT * FROM assignments ORDER BY deadline ASC, priority DESC")
    fun getAll(): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments ORDER BY deadline ASC, priority DESC")
    suspend fun getAllOnce(): List<AssignmentEntity>

    @Query("SELECT * FROM assignments WHERE id = :id")
    suspend fun getById(id: Long): AssignmentEntity?

    @Query("SELECT * FROM assignments WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<AssignmentEntity?>

    @Query("SELECT * FROM assignments WHERE subject_id = :subjectId ORDER BY deadline ASC")
    fun getBySubject(subjectId: Long): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE subject_id = :subjectId ORDER BY deadline ASC")
    suspend fun getBySubjectOnce(subjectId: Long): List<AssignmentEntity>

    @Query("SELECT * FROM assignments WHERE status = :status ORDER BY deadline ASC")
    fun getByStatus(status: String): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE status = 'TODO' OR status = 'IN_PROGRESS' ORDER BY deadline ASC, priority DESC")
    fun getActive(): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE status = 'TODO' OR status = 'IN_PROGRESS' ORDER BY deadline ASC, priority DESC")
    suspend fun getActiveOnce(): List<AssignmentEntity>

    @Query("SELECT * FROM assignments WHERE deadline = :date AND (status = 'TODO' OR status = 'IN_PROGRESS') ORDER BY priority DESC")
    fun getForDate(date: String): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE deadline = :date AND (status = 'TODO' OR status = 'IN_PROGRESS') ORDER BY priority DESC")
    suspend fun getForDateOnce(date: String): List<AssignmentEntity>

    @Query("SELECT * FROM assignments WHERE deadline < :today AND (status = 'TODO' OR status = 'IN_PROGRESS') ORDER BY deadline ASC")
    fun getOverdue(today: String): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE deadline < :today AND (status = 'TODO' OR status = 'IN_PROGRESS') ORDER BY deadline ASC")
    suspend fun getOverdueOnce(today: String): List<AssignmentEntity>

    @Query("SELECT * FROM assignments WHERE deadline BETWEEN :start AND :end AND (status = 'TODO' OR status = 'IN_PROGRESS') ORDER BY deadline ASC")
    fun getInRange(start: String, end: String): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE deadline BETWEEN :start AND :end AND (status = 'TODO' OR status = 'IN_PROGRESS') ORDER BY deadline ASC")
    suspend fun getInRangeOnce(start: String, end: String): List<AssignmentEntity>

    @Query("SELECT COUNT(*) FROM assignments WHERE status = 'DONE' AND subject_id = :subjectId")
    suspend fun getCompletedCount(subjectId: Long): Int

    @Query("SELECT COUNT(*) FROM assignments WHERE subject_id = :subjectId")
    suspend fun getTotalCount(subjectId: Long): Int

    @Query("SELECT COUNT(*) FROM assignments WHERE (status = 'TODO' OR status = 'IN_PROGRESS') AND deadline < :today")
    suspend fun getOverdueCount(today: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assignment: AssignmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assignments: List<AssignmentEntity>)

    @Update
    suspend fun update(assignment: AssignmentEntity)

    @Delete
    suspend fun delete(assignment: AssignmentEntity)

    @Query("DELETE FROM assignments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM assignments")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM assignments")
    suspend fun count(): Int

    // Subtasks
    @Query("SELECT * FROM subtasks WHERE assignment_id = :assignmentId ORDER BY id ASC")
    fun getSubtasks(assignmentId: Long): Flow<List<SubtaskEntity>>

    @Query("SELECT * FROM subtasks WHERE assignment_id = :assignmentId ORDER BY id ASC")
    suspend fun getSubtasksOnce(assignmentId: Long): List<SubtaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: SubtaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(subtasks: List<SubtaskEntity>)

    @Update
    suspend fun updateSubtask(subtask: SubtaskEntity)

    @Delete
    suspend fun deleteSubtask(subtask: SubtaskEntity)

    @Query("DELETE FROM subtasks WHERE assignment_id = :assignmentId")
    suspend fun deleteSubtasksByAssignment(assignmentId: Long)
}
