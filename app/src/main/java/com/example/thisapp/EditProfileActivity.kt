package com.example.thisapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailTextEdit: EditText
    private lateinit var passwordTextEdit: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        nameEditText = findViewById(R.id.textInputLayout2)
        emailTextEdit = findViewById(R.id.textInputLayout)
        passwordTextEdit = findViewById(R.id.textInputLayout3)

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_edit_profile)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
    }
}