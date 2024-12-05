package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        // Referensi ke EditText
        val pin1: EditText = findViewById(R.id.pin1)
        val pin2: EditText = findViewById(R.id.pin2)
        val pin3: EditText = findViewById(R.id.pin3)
        val pin4: EditText = findViewById(R.id.pin4)

        // Ambil userId dari FirebaseAuth
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Pengguna belum login!", Toast.LENGTH_SHORT).show()
            finish() // Jika pengguna belum login, kembali ke layar login
            return
        }

        // Ambil PIN yang disimpan di Firestore
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("PIN").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val savedPin = document.getString("pin")

                    // Pindah fokus antar kotak PIN secara otomatis
                    pin1.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            if (s?.length == 1) pin2.requestFocus()
                        }

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    })

                    pin2.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            if (s?.length == 1) pin3.requestFocus()
                        }

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    })

                    pin3.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            if (s?.length == 1) pin4.requestFocus()
                        }

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    })

                    pin4.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            // Jika semua kotak PIN sudah terisi, validasi PIN
                            if (s?.length == 1) {
                                val enteredPin = "${pin1.text}${pin2.text}${pin3.text}${pin4.text}"
                                validatePin(enteredPin, savedPin)
                            }
                        }

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    })
                } else {
                    Toast.makeText(this, "PIN belum diatur. Harap atur di pengaturan profil.", Toast.LENGTH_SHORT).show()
                    finish() // Kembali jika PIN tidak ditemukan
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mengambil PIN: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validatePin(enteredPin: String, savedPin: String?) {
        // Pastikan savedPin tidak null
        if (enteredPin == savedPin) {
            // PIN benar, arahkan ke BerandaActivity
            Toast.makeText(this, "PIN benar, selamat datang!", Toast.LENGTH_SHORT).show()

            // Arahkan ke BerandaActivity setelah PIN benar
            val intent = Intent(this, BerandaActivity::class.java)
            startActivity(intent)
            finish() // Menutup PinActivity agar tidak bisa kembali ke sini
        } else {
            // PIN salah, hapus semua input
            Toast.makeText(this, "PIN salah, coba lagi!", Toast.LENGTH_SHORT).show()
            clearPinFields()
        }
    }

    private fun clearPinFields() {
        val pin1: EditText = findViewById(R.id.pin1)
        val pin2: EditText = findViewById(R.id.pin2)
        val pin3: EditText = findViewById(R.id.pin3)
        val pin4: EditText = findViewById(R.id.pin4)

        pin1.text.clear()
        pin2.text.clear()
        pin3.text.clear()
        pin4.text.clear()
        pin1.requestFocus()
    }
}
