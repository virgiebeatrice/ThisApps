package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.example.thisapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Cek apakah pengguna sudah login
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Pengguna sudah login, langsung ke PINActivity
            val intent = Intent(this, PINActivity::class.java)
            startActivity(intent)
            finish()  // Tutup MainActivity agar tidak bisa kembali
        } else {
            // Pengguna belum login, arahkan ke LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()  // Tutup MainActivity agar tidak bisa kembali
        }
    }
}