package com.example.sawit.Halaman

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.sawit.Dashboard
import com.example.sawit.R
class SatuFragment : Fragment() {
    private lateinit var btnSkip: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_satu, container, false)

        // Initialize button
        btnSkip = view.findViewById(R.id.Skip)

        // Setup click listener
        btnSkip.setOnClickListener {
            // Navigate to Dashboard Activity
            val intent = Intent(activity, Dashboard::class.java)
            startActivity(intent)

            // Close current activity (ScreenActivity)
            activity?.finish()
        }

        return view
    }
}