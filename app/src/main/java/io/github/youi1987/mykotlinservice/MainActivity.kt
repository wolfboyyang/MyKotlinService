package io.github.youi1987.mykotlinservice

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import android.content.Intent
import android.os.*
import android.util.Log
import android.os.Messenger
import com.example.servicelibrary.MyRemoteService
import com.example.servicelibrary.MyService


class MainActivity : AppCompatActivity() {

    companion object {
        private val log_tag = "MainActivity"
    }

    var mBoundService: MyService? = null
    var mIsBound = false

    var mIsBoundRemote = false

    /** Messenger for communicating with service. */
    var mRemoteService: Messenger? = null
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    val mMessenger = Messenger(incomingHandler)

    /**
     * Handler of incoming messages from service.
     */
    object incomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MyRemoteService.MSG_SET_VALUE ->
                    Log.i(log_tag, "Received from service: ${msg.arg1}")
                else -> super.handleMessage(msg)
            }
        }
    }

    val mConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            mBoundService = null
            Toast.makeText(this@MainActivity, R.string.local_service_stopped, Toast.LENGTH_SHORT).show()
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mBoundService = (service as MyService.WebLogBinder).getService()
            Toast.makeText(this@MainActivity, R.string.local_service_connected, Toast.LENGTH_SHORT).show()
        }
    }

    val mRemoteConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            mRemoteService = null
            Toast.makeText(this@MainActivity, R.string.local_service_stopped, Toast.LENGTH_SHORT).show()
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mRemoteService = Messenger(service)
            Toast.makeText(this@MainActivity, R.string.local_service_connected, Toast.LENGTH_SHORT).show()
            try {
                var msg = Message.obtain(null, MyRemoteService.MSG_REGISTER_CLIENT)
                msg.replyTo = mMessenger
                mRemoteService?.send(msg)

                msg = Message.obtain(null, MyRemoteService.MSG_SET_VALUE, 100, 0)
                mRemoteService?.send(msg)
            } catch (e: RemoteException) {

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClick(view: View) {
        when(view.id) {
            R.id.bindButton -> {
                Log.i(log_tag, "bind button clicked")
                bindWebLogService()
            }
            R.id.unbindButton -> {
                Log.i(log_tag, "unbind button clicked")
                unbindWebLogService()
            }
            R.id.bindRemoteButton -> {
                Log.i(log_tag, "bind remote button clicked")
                bindWebLogRemoteService()
            }
            R.id.unbindRemoteButton -> {
                Log.i(log_tag, "unbind remote button clicked")
                unbindWebLogRemoteService()
            }
        }
    }

    fun bindWebLogService() {
        bindService(Intent(this,
                MyService::class.java), mConnection, Context.BIND_AUTO_CREATE)
        mIsBound = true
    }

    fun unbindWebLogService() {
        if(mIsBound) {
            unbindService(mConnection)
            mIsBound = false
        }
    }

    fun bindWebLogRemoteService() {
        bindService(Intent(this,
                MyRemoteService::class.java), mRemoteConnection, Context.BIND_AUTO_CREATE)
        mIsBoundRemote = true
    }

    fun unbindWebLogRemoteService() {
        if(mIsBoundRemote) {
            try {
                val msg = Message.obtain(null,
                        MyRemoteService.MSG_UNREGISTER_CLIENT)
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
        unbindWebLogService()
    }

}
