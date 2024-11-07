package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var loginButton: Button
    private lateinit var haveAccountText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Inisialisasi elemen-elemen dari layout
        usernameEditText = findViewById(R.id.editText)
        emailEditText = findViewById(R.id.textInputEditText)
        passwordEditText = findViewById(R.id.editText2)
        signUpButton = findViewById(R.id.signupButton)
        loginButton = findViewById(R.id.loginButton)
        haveAccountText = findViewById(R.id.textView3)

        // Set OnClickListener untuk tombol Sign Up
        signUpButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInput(username, email, password)) {
                // Panggil fungsi sign up atau lakukan hal lain sesuai kebutuhan
                Toast.makeText(this, "Sign Up successful", Toast.LENGTH_SHORT).show()

                // Setelah berhasil sign up, Anda bisa berpindah ke Activity lain atau menutup activity ini
                // Misalnya, pindah ke activity Home
                // val intent = Intent(this, HomeActivity::class.java)
                // startActivity(intent)
                // finish()
            }
        }

        // Set OnClickListener untuk tombol Log In
        loginButton.setOnClickListener {
            // Pindah ke activity Log In jika pengguna sudah punya akun
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Fungsi validasi untuk input username, email, dan password
    private fun validateInput(username: String, email: String, password: String): Boolean {
        return when {
            username.isEmpty() -> {
                usernameEditText.error = "Username is required"
                usernameEditText.requestFocus()
                false
            }
            email.isEmpty() -> {
                emailEditText.error = "Email is required"
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
