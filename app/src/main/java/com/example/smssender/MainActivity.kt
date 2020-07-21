package com.example.smssender

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.model.Document
import kotlinx.android.synthetic.main.activity_main.*
import java.util.prefs.Preferences

class MainActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE = 101
    }

    private lateinit var preferences : SharedPreferences

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hideKeyboard(this)
        preferences = getPreferences(Context.MODE_PRIVATE)
        askPermission()
        btnSend.setOnClickListener {
            val start = tvStart.text.toString().toInt()
            val end = tvEnd.text.toString().toInt()
            val code = tvCode.text.toString().toInt()
            val preCode = tvPreCode.text.toString().toInt()
            for (i in start..end) {
                sendSMS("%02d".format(code), "%03d".format(preCode), "%04d".format(i))
            }
        }
        btnImport.setOnClickListener {
            val intent = Intent(this, ImportActivity::class.java)
            startActivity(intent)
        }
    }

    private fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun enableButtons(isEnabled: Boolean) {
        btnSend.isEnabled = isEnabled
        btnImport.isEnabled = isEnabled
        tvInfo.visibility = View.GONE
        progress.visibility = View.GONE
    }

    private fun showMessage(msg: String) {
        tvInfo.text = msg
        tvInfo.visibility = View.VISIBLE
    }

    private fun askPermission() {
        if (PermissionUtils.askPermissions(this, PermissionUtils.SEND_SMS)) {
            btnSend.isEnabled = true
            check()
        } else {
            btnSend.isEnabled = false
        }
    }

    private fun sendSMS(code: String, preCode: String, number: String) {
        try {
            val sms = SmsManager.getDefault()
            val text = getMessage()
            val parts = sms.divideMessage(text)
            sms.sendMultipartTextMessage("+998$code$preCode$number", null, parts, null, null)
        } catch (e: Exception) {
            Log.d("kettime", e.localizedMessage!!)
        }
    }

    private fun getMessage() : String {
        return etText.text.toString()
    }

    private fun check() {
        progress.visibility = View.VISIBLE
        btnSend.isEnabled = false
        val mp : HashMap<String, Any> = HashMap()
        val imei = getIMEI()
        if (preferences.getBoolean("isRegistered", false)) {
            btnSend.isEnabled = false
            progress.visibility = View.VISIBLE
            if (preferences.getBoolean("isAllowed", false)) {
                btnSend.isEnabled = true
                progress.visibility = View.GONE
            } else {
                isAllowed(imei)
            }
        } else {
            mp["isAllowed"] = false
            mp["imei"] = imei
            db.collection("users").document(imei).set(mp)
                .addOnSuccessListener {
                    preferences.edit().putBoolean("isRegistered", true).apply()
                    btnSend.isEnabled = false
                    progress.visibility = View.GONE
                    showMessage("Administrator tárepinen ruxsat berilgennen keyin Refresh túymesin basıń yáki programmanı qayta iske túsiriń")
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
                }
        }

    }

    private fun isAllowed(imei: String) {
        db.collection("users").document(imei).get()
            .addOnSuccessListener { document ->
                if (document.getBoolean("isAllowed")!!) {
                    progress.visibility = View.GONE
                    btnSend.isEnabled = true
                    preferences.edit().putBoolean("isAllowed", true).apply()
                } else {
                    Toast.makeText(this, "Sizge administrator tarepinen ruxsat berilmegen!", Toast.LENGTH_LONG).show()
                    progress.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
            }
    }

    private fun getIMEI() : String {
        var IMEINumber = ""
        val telephonyManager =
            getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                REQUEST_CODE
            )
        }
        IMEINumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            telephonyManager.imei
        } else {
            telephonyManager.deviceId
        }
        return  IMEINumber
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_refresh -> {
                askPermission()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}