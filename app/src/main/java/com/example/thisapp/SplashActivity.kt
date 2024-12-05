package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Delay beberapa detik untuk menampilkan splash screen
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginState()
        }, 2000) // 2 detik delay (bisa disesuaikan)
    }

    private fun checkLoginState() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val isFirstTimeUser = sharedPreferences.getBoolean("isFirstTimeUser", true)
        val isPinSet = sharedPreferences.getBoolean("isPinSet", false) // Cek apakah PIN sudah diatur

        // Jika ini pertama kali aplikasi dijalankan, arahkan ke WelcomeActivity
        if (isFirstTimeUser) {
            // Setel flag isFirstTimeUser ke false untuk aplikasi selanjutnya
            val editor = sharedPreferences.edit()
            editor.putBoolean("isFirstTimeUser", false)
            editor.apply()

            // Arahkan ke WelcomeActivity
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        } else if (isLoggedIn) {
            // Jika sudah login dan PIN sudah diatur, arahkan ke PinActivity
            if (isPinSet) {
                val intent = Intent(this, PinActivity::class.java)
                startActivity(intent)
            } else {
                // Jika sudah login tapi PIN belum diatur, arahkan ke ProfileSettings
                val intent = Intent(this, ProfileSettings::class.java)
                startActivity(intent)
            }
        } else {
            // Jika belum login dan bukan pengguna baru, arahkan ke SignUp
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        finish() // Tutup SplashActivity agar tidak bisa kembali ke sini
    }
}
