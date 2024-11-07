package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inisialisasi elemen-elemen dari layout
        usernameEditText = findViewById(R.id.editText)
        passwordEditText = findViewById(R.id.editText2)
        loginButton = findViewById(R.id.outlinedButton)

        // Set OnClickListener untuk tombol Login
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInput(username, password)) {
                // Panggil fungsi login atau lakukan hal lain sesuai kebutuhan
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                // Setelah login berhasil, Anda bisa pindah ke activity utama
                // val intent = Intent(this, MainActivity::class.java)
                // startActivity(intent)
                // finish()
            }
        }

        // Set OnClickListener untuk tombol Sign Up
        signUpButton.setOnClickListener {
            // Pindah ke activity Sign Up jika pengguna belum punya akun
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Fungsi validasi untuk input username dan password
    private fun validateInput(username: String, password: String): Boolean {
        return when {
            username.isEmpty() -> {
                usernameEditText.error = "Username is required"
                usernameEditText.requestFocus()
                false
            }
            password.isEmpty() -> {
                passwordEditText.error = "Password is required"
                passwordEditText.requestFocus()
                false
            }
            password.length < 6 -> {
                passwordEditText.error = "Password should be at least 6 characters"
                passwordEditText.requestFocus()
                false
            }
            else -> true
        }
    }
}