package com.example.locationapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.launch

class PhotoSwipeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_swipe)

        val sessionId = intent.getStringExtra("SESSION_ID") ?: run { finish(); return }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tvCounter = findViewById<TextView>(R.id.tvCounter)

        lifecycleScope.launch {
            val db     = AppDatabase.getDatabase(this@PhotoSwipeActivity)
            val photos = db.photoDao().getPhotosBySession(sessionId)

            if (photos.isEmpty()) { finish(); return@launch }

            supportActionBar?.title = photos[0].address.ifEmpty { "写真閲覧" }

            val swipeAdapter = PhotoSwipeAdapter(photos)
            viewPager.adapter = swipeAdapter

            tvCounter.text = "1 / ${photos.size}"
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    tvCounter.text = "${position + 1} / ${photos.size}"
                }
            })
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
