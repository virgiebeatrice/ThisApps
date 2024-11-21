package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
<<<<<<< HEAD
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class DiaryActivity : AppCompatActivity() {
=======
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class DiaryActivity : AppCompatActivity() {

>>>>>>> 6f85c5a22a913658878442aa0bb73f80f21ca75e
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary)

        // Inisialisasi Firestore
        firestore = FirebaseFirestore.getInstance()

<<<<<<< HEAD
        val floatingActionButton: ImageButton = findViewById(R.id.floating_action_button)
        floatingActionButton.setOnClickListener {
            // Navigasi ke BerandaActivity
            val intent = Intent(this, BerandaActivity::class.java)
            startActivity(intent)
        }

        val textButton: Button = findViewById(R.id.textButton)
        val editTextDate: EditText = findViewById(R.id.editTextDate2)
        val editTextTitle: EditText = findViewById(R.id.editTextText)
        val editTextisi: EditText = findViewById(R.id.editTextIsi)

        textButton.setOnClickListener {
            // Ambil data dari input
            val date = editTextDate.text.toString()
            val title = editTextTitle.text.toString()
            val content = editTextisi.text.toString()

            // Validasi input
            if (date.isEmpty() || title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simpan data ke Firestore
            val diaryData = hashMapOf(
                "date" to date,
                "title" to title,
                "content" to content
            )

            firestore.collection("Diaries")
                .add(diaryData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Diary berhasil disimpan", Toast.LENGTH_SHORT).show()
                    // Bersihkan input setelah disimpan
                    editTextDate.text.clear()
                    editTextTitle.text.clear()
                    editTextisi.text.clear()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
=======
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
>>>>>>> 6f85c5a22a913658878442aa0bb73f80f21ca75e
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
