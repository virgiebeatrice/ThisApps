package com.example.thisapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import java.text.SimpleDateFormat
import java.util.Date

class ProfileSettings : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var textViewUsername: TextView
    private lateinit var textViewEmail: TextView
    private lateinit var sectionEditProfile: LinearLayout
    private lateinit var logoutButton: Button
    private lateinit var imageViewBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set tema berdasarkan preferensi
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("isDarkMode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        setContentView(R.layout.activity_profile_settings)


//        // Dalam metode onCreate()
//        val backButton: ImageButton = findViewById(R.id.back_button)
//        backButton.setOnClickListener {
//            val intent = Intent(this, HomePageActivity::class.java)
//            startActivity(intent)
//            val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
//            val lastActivity = sharedPreferences.getString("last_activity", "HomePageActivity") ?: "HomePageActivity"
//
////            val intent = when (lastActivity) {
////                "ProfileSettings" -> Intent(this, ProfileSettings::class.java)
////                else -> Intent(this, BerandaActivity::class.java)
////            }
////
////            startActivity(intent)
////            finish() // Menyelesaikan aktivitas saat ini (optional)
//        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            onBackPressed()
        }

        val dateTextView: TextView = findViewById(R.id.date_text)

        val currentDate = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
        dateTextView.text = currentDate

        val switchMode: SwitchCompat = findViewById(R.id.switch_mode)
        switchMode.isChecked = isDarkMode
        switchMode.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            saveThemePreference(isChecked)
            recreate() // Terapkan tema baru
        }

        textViewUsername = findViewById(R.id.usernametext)
        textViewEmail = findViewById(R.id.emailtext)
        sectionEditProfile = findViewById(R.id.section_edit_profile)
        logoutButton = findViewById(R.id.button2)
//        imageViewBack = findViewById(R.id.imageViewback)

        // Fetch user data and display it
        getUserDataFromFirestore()

        // Navbar
        val toolbar: Toolbar = findViewById(R.id.toolbar2)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)


        // Edit Profile
        sectionEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("username", textViewUsername.text.toString())
            intent.putExtra("email", textViewEmail.text.toString())
            startActivity(intent)
        }

        // Logout Button - Add a more stylish logout animation/dialog
        logoutButton.setOnClickListener {
            // Custom Dialog with animations
            val dialog = AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    performLogout()
                }
                .setNegativeButton("No", null)
                .create()

            // Custom Animation (optional)
            dialog.window?.attributes?.windowAnimations = android.R.style.Animation_Dialog
            dialog.show()
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
            apply()
        }
    }

    private fun getUserDataFromFirestore() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userEmail = currentUser.email?.lowercase(Locale.ROOT)

            if (!userEmail.isNullOrEmpty()) {
                db.collection("Users")
                    .document(userEmail)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val username = document.getString("username")
                            val email = document.getString("email")

                            textViewUsername.text = username ?: "Username not set"
                            textViewEmail.text = email ?: userEmail
                        } else {
                            Toast.makeText(this, "User data not found in Firestore", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to fetch user data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "User email is not available", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performLogout() {
        sharedPreferences.edit().clear().apply() // Clear shared preferences
        FirebaseAuth.getInstance().signOut() // Sign out from Firebase

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
