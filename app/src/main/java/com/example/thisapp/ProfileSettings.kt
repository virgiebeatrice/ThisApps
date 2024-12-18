package com.example.thisapp

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ProfileSettings : AppCompatActivity() {
    private var db: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null
    private var textViewUsername: TextView? = null
    private var textViewEmail: TextView? = null
    private var sectionEditProfile: LinearLayout? = null
    private var logoutButton: Button? = null
    private var imageViewBack: ImageView? = null
    private var imageViewAvatar: ImageView? = null
    private var sharedPreferences: SharedPreferences? = null

    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>

    companion object {
        private var isMediaManagerInitialized = false
        private const val REQUEST_CODE_EDIT_PROFILE = 1001 // Define request code for EditProfileActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        // Initialize Firebase and SharedPreferences
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Initialize view components
        textViewUsername = findViewById(R.id.usernametext)
        textViewEmail = findViewById(R.id.emailtext)
        sectionEditProfile = findViewById(R.id.section_edit_profile)
        logoutButton = findViewById(R.id.button2)
        imageViewBack = findViewById(R.id.imageViewback)
        imageViewAvatar = findViewById(R.id.imageView4)

        // Initialize Cloudinary only once
        if (!isMediaManagerInitialized) {
            try {
                MediaManager.init(this, mapOf(
                    "cloud_name" to "dexxmtbcd",
                    "api_key" to "629467946643476",
                    "api_secret" to "DvGbqiuw4uQOyip-DlMZfCrrvVQ"
                ))
                isMediaManagerInitialized = true // Mark as initialized
            } catch (e: Exception) {
                Log.e("ProfileSettings", "MediaManager initialization failed: ${e.message}")
            }
        }

        // Load user data
        loadUserProfile()

        // Initialize image picker
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { uploadImageToCloudinary(it) }
        }

        imageViewAvatar?.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // Handling the edit profile section click
        sectionEditProfile?.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java).apply {
                putExtra("username", textViewUsername?.text.toString())
                putExtra("email", textViewEmail?.text.toString())
            }
            startActivityForResult(intent, REQUEST_CODE_EDIT_PROFILE)
        }

        // Logout button handling
        logoutButton?.setOnClickListener {
            auth?.signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            sharedPreferences?.edit()?.clear()?.apply()

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Back button handling
        imageViewBack?.setOnClickListener {
            startActivity(Intent(this, BerandaActivity::class.java))
            finish()
        }
    }

    // This method is called after finishing EditProfileActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_EDIT_PROFILE && resultCode == RESULT_OK) {
            val updatedUsername = data?.getStringExtra("updatedUsername") ?: return

            // Update the username in Firestore and SharedPreferences
            val email = FirebaseAuth.getInstance().currentUser?.email
            if (email != null) {
                db?.collection("Users")?.document(email)
                    ?.update("username", updatedUsername)
                    ?.addOnSuccessListener {
                        textViewUsername?.text = updatedUsername
                        sharedPreferences?.edit()?.putString("username", updatedUsername)?.apply()
                        Toast.makeText(this, "Username updated successfully", Toast.LENGTH_SHORT).show()
                    }
                    ?.addOnFailureListener {
                        Toast.makeText(this, "Failed to update username", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun loadUserProfile() {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email

        if (userEmail != null) {
            db?.collection("Users")?.document(userEmail)?.get()
                ?.addOnSuccessListener { document: DocumentSnapshot? ->
                    if (document != null && document.exists()) {
                        val avatarUrl = document.getString("avatarUrl") ?: ""
                        val username = document.getString("username") ?: "Unknown User"
                        val email = document.getString("email") ?: "Unknown Email"

                        textViewUsername?.text = username
                        textViewEmail?.text = email

                        // Ensure avatar URL is secure (HTTPS)
                        val secureAvatarUrl = if (avatarUrl.startsWith("http://")) {
                            avatarUrl.replace("http://", "https://")
                        } else {
                            avatarUrl
                        }

                        // Load avatar with Glide
                        if (secureAvatarUrl.startsWith("https://")) {
                            Glide.with(this)
                                .load(secureAvatarUrl)
                                .placeholder(R.drawable.placeholder)
                                .error(R.drawable.error_image)
                                .into(imageViewAvatar!!)
                        } else {
                            imageViewAvatar?.setImageResource(R.drawable.error_image)
                        }
                    } else {
                        Log.e("ProfileSettings", "Document does not exist.")
                    }
                }
                ?.addOnFailureListener { exception ->
                    Log.e("ProfileSettings", "Error getting user data: ", exception)
                }
        } else {
            Log.e("ProfileSettings", "User not logged in.")
        }
    }

    private fun uploadImageToCloudinary(uri: Uri) {
        val filePath = getFilePathFromUri(uri)

        if (filePath.isNotEmpty()) {
            MediaManager.get().upload(filePath)
                .option("public_id", "avatar_${System.currentTimeMillis()}")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val avatarUrl = resultData["url"]?.toString()?.replace("http://", "https://") ?: ""

                        if (avatarUrl.isNotEmpty()) {
                            auth?.currentUser?.email?.let { email ->
                                db?.collection("Users")?.document(email)
                                    ?.set(mapOf("avatarUrl" to avatarUrl), SetOptions.merge())
                                    ?.addOnSuccessListener {
                                        Glide.with(this@ProfileSettings)
                                            .load(avatarUrl)
                                            .placeholder(R.drawable.placeholder)
                                            .error(R.drawable.error_image)
                                            .into(imageViewAvatar!!)
                                        Toast.makeText(
                                            this@ProfileSettings,
                                            "Avatar updated successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    ?.addOnFailureListener { e ->
                                        Toast.makeText(
                                            this@ProfileSettings,
                                            "Error updating avatar: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Toast.makeText(
                            this@ProfileSettings,
                            "Upload failed: ${error.description}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                })
                .dispatch()
        } else {
            Toast.makeText(this, "Failed to get file path", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFilePathFromUri(uri: Uri): String {
        var filePath = ""
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val tempFile = File.createTempFile("upload", ".tmp", cacheDir)
                FileOutputStream(tempFile).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } != -1) {
                        outputStream.write(buffer, 0, length)
                    }
                }
                filePath = tempFile.absolutePath
            }
        } catch (e: IOException) {
            Log.e("ProfileSettings", "Error getting file path from URI", e)
        }
        return filePath
    }
}
