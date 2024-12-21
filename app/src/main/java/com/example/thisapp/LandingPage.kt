package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging

class LandingPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing_page)

        // Button untuk scan mood
        val scanMoodButton: Button = findViewById(R.id.scan_mood_button)

        // Ambil token FCM saat LandingPage dibuat
        getFCMToken()

        scanMoodButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getFCMToken() {
        // Meminta token FCM secara langsung
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Token berhasil didapatkan
                val token = task.result
                Log.d("FCM", "FCM Token: $token")

                // Kirim token ke server atau simpan sesuai kebutuhan
                // sendTokenToServer(token) // Misalnya kirim ke server
            } else {
                // Token gagal didapatkan
                Log.e("FCM", "Gagal mendapatkan token FCM", task.exception)
            }
        }
    }
}
