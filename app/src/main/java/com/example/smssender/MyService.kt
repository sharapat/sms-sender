package com.example.smssender

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log

class MyService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val start = intent?.getIntExtra("start", 0)
        val end = intent?.getIntExtra("end", 0)
        val code = intent?.getStringExtra("code")
        val preCode = intent?.getStringExtra("preCode")
        if (start != null && end!= null) {
            for (i in start..end) {
                Handler().postDelayed({
                    try {
                        val sms = SmsManager.getDefault()
                        val phone = "+998$code$preCode${"%04d".format(i)}"
                        val text = intent.getStringExtra("text")
                        val parts = sms.divideMessage(text)
                        sms.sendMultipartTextMessage(phone, null, parts, null, null)
                        Log.d("kettime", "$phone $text")
                    } catch (e: Exception) {
                        Log.d("kettime", e.localizedMessage!!)
                    }
                }, 3000)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}