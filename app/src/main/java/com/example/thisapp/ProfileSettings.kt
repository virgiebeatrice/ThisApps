package com.example.thisapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class ProfileSettings : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

        // Referensi ke elemen TextView di XML
        val usernameTextView: TextView = findViewById(R.id.textView14) // Untuk Username
        val emailTextView: TextView = findViewById(R.id.textView15)   // Untuk Email
        val pinEditText: TextInputEditText = findViewById(R.id.textpin)
        val saveButton: Button = findViewById(R.id.button)

        // Ambil data dari SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val sharedUsername = sharedPreferences.getString("username", null)
        val sharedEmail = sharedPreferences.getString("email", null)

        // Ambil data username dan email dari Firestore jika tidak ada di SharedPreferences
        val userId = "exampleUserId" // Ganti dengan ID pengguna dari FirebaseAuth jika ada
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val firestoreUsername = document.getString("username") ?: "Default Name"
                    val firestoreEmail = document.getString("email") ?: "Default Email"

                    // Gunakan data dari Firestore jika SharedPreferences kosong
                    val username = sharedUsername ?: firestoreUsername
                    val email = sharedEmail ?: firestoreEmail

                    // Tampilkan data di TextView
                    usernameTextView.text = username
                    emailTextView.text = email

                    // Debugging
                    Log.d("ProfileSettings", "Username: $username, Email: $email")
                } else {
                    // Data Firestore tidak ditemukan
                    Log.d("ProfileSettings", "Document does not exist")
                    usernameTextView.text = sharedUsername ?: "Default Name"
                    emailTextView.text = sharedEmail ?: "Default Email"
                }
            }
            .addOnFailureListener { e ->
                // Gagal mengambil data dari Firestore
                Log.e("ProfileSettings", "Error fetching document", e)
                usernameTextView.text = sharedUsername ?: "Default Name"
                emailTextView.text = sharedEmail ?: "Default Email"
            }

        // Simpan data PIN ke Firestore setelah tombol disimpan diklik
        saveButton.setOnClickListener {
            val pin = pinEditText.text.toString()

            if (pin.isEmpty()) {
                Toast.makeText(this, "PIN tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            } else {
                // Simpan PIN ke SharedPreferences
                val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("pin", pin)  // Menyimpan PIN yang dimasukkan oleh pengguna
                editor.apply()

                // Data yang akan disimpan ke Firestore
                val pinData = hashMapOf(
                    "username" to usernameTextView.text.toString(),
                    "email" to emailTextView.text.toString(),
                    "pin" to pin
                )

                // Simpan PIN ke Firestore
                firestore.collection("PIN")
                    .add(pinData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "PIN berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        Log.d("ProfileSettings", "PIN berhasil disimpan")
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal menyimpan PIN: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("ProfileSettings", "Error menyimpan PIN", e)
                    }
            }
        }
    }
}
