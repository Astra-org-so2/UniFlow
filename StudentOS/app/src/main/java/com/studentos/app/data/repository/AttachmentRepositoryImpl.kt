package com.studentos.app.data.repository

import com.studentos.app.data.local.database.dao.AttachmentDao
import com.studentos.app.data.local.database.toDomain
import com.studentos.app.data.local.database.toEntity
import com.studentos.app.domain.model.Attachment
import com.studentos.app.domain.repository.AttachmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttachmentRepositoryImpl @Inject constructor(
    private val dao: AttachmentDao
) : AttachmentRepository {

    override fun getByEntity(type: String, entityId: Long): Flow<List<Attachment>> = dao.getByEntity(type, entityId).map { list -> list.map { it.toDomain() } }
    override suspend fun getByEntityOnce(type: String, entityId: Long): List<Attachment> = dao.getByEntityOnce(type, entityId).map { it.toDomain() }
    override suspend fun getById(id: Long): Attachment? = dao.getById(id)?.toDomain()
    override suspend fun save(attachment: Attachment): Long = dao.insert(attachment.toEntity())
    override suspend fun delete(attachment: Attachment) = dao.delete(attachment.toEntity())
    override suspend fun deleteByEntity(type: String, entityId: Long) = dao.deleteByEntity(type, entityId)
}
