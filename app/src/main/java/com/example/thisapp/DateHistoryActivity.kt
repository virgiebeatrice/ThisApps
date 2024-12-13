package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
        val homeIcon: ImageView = findViewById(R.id.home_icon)
        homeIcon.setOnClickListener {
            // Intent to open HomePageActivity
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
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
                            val mood = document.getString("mood") ?: "Tidak ada mood"
                            historyList.add(DiaryEntry(date, title, content, mood))
                        }
                        Log.d("DateHistoryActivity", "HistoryList after loading: $historyList")
                        historyAdapter.notifyDataSetChanged()}
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
