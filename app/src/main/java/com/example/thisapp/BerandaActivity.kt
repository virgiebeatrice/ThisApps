package com.example.thisapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class BerandaActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beranda)

        val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

        // Ambil username dari SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val sharedUsername = sharedPreferences.getString("username", null) // Ambil username yang disimpan

        // Referensi ke TextView untuk greeting
        val greetingTextView: TextView = findViewById(R.id.greeting_text)

        // Jika username tidak ditemukan di SharedPreferences, ambil dari Firestore
        val userId = "exampleUserId" // Ganti dengan ID pengguna FirebaseAuth
        if (sharedUsername == null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firestoreUsername = document.getString("username") ?: "User"
                        greetingTextView.text = "Hello, $firestoreUsername"

                        // Simpan username ke SharedPreferences untuk penggunaan di masa depan
                        with(sharedPreferences.edit()) {
                            putString("username", firestoreUsername)
                            apply()
                        }
                    } else {
                        greetingTextView.text = "Hello, User"
                    }
                }
                .addOnFailureListener {
                    greetingTextView.text = "Hello, User"
                }
        } else {
            // Jika username ditemukan di SharedPreferences
            greetingTextView.text = "Hello, $sharedUsername"
        }

        // Tombol untuk menulis diary
        val writeDiaryButton: Button = findViewById(R.id.write_diary_button)
        writeDiaryButton.setOnClickListener {
            val intent = Intent(this, DiaryActivity::class.java)
            startActivity(intent)
        }

        // Tombol untuk membuka pengaturan profil
        val profileIcon: ImageButton = findViewById(R.id.profile_icon)
        profileIcon.setOnClickListener {
            val intent = Intent(this, ProfileSettings::class.java)
            startActivity(intent)
        }
    }
}
