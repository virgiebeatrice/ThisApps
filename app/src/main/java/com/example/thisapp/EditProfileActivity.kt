package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var editTextName: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var editTextPIN: EditText
    private lateinit var buttonSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        editTextName = findViewById(R.id.editTextText2)
        editTextPassword = findViewById(R.id.editTextText4)
        editTextConfirmPassword = findViewById(R.id.editTextText5)
        editTextPIN = findViewById(R.id.editTextText6)
        buttonSave = findViewById(R.id.button4)

        // Set initial values from intent
        val username = intent.getStringExtra("username")
        editTextName.setText(username)

        buttonSave.setOnClickListener {
            saveProfileData()
        }
    }

    private fun saveProfileData() {
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser == null) {
            showToast("No user is logged in")
            return
        }

        val username = editTextName.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        val confirmPassword = editTextConfirmPassword.text.toString().trim()
        val pin = editTextPIN.text.toString().trim()
        val email = currentUser.email ?: run {
            showToast("User email not found")
            return
        }

        // Validate input
        if (username.isEmpty()) {
            Toast.makeText(this, "Username cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.isNotEmpty() && password != confirmPassword) {
            Toast.makeText(this, "Password and Confirm Password do not match!", Toast.LENGTH_SHORT).show()
            return
        }
        if (pin.isNotEmpty() && pin.length != 4) {
            Toast.makeText(this, "PIN must be exactly 4 digits!", Toast.LENGTH_SHORT).show()
            return
        }

        // Prepare data for update
        val profileData = mapOf("username" to username)

        // Update Firestore document for username
        db.collection("Users").document(email)
            .update(profileData)
            .addOnSuccessListener {
                // Save username in SharedPreferences
                val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                sharedPreferences.edit().putString("username", username).apply()

                // Handle password and PIN updates
                if (password.isNotEmpty()) {
                    updatePassword(currentUser, password, pin, email, username)
                } else if (pin.isNotEmpty()) {
                    updatePin(email, pin, username)
                } else {
                    sendResultAndFinish(username, email)
                }
            }
            .addOnFailureListener { e ->
                showToast("Failed to update profile: ${e.message}")
            }
    }

    private fun updatePassword(
        currentUser: FirebaseUser,
        password: String,
        pin: String,
        email: String,
        username: String
    ) {
        // Update password if provided
        currentUser.updatePassword(password)
            .addOnSuccessListener {
                if (pin.isNotEmpty()) {
                    updatePin(email, pin, username)
                } else {
                    sendResultAndFinish(username, email)
                }
            }
            .addOnFailureListener { e ->
                showToast("Failed to update password: ${e.message}")
            }
    }

    private fun updatePin(email: String, pin: String, username: String) {
        // Update PIN if provided
        db.collection("PIN").document(email)
            .set(mapOf("pin" to pin))
            .addOnSuccessListener {
                sendResultAndFinish(username, email)
            }
            .addOnFailureListener { e ->
                showToast("Failed to update PIN: ${e.message}")
            }
    }

    private fun sendResultAndFinish(username: String, email: String) {
        // Send updated data back to the calling activity and close the current activity
        val resultIntent = Intent().apply {
            putExtra("updatedUsername", username)
            putExtra("updatedEmail", email)
        }
        setResult(RESULT_OK, resultIntent)
        finish() // Close activity after update
    }

    private fun showToast(message: String) {
        // Helper method to show Toast messages
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
