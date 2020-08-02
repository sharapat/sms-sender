package com.example.smssender

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE = 101
    }

    private lateinit var preferences : SharedPreferences

    private val db = FirebaseFirestore.getInstance()
    private var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hideKeyboard(this)
        preferences = getSharedPreferences("MyAppSharedPreference", Context.MODE_PRIVATE)
        if(!preferences.getBoolean("isUsernameSet", false)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            username = preferences.getString("username", "")!!
        }
        btnSend.setOnClickListener {
            val start = tvStart.text.toString().toInt()
            val end = tvEnd.text.toString().toInt()
            val code = tvCode.text.toString().toInt()
            val preCode = tvPreCode.text.toString().toInt()

            AlertDialog.Builder(this)
                .setPositiveButton("AWA") { dialog, which ->
                    for (i in start..end) {
                        sendSMS("%02d".format(code), "%03d".format(preCode), "%04d".format(i))
                    }
                    btnImport.visibility = View.INVISIBLE
                    btnSend.visibility = View.INVISIBLE
                    supportFragmentManager.beginTransaction().replace(R.id.container, FinishFragment()).commit()
                    dialog.dismiss()
                }
                .setNegativeButton("YAQ") { dialog, which ->
                    dialog.dismiss()
                }
                .setTitle("Dıqqat!")
                .setMessage("SMS xabarlardı jiberiwge isenimińiz kámilme?")
                .show()
        }
        btnImport.setOnClickListener {
            val intent = Intent(this, ImportActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        askPermission()
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
        btnSend.background = ContextCompat.getDrawable(this, if (isEnabled) R.drawable.button_background else R.drawable.disable_button_background)
        btnImport.background = ContextCompat.getDrawable(this, if (isEnabled) R.drawable.button_background else R.drawable.disable_button_background)
    }

    private fun showMessage(msg: String) {
        tvInfo.text = msg
        tvInfo.visibility = View.VISIBLE
    }

    private fun askPermission() {
        if (PermissionUtils.askPermissions(this, PermissionUtils.SEND_SMS)) {
            check()
        } else {
            enableButtons(false)
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
        enableButtons(false)
        val mp : HashMap<String, Any> = HashMap()
        val imei = getIMEI()
        if (preferences.getBoolean("isRegistered", false)) {
            enableButtons(false)
            progress.visibility = View.VISIBLE
            if (preferences.getBoolean("isAllowed", false)) {
                enableButtons(true)
                progress.visibility = View.GONE
            } else {
                isAllowed(imei)
            }
        } else {
            progress.visibility = View.VISIBLE
            mp["isAllowed"] = false
            mp["imei"] = imei
            db.collection("users").document("$username-$imei").set(mp)
                .addOnSuccessListener {
                    preferences.edit().putBoolean("isRegistered", true).apply()
                    progress.visibility = View.GONE
                    showMessage("Administrator tárepinen ruxsat berilgennen keyin Refresh túymesin basıń yáki programmanı qayta iske túsiriń")
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
                    progress.visibility = View.GONE
                }
        }

    }

    private fun isAllowed(imei: String) {
        db.collection("users").document("$username-$imei").get()
            .addOnSuccessListener { document ->
                if (document.getBoolean("isAllowed")!!) {
                    progress.visibility = View.GONE
                    enableButtons(true)
                    tvInfo.visibility = View.GONE
                    preferences.edit().putBoolean("isAllowed", true).apply()
                } else {
                    showMessage("Sizge administrator tarepinen ruxsat berilmegen!")
                    progress.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                progress.visibility = View.GONE
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PermissionUtils.onRequestPermissionsResult(requestCode, grantResults,
                arrayListOf(PermissionUtils.SEND_SMS, PermissionUtils.READ_PHONE_STATE, PermissionUtils.READ_EXTERNAL_STORAGE))) {
            check()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Arnawlı ruxsatlar kerek")
                .setMessage("Programma islewi ushın arnawlı ruxsatlar kerek. Sazlamalardı ashıp ruxsatlardı beresizbe?")
                .setNegativeButton("Yaq") { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton("Awa, ashıń") { dialog, which ->
                    PermissionUtils.openPermissionSettings(this)
                }
                .show()
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