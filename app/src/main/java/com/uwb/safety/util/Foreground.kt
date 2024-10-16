package com.uwb.safety.util

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.uwb.safety.R
import com.uwb.safety.src.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



class Foreground : Service() {

    val CHANNEL_ID = "fg"
    val NOTIFICATION_ID = 1
    private val binder = LocalBinder()

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job) // Use Dispatchers.IO for background tasks

    // Binder 클래스 정의
    inner class LocalBinder : Binder() {
        fun getService(): Foreground = this@Foreground
    }

    fun createNotificationChannel(){
        val serviceChannel = NotificationChannel(CHANNEL_ID, "FOREGROUND", NotificationManager.IMPORTANCE_DEFAULT)
        val manager = getSystemService(NotificationManager::class.java)

        manager.createNotificationChannel(serviceChannel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        createNotificationChannel()
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,0,intent, PendingIntent.FLAG_IMMUTABLE)
        val manager = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("UWB Safty Running")
            .setSmallIcon(R.drawable.white_beacon)
            .setContentText("진행상황 : 0")
            .setProgress(1000,0,false)
            .setContentIntent(pendingIntent)
            .setOngoing(true)

        startForeground(NOTIFICATION_ID, notification.build())

        // Launch the coroutine
        coroutineScope.launch {
            for (i in 0 until 1000) {
                notification.setContentText("진행상황 : $i").setProgress(1000, i, false)
                delay(1000) // Suspend function to wait for 1 second
                manager.notify(NOTIFICATION_ID, notification.build())

                Log.i("thread", "COUNT : $i")
            }

            // 작업이 완료된 후 알림 업데이트
            notification.setContentText("작업 완료")
                .setProgress(0, 0, false)  // 진행바 제거
            manager.notify(NOTIFICATION_ID, notification.build())
        }

        //runBackground()

        return super.onStartCommand(intent, flags, startId)
    }

    fun runBackground(){

    }

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }

    // Foreground에서 실행할 메서드
    fun startScanning() {
        Log.d("Foreground", "UWB 스캐닝 시작")
        //uwbManager.startDeviceScanning(this) // 비콘 스캐닝 시작
    }

    fun stopScanning() {
        Log.d("Foreground", "UWB 스캐닝 종료")
        // 스캐닝 종료 코드
        //uwbManager.stopDeviceScanning()
    }
}