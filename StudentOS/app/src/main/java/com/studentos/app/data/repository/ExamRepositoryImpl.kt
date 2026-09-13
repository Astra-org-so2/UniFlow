package com.studentos.app.data.repository

import com.studentos.app.data.local.database.dao.ExamDao
import com.studentos.app.data.local.database.toDomain
import com.studentos.app.data.local.database.toEntity
import com.studentos.app.domain.model.Exam
import com.studentos.app.domain.repository.ExamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamRepositoryImpl @Inject constructor(
    private val dao: ExamDao
) : ExamRepository {

    override fun getAll(): Flow<List<Exam>> = dao.getAll().map { list -> list.map { it.toDomain() } }
    override suspend fun getAllOnce(): List<Exam> = dao.getAllOnce().map { it.toDomain() }
    override suspend fun getById(id: Long): Exam? = dao.getById(id)?.toDomain()
    override fun getBySubject(subjectId: Long): Flow<List<Exam>> = dao.getBySubject(subjectId).map { list -> list.map { it.toDomain() } }
    override fun getUpcoming(): Flow<List<Exam>> = dao.getUpcoming(LocalDate.now().toString()).map { list -> list.map { it.toDomain() } }
    override suspend fun getUpcomingOnce(): List<Exam> = dao.getUpcomingOnce(LocalDate.now().toString()).map { it.toDomain() }
    override fun getInRange(start: LocalDate, end: LocalDate): Flow<List<Exam>> = dao.getInRange(start.toString(), end.toString()).map { list -> list.map { it.toDomain() } }
    override suspend fun save(exam: Exam): Long = dao.insert(exam.toEntity())
    override suspend fun update(exam: Exam) = dao.update(exam.toEntity())
    override suspend fun delete(exam: Exam) = dao.delete(exam.toEntity())
    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
