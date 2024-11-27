package com.example.thisapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PINActivity : AppCompatActivity() {

    private lateinit var pin1: EditText
    private lateinit var pin2: EditText
    private lateinit var pin3: EditText
    private lateinit var pin4: EditText
    private lateinit var submitButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        // Inisialisasi komponen UI
        pin1 = findViewById(R.id.pin1)
        pin2 = findViewById(R.id.pin2)
        pin3 = findViewById(R.id.pin3)
        pin4 = findViewById(R.id.pin4)
        submitButton = findViewById(R.id.button)

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val storedPin = sharedPreferences.getString("pin", null)

        submitButton.setOnClickListener {
            // Gabungkan input PIN dari empat EditText
            val enteredPin = pin1.text.toString().trim() +
                    pin2.text.toString().trim() +
                    pin3.text.toString().trim() +
                    pin4.text.toString().trim()

            if (enteredPin == storedPin) {
                // PIN benar, arahkan ke halaman beranda
                val intent = Intent(this, BerandaActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // PIN salah
                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
