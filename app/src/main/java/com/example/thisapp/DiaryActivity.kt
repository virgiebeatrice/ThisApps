package com.example.thisapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DiaryActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary)

        // Inisialisasi FirebaseAuth dan Firestore
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Ambil userId
        val currentUser = firebaseAuth.currentUser
        userId = currentUser?.uid

        // Jika pengguna belum login, arahkan ke halaman login
        if (userId == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Inisialisasi komponen UI
        val tvDate = findViewById<TextView>(R.id.tvDate)
        val backgroundRectangle: FrameLayout = findViewById(R.id.backgroundRectangle)
        val floatingActionButton: ImageButton = findViewById(R.id.floating_action_button)
        val textButton: Button = findViewById(R.id.textButton)
        val editTextTitle: EditText = findViewById(R.id.editTextText)
        val editTextIsi: EditText = findViewById(R.id.editTextIsi)

        // Atur tanggal otomatis
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        tvDate.text = currentDate

        // Ambil mood dari Intent dengan nilai default
        val mood = intent.getStringExtra("MOOD") ?: "Neutral"

        // Ganti background sesuai mood
        when (mood) {
            "Happy 😄" -> backgroundRectangle.setBackgroundResource(R.drawable.gradient_happy)
            "Sad 😢" -> backgroundRectangle.setBackgroundResource(R.drawable.gradient_sad)
            "Angry 😡" -> backgroundRectangle.setBackgroundResource(R.drawable.gradient_angry)
            "Scared 😨" -> backgroundRectangle.setBackgroundResource(R.drawable.gradient_scared)
            "Surprised 😲" -> backgroundRectangle.setBackgroundResource(R.drawable.gradient_surprised)
            else -> backgroundRectangle.setBackgroundResource(R.drawable.gradient_neutral)
        }

        // Navigasi ke BerandaActivity
        floatingActionButton.setOnClickListener {
            val intent = Intent(this, BerandaActivity::class.java)
            startActivity(intent)
        }

        // Tombol untuk menyimpan diary
        textButton.setOnClickListener {
            val date = tvDate.text.toString()
            val title = editTextTitle.text.toString()
            val content = editTextIsi.text.toString()

            // Validasi input
            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Please fill in the diary content", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Siapkan data diary untuk disimpan
            val diaryData = hashMapOf(
                "userId" to userId,
                "date" to date,
                "title" to title,
                "content" to content,
                "mood" to mood
            )

            // Simpan data ke Firestore
            firestore.collection("Diaries")
                .add(diaryData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Diary successfully saved", Toast.LENGTH_SHORT).show()
                    // Reset input setelah menyimpan
                    editTextTitle.text.clear()
                    editTextIsi.text.clear()

                    // Pindah ke DateHistoryActivity
                    val intent = Intent(this, DateHistoryActivity::class.java)
                    intent.putExtra("date", date)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("DiaryActivity", "An error occurred while saving the diary: ${e.message}")
                }
        }
    }

    // Fungsi untuk menampilkan DatePickerDialog
    private fun showDatePickerDialog(textView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                textView.text = formattedDate
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }
}
