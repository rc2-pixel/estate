package com.example.locationapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhotoAdapter(
    private val onPhotoClick: (Photo) -> Unit,
    private val onMapClick: (Photo) -> Unit
) : ListAdapter<Photo, PhotoAdapter.PhotoViewHolder>(DiffCallback()) {

    class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPhoto: ImageView   = view.findViewById(R.id.ivPhoto)
        val tvAddress: TextView  = view.findViewById(R.id.tvItemAddress)
        val tvAltitude: TextView = view.findViewById(R.id.tvItemAltitude)
        val tvDate: TextView     = view.findViewById(R.id.tvItemDate)
        val tvDetail: TextView   = view.findViewById(R.id.tvItemDetail)
        val btnMap: ImageButton  = view.findViewById(R.id.btnMap)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = getItem(position)

        holder.tvAddress.text  = photo.address.ifEmpty { "住所不明" }
        holder.tvAltitude.text = "標高: %.1f m".format(photo.altitude)

        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPANESE)
        holder.tvDate.text = sdf.format(Date(photo.timestamp))

        val details = mutableListOf<String>()
        if (photo.area.isNotEmpty()) details.add("面積: ${photo.area}㎡")
        if (photo.landCategory.isNotEmpty() && photo.landCategory != "（未選択）") details.add("地目: ${photo.landCategory}")
        if (photo.frontage.isNotEmpty()) details.add("間口: ${photo.frontage}m")
        if (photo.roadWidth.isNotEmpty()) details.add("道路幅: ${photo.roadWidth}m")
        if (photo.roadDirection.isNotEmpty() && photo.roadDirection != "（未選択）") details.add("道路向き: ${photo.roadDirection}")
        if (photo.structure.isNotEmpty() && photo.structure != "（未選択）") details.add("構造: ${photo.structure}")
        if (photo.builtYear.isNotEmpty()) details.add("築年: ${photo.builtYear}年")
        if (photo.floors.isNotEmpty()) details.add("階数: ${photo.floors}階")
        if (photo.layout.isNotEmpty() && photo.layout != "（未選択）") details.add("間取り: ${photo.layout}")
        if (photo.parking.isNotEmpty()) details.add("駐車: ${photo.parking}台")
        if (photo.waterSupply.isNotEmpty()) details.add("上水: ${photo.waterSupply}")
        if (photo.sewage.isNotEmpty()) details.add("下水: ${photo.sewage}")
        if (photo.memo.isNotEmpty()) details.add("メモ: ${photo.memo}")
        holder.tvDetail.text = if (details.isNotEmpty()) details.joinToString("　") else "詳細情報なし"

        Glide.with(holder.ivPhoto.context)
            .load(File(photo.filePath))
            .centerCrop()
            .into(holder.ivPhoto)

        holder.ivPhoto.setOnClickListener { onPhotoClick(photo) }
        holder.itemView.setOnClickListener { onPhotoClick(photo) }
        holder.btnMap.setOnClickListener { onMapClick(photo) }
    }

    class DiffCallback : DiffUtil.ItemCallback<Photo>() {
        override fun areItemsTheSame(oldItem: Photo, newItem: Photo) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Photo, newItem: Photo) = oldItem == newItem
    }
}
