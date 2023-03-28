package com.example.neuralefficientcoding

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.neuralefficientcoding.about.AboutPage1Activity
import com.example.neuralefficientcoding.about.AppInfoActivity
import com.example.neuralefficientcoding.databinding.ActivityHomepageBinding
import com.example.neuralefficientcoding.databinding.ActivityWelcomeBinding

class HomePageActivity : AppCompatActivity() {
    private lateinit var homePageBinding: ActivityHomepageBinding
    private lateinit var welcomePageBinding: ActivityWelcomeBinding
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homePageBinding = DataBindingUtil.setContentView(this, R.layout.activity_homepage)

        homePageBinding.buttonImages.setOnClickListener {

        }

        homePageBinding.buttonSounds.setOnClickListener {

        }

        preferences = getSharedPreferences("com.example.neuroscience", MODE_PRIVATE)

    }

    override fun onResume() {
        super.onResume()

        if (preferences.getBoolean("first_run", true)) {
            welcomePageBinding = DataBindingUtil.setContentView(this, R.layout.activity_welcome)
            supportActionBar?.hide()

            welcomePageBinding.exitText.setOnClickListener {
                startActivity(Intent(this, HomePageActivity::class.java))
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
            welcomePageBinding.nextText.setOnClickListener {
                startActivity(Intent(this, AboutPage1Activity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            preferences.edit().putBoolean("first_run", false).apply()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.info_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.aboutAppBtn -> {
                startActivity(Intent(this, AboutPage1Activity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                true
            }
            R.id.appInfoBtn -> {
                startActivity(Intent(this, AppInfoActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}