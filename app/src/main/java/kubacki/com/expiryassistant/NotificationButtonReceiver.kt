package kubacki.com.expiryassistant

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.provider.BaseColumns
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import android.widget.Toast

class NotificationButtonReceiver : BroadcastReceiver() {

    lateinit var dbHelper : ProductsDbHelper

    override fun onReceive(p0: Context?, p1: Intent?) {

        // debug
        Log.d("Receiver", "Broadcast received")

        Log.d("Receiver", "rowID:" + p1?.getLongExtra("rowID", -1))

        // get Extras
        val rowID = p1?.getLongExtra("rowID", -1)

        // Cancel notification
        val notificationManager = p0?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.cancel(rowID?.toInt()!!)

        // get DB helper
        dbHelper = ProductsDbHelper(p0)

        // Start async
        val asyncTask = Task(p1, dbHelper)
        asyncTask.execute(rowID)

    }

    private class Task(private val intent: Intent, private val db: ProductsDbHelper) : AsyncTask<Long, Int, Void>() {


        override fun doInBackground(vararg p0: Long?): Void? {

            Log.d("Async", "Async TASK executed")

            // TODO: List of products should be updated when app is running or sth else to do here :)

            /*
            Delete product entry from database
             */

            // Get writable database
            val dbDeletor = db.writableDatabase

            // Delete row
            val selection = "${BaseColumns._ID} = ?"

            dbDeletor.delete(ProductsEntry.TABLE_NAME, selection, arrayOf(p0[0].toString()))

            return null

        }

    }
}
