package com.studentos.app.data.repository

import com.studentos.app.data.local.database.dao.NoteDao
import com.studentos.app.data.local.database.toDomain
import com.studentos.app.data.local.database.toEntity
import com.studentos.app.domain.model.Note
import com.studentos.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDao
) : NoteRepository {

    override fun getAll(): Flow<List<Note>> = dao.getAll().map { list -> list.map { it.toDomain() } }
    override suspend fun getAllOnce(): List<Note> = dao.getAllOnce().map { it.toDomain() }
    override suspend fun getById(id: Long): Note? = dao.getById(id)?.toDomain()
    override fun getByIdFlow(id: Long): Flow<Note?> = dao.getByIdFlow(id).map { it?.toDomain() }
    override fun getBySubject(subjectId: Long): Flow<List<Note>> = dao.getBySubject(subjectId).map { list -> list.map { it.toDomain() } }
    override fun search(query: String): Flow<List<Note>> = dao.search(query).map { list -> list.map { it.toDomain() } }

    override suspend fun save(note: Note): Long {
        val now = Instant.now()
        return if (note.id == 0L) {
            dao.insert(note.toEntity().copy(createdAt = now, updatedAt = now))
        } else {
            dao.insert(note.toEntity().copy(updatedAt = now))
        }
    }

    override suspend fun update(note: Note) {
        dao.update(note.toEntity().copy(updatedAt = Instant.now()))
    }

    override suspend fun delete(note: Note) = dao.delete(note.toEntity())
    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
