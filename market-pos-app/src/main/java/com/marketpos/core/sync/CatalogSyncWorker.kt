package com.marketpos.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.marketpos.domain.repository.CatalogSyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class CatalogSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val catalogSyncRepository: CatalogSyncRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return catalogSyncRepository.flushPending()
            .fold(
                onSuccess = { Result.success(workDataOf("processed" to it.processedCount)) },
                onFailure = {
                    val message = it.message.orEmpty()
                    if (message.contains("aktivasyon", ignoreCase = true) ||
                        message.contains("gecersiz", ignoreCase = true) ||
                        message.contains("geçersiz", ignoreCase = true)
                    ) {
                        Result.success()
                    } else {
                        Result.retry()
                    }
                }
            )
    }

    companion object {
        private const val UNIQUE_NAME = "catalog_sync_worker"

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<CatalogSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(UNIQUE_NAME, ExistingWorkPolicy.KEEP, request)
        }
    }
}
