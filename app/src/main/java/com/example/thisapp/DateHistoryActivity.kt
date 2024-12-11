package com.example.thisapp

import DiaryAdapter
import android.os.Bundle
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateHistoryActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var historyAdapter: DiaryAdapter
    private val historyList = mutableListOf<DiaryEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_history)

        firestore = FirebaseFirestore.getInstance()

        // Inisialisasi RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.diaryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        historyAdapter = DiaryAdapter(historyList)
        recyclerView.adapter = historyAdapter

        // Setel listener untuk CalendarView
        val calendarView: CalendarView = findViewById(R.id.calendarView2)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            loadHistoryForDate(date)
        }

        // Tampilkan data untuk tanggal hari ini
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        loadHistoryForDate(currentDate)
    }

    private fun loadHistoryForDate(date: String) {
        firestore.collection("Diaries")
            .whereEqualTo("date", date)
            .get()
            .addOnSuccessListener { documents ->
                historyList.clear()
                for (document in documents) {
                    val title = document.getString("title") ?: ""
                    val content = document.getString("content") ?: ""
                    historyList.add(DiaryEntry(date, title, content))
                }
                historyAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal memuat data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
