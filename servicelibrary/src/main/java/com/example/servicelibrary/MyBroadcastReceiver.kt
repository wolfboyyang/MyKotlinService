package com.example.servicelibrary

import android.widget.Toast
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log


/**
 * Created by youi1 on 2017/4/9.
 */
class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sb = StringBuilder()
        sb.append("Action: " + intent.action + "\n")
        sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n")
        val log = sb.toString()
        Log.d(TAG, log)
        Toast.makeText(context, log, Toast.LENGTH_LONG).show()

        val serviceIntent = Intent(context, MyRemoteService::class.java)
        context.startService(serviceIntent)
    }

    companion object {
        private val TAG = "MyBroadcastReceiver"
    }
}