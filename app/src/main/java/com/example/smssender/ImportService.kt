package com.example.smssender

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log

class ImportService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val numbers = intent?.getStringArrayExtra("numbers")
        val text = intent?.getStringExtra("text")
        Log.d("protsess", "onStartCommand isledi")
        numbers?.forEach {
            Handler().postDelayed({
                Log.d("protsess", "Handlerge kirdi")
                try {
                    val sms = SmsManager.getDefault()
                    val parts = sms.divideMessage(text)
                    sms.sendMultipartTextMessage(it, null, parts, null, null)
                    Log.d("protsess", "jiberdi")
                    Log.d("kettime", "$it $text")
                } catch (e: Exception) {
                    Log.d("kettime", e.localizedMessage!!)
                }
            }, 3000)
        }

        return super.onStartCommand(intent, flags, startId)
    }
}