package com.example.thisapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var saveButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Bind views
        usernameEditText = findViewById(R.id.editTextText2)
        emailEditText = findViewById(R.id.editTextText3)
        passwordEditText = findViewById(R.id.editTextText4)
        saveButton = findViewById(R.id.button4)

        // Set current email
        val currentEmail = intent.getStringExtra("email") ?: "No email provided"


        // Populate current username
        val currentUsername = intent.getStringExtra("username") ?: ""
        usernameEditText.setText(currentUsername)

        // Save button listener
        saveButton.setOnClickListener {
            val updatedUsername = usernameEditText.text.toString().trim()
            val updatedEmail = emailEditText.text.toString().trim()
            val updatedPassword = passwordEditText.text.toString().trim()

            when {
                updatedEmail.isNotEmpty() && updatedPassword.isNotEmpty() -> {
                    // Update both email and password
                    updateEmailAndPassword(updatedEmail, updatedPassword)
                }
                updatedUsername.isNotEmpty() -> {
                    // Only update username
                    updateUsername(updatedUsername)
                }
                else -> {
                    Toast.makeText(this, "Please enter the required fields", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateEmailAndPassword(updatedEmail: String, updatedPassword: String) {
        val user = auth.currentUser
        user?.let {
            // Update email in Firebase Authentication
            it.updateEmail(updatedEmail).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update password if provided
                    if (updatedPassword.isNotEmpty()) {
                        it.updatePassword(updatedPassword)
                            .addOnCompleteListener { passwordTask ->
                                if (passwordTask.isSuccessful) {
                                    Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "Failed to update password: ${passwordTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                    // Update email in Firestore
                    updateEmailInFirestore(updatedEmail)

                    // Logout and prompt user to log in again
                    auth.signOut()
                    Toast.makeText(this, "Email updated. Please log in again.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Failed to update email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUsername(updatedUsername: String) {
        val user = auth.currentUser
        user?.let {
            // Update username in Firestore
            val userRef = db.collection("users").document(it.uid)
            userRef.update("username", updatedUsername)
                .addOnSuccessListener {
                    Toast.makeText(this, "Username updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update username: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEmailInFirestore(updatedEmail: String) {
        val user = auth.currentUser
        user?.let {
            val userRef = db.collection("users").document(it.uid)
            userRef.update("email", updatedEmail)
                .addOnSuccessListener {
                    Toast.makeText(this, "Email updated in Firestore", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update email in Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
}