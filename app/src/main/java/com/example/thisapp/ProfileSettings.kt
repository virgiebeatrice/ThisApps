package com.example.thisapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

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
        setContentView(R.layout.activity_profile_settings)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        textViewUsername = findViewById(R.id.usernametext)
        textViewEmail = findViewById(R.id.emailtext)
        sectionEditProfile = findViewById(R.id.section_edit_profile)
        logoutButton = findViewById(R.id.button2)
        imageViewBack = findViewById(R.id.imageViewback)

        // Fetch user data and display it
        getUserDataFromFirestore()

        // Edit Profile
        sectionEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("username", textViewUsername.text.toString())
            intent.putExtra("email", textViewEmail.text.toString())
            startActivity(intent)
        }

        // Logout
        logoutButton.setOnClickListener {
            performLogout()
        }

        // Back button
        imageViewBack.setOnClickListener {
            val intent = Intent(this, BerandaActivity::class.java)
            startActivity(intent)
            finish()
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
