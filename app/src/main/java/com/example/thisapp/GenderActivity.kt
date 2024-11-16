package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GenderActivity : AppCompatActivity() {

    private lateinit var FemaleButton: Button
    private lateinit var MaleButton: Button
    private lateinit var YourNameText: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gender)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Bind views
        FemaleButton = findViewById(R.id.FemaleButton)
        MaleButton = findViewById(R.id.MaleButton)
        YourNameText = findViewById(R.id.YourNameText)

        // Set up button listeners
        FemaleButton.setOnClickListener {
            // Add logic for Female button click
        }

        MaleButton.setOnClickListener {
            // Add logic for Male button click
        }

        // Navigate to MainActivity when either button is clicked
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
