package com.studentos.app.data.repository

import com.studentos.app.data.local.database.dao.AttendanceDao
import com.studentos.app.data.local.database.toDomain
import com.studentos.app.data.local.database.toEntity
import com.studentos.app.domain.model.AttendanceRecord
import com.studentos.app.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepositoryImpl @Inject constructor(
    private val dao: AttendanceDao
) : AttendanceRepository {

    override fun getAll(): Flow<List<AttendanceRecord>> = dao.getAll().map { list -> list.map { it.toDomain() } }
    override suspend fun getAllOnce(): List<AttendanceRecord> = dao.getAllOnce().map { it.toDomain() }
    override suspend fun getById(id: Long): AttendanceRecord? = dao.getById(id)?.toDomain()
    override fun getBySubject(subjectId: Long): Flow<List<AttendanceRecord>> = dao.getBySubject(subjectId).map { list -> list.map { it.toDomain() } }
    override suspend fun getBySubjectOnce(subjectId: Long): List<AttendanceRecord> = dao.getBySubjectOnce(subjectId).map { it.toDomain() }
    override fun getByDate(date: LocalDate): Flow<List<AttendanceRecord>> = dao.getByDate(date.toString()).map { list -> list.map { it.toDomain() } }
    override suspend fun getByScheduleAndDate(scheduleItemId: Long, date: LocalDate): AttendanceRecord? = dao.getByScheduleAndDate(scheduleItemId, date.toString())?.toDomain()
    override suspend fun getTotalClasses(subjectId: Long): Int = dao.getTotalClasses(subjectId)
    override suspend fun getPresentCount(subjectId: Long): Int = dao.getPresentCount(subjectId)
    override suspend fun getAbsentCount(subjectId: Long): Int = dao.getAbsentCount(subjectId)
    override suspend fun getExcusedCount(subjectId: Long): Int = dao.getExcusedCount(subjectId)
    override suspend fun getLateCount(subjectId: Long): Int = dao.getLateCount(subjectId)
    override suspend fun getAttendedCount(subjectId: Long): Int = dao.getAttendedCount(subjectId)
    override suspend fun save(record: AttendanceRecord): Long = dao.insert(record.toEntity())
    override suspend fun update(record: AttendanceRecord) = dao.update(record.toEntity())
    override suspend fun delete(record: AttendanceRecord) = dao.delete(record.toEntity())
    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
