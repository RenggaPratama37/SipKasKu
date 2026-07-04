package com.renium.sipkasku.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.renium.sipkasku.R

object NotificationHelper {

    private const val CHANNEL_ID_SUCCESS = "recurring_success"
    private const val CHANNEL_ID_FAILED = "recurring_failed"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        val successChannel = NotificationChannel(
            CHANNEL_ID_SUCCESS,
            "Auto-Recurring transaction success",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Recurring transaction notification has executed"
        }

        val failedChannel = NotificationChannel(
            CHANNEL_ID_FAILED,
            "Auto-Recurring transaction failed",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Recurring transaction notification failed due to insufficient balance"
        }

        manager.createNotificationChannel(successChannel)
        manager.createNotificationChannel(failedChannel)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun notifySuccess(context: Context, title: String, amount: Double) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SUCCESS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Auto-Recurring transaction success")
            .setContentText("$title — ${formatRupiah(amount)} has added to transaction")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(title.hashCode(), notification)
        } catch (e: SecurityException) {
            // Permission is granted by user
        }
    }

    fun notifyFailed(context: Context, title: String, amount: Double, pocketName: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_FAILED)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("⚠️ Auto-Recurring transaction failed")
            .setContentText("$title — $pocketName balance is not enough for ${formatRupiah(amount)}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(" The plan \"$title\" of ${formatRupiah(amount)} could not be executed because the pocket balance of $pocketName was insufficient.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(title.hashCode() + 1, notification)
        } catch (e: SecurityException) {
            // Permission is granted by user
        }
    }
}
