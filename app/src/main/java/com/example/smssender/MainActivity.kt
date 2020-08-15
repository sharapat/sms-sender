package com.example.smssender

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
                    sendSMS("%02d".format(code), "%03d".format(preCode), start, end)
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

    private fun sendSMS(code: String, preCode: String, start: Int, end: Int) {
        val intent = Intent(this, MyService::class.java)
        intent.putExtra("start", start)
        intent.putExtra("end", end)
        intent.putExtra("code", code)
        intent.putExtra("preCode", preCode)
        intent.putExtra("text", getMessage())
        startService(intent)
    }

    private fun getMessage() : String {
        return etText.text.toString()
    }

    private fun check() {
        progress.visibility = View.VISIBLE
        enableButtons(false)
        val mp : HashMap<String, Any> = HashMap()
        mp["username"] = username
        db.collection("users").document(username).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    if (doc.get("isAllowed") as Boolean) {
                        progress.visibility = View.GONE
                        enableButtons(true)
                        tvInfo.visibility = View.GONE
                    } else {
                        showMessage("Sizge administrator tarepinen ruxsat berilmegen! +998 97 3558787")
                        progress.visibility = View.GONE
                    }
                } else {
                    progress.visibility = View.VISIBLE
                    mp["isAllowed"] = false
                    db.collection("users").document(username).set(mp)
                        .addOnSuccessListener {
                            preferences.edit().putBoolean("isRegistered", true).apply()
                            progress.visibility = View.GONE
                            showMessage("Administrator tárepinen ruxsat berilgennen keyin Refresh túymesin basıń yáki programmanı qayta iske túsiriń. +998 97 3558787")
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
                            progress.visibility = View.GONE
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                progress.visibility = View.GONE
            }
    }

//    private fun isAllowed() {
//        db.collection("users").document(username).get()
//            .addOnSuccessListener { document ->
//                if (document.getBoolean("isAllowed")!!) {
//                    progress.visibility = View.GONE
//                    enableButtons(true)
//                    tvInfo.visibility = View.GONE
//                    preferences.edit().putBoolean("isAllowed", true).apply()
//                } else {
//                    showMessage("Sizge administrator tarepinen ruxsat berilmegen!")
//                    progress.visibility = View.GONE
//                }
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
//                progress.visibility = View.GONE
//            }
//    }

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