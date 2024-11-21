package com.example.thisapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class DiaryActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary)

        // Inisialisasi Firestore
        firestore = FirebaseFirestore.getInstance()

        val dateEditText: EditText = findViewById(R.id.editTextDate2)
        val titleEditText: EditText = findViewById(R.id.editTextText)
        val isiEditText: EditText = findViewById(R.id.editTextIsi)
        val doneButton: Button = findViewById(R.id.textButton)

        doneButton.setOnClickListener {
            val date = dateEditText.text.toString().trim()
            val title = titleEditText.text.toString().trim()
            val isi = isiEditText.text.toString().trim()

            if (date.isEmpty() || title.isEmpty() || isi.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveDiary(date, title, isi)
        }
    }

    private fun saveDiary(date: String, title: String, isi: String) {
        // Membuat objek data untuk disimpan
        val diary = hashMapOf(
            "date" to date,
            "title" to title,
            "isi" to isi,
            "timestamp" to Timestamp.now() // Menyimpan waktu saat ini dengan format Firestore
        )

        // Menyimpan data ke Firestore di koleksi "Diaries"
        firestore.collection("Diaries")
            .add(diary)
            .addOnSuccessListener {
                Toast.makeText(this, "Diary saved successfully", Toast.LENGTH_SHORT).show()
                clearFields() // Mengosongkan input setelah berhasil disimpan
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Failed to save diary: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadDiaries() {
        firestore.collection("Diaries")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val title = document.getString("title")
                    val isi = document.getString("isi")
                    val date = document.getString("date")
                    // Tambahkan logika untuk menampilkan diary di UI
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Failed to load diaries: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        findViewById<EditText>(R.id.editTextDate2).text.clear()
        findViewById<EditText>(R.id.editTextText).text.clear()
        findViewById<EditText>(R.id.editTextIsi).text.clear()
    }
}
