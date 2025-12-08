package com.example.sawit.IsiDashboard

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.sawit.R
import com.example.sawit.login

class DuaFragment : Fragment() {
    private lateinit var btnMulai: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dua, container, false)

        // Initialize button
        btnMulai = view.findViewById(R.id.Mulai)

        // Setup click listener
        btnMulai.setOnClickListener {
            val intent = Intent(activity, login::class.java)
            startActivity(intent)

            // Close ScreenActivity
            activity?.finish()
        }

        return view
    }
}