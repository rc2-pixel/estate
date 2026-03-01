package com.example.locationapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

class PhotoSwipeAdapter(private val photos: List<Photo>) :
    RecyclerView.Adapter<PhotoSwipeAdapter.SwipeViewHolder>() {

    class SwipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.ivSwipePhoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_swipe_photo, parent, false)
        return SwipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: SwipeViewHolder, position: Int) {
        Glide.with(holder.imageView.context)
            .load(File(photos[position].filePath))
            .fitCenter()
            .into(holder.imageView)
    }

    override fun getItemCount() = photos.size
}
