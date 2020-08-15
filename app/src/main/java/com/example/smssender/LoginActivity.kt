package com.example.smssender

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var preferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = getSharedPreferences("MyAppSharedPreference", Context.MODE_PRIVATE)
        setContentView(R.layout.activity_login)
        btnMainMenu.setOnClickListener {
            if (etUsername.text.isNullOrEmpty()) {
                etUsername.error = "Atıńızdı kiritiń"
            } else {
                db.collection("users").document(etUsername.text.toString()).get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            etUsername.error = "Bunday at penen aldın aldın dizimnen, iltimas, basqa at penen dizimnen ótiń"
                        } else {
                            preferences.edit().putBoolean("isUsernameSet", true).apply()
                            preferences.edit().putString("username", etUsername.text.toString()).apply()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
            }
        }
    }
}