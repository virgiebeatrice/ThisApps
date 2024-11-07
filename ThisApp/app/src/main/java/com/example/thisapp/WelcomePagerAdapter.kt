package com.example.thisapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class WelcomePagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    // Mengembalikan jumlah fragment yang ada
    override fun getItemCount(): Int {
        return 3 // Ada 3 fragment
    }

    // Mengembalikan fragment berdasarkan posisi
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WelcomeFragment()  // Menampilkan fragment pertama
            1 -> WelcomeFragment2()  // Menampilkan fragment kedua
            else -> WelcomeFragment3()  // Menampilkan fragment ketiga
        }
    }
}
