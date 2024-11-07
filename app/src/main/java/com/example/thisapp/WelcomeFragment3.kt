package com.example.thisapp

import android.content.Intent  // Tambahkan impor Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button  // Tambahkan impor Button
import androidx.fragment.app.Fragment

class WelcomeFragment3 : Fragment(R.layout.fragment_welcome3) {

    private lateinit var nextButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi tombol Next
        nextButton = view.findViewById(R.id.buttonNext)

        // Set OnClickListener untuk tombol Next
        nextButton.setOnClickListener {
            // Pindah ke SignUpActivity saat tombol Next ditekan
            val intent = Intent(activity, SignupActivity::class.java)  // Pastikan nama activity sesuai dengan yang ada
            startActivity(intent)
            activity?.finish()  // Menutup aktivitas saat ini
        }
    }
}
