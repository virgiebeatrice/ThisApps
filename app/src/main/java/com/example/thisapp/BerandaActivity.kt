package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BerandaActivity : AppCompatActivity() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beranda)

        // Referensi UI
        val greetingTextView: TextView = findViewById(R.id.greeting_text)
        val moodImageView: ImageView = findViewById(R.id.moodImageView)
        val subtitleTextView: TextView = findViewById(R.id.subtitle_text)
        val moodWordTextView: TextView = findViewById(R.id.mood_word)
        val writeDiaryButton: Button = findViewById(R.id.write_diary_button)
        val profileIcon: ImageButton = findViewById(R.id.profile_icon)

        // Set greeting
        setupGreeting(greetingTextView)

        // Tampilkan mood terakhir
        displayLastDetectedMood(moodImageView, subtitleTextView, moodWordTextView, writeDiaryButton)

        // Tombol untuk menulis catatan
        writeDiaryButton.setOnClickListener {
            navigateToDiary()
        }

        // Navigasi ke pengaturan profil
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
                    val username = document?.getString("username") ?: "User"
                    greetingTextView.text = "Hello, $username"
                    saveUsernameToPrefs(username)
                }
                .addOnFailureListener {
                    greetingTextView.text = "Hello, User"
                }
        } else {
            greetingTextView.text = "Hello, Guest"
        }
    }

    private fun saveUsernameToPrefs(username: String) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("username", username)
            apply()
        }
    }

    private fun displayLastDetectedMood(
        moodImageView: ImageView,
        subtitleTextView: TextView,
        moodWordTextView: TextView,
        writeDiaryButton: Button
    ) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val mood = document?.getString("mood")

                    if (mood != null) {
                        saveMoodToPrefs(mood)
                        updateUIWithMood(mood, moodImageView, subtitleTextView, moodWordTextView, writeDiaryButton)
                    } else {
                        loadMoodFromPrefs(moodImageView, subtitleTextView, moodWordTextView, writeDiaryButton)
                    }
                }
                .addOnFailureListener {
                    loadMoodFromPrefs(moodImageView, subtitleTextView, moodWordTextView, writeDiaryButton)
                }
        } else {
            loadMoodFromPrefs(moodImageView, subtitleTextView, moodWordTextView, writeDiaryButton)
        }
    }

    private fun saveMoodToPrefs(mood: String) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("last_mood", mood)
            apply()
        }
    }

    private fun loadMoodFromPrefs(
        moodImageView: ImageView,
        subtitleTextView: TextView,
        moodWordTextView: TextView,
        writeDiaryButton: Button
    ) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val lastMood = sharedPreferences.getString("last_mood", null)

        if (lastMood != null) {
            updateUIWithMood(lastMood, moodImageView, subtitleTextView, moodWordTextView, writeDiaryButton)
        } else {
            // Tampilkan tampilan default
            moodImageView.setImageResource(R.drawable.neutral)
            subtitleTextView.text = "No mood detected yet."
            moodWordTextView.text = "Please detect your mood to get started."
        }
    }

    private fun updateUIWithMood(
        mood: String,
        moodImageView: ImageView,
        subtitleTextView: TextView,
        moodWordTextView: TextView,
        writeDiaryButton: Button
    ) {
        val moodDrawable = when (mood) {
            "Happy 😄" -> R.drawable.happy
            "Sad 😢" -> R.drawable.sad
            "Angry 😡" -> R.drawable.angry
            "Scared 😨" -> R.drawable.scared
            "Surprised 😲" -> R.drawable.surprised
            else -> R.drawable.neutral
        }
        moodImageView.setImageResource(moodDrawable)

        val (subtitle, moodWord, moodColor) = when (mood) {
            "Happy 😄" -> Triple("You are in a good mood today :)", "Glad to see you happy", R.color.yellow)
            "Sad 😢" -> Triple("Not your best day?", "It's okay, tell us about it", R.color.blue)
            "Angry 😡" -> Triple("Looks like you’re a bit angry right now", "What’s wrong?", R.color.red)
            "Scared 😨" -> Triple("Feeling a bit uneasy?", "It’s okay to take things one step at a time", R.color.purple)
            "Surprised 😲" -> Triple("Something unexpected happened to you!", "Hope it’s a good kind of surprise—tell me more!", R.color.orange)
            else -> Triple("Just a calm day, huh?", "Steady as you go, balance is key", R.color.grey)
        }

        subtitleTextView.text = subtitle
        moodWordTextView.text = moodWord
        writeDiaryButton.backgroundTintList = ContextCompat.getColorStateList(this, moodColor)
    }

    private fun navigateToDiary() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val lastMood = sharedPreferences.getString("last_mood", null)

        if (lastMood != null) {
            val intent = Intent(this, DiaryActivity::class.java)
            intent.putExtra("MOOD", lastMood)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Mood not detected. Please detect your mood first.", Toast.LENGTH_SHORT).show()
        }
    }
}
