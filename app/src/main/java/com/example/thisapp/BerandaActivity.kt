package com.example.thisapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BerandaActivity : AppCompatActivity() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beranda)

        // Referensi ke UI
        val greetingTextView: TextView = findViewById(R.id.greeting_text)
        val moodImageView: ImageView = findViewById(R.id.moodImageView)
        val subtitleTextView: TextView = findViewById(R.id.subtitle_text) // Subtitle TextView
        val moodWordTextView: TextView = findViewById(R.id.mood_word) // Mood Word TextView
        val writeDiaryButton: Button = findViewById(R.id.write_diary_button)
        val profileIcon: ImageButton = findViewById(R.id.profile_icon)

        // Atur greeting text dengan username
        setupGreeting(greetingTextView)

        // Ambil dan tampilkan mood dari intent
        displayDetectedMood(moodImageView, subtitleTextView, moodWordTextView, writeDiaryButton)

        // Navigasi ke DiaryActivity
        writeDiaryButton.setOnClickListener {
            // Ambil mood yang sudah terdeteksi
            val detectedMood = intent.getStringExtra("MOOD")

            if (detectedMood != null) {
                // Buat Intent ke DiaryActivity dan kirimkan mood
                val intent = Intent(this, DiaryActivity::class.java)
                intent.putExtra("MOOD", detectedMood) // Kirimkan nilai mood ke DiaryActivity
                startActivity(intent)
            } else {
                // Tangani kasus jika mood tidak terdeteksi (misalnya, tampilkan Toast)
                Toast.makeText(this, "Mood tidak terdeteksi", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigasi ke ProfileSettings
        profileIcon.setOnClickListener {
            startActivity(Intent(this, ProfileSettings::class.java))
        }
    }

    private fun setupGreeting(greetingTextView: TextView) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val sharedUsername = sharedPreferences.getString("username", null)
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (sharedUsername != null) {
            greetingTextView.text = "Hello, $sharedUsername"
        } else if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username") ?: "User"
                        greetingTextView.text = "Hello, $username"
                        saveUsernameToPrefs(username)
                    } else {
                        greetingTextView.text = "Hello, User"
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load user data.", Toast.LENGTH_SHORT).show()
                    greetingTextView.text = "Hello, User"
                }
        } else {
            greetingTextView.text = "Hello, Guest"
        }
    }

    private fun displayDetectedMood(
        moodImageView: ImageView,
        subtitleTextView: TextView,
        moodWordTextView: TextView,
        writeDiaryButton: Button
    ) {
        val mood = intent.getStringExtra("MOOD") // Ambil mood dari Intent
        if (mood != null) {
            // Pilih drawable sesuai mood
            val moodDrawable = when (mood) {
                "Happy 😄" -> R.drawable.happy
                "Sad 😢" -> R.drawable.sad
                "Angry 😡" -> R.drawable.angry
                "Scared 😨" -> R.drawable.scared
                "Surprised 😲" -> R.drawable.surprised
                else -> R.drawable.neutral // Fallback
            }
            moodImageView.setImageResource(moodDrawable)

            // Teks subtitle dan mood word berdasarkan mood
            val (subtitle, moodWord, moodColor) = when (mood) {
                "Happy 😄" -> Triple("You are in a good\nmood today :)", "Glad to see you happy", R.color.yellow)
                "Sad 😢" -> Triple("Not your best day?", "It's okay, tell us about it", R.color.blue)
                "Angry 😡" -> Triple("Looks like you’re a\nbit angry right now", "What’s wrong?", R.color.red)
                "Scared 😨" -> Triple("Feeling a bit uneasy?", "It’s okay to take things\none step at a time", R.color.purple)
                "Surprised 😲" -> Triple("Something unexpected\nhappened to you!", "Hope it’s a good kind of\nsurprise—tell me more!", R.color.orange)
                else -> Triple("Just a calm day,\nhuh?", "Steady as you go,\nbalance is key", R.color.grey)
            }

            subtitleTextView.text = subtitle
            moodWordTextView.text = moodWord

            // Ubah warna tombol sesuai mood
            writeDiaryButton.backgroundTintList = ContextCompat.getColorStateList(this, moodColor)
        } else {
            // Kosongkan tampilan jika mood tidak ditemukan
            moodImageView.setImageDrawable(null)
            subtitleTextView.text = ""
            moodWordTextView.text = ""
        }
    }

    private fun saveUsernameToPrefs(username: String) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("username", username)
            apply()
        }
    }
}
