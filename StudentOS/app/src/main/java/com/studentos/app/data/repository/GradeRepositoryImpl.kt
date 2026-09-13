package com.studentos.app.data.repository

import com.studentos.app.data.local.database.dao.GradeDao
import com.studentos.app.data.local.database.toDomain
import com.studentos.app.data.local.database.toEntity
import com.studentos.app.domain.model.Grade
import com.studentos.app.domain.repository.GradeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GradeRepositoryImpl @Inject constructor(
    private val dao: GradeDao
) : GradeRepository {

    override fun getAll(): Flow<List<Grade>> = dao.getAll().map { list -> list.map { it.toDomain() } }
    override suspend fun getAllOnce(): List<Grade> = dao.getAllOnce().map { it.toDomain() }
    override suspend fun getById(id: Long): Grade? = dao.getById(id)?.toDomain()
    override fun getBySubject(subjectId: Long): Flow<List<Grade>> = dao.getBySubject(subjectId).map { list -> list.map { it.toDomain() } }
    override suspend fun getBySubjectOnce(subjectId: Long): List<Grade> = dao.getBySubjectOnce(subjectId).map { it.toDomain() }
    override fun getInRange(start: LocalDate, end: LocalDate): Flow<List<Grade>> = dao.getInRange(start.toString(), end.toString()).map { list -> list.map { it.toDomain() } }
    override suspend fun getAverageForSubject(subjectId: Long): Double? = dao.getAverageForSubject(subjectId)
    override suspend fun getWeightedAverageForSubject(subjectId: Long): Double? = dao.getWeightedAverageForSubject(subjectId)
    override suspend fun save(grade: Grade): Long = dao.insert(grade.toEntity())
    override suspend fun update(grade: Grade) = dao.update(grade.toEntity())
    override suspend fun delete(grade: Grade) = dao.delete(grade.toEntity())
    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
