package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class SetupPinActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_pin)

        // Referensi EditText PIN
        val pin1 = findViewById<EditText>(R.id.pin1)
        val pin2 = findViewById<EditText>(R.id.pin2)
        val pin3 = findViewById<EditText>(R.id.pin3)
        val pin4 = findViewById<EditText>(R.id.pin4)
        val submitButton = findViewById<Button>(R.id.submit_pin_button)

        // Pindah otomatis antar kotak PIN dan mendukung tombol delete
        setupPinAutoFocusWithDelete(pin1, null, pin2)
        setupPinAutoFocusWithDelete(pin2, pin1, pin3)
        setupPinAutoFocusWithDelete(pin3, pin2, pin4)
        setupPinAutoFocusWithDelete(pin4, pin3, null)

        // Tangkap event ketika user selesai mengisi PIN
        submitButton.setOnClickListener { v: View? ->
            val pin =
                pin1.text.toString() + pin2.text.toString() + pin3.text
                    .toString() + pin4.text.toString()
            if (validatePin(pin)) {
                savePinToDatabase(pin)
            } else {
                Toast.makeText(
                    this,
                    "Please enter a valid 4-digit PIN",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupPinAutoFocusWithDelete(
        current: EditText,
        previous: EditText?,
        next: EditText?
    ) {
        current.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.length == 1 && next != null) {
                    next.requestFocus()
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        current.setOnKeyListener { v: View?, keyCode: Int, event: KeyEvent ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                if (current.text.toString().isEmpty() && previous != null) {
                    previous.requestFocus()
                    previous.setSelection(previous.text.length)
                }
            }
            false
        }
    }

    private fun validatePin(pin: String): Boolean {
        return pin.length == 4 && pin.chars().allMatch { codePoint: Int ->
            Character.isDigit(
                codePoint
            )
        }
    }

    private fun savePinToDatabase(pin: String) {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            val firestore = FirebaseFirestore.getInstance()

            // Ambil data email pengguna
            val email = auth.currentUser!!.email
            val username = auth.currentUser!!.displayName

            if (email != null) {
                // Simpan PIN ke koleksi "PIN" berdasarkan email
                firestore.collection("PIN")
                    .document(email.lowercase(Locale.getDefault())) // Using email as document ID
                    .set(object : HashMap<String?, Any?>() {
                        init {
                            put("pin", pin)
                            put("email", email)
                            put("username", username)
                        }
                    })
                    .addOnSuccessListener { aVoid: Void? ->
                        Toast.makeText(this, "PIN set successfully!", Toast.LENGTH_SHORT)
                            .show()
                        // Update status PIN di SharedPreferences
                        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit()
                            .putBoolean("isPinSet", true)
                            .apply()

                        // Intent ke MainActivity
                        val intent = Intent(
                            this,
                            MainActivity::class.java
                        )
                        intent.putExtra(
                            "fromSetupPin",
                            true
                        ) // Flag untuk menandakan navigasi dari setup PIN
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e: Exception ->
                        Toast.makeText(
                            this,
                            "Failed to save PIN. Try again: " + e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(this, "User email is missing", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
}