package com.renium.sipkasku.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.renium.sipkasku.data.local.DatabaseProvider
import com.renium.sipkasku.data.local.RecurrenceFrequency
import com.renium.sipkasku.data.local.TransactionEntity
import com.renium.sipkasku.data.repository.PocketRepository
import com.renium.sipkasku.data.repository.RecurringRepository
import com.renium.sipkasku.data.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar

class RecurringWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {

        val db = DatabaseProvider.get(applicationContext)
        val transactionRepository = TransactionRepository(db.transactionDao())
        val recurringRepository = RecurringRepository(db.recurringDao())
        val pocketRepository = PocketRepository(db.pocketDao())

        try {
            val today = Calendar.getInstance()
            val currentDay = today.get(Calendar.DAY_OF_MONTH)
            val currentDayOfWeek = today.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 2=Monday, etc
            val currentHour = today.get(Calendar.HOUR_OF_DAY)

            val recurrings = recurringRepository.getAll().first()

            recurrings.filter { it.isActive }.forEach { r ->
                val lastRunCalendar = if (r.lastRun == 0L) {
                    Calendar.getInstance().apply { timeInMillis = 0 }
                } else {
                    Calendar.getInstance().apply { timeInMillis = r.lastRun }
                }

                val shouldRun = when (r.frequency) {
                    RecurrenceFrequency.DAILY.name -> {
                        // Run once per day (check if lastRun was on a different day)
                        val lastRunDay = lastRunCalendar.get(Calendar.DAY_OF_YEAR)
                        val todayDay = today.get(Calendar.DAY_OF_YEAR)
                        lastRunDay != todayDay
                    }

                    RecurrenceFrequency.WEEKLY.name -> {
                        // Run on specified day of week
                        val targetDayOfWeek = r.dayOfWeek ?: 2 // Default Monday if not set
                        val lastRunDayOfWeek = lastRunCalendar.get(Calendar.DAY_OF_WEEK)

                        // Convert: our 1=Monday -> Calendar.DAY_OF_WEEK (1=Sunday, 2=Monday)
                        val calendarDayOfWeek = if (targetDayOfWeek == 7) 1 else (targetDayOfWeek + 1)

                        currentDayOfWeek == calendarDayOfWeek && lastRunDayOfWeek != calendarDayOfWeek
                    }

                    RecurrenceFrequency.MONTHLY.name -> {
                        val lastRunMonth = lastRunCalendar.get(Calendar.MONTH)
                        val lastRunYear = lastRunCalendar.get(Calendar.YEAR)
                        val todayMonth = today.get(Calendar.MONTH)
                        val todayYear = today.get(Calendar.YEAR)

                        currentDay == r.dayOfMonth &&
                                !(lastRunMonth == todayMonth && lastRunYear == todayYear)
                    }

                    RecurrenceFrequency.END_OF_MONTH.name -> {
                        // Run on last day of month
                        val lastDayOfMonth = today.apply { set(Calendar.DATE, 1) }.let {
                            it.add(Calendar.MONTH, 1)
                            it.add(Calendar.DATE, -1)
                            it.get(Calendar.DAY_OF_MONTH)
                        }

                        val lastRunDay = lastRunCalendar.get(Calendar.DAY_OF_MONTH)
                        currentDay == lastDayOfMonth && lastRunDay != lastDayOfMonth
                    }

                    else -> false
                }

                if (shouldRun) {
                    val startOfDay = Calendar.getInstance().apply () {
                        set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0)
                        set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0)
                    }.timeInMillis

                    val endOfDay = Calendar.getInstance().apply() {
                        set(Calendar.HOUR_OF_DAY,23); set(Calendar.MINUTE,59)
                        set(Calendar.SECOND,59); set(Calendar.MILLISECOND,999)
                    }.timeInMillis

                    val alreadyRun = transactionRepository.countByRecurringAndDate(
                        recurringId = r.id,
                        startOfDay = startOfDay,
                        endOfDay = endOfDay
                    ) > 0

                    if (!alreadyRun) {
                        transactionRepository.insertTransaction(
                            TransactionEntity(
                                title = r.title,
                                amount = r.amount,
                                categoryId = r.categoryId,
                                isIncome = r.isIncome,
                                date = System.currentTimeMillis(),
                                pocketId = r.pocketId,
                                recurringId = r.id
                            )
                        )

                        r.pocketId?.let { pocketId ->
                            val delta = if (r.isIncome) r.amount else -r.amount
                            pocketRepository.adjustBalance(pocketId, delta)
                        }

                        recurringRepository.update(r.copy(lastRun = System.currentTimeMillis()))
                    }
                }
            }

        } catch (t: Throwable) {
            android.util.Log.e("RecurringWorker", "Error processing recurrings", t)
            return Result.failure()
        }

        return Result.success()
    }
}
