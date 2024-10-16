package com.uwb.safeguard.util

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log

class Alarm (private val con : Context){
    val dist = 0
    private val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    private val ringtone = RingtoneManager.getRingtone(con, notification)

    // 진동 객체 얻기
    private val vibratorManager = con.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
    private val vibrator = vibratorManager.defaultVibrator
    fun ringAlarm()   {
        Log.i("Alarm","Alarm ringing")

        //ringtone.play()
        vibrator.vibrate(
            VibrationEffect.createWaveform(
                longArrayOf(300, 1000, 300, 2000),
                intArrayOf(0, 50, 0, 200),
                0
            )
        )
    }

    fun stopAlarm(){
        //ringtone.stop()
        vibrator.cancel()
    }
}