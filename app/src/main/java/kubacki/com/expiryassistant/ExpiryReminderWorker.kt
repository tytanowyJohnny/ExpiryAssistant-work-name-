package kubacki.com.expiryassistant

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class ExpiryReminderWorker(context : Context, params : WorkerParameters) : Worker(context, params){

    private val CHANNEL_ID = "ExpiryAssistantChannel"

    override fun doWork(): Result {

        /*
        Create new notification about product which is about to expire
         */

        // Get product name and rowID
        val pName = inputData.getString("pName")
        val rowID = inputData.getLong("rowID", -1)

        Log.d("Worker", "rowID: $rowID")

        // Set tap action
        val intent = Intent(applicationContext, MainActivity::class.java).apply {

            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)

        // Set action button "Delete product"
        val deleteIntent = Intent(applicationContext, NotificationButtonReceiver::class.java).apply {

            Log.d("deleteIntent", "rowID: $rowID")
            putExtra("rowID", rowID)
        }

        val pendingDeleteIntent = PendingIntent.getBroadcast(applicationContext, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Build notification
        val mBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_alarm_black_48)
                .setContentTitle("Expiry Assistant")
                .setContentText("Product: $pName will expire today!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.baseline_clear_black_24, "Delete product entry", pendingDeleteIntent)

        with(NotificationManagerCompat.from(applicationContext)) {

            notify(rowID.toInt(), mBuilder.build())

        }

        return Result.SUCCESS
    }


}