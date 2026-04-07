package com.expensetracker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.expensetracker.data.repository.CurrencyRepository
import com.expensetracker.data.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class CurrencyRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val currencyRepository: CurrencyRepository,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "currency_refresh_work"
        const val CHANNEL_ID = "currency_updates"
        const val CHANNEL_NAME = "Currency Rate Updates"
        const val NOTIFICATION_ID = 1001

        fun buildRequest(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<CurrencyRefreshWorker>(
                repeatInterval = 6,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build()
    }

    override suspend fun doWork(): Result {
        return try {
            currencyRepository.refreshRates()

            val notificationsEnabled = settingsRepository.notificationsEnabled.first()
            if (notificationsEnabled && currencyRepository.rates.value.isNotEmpty()) {
                showRatesUpdatedNotification()
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun showRatesUpdatedNotification() {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            )
        }

        val rates = currencyRepository.rates.value
        val ratesSummary = rates.joinToString(" · ") { "${it.abbreviation}: ${String.format("%.4f", it.rate)}" }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Exchange rates updated")
            .setContentText(ratesSummary)
            .setStyle(NotificationCompat.BigTextStyle().bigText(ratesSummary))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
