package com.example.thisapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.thisapp.R

class WelcomeActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var dotsLayout: LinearLayout
    private lateinit var dots: Array<ImageView?>
    private lateinit var adapter: WelcomePagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Menghubungkan view dengan ID
        viewPager = findViewById(R.id.viewPager)
        dotsLayout = findViewById(R.id.pagination_dots)

        // Menyiapkan adapter untuk ViewPager2
        adapter = WelcomePagerAdapter(this)
        viewPager.adapter = adapter

        // Membuat pagination dots
        createDots(0)

        // Listener untuk mengupdate pagination dots
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                createDots(position)
            }
        })

        // Set listener untuk melanjutkan ke halaman berikutnya (misal: LoginActivity atau PinActivity)
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Update status pengguna menjadi bukan pengguna pertama kali
        editor.putBoolean("isFirstTimeUser", false)
        editor.apply()

        // Simulasikan login otomatis atau kondisi lain setelah WelcomeActivity
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val isPinSet = sharedPreferences.getBoolean("isPinSet", false) // Cek apakah PIN sudah diatur

        // Menentukan activity selanjutnya berdasarkan status login
        if (isLoggedIn) {
            if (isPinSet) {
                // Jika sudah login dan PIN sudah diatur, arahkan ke PinActivity
                val intent = Intent(this, PinActivity::class.java)
                startActivity(intent)
            } else {
                // Jika sudah login tapi PIN belum diatur, arahkan ke PinActivity
                val intent = Intent(this, PinActivity::class.java)
                startActivity(intent)
            }
        } else {
            // Jika belum login, arahkan ke LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        finish() // Tutup WelcomeActivity agar tidak bisa kembali ke sini
    }

    // Fungsi untuk mengupdate indikator dots
    private fun createDots(position: Int) {
        dotsLayout.removeAllViews()

        dots = Array(3) { null }

        for (i in 0 until 3) {
            dots[i] = ImageView(this)
            dots[i]?.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_dot_shape))

            val params = LinearLayout.LayoutParams(8.dpToPx(), 8.dpToPx()) // Ukuran dot bisa disesuaikan
            params.setMargins(4.dpToPx(), 0, 4.dpToPx(), 0)
            dotsLayout.addView(dots[i], params)
        }

        // Set active dot
        dots[position]?.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.active_dot_shape))
    }

    // Extension function untuk mengonversi dp ke px
    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }
}
