package com.studentos.app.data.repository

import com.studentos.app.data.local.database.dao.TeacherDao
import com.studentos.app.data.local.database.toDomain
import com.studentos.app.data.local.database.toEntity
import com.studentos.app.domain.model.Teacher
import com.studentos.app.domain.repository.TeacherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeacherRepositoryImpl @Inject constructor(
    private val dao: TeacherDao
) : TeacherRepository {

    override fun getAll(): Flow<List<Teacher>> = dao.getAll().map { list -> list.map { it.toDomain() } }
    override suspend fun getAllOnce(): List<Teacher> = dao.getAllOnce().map { it.toDomain() }
    override suspend fun getById(id: Long): Teacher? = dao.getById(id)?.toDomain()
    override fun getByIdFlow(id: Long): Flow<Teacher?> = dao.getByIdFlow(id).map { it?.toDomain() }
    override fun search(query: String): Flow<List<Teacher>> = dao.search(query).map { list -> list.map { it.toDomain() } }
    override suspend fun save(teacher: Teacher): Long = dao.insert(teacher.toEntity())
    override suspend fun update(teacher: Teacher) = dao.update(teacher.toEntity())
    override suspend fun delete(teacher: Teacher) = dao.delete(teacher.toEntity())
    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
