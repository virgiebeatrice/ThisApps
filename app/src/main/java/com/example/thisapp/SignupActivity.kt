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
        passwordVisibilityToggle = findViewById(R.id.passwordVisibilityToggle) // Menghubungkan ImageView untuk toggle
        signUpButton = findViewById(R.id.signupButton)
        loginButton = findViewById(R.id.loginButton)

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
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Fungsi untuk toggle visibilitas password
        passwordVisibilityToggle.setOnClickListener {
            if (passwordEditText.inputType == 129) { // Password visibility off
                passwordEditText.inputType = 1 // Set input type to text
                passwordVisibilityToggle.setImageResource(R.drawable.eye_open) // Ganti gambar mata
            } else { // Password visibility on
                passwordEditText.inputType = 129 // Set input type to password
                passwordVisibilityToggle.setImageResource(R.drawable.eye_closed) // Ganti gambar mata
            }
            passwordEditText.setSelection(passwordEditText.text.length) // Agar posisi kursor tetap di akhir
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
        db.collection("users").document(userId).set(user)
            .addOnSuccessListener {
                showToast("Pendaftaran berhasil")
                // Pindah ke halaman login setelah sign up sukses
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish() // menutup SignupActivity
            }
            .addOnFailureListener { e ->
                showToast("Gagal menyimpan data pengguna: ${e.message}")
            }
    }

    private fun validateInput(username: String, email: String, password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{6}\$")

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
                passwordEditText.error = "Password harus mengandung huruf besar, huruf kecil, angka, karakter spesial, dan panjang 6 karakter"
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
