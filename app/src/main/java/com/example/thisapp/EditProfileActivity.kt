package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar

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

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("isDarkMode", false)

        // Set tema sebelum setContentView
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        setContentView(R.layout.activity_edit_profile)

        val toolbar: Toolbar = findViewById(R.id.toolbar2)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val switchMode: SwitchCompat = findViewById(R.id.switch_mode)

        switchMode.isChecked = isDarkMode

        switchMode.setOnCheckedChangeListener { _, isChecked ->
            // Set tema sesuai dengan status switch
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )

            saveThemePreference(isChecked)
            recreate() // Memastikan tema baru diterapkan setelah switch berubah
        }

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

    private fun saveThemePreference(isDarkMode: Boolean) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean("isDarkMode", isDarkMode)
            apply()
        }
    }

    private fun saveProfileData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val username = editTextName.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val confirmPassword = editTextConfirmPassword.text.toString().trim()
            val pin = editTextPIN.text.toString().trim()

            // Check if password and confirm password match
            if (password.isNotEmpty() && password != confirmPassword) {
                Toast.makeText(this, "Password and Confirm Password do not match!", Toast.LENGTH_SHORT).show()
                return
            }

            // Prepare data to update
            val email = currentUser.email ?: return
            val profileData = mutableMapOf<String, Any>("username" to username)

            // Update profile data in Firestore
            db.collection("editprofile").document(email)
                .set(profileData)
                .addOnSuccessListener {
                    // Update password if provided
                    if (password.isNotEmpty()) {
                        currentUser.updatePassword(password)
                            .addOnSuccessListener {
                                // Update PIN
                                db.collection("PIN").document(email)
                                    .set(mapOf("pin" to pin))
                                    .addOnSuccessListener {
                                        // Send updated username and email back to ProfileSettings
                                        val resultIntent = Intent()
                                        resultIntent.putExtra("updatedUsername", username)
                                        resultIntent.putExtra("updatedEmail", email)
                                        setResult(RESULT_OK, resultIntent)
                                        finish()  // Close the EditProfileActivity
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Failed to update PIN: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to update password: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // No password update, just update the username
                        val resultIntent = Intent()
                        resultIntent.putExtra("updatedUsername", username)
                        resultIntent.putExtra("updatedEmail", email)
                        setResult(RESULT_OK, resultIntent)
                        finish()  // Close the EditProfileActivity
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
