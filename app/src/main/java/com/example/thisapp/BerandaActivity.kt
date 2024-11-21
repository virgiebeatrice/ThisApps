package com.example.thisapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class BerandaActivity : AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beranda)

        val writeDiaryButton: Button = findViewById(R.id.write_diary_button)

        writeDiaryButton.setOnClickListener {
            val intent = Intent(this, DiaryActivity::class.java)
            startActivity(intent)
        }

        val profileIcon: ImageButton = findViewById(R.id.profile_icon)
        profileIcon.setOnClickListener {
            val intent = Intent(this, ProfileSettings::class.java)
            startActivity(intent)
        }
    }
}
