package com.example.thisapp

import android.os.Bundle
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class KalenderActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var database: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kalender)

        // Inisialisasi view
        calendarView = findViewById(R.id.calendarView2)

        // Inisialisasi Firebase
        database = FirebaseDatabase.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Ambil data dari database
        loadEventsFromFirestore() // Untuk Firestore
        // loadEventsFromRealtimeDatabase() // Untuk Realtime Database
    }

    private fun loadEventsFromFirestore() {
        firestore.collection("events")
            .get()
            .addOnSuccessListener { kalender ->
                for (kalender in kalender) {
                    val date = kalender.id // Format tanggal "yyyy-MM-dd"
                    val title = kalender.getString("title") ?: ""
                    val content = kalender.getString("content") ?: ""

                    // Menampilkan data pada kalender
                    showEventOnDate(date, title, content)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadEventsFromRealtimeDatabase() {
        database.reference.child("events").get()
            .addOnSuccessListener { snapshot ->
                snapshot.children.forEach { data ->
                    val date = data.key ?: ""
                    val title = data.child("title").value.toString()
                    val content = data.child("content").value.toString()

                    // Menampilkan data pada kalender
                    showEventOnDate(date, title, content)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEventOnDate(date: String, title: String, content: String) {
        // Logika untuk menampilkan event di tanggal tertentu
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            if (selectedDate == date) {
                Toast.makeText(this, "Event: $title\n$content", Toast.LENGTH_LONG).show()
            }
        }
    }
}
