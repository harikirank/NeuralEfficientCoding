package com.example.neuralefficientcoding

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.neuralefficientcoding.about.AboutPage1Activity
import com.example.neuralefficientcoding.about.AppInfoActivity
import com.example.neuralefficientcoding.databinding.ActivityHomepageBinding

class HomePageActivity : AppCompatActivity() {
    private lateinit var homePageBinding: ActivityHomepageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homePageBinding = DataBindingUtil.setContentView(this, R.layout.activity_homepage)

        homePageBinding.buttonImages.setOnClickListener {

        }

        homePageBinding.buttonSounds.setOnClickListener {

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