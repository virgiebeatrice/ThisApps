package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
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

        // Initialize Home Icon
        val homeIcon: ImageView = findViewById(R.id.home_icon)
        homeIcon.setOnClickListener {
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
        }

        // Navigate to MainActivity (mood scan)
        val scanMoodButton: Button = findViewById(R.id.scan_mood_button)
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
                        noDataTextView.text = "Tidak ada data untuk tanggal $date"
                    } else {
                        // Show data in RecyclerView and hide "No Data" message
                        recyclerView.visibility = View.VISIBLE
                        noDataTextView.visibility = View.GONE

                        for (document in documents) {
                            val title = document.getString("title") ?: "Tidak ada judul"
                            val content = document.getString("content") ?: "Konten kosong"
                            val mood= document.getString("mood") ?: "mood kosong"
                            historyList.add(DiaryEntry(date, title, content, mood))
                        }
                        historyAdapter.notifyDataSetChanged()  // Notify adapter to update the UI
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Gagal memuat data: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}
