package com.safefleet.ai.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [OutboxEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class SafeFleetDatabase : RoomDatabase() {
    abstract fun outboxDao(): OutboxDao
}
