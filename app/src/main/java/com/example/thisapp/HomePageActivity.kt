package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomePageActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var historyAdapter: DiaryAdapter
    private val historyList = mutableListOf<DiaryEntry>()
    private val calendar: Calendar = Calendar.getInstance()

    // Initialize RecyclerView and No Data TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var noDataTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("isDarkMode", false)

        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        // Initialize FirebaseAuth and Firestore
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Get userId from FirebaseAuth
        val currentUser = firebaseAuth.currentUser
        val userId = currentUser?.uid  // Get UID of the currently logged-in user

        // If user is not logged in, redirect to login page
        if (userId == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()  // Close HomePageActivity if user is not logged in
        }

        // Initialize RecyclerView and No Data TextView
        recyclerView = findViewById(R.id.diaryRecyclerView)
        noDataTextView = findViewById(R.id.noDataTextView)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        historyAdapter = DiaryAdapter(historyList)
        recyclerView.adapter = historyAdapter


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

        // Navigate to MainActivity (mood scan)
        val scanMoodButton: ImageButton = findViewById(R.id.scan_mood_button) // Ganti dengan ID ImageButton
        scanMoodButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Date navigation buttons (left and right)
        val dateTextView: TextView = findViewById(R.id.textViewDate)
        updateDateText(dateTextView)

        findViewById<ImageView>(R.id.imageViewLeft).setOnClickListener {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            updateDateText(dateTextView)
            loadHistoryForDate(getFormattedDate(), userId)
        }

        findViewById<ImageView>(R.id.imageViewRight).setOnClickListener {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            updateDateText(dateTextView)
            loadHistoryForDate(getFormattedDate(), userId)
        }

        // Display data for today's date
        loadHistoryForDate(getFormattedDate(), userId)
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

    private fun updateDateText(dateTextView: TextView) {
        dateTextView.text = getFormattedDate()
    }

    private fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun loadHistoryForDate(date: String, userId: String?) {
        if (userId != null) {
            firestore.collection("Diaries")
                .whereEqualTo("date", date)  // Filter by date
                .whereEqualTo("userId", userId)  // Filter by userId
                .get()
                .addOnSuccessListener { documents ->
                    historyList.clear()
                    if (documents.isEmpty) {
                        // Display "No Data" message and hide RecyclerView
                        recyclerView.visibility = View.GONE
                        noDataTextView.visibility = View.VISIBLE
                        noDataTextView.text = "There are no diary entries for $date"
                    } else {
                        // Show data in RecyclerView and hide "No Data" message
                        recyclerView.visibility = View.VISIBLE
                        noDataTextView.visibility = View.GONE

                        for (document in documents) {
                            val title = document.getString("title") ?: "There is no title"
                            val content = document.getString("content") ?: "Empty content"
                            val mood= document.getString("mood") ?: "No mood"
                            historyList.add(DiaryEntry(date, title, content, mood))
                        }
                        historyAdapter.notifyDataSetChanged()  // Notify adapter to update the UI
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Failed to load data: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}