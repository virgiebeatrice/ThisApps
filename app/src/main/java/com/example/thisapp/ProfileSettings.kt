package com.example.thisapp

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ProfileSettings : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var textViewUsername: TextView
    private lateinit var textViewEmail: TextView
    private lateinit var sectionEditProfile: LinearLayout
    private lateinit var sectionChooseLanguage: LinearLayout
    private lateinit var logoutButton: Button
    private lateinit var imageViewBack: ImageView

    private val editProfileResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Ambil data yang diperbarui dari Intent
                val updatedUsername = result.data?.getStringExtra("updatedUsername")
                val updatedEmail = result.data?.getStringExtra("updatedEmail")

                // Update UI dengan data yang baru
                textViewUsername.text = updatedUsername ?: "Username"
                textViewEmail.text = updatedEmail ?: "Email"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Bind the views
        textViewUsername = findViewById(R.id.usernametext)
        textViewEmail = findViewById(R.id.emailtext)
        sectionEditProfile = findViewById(R.id.section_edit_profile)
        sectionChooseLanguage = findViewById(R.id.section_choose_language)
        logoutButton = findViewById(R.id.button2)
        imageViewBack = findViewById(R.id.imageViewback) // Bind ImageView Back

        // Get user data from Firestore
        getUserData()

        // Set up click listeners
        sectionEditProfile.setOnClickListener {
            // Navigate to EditProfileActivity
            val intent = Intent(this, EditProfileActivity::class.java)

            // Kirim data lama ke EditProfileActivity (username dan email)
            intent.putExtra("username", textViewUsername.text.toString())
            intent.putExtra("email", textViewEmail.text.toString())

            // Mulai EditProfileActivity dan tunggu hasilnya
            editProfileResultLauncher.launch(intent)
        }

        sectionChooseLanguage.setOnClickListener {
            // Show language selection dialog
            showLanguageDialog()
        }

        logoutButton.setOnClickListener {
            // Log out the user
            auth.signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Navigate to login screen or main screen
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Set click listener for the back icon
        imageViewBack.setOnClickListener {
            // Navigate back to the home or main activity
            val intent = Intent(this, BerandaActivity::class.java)  // Ganti MainActivity dengan activity utama
            startActivity(intent)
            finish() // Optional: finish this activity if you don't want it to remain in the back stack
        }
    }

    private fun getUserData() {
        // Check if the user is logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Fetch user data from Firestore
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // Assuming user data is stored with fields "username" and "email"
                        val username = document.getString("username")
                        val email = document.getString("email")

                        // Set the retrieved data to the TextViews
                        textViewUsername.text = username ?: "Username"
                        textViewEmail.text = email ?: "Email"
                    } else {
                        // Handle error (user data not found)
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    // Handle failure to fetch data
                    Toast.makeText(this, "Failed to fetch user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Handle case where user is not logged in
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Indonesia", "Español", "Melayu")
        val builder = AlertDialog.Builder(this)
            .setTitle(getString(R.string.choose_language))
            .setItems(languages) { _, which ->
                when (which) {
                    0 -> changeLanguage("en")  // English
                    1 -> changeLanguage("id")  // Indonesia
                    2 -> changeLanguage("es")  // Spanish
                    3 -> changeLanguage("ms")  // Malay
                }
            }
        builder.show()
    }

    private fun changeLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)

        // Recreate the activity to apply the language change
        val intent = Intent(this, ProfileSettings::class.java)
        startActivity(intent)
        finish()
    }
}