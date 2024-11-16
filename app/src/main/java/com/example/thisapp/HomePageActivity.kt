//package com.example.thisapp
//
//import android.os.Bundle
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//
//class HomePageActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_home_page)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//    }
//}

package com.example.thisapp

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.*

class HomePageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        // Get the current date
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        // Display 5 days (Monday to Friday)
        for (i in 0..4) {
            val day = currentDay + i
            val dayOfWeek =
                (calendar.get(Calendar.DAY_OF_WEEK) + i - 1) % 7 // Calculate day of the week

            // Create a TextView for the day
            val dayView = TextView(this)
            dayView.text = day.toString()
            dayView.gravity = Gravity.CENTER
            dayView.setPadding(16, 16, 16, 16)
            dayView.textSize = 18f

            // Create a LinearLayout for each day
            val dayContainer = LinearLayout(this)
            dayContainer.orientation = LinearLayout.VERTICAL
            dayContainer.gravity = Gravity.CENTER_HORIZONTAL
            dayContainer.setPadding(16, 16, 16, 16) // Add padding to each day
            dayContainer.addView(dayView)

            // Set background color for the current day
            if (i == 0) {
                dayContainer.setBackgroundResource(R.drawable.bg_circle)
            } else {
                dayContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            }
        }
    }
}