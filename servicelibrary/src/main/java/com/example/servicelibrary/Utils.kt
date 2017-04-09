package com.example.servicelibrary

import android.util.Log
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

/**
 * Created by youi1 on 2017/4/8.
 */
val LOG_TAG = "Utils"

fun ping(address: String) {
    launch(CommonPool) {
        Log.i(LOG_TAG, "Ping $address in Thread:${Thread.currentThread().name}")
        var isReachable = false
        val socketAddress = InetSocketAddress(address, 80)
        val time = measureNanoTime {
            isReachable = isReachable(socketAddress, 2000)
        }
        Log.i(LOG_TAG,
                if (isReachable)
                    "$address is reachable in ${time * 0.000001} ms"
                else
                    "ping $address  timeout")
    }
}

// Any Open port on other machine
// openPort =  22 - ssh, 80 or 443 - webserver, 25 - mailserver etc.
private inline fun isReachable(endpoint: InetSocketAddress, timeOutMillis: Int) =
        try {
            Socket().use {
                soc ->
                soc.connect(endpoint, timeOutMillis)
            }
            true
        } catch (ex: IOException) {
            false
        }
