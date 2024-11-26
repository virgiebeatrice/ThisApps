package com.example.thisapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileSettings : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        // Referensi ke elemen TextView di XML
        val usernameTextView: TextView = findViewById(R.id.textView14)
        val emailTextView: TextView = findViewById(R.id.textView15)

        // Ambil data dari SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "Default Name")
        val email = sharedPreferences.getString("email", "Default Email")

        // Debugging: Log data yang diambil
        Log.d("ProfileSettings", "Username: $username, Email: $email")

        // Tampilkan data ke TextView yang ada
        usernameTextView.text = username
        emailTextView.text = email
    }
}
