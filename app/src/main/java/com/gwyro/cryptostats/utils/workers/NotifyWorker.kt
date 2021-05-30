package com.gwyro.cryptostats.utils.workers

import android.app.Notification.DEFAULT_ALL
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result.failure
import androidx.work.ListenableWorker.Result.success
import androidx.work.WorkerParameters
import com.gwyro.cryptostats.MainActivity
import com.gwyro.cryptostats.R
import com.gwyro.cryptostats.data.db.UserCrypto
import com.gwyro.cryptostats.data.db.UserCryptoRepo
import com.gwyro.cryptostats.data.model.DataLunarItem
import com.gwyro.cryptostats.domain.usecase.UseCaseCryptoInfo
import com.gwyro.cryptostats.utils.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class NotifyWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val userCryptoRepo: UserCryptoRepo,
    private val useCaseCryptoInfo: UseCaseCryptoInfo
) : CoroutineWorker(context, params) {

    private val workerContext: Context = context
    private var userCryptoList = listOf<UserCrypto>()

    override suspend fun doWork(): Result {
        val repository = userCryptoRepo

        userCryptoList = repository.getAllCrypto()
        val stringBuilder = StringBuilder()
        for ((size, element) in userCryptoList.withIndex()) {
            if (element.isFavourite) {
                if (size == 0) {
                    stringBuilder.append(element.currency)
                } else {
                    stringBuilder.append("," + element.currency)
                }
            }
        }
        if (stringBuilder.toString().isNotEmpty()) {
            if (Utils.isNetworkAvailable(workerContext)) {
                when (val resultLunar =
                    useCaseCryptoInfo.getCryptoValueLunar(stringBuilder.toString())) {
                    is com.gwyro.cryptostats.utils.Result.Success -> {
                        val lista = resultLunar.data.data
                        for (el in lista) {
                            if (el.percent_change_24h > 5.0) {
                                sendNotification(el, true)
                            } else if (el.percent_change_24h < -5.0) {
                                sendNotification(el, false)
                            }
                        }
                    }
                    else -> {
                        return failure()
                    }
                }
            } else {
                return failure()
            }

        }
        return success()
    }

    private fun sendNotification(item: DataLunarItem, isIncrement: Boolean) {
        var name = ""
        var id = 0
        for (user in userCryptoList) {
            if (user.currency == item.symbol) {
                name = user.name
                id = user.id
                break
            }
        }
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(NOTIFICATION_ID, id)
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val titleNotification = workerContext.getString(R.string.title_notif)
        val increment =
            if (isIncrement) workerContext.getString(R.string.notif_increase) else workerContext.getString(
                R.string.notif_decrease
            )
        val sign = if (isIncrement) workerContext.getString(R.string.symbol_plus) else ""

        val subtitleNotification = workerContext.getString(R.string.notif_desc).replace(
            CRYPTO_NAME_PLACEHOLDER, name
        ).replace(
            SYMBOL_PLACEHOLDER, item.symbol
        ).replace(SIGN_PLACEHOLDER, sign).replace(INCREMENT_PLACEHOLDER, increment).replace(
            PERC_PLACEHOLDER, item.percent_change_24h.toString()
        )
        val pendingIntent = getActivity(applicationContext, 0, intent, 0)
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    workerContext.resources,
                    R.mipmap.ic_icon_tracker
                )
            )
            .setContentTitle(titleNotification).setContentText(subtitleNotification)
            .setDefaults(DEFAULT_ALL).setContentIntent(pendingIntent).setAutoCancel(true)

        notification.setSmallIcon(R.mipmap.ic_icon_tracker)
        notification.color = Color.WHITE
        notification.priority = PRIORITY_DEFAULT

        if (SDK_INT >= O) {
            notification.setChannelId(NOTIFICATION_CHANNEL)
            val channel =
                NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_NAME, IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(id, notification.build())
    }

    companion object {
        const val NOTIFICATION_ID = "appName_notification_id"
        const val NOTIFICATION_NAME = "appName"
        const val NOTIFICATION_CHANNEL = "appName_channel_01"
    }
}
