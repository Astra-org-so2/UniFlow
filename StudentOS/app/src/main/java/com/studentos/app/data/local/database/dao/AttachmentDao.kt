package com.studentos.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.studentos.app.data.local.database.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {

    @Query("SELECT * FROM attachments ORDER BY id DESC")
    fun getAll(): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE related_entity_type = :type AND related_entity_id = :entityId")
    fun getByEntity(type: String, entityId: Long): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE related_entity_type = :type AND related_entity_id = :entityId")
    suspend fun getByEntityOnce(type: String, entityId: Long): List<AttachmentEntity>

    @Query("SELECT * FROM attachments WHERE id = :id")
    suspend fun getById(id: Long): AttachmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: AttachmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attachments: List<AttachmentEntity>)

    @Delete
    suspend fun delete(attachment: AttachmentEntity)

    @Query("DELETE FROM attachments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM attachments WHERE related_entity_type = :type AND related_entity_id = :entityId")
    suspend fun deleteByEntity(type: String, entityId: Long)

    @Query("DELETE FROM attachments")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM attachments")
    suspend fun count(): Int
}
