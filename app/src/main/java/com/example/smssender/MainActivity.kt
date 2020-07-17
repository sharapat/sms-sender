package com.example.smssender

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.View
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
        preferences = getPreferences(Context.MODE_PRIVATE)
        check()
    }

    private fun check() {
        progress.visibility = View.VISIBLE
        btnSend.isEnabled = false
        val mp : HashMap<String, Any> = HashMap()
        val imei = getIMEI()
        if (preferences.getBoolean("isRegistered", false)) {
            btnSend.isEnabled = false
            progress.visibility = View.VISIBLE
            isAllowed(imei)
        } else {
            mp["isAllowed"] = false
            mp["imei"] = imei
            db.collection("users").add(mp)
                .addOnSuccessListener {
                    preferences.edit().putBoolean("isRegistered", true).apply()
                    btnSend.isEnabled = false
                    progress.visibility = View.VISIBLE
                    Toast.makeText(this, "Administrator tarepinen ruxsat berilgennen keyin programmani qayta iske tusirin", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
                }
        }

    }

    private fun isAllowed(imei: String) {
        db.collection("users").whereEqualTo("imei", imei).get()
            .addOnSuccessListener {
                var document : DocumentSnapshot
                it.documents.forEach { doc ->
                    document = doc
                }
                if (document.getBoolean("isAllowed")!!) {
                    progress.visibility = View.GONE
                    btnSend.isEnabled = true
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
}