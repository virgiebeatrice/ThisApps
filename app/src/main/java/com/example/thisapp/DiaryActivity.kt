package com.example.thisapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

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

        // Tambahkan listener untuk membuka kalender
        editTextDate.setOnClickListener {
            showDatePickerDialog(editTextDate)
        }

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

    // Fungsi untuk menampilkan DatePickerDialog
    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Set tanggal ke format yyyy-MM-dd
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                editText.setText(formattedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }
}