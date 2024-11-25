package com.example.thisapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PINActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        // Sesuaikan padding untuk edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.pin1)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup PIN input logic
        val pins = listOf(
            findViewById<EditText>(R.id.pin1),
            findViewById<EditText>(R.id.pin2),
            findViewById<EditText>(R.id.pin3),
            findViewById<EditText>(R.id.pin4)
        )

        setupPinInputs(pins)
    }

    private fun setupPinInputs(pins: List<EditText>) {
        for (i in pins.indices) {
            // Tambahkan TextWatcher untuk mendeteksi input
            pins[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        if (i < pins.size - 1) {
                            pins[i + 1].requestFocus()
                        } else {
                            pins[i].clearFocus()
                        }
                    } else if (s?.length ?: 0 > 1) {
                        pins[i].setText(s?.subSequence(0, 1))
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            // Tambahkan listener untuk mendeteksi penghapusan
            pins[i].setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (pins[i].text.isEmpty() && i > 0) {
                        pins[i - 1].setText("")
                        pins[i - 1].requestFocus()
                    }
                }
                false
            }
        }
    }
}
