package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Delay for splash screen
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginState()
        }, 2000) // 2 seconds delay
    }

    private fun checkLoginState() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val isPinSet = sharedPreferences.getBoolean("isPinSet", false)
        val currentUser = auth.currentUser

        Log.d("SplashActivity", "isLoggedIn: $isLoggedIn, isPinSet: $isPinSet, currentUser: $currentUser")

        if (currentUser != null) {
            if (isLoggedIn) {
                // Check if the PIN is set
                if (isPinSet) {
                    // If user is logged in and PIN is set, navigate to PIN screen
                    startActivity(Intent(this, PinActivity::class.java))
                } else {
                    // If user is logged in but PIN is not set, navigate to SetupPinActivity
                    startActivity(Intent(this, SetupPinActivity::class.java))
                }
            } else {
                // If user is not logged in, navigate to LoginActivity
                startActivity(Intent(this, LoginActivity::class.java))
            }
        } else {
            // If there is no current user (user not authenticated), navigate to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish() // Close SplashActivity so that the user cannot navigate back to it
    }

    private fun navigateTo(destination: Class<*>) {
        val intent = Intent(this, destination)
        startActivity(intent)
        finish() // Remove SplashActivity from the back stack
    }
}
