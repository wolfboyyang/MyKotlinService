package com.example.servicelibrary

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast


/**
 * Created by youi1 on 2017/4/6.
 */
class MyService : Service() {

    private var mNM: NotificationManager? = null

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private val NOTIFICATION = R.string.local_service_started

    var clientIntent: Intent? = null

    class WebLogBinder : Binder() {
        fun getService(): MyService = webLogService
    }

    override fun onCreate() {
        Log.i(log_tag, "onCreate")
        mNM = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(log_tag, "Received start id  $startId : $intent")
        return START_NOT_STICKY;
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.i(log_tag, "onBind")
        clientIntent = intent
        return WebLogBinder()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(log_tag, "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.i(log_tag, "onDestroy")
        // Cancel the persistent notification.
        mNM?.cancel(NOTIFICATION)

        // Tell the user we stopped.
        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show()
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private val mBinder = WebLogBinder()

    /**
     * Show a notification while this service is running.
     */
    private fun showNotification() {
        Log.i(log_tag, "showNotification")
        // In this sample, we'll use the same text for the ticker and the expanded notification
        val text = getText(R.string.local_service_started)

        // The PendingIntent to launch our activity if the user selects this notification
        val contentIntent = PendingIntent.getActivity(this, 0,
                clientIntent, 0)

        // Set the info for the views that show in the notification panel.
        val notification = Notification.Builder(this)
                .setSmallIcon(R.drawable.star)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.local_service_label))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build()

        // Send the notification.
        mNM?.notify(NOTIFICATION, notification)
    }

    companion object {
        private val log_tag = "MyService"
        private val webLogService: MyService by lazy {
            MyService()
        }
    }

}