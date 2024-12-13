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

        val pin1: EditText = findViewById(R.id.pin1)
        val pin2: EditText = findViewById(R.id.pin2)
        val pin3: EditText = findViewById(R.id.pin3)
        val pin4: EditText = findViewById(R.id.pin4)

        val user = FirebaseAuth.getInstance().currentUser

        // Periksa jika user belum login
        if (user == null) {
            Toast.makeText(this, "Pengguna belum login!", Toast.LENGTH_SHORT).show()
            redirectToLogin()
            return
        }

        val firestore = FirebaseFirestore.getInstance()

        // Ambil email pengguna yang sedang login
        val email = user.email
        if (email != null) {
            firestore.collection("PIN").whereEqualTo("email", email.toLowerCase())
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        redirectToSetupPin()
                    } else {
                        val document = querySnapshot.documents.first()
                        val savedPin = document.getString("pin")

                        if (!savedPin.isNullOrEmpty()) {
                            setupPinInput(savedPin)
                        } else {
                            redirectToSetupPin()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal mengambil data PIN. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                    redirectToLogin()
                }
        }
    }

    private fun setupPinInput(savedPin: String) {
        val pin1: EditText = findViewById(R.id.pin1)
        val pin2: EditText = findViewById(R.id.pin2)
        val pin3: EditText = findViewById(R.id.pin3)
        val pin4: EditText = findViewById(R.id.pin4)

        pin1.addTextChangedListener(createPinTextWatcher(pin1, pin2, null, savedPin))
        pin2.addTextChangedListener(createPinTextWatcher(pin2, pin3, pin1, savedPin))
        pin3.addTextChangedListener(createPinTextWatcher(pin3, pin4, pin2, savedPin))
        pin4.addTextChangedListener(createPinTextWatcher(pin4, null, pin3, savedPin))
    }

    private fun createPinTextWatcher(current: EditText, nextField: EditText?, previousField: EditText?, savedPin: String): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 1) {
                    nextField?.requestFocus()
                } else if (s?.isEmpty() == true) {
                    previousField?.requestFocus()
                }

                // Cek jika semua kolom terisi
                val pin1: EditText = findViewById(R.id.pin1)
                val pin2: EditText = findViewById(R.id.pin2)
                val pin3: EditText = findViewById(R.id.pin3)
                val pin4: EditText = findViewById(R.id.pin4)

                if (pin1.text.length == 1 && pin2.text.length == 1 && pin3.text.length == 1 && pin4.text.length == 1) {
                    val enteredPin = "${pin1.text}${pin2.text}${pin3.text}${pin4.text}"
                    validatePin(enteredPin, savedPin)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
    }

    private fun validatePin(enteredPin: String, savedPin: String) {
        if (enteredPin == savedPin) {
            Toast.makeText(this, "PIN benar, selamat datang!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, BerandaActivity::class.java))
            finish()
        } else {
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

    private fun redirectToSetupPin() {
        startActivity(Intent(this, SetupPinActivity::class.java))
        finish()
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
