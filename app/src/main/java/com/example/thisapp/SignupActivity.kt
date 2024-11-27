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

        // Inisialisasi Firebase Authentication dan Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inisialisasi elemen-elemen dari layout
        usernameEditText = findViewById(R.id.editText)
        emailEditText = findViewById(R.id.textInputEditText)
        passwordEditText = findViewById(R.id.editText2)
        passwordVisibilityToggle = findViewById(R.id.passwordVisibilityToggle)
        signUpButton = findViewById(R.id.signupButton)
        loginButton = findViewById(R.id.loginButton)

        // Tombol Sign Up
        signUpButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInput(username, email, password)) {
                signUpUser(username, email, password)
            }
        }

        // Tombol Login
        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Toggle visibilitas password
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
                    showToast("Pendaftaran gagal: ${task.exception?.message}")
                }
            }
    }

    private fun saveUserDataToFirestore(userId: String, user: Map<String, String>) {
        val email = user["email"] ?: return
        db.collection("Users").document(email).set(user)
            .addOnSuccessListener {
                val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("active_user_email", email)
                editor.putString("username", user["username"])
                editor.apply()

                showToast("Pendaftaran berhasil")
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                showToast("Gagal menyimpan data pengguna: ${e.message}")
            }
    }

    private fun validateInput(username: String, email: String, password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{6,}\$")

        return when {
            username.isEmpty() -> {
                usernameEditText.error = "Username diperlukan"
                false
            }
            email.isEmpty() -> {
                emailEditText.error = "Email diperlukan"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailEditText.error = "Masukkan email yang valid"
                false
            }
            password.isEmpty() -> {
                passwordEditText.error = "Password diperlukan"
                false
            }
            !password.matches(passwordPattern) -> {
                passwordEditText.error = "Password harus mengandung huruf besar, huruf kecil, angka, karakter spesial, dan panjang minimal 6 karakter"
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
