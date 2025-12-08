package com.example.sawit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sawit.IsiDashboard.SatuFragment

class ScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen)

        // Load SatuFragment pertama kali
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.viewPager, SatuFragment())
                .commit()
        }
    }
}