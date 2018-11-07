package kubacki.com.expiryassistant

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessaging : FirebaseMessagingService() {

    private val TAG = "FIREBASE"

    override fun onMessageReceived(p0: RemoteMessage?) {

        Log.i(TAG, "From: " + p0?.from)
        Log.i(TAG, "Message: " + p0?.notification)
        Log.i(TAG, "Body: " + p0?.notification?.body)

    }

    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)

        Log.i(TAG, "TOKEN: $p0")

    }

}
