package com.example.neuralefficientcoding

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.neuralefficientcoding.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences = getSharedPreferences("com.example.neuroscience", MODE_PRIVATE)
        if (preferences.getBoolean("first_run", true)) {
            binding = DataBindingUtil.setContentView(this, R.layout.activity_welcome)
            hideActionBar()

            binding.exitText.setOnClickListener {
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
            binding.nextText.setOnClickListener {
                preferences.edit().putBoolean("first_run", false).apply()

                gotoHomepage()
            }
        } else {
            gotoHomepage()
        }
    }

    private fun gotoHomepage() {
        startActivity(Intent(applicationContext, HomePageActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }

    private fun hideActionBar() {
        supportActionBar?.hide()
    }
}