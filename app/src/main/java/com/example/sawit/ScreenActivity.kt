package com.example.sawit

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sawit.Halaman.DuaFragment
import com.example.sawit.Halaman.SatuFragment
import com.example.sawit.Halaman.ScreenAdapter
import com.example.sawit.databinding.ActivityScreenBinding
//import com.example.sawit.Halaman.DuaFragment
//import com.example.sawit.Halaman.SatuFragment
//import com.example.sawit.Halaman.ScreenAdapter
//import com.example.sawit.Halaman.TigaFragment

class ScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)){ v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val fragmentList = listOf(SatuFragment(), DuaFragment())
        val adapter = ScreenAdapter(this, fragmentList)
        binding.viewPager.adapter = adapter
    }
}