package com.safefleet.ai.mobile.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {
    @Query("SELECT * FROM mobile_outbox ORDER BY created_at_epoch_ms ASC")
    fun observeAll(): Flow<List<OutboxEntity>>

    @Query("SELECT * FROM mobile_outbox WHERE state = 'PENDING' ORDER BY created_at_epoch_ms ASC LIMIT :limit")
    suspend fun pending(limit: Int): List<OutboxEntity>

    @Upsert
    suspend fun upsert(item: OutboxEntity)

    @Delete
    suspend fun delete(item: OutboxEntity)
}
