package com.studentos.app.data.repository

import com.studentos.app.data.local.database.dao.ScheduleDao
import com.studentos.app.data.local.database.toDomain
import com.studentos.app.data.local.database.toEntity
import com.studentos.app.domain.model.ScheduleItem
import com.studentos.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val dao: ScheduleDao
) : ScheduleRepository {

    override fun getAll(): Flow<List<ScheduleItem>> = dao.getAll().map { list -> list.map { it.toDomain() } }
    override suspend fun getAllOnce(): List<ScheduleItem> = dao.getAllOnce().map { it.toDomain() }
    override suspend fun getById(id: Long): ScheduleItem? = dao.getById(id)?.toDomain()
    override fun getByDay(dayOfWeek: Int): Flow<List<ScheduleItem>> = dao.getByDay(dayOfWeek).map { list -> list.map { it.toDomain() } }
    override fun getBySubject(subjectId: Long): Flow<List<ScheduleItem>> = dao.getBySubject(subjectId).map { list -> list.map { it.toDomain() } }
    override fun getByTeacher(teacherId: Long): Flow<List<ScheduleItem>> = dao.getByTeacher(teacherId).map { list -> list.map { it.toDomain() } }

    override fun getForDate(date: LocalDate): Flow<List<ScheduleItem>> {
        val dayOfWeek = date.dayOfWeek.value
        return dao.getForDate(dayOfWeek, date.toString()).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getForDateOnce(date: LocalDate): List<ScheduleItem> {
        val dayOfWeek = date.dayOfWeek.value
        return dao.getForDateOnce(dayOfWeek, date.toString()).map { it.toDomain() }
    }

    override fun getForCurrentWeek(): Flow<List<ScheduleItem>> {
        val today = LocalDate.now()
        return dao.getForWeek(today.toString()).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun save(item: ScheduleItem): Long = dao.insert(item.toEntity())
    override suspend fun update(item: ScheduleItem) = dao.update(item.toEntity())
    override suspend fun delete(item: ScheduleItem) = dao.delete(item.toEntity())
    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    override suspend fun duplicate(id: Long): Long? {
        val original = dao.getById(id) ?: return null
        val copy = original.copy(id = 0)
        return dao.insert(copy)
    }
}
