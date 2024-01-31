package com.example.guard_patrol.Activity

import BasedActivity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.guard_patrol.Fragment.HomeFragment
import com.example.guard_patrol.Fragment.ProfileFragment
import com.example.guard_patrol.R
import com.example.guard_patrol.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : BasedActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.pageHome

        setCurrFragment(HomeFragment())
        bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.pageHome -> {
                    setCurrFragment(HomeFragment())
                }
                R.id.pageSetting -> {

                }
                R.id.pageProfile -> {
                    setCurrFragment(ProfileFragment())
                }
            }
            true
        }

    }

    private fun setCurrFragment(fragment : Fragment){

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer,fragment)
            commit()
        }

    }
}