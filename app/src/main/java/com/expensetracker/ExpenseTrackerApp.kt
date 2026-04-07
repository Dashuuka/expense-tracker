package com.expensetracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.expensetracker.worker.BudgetAlertWorker
import com.expensetracker.worker.CurrencyRefreshWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import androidx.work.PeriodicWorkRequestBuilder

@HiltAndroidApp
class ExpenseTrackerApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        schedulePeriodicalWork()
    }

    private fun schedulePeriodicalWork() {
        val workManager = WorkManager.getInstance(this)

        // Currency rates refresh every 6 hours
        workManager.enqueueUniquePeriodicWork(
            CurrencyRefreshWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            CurrencyRefreshWorker.buildRequest()
        )

        // Budget alerts check every day
        workManager.enqueueUniquePeriodicWork(
            BudgetAlertWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<BudgetAlertWorker>(1, TimeUnit.DAYS).build()
        )
    }
}
