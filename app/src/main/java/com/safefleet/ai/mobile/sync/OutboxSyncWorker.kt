package com.safefleet.ai.mobile.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * M0 establishes the WorkManager boundary. M4 replaces this no-op worker body with
 * authenticated outbox batching and ACCEPTED/DUPLICATE/REJECTED reconciliation.
 */
class OutboxSyncWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result = Result.success()
}
