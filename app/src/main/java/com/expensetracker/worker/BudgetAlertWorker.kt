package com.expensetracker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.expensetracker.data.repository.BudgetRepository
import com.expensetracker.data.repository.CategoryRepository
import com.expensetracker.data.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class BudgetAlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "budget_alert_work"
        const val CHANNEL_ID = "budget_alerts"
        const val CHANNEL_NAME = "Budget Alerts"
    }

    override suspend fun doWork(): Result {
        val notificationsEnabled = settingsRepository.notificationsEnabled.first()
        if (!notificationsEnabled) return Result.success()

        return try {
            val budgets = budgetRepository.getCurrentMonthBudgets().first()
            val categories = categoryRepository.getAllCategories().first()
            val categoryMap = categories.associateBy { it.id }

            budgets.forEach { budget ->
                when {
                    budget.isOverBudget -> {
                        val catName = categoryMap[budget.categoryId]?.name ?: "Category"
                        showNotification(
                            id = budget.categoryId.toInt() + 2000,
                            title = "Budget exceeded! 🚨",
                            text = "$catName budget exceeded by ${String.format("%.2f", budget.spentAmount - budget.limitAmount)} BYN"
                        )
                    }
                    budget.progressFraction >= 0.9f -> {
                        val catName = categoryMap[budget.categoryId]?.name ?: "Category"
                        showNotification(
                            id = budget.categoryId.toInt() + 3000,
                            title = "Budget warning ⚠️",
                            text = "$catName: ${(budget.progressFraction * 100).toInt()}% of budget used"
                        )
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun showNotification(id: Int, title: String, text: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            )
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }
}
