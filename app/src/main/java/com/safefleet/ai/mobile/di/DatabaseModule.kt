package com.safefleet.ai.mobile.di

import android.content.Context
import androidx.room.Room
import com.safefleet.ai.mobile.data.local.OutboxDao
import com.safefleet.ai.mobile.data.local.SafeFleetDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SafeFleetDatabase =
        Room.databaseBuilder(
            context,
            SafeFleetDatabase::class.java,
            "safefleet-mobile.db",
        ).build()

    @Provides
    fun provideOutboxDao(database: SafeFleetDatabase): OutboxDao = database.outboxDao()
}
