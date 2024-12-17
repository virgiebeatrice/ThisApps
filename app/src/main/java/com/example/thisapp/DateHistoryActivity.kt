package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar

class DateHistoryActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var historyAdapter: DiaryAdapter
    private val historyList = mutableListOf<DiaryEntry>()

    // Declare RecyclerView and No Data TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var noDataTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_history)

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
        val userId = currentUser?.uid  // Get the UID of the currently logged-in user

        // If user is not logged in, redirect to login page
        if (userId == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()  // Close DateHistoryActivity if user is not logged in
        }

        // Initialize RecyclerView and No Data TextView
        recyclerView = findViewById(R.id.diaryRecyclerView)
        noDataTextView = findViewById(R.id.noDataTextView)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        historyAdapter = DiaryAdapter(historyList)
        recyclerView.adapter = historyAdapter

        // Set up Home Icon
        val homeIcon: ImageView = findViewById(R.id.home_button)
        homeIcon.setOnClickListener {
            // Intent to open HomePageActivity
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar2)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val dateTextView: TextView = findViewById(R.id.date_text)

        val currentDate1 = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
        dateTextView.text = currentDate1

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

        // Set listener for CalendarView
        val calendarView: CalendarView = findViewById(R.id.calendarView2)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            loadHistoryForDate(date, userId)
        }

        // Display data for today's date
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        loadHistoryForDate(currentDate, userId)
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
                            val content = document.getString("content") ?: "Empty Content"
                            val mood = document.getString("mood") ?: "No mood"
                            historyList.add(DiaryEntry(date, title, content, mood))
                        }
                        Log.d("DateHistoryActivity", "HistoryList after loading: $historyList")
                        historyAdapter.notifyDataSetChanged()}
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
