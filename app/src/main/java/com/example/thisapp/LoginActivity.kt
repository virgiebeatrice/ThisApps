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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inisialisasi Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Nonaktifkan Firestore cache untuk mendapatkan data terbaru
        FirebaseFirestore.getInstance().firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false) // Nonaktifkan cache Firestore
            .build()

        // Periksa status login dan PIN saat aplikasi dibuka
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            // Jika sudah login, cek status PIN
            val email = sharedPreferences.getString("active_user_email", "") ?: ""
            if (email.isNotEmpty()) {
                checkUserPinStatus(email) // Periksa PIN setelah login
            } else {
                navigateToLogin()
            }
        }

        // Initialize views
        emailEditText = findViewById(R.id.editText)
        passwordEditText = findViewById(R.id.editText2)
        passwordVisibilityToggle = findViewById(R.id.passwordVisibilityToggle1)
        loginButton = findViewById(R.id.loginButton)
        signUpButton = findViewById(R.id.signupButton)

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
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        saveLoginState(email)
                        checkUserPinStatus(email) // Check PIN status after login
                    } else {
                        Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun checkUserPinStatus(email: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("PIN")  // Mengakses koleksi PIN, bukan Users
            .whereEqualTo("email", email.toLowerCase()) // Menjaga konsistensi email (case-insensitive)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // Jika PIN belum diatur, arahkan ke SetupPinActivity
                    navigateToSetupPin()
                } else {
                    val pinDocument = querySnapshot.documents.firstOrNull()
                    val pin = pinDocument?.getString("pin")
                    if (pin.isNullOrEmpty()) {
                        // Jika PIN tidak ada, arahkan ke SetupPinActivity
                        navigateToSetupPin()
                    } else {
                        // Jika PIN sudah ada, lanjutkan ke LandingPage dan deteksi mood
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
            putString("active_user_email", email) // Store active user's email
            apply()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$")

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
                passwordEditText.error = "Password must contain uppercase, lowercase, number, special character, and be at least 6 characters long"
                passwordEditText.requestFocus()
                false
            }

            else -> true
        }
    }

    private fun togglePasswordVisibility() {
        if (passwordEditText.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordVisibilityToggle.setImageResource(R.drawable.eye_open) // Open eye icon
        } else {
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordVisibilityToggle.setImageResource(R.drawable.eye_closed) // Closed eye icon
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
}
