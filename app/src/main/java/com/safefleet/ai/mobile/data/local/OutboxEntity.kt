package com.safefleet.ai.mobile.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mobile_outbox")
data class OutboxEntity(
    @PrimaryKey
    val id: String,
    val type: String,
    val payload: String,
    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long,
    @ColumnInfo(name = "attempt_count")
    val attemptCount: Int = 0,
    val state: String = "PENDING",
)
