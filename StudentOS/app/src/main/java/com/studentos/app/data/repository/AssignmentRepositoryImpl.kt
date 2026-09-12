package com.studentos.app.data.repository

import com.studentos.app.data.local.database.dao.AssignmentDao
import com.studentos.app.data.local.database.toDomain
import com.studentos.app.data.local.database.toEntity
import com.studentos.app.domain.model.Assignment
import com.studentos.app.domain.model.AssignmentStatus
import com.studentos.app.domain.model.Subtask
import com.studentos.app.domain.repository.AssignmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssignmentRepositoryImpl @Inject constructor(
    private val dao: AssignmentDao
) : AssignmentRepository {

    override fun getAll(): Flow<List<Assignment>> = dao.getAll().map { list ->
        list.map { entity ->
            val subtasks = dao.getSubtasksOnce(entity.id)
            entity.toDomain(subtasks)
        }
    }

    override suspend fun getAllOnce(): List<Assignment> = dao.getAllOnce().map { entity ->
        val subtasks = dao.getSubtasksOnce(entity.id)
        entity.toDomain(subtasks)
    }

    override suspend fun getById(id: Long): Assignment? {
        val entity = dao.getById(id) ?: return null
        val subtasks = dao.getSubtasksOnce(id)
        return entity.toDomain(subtasks)
    }

    override fun getByIdFlow(id: Long): Flow<Assignment?> = dao.getByIdFlow(id).map { it?.toDomain() }

    override fun getBySubject(subjectId: Long): Flow<List<Assignment>> = dao.getBySubject(subjectId).map { list -> list.map { it.toDomain() } }

    override fun getByStatus(status: AssignmentStatus): Flow<List<Assignment>> = dao.getByStatus(status.name).map { list -> list.map { it.toDomain() } }

    override fun getActive(): Flow<List<Assignment>> = dao.getActive().map { list -> list.map { it.toDomain() } }

    override suspend fun getActiveOnce(): List<Assignment> = dao.getActiveOnce().map { it.toDomain() }

    override fun getForDate(date: LocalDate): Flow<List<Assignment>> = dao.getForDate(date.toString()).map { list -> list.map { it.toDomain() } }

    override fun getOverdue(): Flow<List<Assignment>> = dao.getOverdue(LocalDate.now().toString()).map { list -> list.map { it.toDomain() } }

    override suspend fun getOverdueOnce(): List<Assignment> = dao.getOverdueOnce(LocalDate.now().toString()).map { it.toDomain() }

    override fun getInRange(start: LocalDate, end: LocalDate): Flow<List<Assignment>> = dao.getInRange(start.toString(), end.toString()).map { list -> list.map { it.toDomain() } }

    override suspend fun getOverdueCount(): Int = dao.getOverdueCount(LocalDate.now().toString())

    override suspend fun save(assignment: Assignment): Long = dao.insert(assignment.toEntity())

    override suspend fun update(assignment: Assignment) = dao.update(assignment.toEntity())

    override suspend fun delete(assignment: Assignment) = dao.delete(assignment.toEntity())

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    override suspend fun markComplete(id: Long) {
        val entity = dao.getById(id) ?: return
        dao.update(entity.copy(
            status = AssignmentStatus.DONE.name,
            completedAt = Instant.now()
        ))
    }

    override suspend fun getSubtasks(assignmentId: Long): List<Subtask> =
        dao.getSubtasksOnce(assignmentId).map { it.toDomain() }

    override fun getSubtasksFlow(assignmentId: Long): Flow<List<Subtask>> =
        dao.getSubtasks(assignmentId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveSubtask(subtask: Subtask): Long = dao.insertSubtask(subtask.toEntity())

    override suspend fun updateSubtask(subtask: Subtask) = dao.updateSubtask(subtask.toEntity())

    override suspend fun deleteSubtask(subtask: Subtask) = dao.deleteSubtask(subtask.toEntity())
}
