package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordVisibilityToggle: ImageView
    private lateinit var loginButton: Button
    private lateinit var signUpButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Disable Firestore cache to fetch fresh data
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()

        // Initialize views
        emailEditText = findViewById(R.id.editText)
        passwordEditText = findViewById(R.id.editText2)
        passwordVisibilityToggle = findViewById(R.id.passwordVisibilityToggle1)
        loginButton = findViewById(R.id.loginButton)
        signUpButton = findViewById(R.id.signupButton)

        // Check login status and handle accordingly
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            val email = sharedPreferences.getString("active_user_email", "") ?: ""
            if (email.isNotEmpty()) {
                checkUserPinStatus(email)
            } else {
                navigateToLogin()
            }
        }

        // Set listeners
        loginButton.setOnClickListener { handleLogin() }
        passwordVisibilityToggle.setOnClickListener { togglePasswordVisibility() }
        signUpButton.setOnClickListener { navigateToSignUp() }

        // Handle "Enter" key on password field
        passwordEditText.setOnEditorActionListener { _, _, _ ->
            handleLogin()
            true
        }
    }

    private fun handleLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (validateInput(email, password)) {
            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    fetchAndSaveUserData(email)
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                    // Redirect ke halaman utama
                    val intent = Intent(this, LandingPage::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserPinStatus(email: String) {
        db.collection("PIN")
            .whereEqualTo("email", email.toLowerCase())
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    navigateToSetupPin()
                } else {
                    val pinDocument = querySnapshot.documents.firstOrNull()
                    val pin = pinDocument?.getString("pin")
                    if (pin.isNullOrEmpty()) {
                        navigateToSetupPin()
                    } else {
                        navigateToLanding()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to check PIN status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveLoginState(email: String) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean("isLoggedIn", true)
            putString("active_user_email", email)
            apply()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        // Updated password pattern: Minimum 8 characters, 1 uppercase, 1 lowercase, 1 digit, 1 special character
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,12}$")

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
                passwordEditText.error = "Password must be 8-12 characters, contain uppercase, lowercase, number, and special character"
                passwordEditText.requestFocus()
                false
            }
            else -> true
        }
    }

    private fun togglePasswordVisibility() {
        if (passwordEditText.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordVisibilityToggle.setImageResource(R.drawable.eye_open)
        } else {
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordVisibilityToggle.setImageResource(R.drawable.eye_closed)
        }
        passwordEditText.setSelection(passwordEditText.text.length)
    }

    private fun navigateToSetupPin() {
        val intent = Intent(this, SetupPinActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLanding() {
        val intent = Intent(this, LandingPage::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToSignUp() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
    }

    private fun fetchAndSaveUserData(email: String) {
        db.collection("Users").document(email)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username") ?: "Default Username"

                    // Save username to SharedPreferences
                    val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("username", username)
                    editor.apply()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
