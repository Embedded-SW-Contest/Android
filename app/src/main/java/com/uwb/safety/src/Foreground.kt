package com.uwb.safety.src

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.uwb.safety.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Foreground : Service() {

    val CHANNEL_ID = "fg"
    val NOTIFICATION_ID = 1

    fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val serviceChannel = NotificationChannel(CHANNEL_ID, "FOREGROUND", NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        createNotificationChannel()
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,0,intent, PendingIntent.FLAG_IMMUTABLE)
        val manager = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setSmallIcon(R.drawable.white_beacon)
            .setContentText("진행상황 : 0")
            .setProgress(1000,0,false)
            .setContentIntent(pendingIntent)


        startForeground(NOTIFICATION_ID, notification.build())

        GlobalScope.launch {
            for (i in 0 until 1000) {
                notification.setContentText("진행상황 : $i").setProgress(1000,i,false)
                delay(1000)
                manager.notify(NOTIFICATION_ID, notification.build())

                Log.d("thread", "COUNT : $i")
            }
        }

        //runBackground()

        return super.onStartCommand(intent, flags, startId)
    }

    fun runBackground(){

    }

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }
}