package com.example.thisapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordVisibilityToggle1: ImageView
    private lateinit var loginButton: Button
    private lateinit var signUpButton: Button
    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.editText)
        passwordEditText = findViewById(R.id.editText2)
        passwordVisibilityToggle1 = findViewById(R.id.passwordVisibilityToggle1)
        loginButton = findViewById(R.id.loginButton)
        signUpButton = findViewById(R.id.signupButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        // Listener untuk toggle visibilitas password
        passwordVisibilityToggle1.setOnClickListener {
            togglePasswordVisibility()
        }

        // Listener untuk tombol "Enter" pada keyboard
        passwordEditText.setOnEditorActionListener { _, _, _ ->
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            if (validateInput(email, password)) {
                loginUser(email, password)
            }
            true
        }

        // Set OnClickListener untuk tombol Sign Up
        signUpButton.setOnClickListener {
            // Pindah ke activity Sign Up jika pengguna belum punya akun
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Fungsi toggle visibilitas password
    private fun togglePasswordVisibility() {
        if (passwordEditText.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            // Password tersembunyi, tampilkan password
            passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordVisibilityToggle1.setImageResource(R.drawable.eye_open) // Ganti dengan ikon mata terbuka
        } else {
            // Password terlihat, sembunyikan password
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordVisibilityToggle1.setImageResource(R.drawable.eye_closed) // Ganti dengan ikon mata tertutup
        }
        // Pindahkan kursor ke akhir teks
        passwordEditText.setSelection(passwordEditText.text.length)
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
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    // Fungsi validasi untuk input email dan password
    private fun validateInput(email: String, password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{6}\$")

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

            !password.matches(passwordPattern) -> {
                passwordEditText.error =
                    "Password must contain uppercase, lowercase, number, special character, and be 6 characters long"
                passwordEditText.requestFocus()
                false
            }

            else -> true
        }
    }
}
