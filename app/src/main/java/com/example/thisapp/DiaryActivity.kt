package com.example.thisapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar  // Pastikan untuk mengimpor ini

class DiaryActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary)

        // Inisialisasi Firestore
        firestore = FirebaseFirestore.getInstance()

        val backgroundRectangle: FrameLayout = findViewById(R.id.backgroundRectangle)
        val floatingActionButton: ImageButton = findViewById(R.id.floating_action_button)
        val textButton: Button = findViewById(R.id.textButton)
        val textViewDate: TextView = findViewById(R.id.editTextDate2)
        val editTextTitle: EditText = findViewById(R.id.editTextText)
        val editTextIsi: EditText = findViewById(R.id.editTextIsi)

        // Ambil mood dari Intent tanpa fallback
        val mood = intent.getStringExtra("MOOD") // Jika mood tidak ada, nilai ini akan null

        // Periksa apakah mood ada dan ganti background sesuai mood
        if (mood != null) {
            when (mood) {
                "Happy 😄" -> backgroundRectangle.setBackgroundResource(R.drawable.gradient_happy)
                "Sad 😢" -> backgroundRectangle.setBackgroundResource(R.drawable.gradient_sad)
                "Angry 😡" -> backgroundRectangle.setBackgroundResource(R.drawable.gradient_angry)
                "Scared 😨" -> backgroundRectangle.setBackgroundResource(R.drawable.gradient_scared)
                "Surprised 😲" -> backgroundRectangle.setBackgroundResource(R.drawable.gradient_surprised)
                else -> backgroundRectangle.setBackgroundResource(R.drawable.gradient_neutral)
            }
        } else {
            // Jika mood tidak ada, bisa kosongkan atau beri default behavior lain
            backgroundRectangle.setBackgroundResource(R.drawable.gradient_neutral)
        }

        // Navigasi ke BerandaActivity
        floatingActionButton.setOnClickListener {
            val intent = Intent(this, BerandaActivity::class.java)
            startActivity(intent)
        }

        // Tambahkan listener untuk membuka kalender saat klik pada TextView
        textViewDate.setOnClickListener {
            showDatePickerDialog(textViewDate)
        }

        // Tombol untuk menyimpan diary
        textButton.setOnClickListener {
            // Ambil data dari input
            val date = textViewDate.text.toString()
            val title = editTextTitle.text.toString()
            val content = editTextIsi.text.toString()

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
                    textViewDate.text = "Select a date"
                    editTextTitle.text.clear()
                    editTextIsi.text.clear()

                    val intent = Intent(this, HomePageActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Fungsi untuk menampilkan DatePickerDialog
    private fun showDatePickerDialog(textView: TextView) {
        val calendar = Calendar.getInstance()  // Pastikan ini ada di sini
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Set tanggal ke format yyyy-MM-dd
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                textView.text = formattedDate // Tampilkan tanggal yang dipilih di TextView
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }
}
