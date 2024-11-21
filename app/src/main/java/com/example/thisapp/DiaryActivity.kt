package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class DiaryActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary)

        // Inisialisasi Firestore
        firestore = FirebaseFirestore.getInstance()

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
        }
    }
}