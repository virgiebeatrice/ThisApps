package com.example.thisapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileSettings : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

        // Referensi ke elemen TextView di XML
        val usernameTextView: TextView = findViewById(R.id.textView14)
        val emailTextView: TextView = findViewById(R.id.textView15)
        val pinEditText: TextInputEditText = findViewById(R.id.textpin)
        val saveButton: Button = findViewById(R.id.button)

        // Ambil userId dari FirebaseAuth
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Pengguna belum login!", Toast.LENGTH_SHORT).show()
            return
        }

        // Ambil data username dan email dari Firestore
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username") ?: "Default Name"
                    val email = document.getString("email") ?: "Default Email"

                    // Tampilkan data di TextView
                    usernameTextView.text = username
                    emailTextView.text = email
                } else {
                    Toast.makeText(this, "Data pengguna tidak ditemukan.", Toast.LENGTH_SHORT)
                        .show()
                    usernameTextView.text = "Default Name"
                    emailTextView.text = "Default Email"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mengambil data: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                usernameTextView.text = "Default Name"
                emailTextView.text = "Default Email"
                Log.e("ProfileSettings", "Error fetching document", e)
            }

        // Simpan data PIN ke Firestore
        saveButton.setOnClickListener {
            val pin = pinEditText.text.toString()

            // Validasi PIN
            if (pin.isEmpty() || pin.length != 4 || !pin.all { it.isDigit() }) {
                Toast.makeText(this, "PIN harus terdiri dari 4 digit angka!", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Simpan PIN ke Firestore
            val pinData = hashMapOf(
                "pin" to pin
            )

            firestore.collection("PIN")
                .document(userId) // Menyimpan PIN di bawah ID pengguna
                .set(pinData) // Gunakan set agar data tidak duplikat
                .addOnSuccessListener {
                    Toast.makeText(this, "PIN berhasil disimpan!", Toast.LENGTH_SHORT).show()

                    // Bersihkan PIN setelah berhasil disimpan
                    pinEditText.setText("") // Kosongkan kotak teks PIN
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menyimpan PIN: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                    Log.e("ProfileSettings", "Error menyimpan PIN", e)
                }
        }
    }
}
