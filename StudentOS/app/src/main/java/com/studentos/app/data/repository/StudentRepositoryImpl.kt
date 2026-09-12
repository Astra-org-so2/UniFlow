package com.studentos.app.data.repository

import com.studentos.app.data.local.database.dao.StudentDao
import com.studentos.app.data.local.database.toDomain
import com.studentos.app.data.local.database.toEntity
import com.studentos.app.domain.model.Student
import com.studentos.app.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentRepositoryImpl @Inject constructor(
    private val dao: StudentDao
) : StudentRepository {

    override fun getCurrentStudent(): Flow<Student?> =
        dao.getCurrentStudent().map { it?.toDomain() }

    override suspend fun getCurrentStudentOnce(): Student? =
        dao.getCurrentStudentOnce()?.toDomain()

    override suspend fun save(student: Student): Long =
        dao.insert(student.toEntity())

    override suspend fun update(student: Student) =
        dao.update(student.toEntity())

    override suspend fun delete(student: Student) =
        dao.delete(student.toEntity())
}
