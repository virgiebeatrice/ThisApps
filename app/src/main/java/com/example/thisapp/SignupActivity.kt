package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var loginButton: Button
    private lateinit var haveAccountText: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Inisialisasi Firebase Authentication dan Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

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
                signUpUser(username, email, password)
            }
        }

        // Set OnClickListener untuk tombol Login, pindah ke halaman LoginActivity
        loginButton.setOnClickListener {
            Toast.makeText(this, "Login button clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signUpUser(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Jika registrasi berhasil, simpan data pengguna di Firestore
                    val userId = auth.currentUser?.uid
                    val user = hashMapOf(
                        "username" to username,
                        "email" to email
                    )
                    userId?.let {
                        saveUserDataToFirestore(it, user)
                    }
                } else {
                    showToast("Sign Up failed: ${task.exception?.message}")
                }
            }
    }

    private fun saveUserDataToFirestore(userId: String, user: Map<String, String>) {
        db.collection("users").document(userId).set(user)
            .addOnSuccessListener {
                showToast("Sign Up successful")
                navigateToMainActivity()
            }
            .addOnFailureListener { e ->
                showToast("Failed to save user data: ${e.message}")
            }
    }

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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
