package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordVisibilityToggle: ImageView
    private lateinit var signUpButton: Button
    private lateinit var loginButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize Firebase Authentication and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI elements
        usernameEditText = findViewById(R.id.editText)
        emailEditText = findViewById(R.id.textInputEditText)
        passwordEditText = findViewById(R.id.editText2)
        passwordVisibilityToggle = findViewById(R.id.passwordVisibilityToggle)
        signUpButton = findViewById(R.id.signupButton)
        loginButton = findViewById(R.id.loginButton)

        // Sign up button click
        signUpButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInput(username, email, password)) {
                signUpUser(username, email, password)
            }
        }

        // Login button click
        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Password visibility toggle
        passwordVisibilityToggle.setOnClickListener {
            if (passwordEditText.inputType == 129) {
                passwordEditText.inputType = 1
                passwordVisibilityToggle.setImageResource(R.drawable.eye_open)
            } else {
                passwordEditText.inputType = 129
                passwordVisibilityToggle.setImageResource(R.drawable.eye_closed)
            }
            passwordEditText.setSelection(passwordEditText.text.length)
        }
    }

    private fun signUpUser(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // After registration is successful, log the user in
                    loginUser(email, password)
                } else {
                    showToast("Pendaftaran gagal: ${task.exception?.message}")
                }
            }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Save the user's data to Firestore
                    val userId = auth.currentUser?.uid
                    val user = hashMapOf(
                        "email" to email,
                        "username" to usernameEditText.text.toString().trim()
                    )
                    if (userId != null) {
                        saveUserDataToFirestore(userId, user)
                    }
                    // After saving user data, check the user's PIN status
                    checkUserPinStatus(email)
                } else {
                    showToast("Login gagal: ${task.exception?.message}")
                }
            }
    }

    private fun saveUserDataToFirestore(userId: String, user: Map<String, String>) {
        val email = user["email"] ?: return
        val emailForDocId = email.toLowerCase() // Make sure the email is lowercase to avoid any inconsistencies

        db.collection("Users").document(emailForDocId) // Use email as document ID
            .set(user)
            .addOnSuccessListener {
                // Successfully saved user data, now proceed with login and PIN setup
                val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("active_user_email", email)
                editor.putString("username", user["username"])
                editor.putBoolean("isLoggedIn", true) // Save login status
                editor.apply()

                showToast("Registration successful")
            }
            .addOnFailureListener { e ->
                showToast("Failed to save user data: ${e.message}")
            }
    }

    private fun checkUserPinStatus(email: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("PIN")
            .whereEqualTo("email", email.toLowerCase()) // Check email in the PIN collection
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // If PIN is not set, navigate to PIN setup
                    navigateToSetupPin()
                } else {
                    // If PIN is set, navigate to MainActivity
                    navigateToLogin()
                }
            }
            .addOnFailureListener { e ->
                showToast("Failed to check PIN status: ${e.message}")
            }
    }

    private fun navigateToSetupPin() {
        val intent = Intent(this, SetupPinActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun validateInput(username: String, email: String, password: String): Boolean {
        // Updated password validation (min 6, max 8 characters)
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{6,8}\$")

        return when {
            username.isEmpty() -> {
                usernameEditText.error = "Username is required"
                false
            }
            email.isEmpty() -> {
                emailEditText.error = "Email is required"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailEditText.error = "Please enter a valid email"
                false
            }
            password.isEmpty() -> {
                passwordEditText.error = "Password is required"
                false
            }
            !password.matches(passwordPattern) -> {
                passwordEditText.error = "Password must contain uppercase letters, lowercase letters, numbers, special characters, and be at least 6 characters long."

                false
            }
            else -> true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
