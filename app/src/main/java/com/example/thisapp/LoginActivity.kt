package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inisialisasi FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Inisialisasi elemen-elemen dari layout
        emailEditText = findViewById(R.id.editText) // Ganti dengan ID EditText untuk email
        passwordEditText = findViewById(R.id.editText2) // Ganti dengan ID EditText untuk password
        loginButton = findViewById(R.id.loginButton)
        signUpButton = findViewById(R.id.signupButton)

        // Set OnClickListener untuk tombol Login
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
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

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login berhasil, pindah ke MainActivity
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Login gagal
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Fungsi validasi untuk input email dan password
    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                emailEditText.error = "Email is required"
                emailEditText.requestFocus()
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailEditText.error = "Please enter a valid email"
                emailEditText.requestFocus()
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
