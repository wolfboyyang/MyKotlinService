package io.github.youi1987.mykotlinservice

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch


//import com.example.servicelibrary.MyRemoteService
//import com.example.servicelibrary.MyService


class MainActivity : AppCompatActivity() {

    companion object {
        private val log_tag = "MainActivity"

        /**
         * Command to the service to register a client, receiving callbacks
         * from the service.  The Message's replyTo field must be a Messenger of
         * the client where callbacks should be sent.
         */
        val MSG_REGISTER_CLIENT = 1

        /**
         * Command to the service to unregister a client, ot stop receiving callbacks
         * from the service.  The Message's replyTo field must be a Messenger of
         * the client as previously given with MSG_REGISTER_CLIENT.
         */
        val MSG_UNREGISTER_CLIENT = 2

        /**
         * Command to service to set a new value.  This can be sent to the
         * service to supply a new value, and will be sent by the service to
         * any registered clients with the new value.
         */
        val MSG_SET_VALUE = 3

        val MSG_PING = 4

        val pingList by lazy {
            listOf("127.0.0.1",
                    "www.baidu.com",
                    "www.bing.com",
                    "www.google.com")
        }
    }

    //var mBoundService: MyService? = null
    var mIsBound = false

    var mIsBoundRemote = false

    val myRemoteServiceIntent: Intent  by lazy {
        intent = Intent("com.example.servicelibrary.MyRemoteService.BIND")
        intent.`package` = "io.github.youi1987.mykotlinservice.services"
        //val intent = Intent()
        //intent.component = ComponentName("io.github.youi1987.mykotlinservice.services",
        //        "com.example.servicelibrary.MyRemoteService")
        intent
    }


    /** Messenger for communicating with service. */
    var mRemoteService: Messenger? = null
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    val mMessenger = Messenger(incomingHandler)

    val myMessageBundle = Bundle()

    /**
     * Handler of incoming messages from service.
     */
    object incomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_SET_VALUE ->
                    Log.i(log_tag, "Received from service: ${msg.arg1}")
                else -> super.handleMessage(msg)
            }
        }
    }

//    val mConnection = object : ServiceConnection {
//        override fun onServiceDisconnected(name: ComponentName?) {
//            mBoundService = null
//            Toast.makeText(this@MainActivity, R.string.local_service_stopped, Toast.LENGTH_SHORT).show()
//        }
//
//        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            mBoundService = (service as MyService.MyBinder).getService()
//            Toast.makeText(this@MainActivity, R.string.local_service_connected, Toast.LENGTH_SHORT).show()
//        }
//    }

    val mRemoteConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            mRemoteService = null
            Log.i(log_tag, "Service Disconnected")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mRemoteService = Messenger(service)
            Log.i(log_tag, "Service Connected")
            try {
                var myMessage = Message.obtain()
                myMessage.what = MSG_REGISTER_CLIENT
                myMessage.replyTo = mMessenger
                mRemoteService?.send(myMessage)

                myMessage = Message.obtain()
                myMessage.what = MSG_SET_VALUE
                myMessage.arg1 = 100
                mRemoteService?.send(myMessage)
            } catch (e: RemoteException) {

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.sendButton -> {
                Log.i(log_tag, "send button clicked")
                val intent = Intent()
                intent.component = ComponentName("io.github.youi1987.mykotlinservice.services",
                        "com.example.servicelibrary.MyRemoteService")
                sendBroadcast(intent, "com.example.servicelibrary.MyRemoteService.START")
            }
            R.id.startButton -> {
                Log.i(log_tag, "bind button clicked")
                val intent = Intent()
                intent.component = ComponentName("io.github.youi1987.mykotlinservice.services",
                        "com.example.servicelibrary.MyRemoteService")
                startService(intent)
            }
            R.id.stopButton -> {
                Log.i(log_tag, "unbind button clicked")
                stopService(myRemoteServiceIntent)
            }
            R.id.bindRemoteButton -> {
                Log.i(log_tag, "bind remote button clicked")
                bindMyRemoteService()
            }
            R.id.unbindRemoteButton -> {
                Log.i(log_tag, "unbind remote button clicked")
                unbindMyRemoteService()
            }
            R.id.pingButton -> {
                Log.i(log_tag, "ping in RemoteService")
                doPing()
            }
        }
    }

    fun doPing() {
        pingList.forEach {
            launch(CommonPool) {
                try {
                    val myMessage = Message.obtain()
                    myMessageBundle.putString("address", it)
                    myMessage.what = MSG_PING
                    myMessage.obj = myMessageBundle
                    mRemoteService?.send(myMessage)
                } catch (e: RemoteException) {
                    Log.i(log_tag, "cannot connect remote service" + e.message)
                }
            }
        }
    }

//    fun bindMyService() {
//        bindService(Intent(this,
//                MyService::class.java), mConnection, Context.BIND_AUTO_CREATE)
//        mIsBound = true
//    }
//
//    fun unbindMyService() {
//        if (mIsBound) {
//            unbindService(mConnection)
//            mIsBound = false
//        }
//    }

    fun bindMyRemoteService() {
        val packageManager = packageManager
        val activities = packageManager.queryIntentServices(myRemoteServiceIntent,
                PackageManager.MATCH_DEFAULT_ONLY)
        val isIntentSafe = activities.size > 0
        println("MyRemoteService Available?$isIntentSafe")
        if (isIntentSafe) {
            bindService(myRemoteServiceIntent, mRemoteConnection, Context.BIND_AUTO_CREATE)
            mIsBoundRemote = true
        }
    }

    fun unbindMyRemoteService() {
        if (mIsBoundRemote) {
            try {
                val msg = Message.obtain(null,
                        MSG_UNREGISTER_CLIENT)
                msg.replyTo = mMessenger
                mRemoteService?.send(msg)
            } catch (e: RemoteException) {

            }
            unbindService(mRemoteConnection)
            Log.i(log_tag, "unbinding remote service")
            mIsBoundRemote = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindMyRemoteService()
    }

}
