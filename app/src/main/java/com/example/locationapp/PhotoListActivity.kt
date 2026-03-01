package com.example.locationapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class PhotoListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_list)
        supportActionBar?.title = "撮影済み写真"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val db = AppDatabase.getDatabase(this)
        val adapter = PhotoAdapter(
            onPhotoClick = { photo ->
                val intent = Intent(this, PhotoSwipeActivity::class.java)
                intent.putExtra("SESSION_ID", photo.sessionId)
                startActivity(intent)
            },
            onMapClick = { photo ->
                val uri = Uri.parse(
                    "geo:${photo.latitude},${photo.longitude}" +
                    "?q=${photo.latitude},${photo.longitude}(${photo.address})"
                )
                val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                mapIntent.setPackage("com.google.android.apps.maps")
                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    val browserUri = Uri.parse(
                        "https://maps.google.com/?q=${photo.latitude},${photo.longitude}")
                    startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                }
            }
        )
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            db.photoDao().getMainPhotos().collect { photos ->
                adapter.submitList(photos)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
