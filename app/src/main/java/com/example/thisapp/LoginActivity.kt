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

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize views
        emailEditText = findViewById(R.id.editText)
        passwordEditText = findViewById(R.id.editText2)
        passwordVisibilityToggle1 = findViewById(R.id.passwordVisibilityToggle1)
        loginButton = findViewById(R.id.loginButton)
        signUpButton = findViewById(R.id.signupButton)

        // Login Button click listener
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        // Toggle password visibility
        passwordVisibilityToggle1.setOnClickListener {
            togglePasswordVisibility()
        }

        // Handle "Enter" key on keyboard
        passwordEditText.setOnEditorActionListener { _, _, _ ->
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            if (validateInput(email, password)) {
                loginUser(email, password)
            }
            true
        }

        // Go to SignupActivity if no account
        signUpButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Toggle password visibility
    private fun togglePasswordVisibility() {
        if (passwordEditText.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            // Password is hidden, show it
            passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordVisibilityToggle1.setImageResource(R.drawable.eye_open) // Open eye icon
        } else {
            // Password is visible, hide it
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordVisibilityToggle1.setImageResource(R.drawable.eye_closed) // Closed eye icon
        }
        // Move cursor to end of text
        passwordEditText.setSelection(passwordEditText.text.length)
    }

    // Login user with email and password
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login successful, navigate to BerandaActivity
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, BerandaActivity::class.java)
                    startActivity(intent)
                    finish() // Close LoginActivity to prevent back navigation
                } else {
                    // Login failed
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    // Validate email and password inputs
    private fun validateInput(email: String, password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{6,}\$")

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
                    "Password must contain uppercase, lowercase, number, special character, and be at least 6 characters long"
                passwordEditText.requestFocus()
                false
            }

            else -> true
        }
    }
}
