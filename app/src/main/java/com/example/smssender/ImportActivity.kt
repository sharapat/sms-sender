package com.example.smssender

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.telephony.SmsManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import jxl.Workbook
import jxl.WorkbookSettings
import jxl.read.biff.BiffException
import kotlinx.android.synthetic.main.activity_import.*
import kotlinx.android.synthetic.main.activity_import.btnImport
import kotlinx.android.synthetic.main.activity_import.btnSend
import java.io.File
import java.io.IOException


class ImportActivity : AppCompatActivity() {

    private var workbook: Workbook? = null
    private val adapter = ContactAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        rvContact.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rvContact.adapter = adapter
        btnImport.setOnClickListener {
            showFileChooser()
        }
        btnSend.setOnClickListener {
            AlertDialog.Builder(this)
                .setPositiveButton("AWA") { dialog, which ->
                    adapter.contactList.forEach {
                        sendSMS(it)
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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return false
    }

    private fun sendSMS(number: String) {
        try {
            val sms = SmsManager.getDefault()
            val text = etText.text.toString()
            val parts = sms.divideMessage(text)
            sms.sendMultipartTextMessage(number, null, parts, null, null)
        } catch (e: Exception) {
            Log.d("kettime", e.localizedMessage!!)
        }
    }

    private fun showFileChooser() {
        val intent = Intent()
        intent.type = "application/vnd.ms-excel"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select file"), 1)
    }

    private fun readContacts(uri: String) {
        val contactList : MutableList<String> = mutableListOf()
        val ws = WorkbookSettings()
        ws.gcDisabled = true
        val file = File("/storage/emulated/0/$uri")
        if (file.exists()) {
            Log.d("bar", "bar")
        }
        try {
            workbook = Workbook.getWorkbook(file)
            workbook?.sheets?.forEach {
                for (i in 0 until it.rows) {
                    contactList.add(it.getRow(i)!![0].contents)
                }
            }
            adapter.contactList = contactList
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: BiffException) {
            e.printStackTrace()
            Log.d("qate", e.localizedMessage!!.toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && data != null && data.data != null && resultCode == Activity.RESULT_OK) {
            Log.d("jol", getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString())
            Log.d("jol", data.dataString!!)
            val uri: Uri = data.data!!
            val index = uri.path.toString().indexOf(':')
            val path = uri.path.toString().removeRange(0, index+1)
            Log.d("jol", path)
            readContacts(path)
        }
    }
}