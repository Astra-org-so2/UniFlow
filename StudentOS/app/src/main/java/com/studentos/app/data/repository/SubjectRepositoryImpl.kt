package com.studentos.app.data.repository

import com.studentos.app.data.local.database.dao.SubjectDao
import com.studentos.app.data.local.database.toDomain
import com.studentos.app.data.local.database.toEntity
import com.studentos.app.domain.model.Subject
import com.studentos.app.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectRepositoryImpl @Inject constructor(
    private val dao: SubjectDao
) : SubjectRepository {

    override fun getAll(): Flow<List<Subject>> = dao.getAll().map { list -> list.map { it.toDomain() } }
    override suspend fun getAllOnce(): List<Subject> = dao.getAllOnce().map { it.toDomain() }
    override fun getBySemester(semester: Int): Flow<List<Subject>> = dao.getBySemester(semester).map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: Long): Subject? = dao.getById(id)?.toDomain()
    override fun getByIdFlow(id: Long): Flow<Subject?> = dao.getByIdFlow(id).map { it?.toDomain() }
    override fun getByTeacher(teacherId: Long): Flow<List<Subject>> = dao.getByTeacher(teacherId).map { list -> list.map { it.toDomain() } }
    override fun search(query: String): Flow<List<Subject>> = dao.search(query).map { list -> list.map { it.toDomain() } }
    override suspend fun save(subject: Subject): Long = dao.insert(subject.toEntity())
    override suspend fun update(subject: Subject) = dao.update(subject.toEntity())
    override suspend fun delete(subject: Subject) = dao.delete(subject.toEntity())
    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
