package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditProfileActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var editTextName: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var editTextConfirmPassword: TextInputEditText
    private lateinit var editTextPIN: TextInputEditText
    private lateinit var buttonSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("isDarkMode", false)

        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        editTextName = findViewById(R.id.editTextUsername) // ID sesuai dengan XML
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        editTextPIN = findViewById(R.id.editTextPIN)
        buttonSave = findViewById(R.id.button4)

        val backIcon: ImageView = findViewById(R.id.back_button)
        backIcon.setOnClickListener {
            // Intent to open HomePageActivity
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar2)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val dateTextView1: TextView = findViewById(R.id.date_text)

        val currentDate1 = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
        dateTextView1.text = currentDate1

        val switchMode: SwitchCompat = findViewById(R.id.switch_mode)

        switchMode.isChecked = isDarkMode

        switchMode.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            saveThemePreference(isChecked)
            recreate() // Refresh aktivitas agar perubahan tema terlihat
        }

        // Set initial values from intent
        val username = intent.getStringExtra("username")
        editTextName.setText(username)

        buttonSave.setOnClickListener {
            saveProfileData()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                // Aksi untuk tombol profile, misalnya pindah ke halaman profil
                val intent = Intent(this, ProfileSettings::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveThemePreference(isDarkMode: Boolean) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean("isDarkMode", isDarkMode)
            apply() // Pastikan perubahan disimpan
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