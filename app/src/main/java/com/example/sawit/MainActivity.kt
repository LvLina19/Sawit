package com.example.sawit

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentTransaction
import com.example.sawit.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import androidx.activity.enableEdgeToEdge

class MainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding
    lateinit var toggle: ActionBarDrawerToggle
//    lateinit var kameraFragment: HalamanKamera
//    lateinit var draftFragment: HalamanDraft
//    lateinit var settingFragment: HalamanSetting
//    lateinit var helpFragment: HalamanHelp
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        toggle = ActionBarDrawerToggle(this, binding.drawerLayout,
//            R.string.Open, R.string.Close)
//        binding.drawerLayout.addDrawerListener(toggle)
//        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        binding.navView.setNavigationItemSelectedListener(this)
    }

//    override fun onNavigationItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.inbox -> {
//                kameraFragment = HalamanKamera()
//                supportFragmentManager
//                    .beginTransaction()
//                    .replace(R.id.frame_layout, kameraFragment)
//                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                    .commit()
//                Toast.makeText(applicationContext, "Ini Halaman Kamera", Toast.LENGTH_SHORT)
//                    .show()
//            }R.id.draft -> {
//            draftFragment = HalamanDraft()
//            supportFragmentManager
//                .beginTransaction()
//                .replace(R.id.frame_layout, draftFragment)
//                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                .commit()
//            Toast.makeText(applicationContext, "Ini Halaman History", Toast.LENGTH_SHORT)
//                .show()
//        }R.id.setting -> {
//            settingFragment = HalamanSetting()
//            supportFragmentManager
//                .beginTransaction()
//                .replace(R.id.frame_layout, settingFragment)
//                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                .commit()
//            Toast.makeText(applicationContext, "Ini Halaman Setting", Toast.LENGTH_SHORT)
//                .show()
//        }R.id.help -> {
//            helpFragment = HalamanHelp()
//            supportFragmentManager
//                .beginTransaction()
//                .replace(R.id.frame_layout, helpFragment)
//                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                .commit()
//            Toast.makeText(applicationContext, "Ini Halaman Help", Toast.LENGTH_SHORT)
//                .show()
//        }
//        }
//        binding.drawerLayout.closeDrawers()
//        return true
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}