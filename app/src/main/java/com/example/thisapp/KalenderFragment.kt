package com.example.thisapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class KalenderFragment : Fragment() {


    private lateinit var calendarView: CalendarView
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_kalender, container, false)
        calendarView = view.findViewById(R.id.calendarView2)
        firestore = FirebaseFirestore.getInstance()

        loadEvents()
        return view
    }

    private fun loadEvents() {
        firestore.collection("events")
            .get()
            .addOnSuccessListener { kalender ->
                for (kalender in kalender) {
                    val date = kalender.id
                    val title = kalender.getString("title") ?: ""
                    val content = kalender.getString("content") ?: ""

                    // Logika tampilan
                    calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                        val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                        if (selectedDate == date) {
                            Toast.makeText(context, "Event: $title\n$content", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}