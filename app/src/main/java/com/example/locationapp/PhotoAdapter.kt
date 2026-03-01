package com.example.locationapp

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
        val btnDetail: Button    = view.findViewById(R.id.btnDetail)
        val btnMap: Button       = view.findViewById(R.id.btnMap)
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
        holder.tvDetail.visibility = View.GONE

        Glide.with(holder.ivPhoto.context)
            .load(File(photo.filePath))
            .centerCrop()
            .into(holder.ivPhoto)

        holder.ivPhoto.setOnClickListener { onPhotoClick(photo) }
        holder.itemView.setOnClickListener { onPhotoClick(photo) }
        holder.btnDetail.setOnClickListener { showDetailDialog(it, photo) }
        holder.btnMap.setOnClickListener { onMapClick(photo) }
    }

    private fun showDetailDialog(view: View, photo: Photo) {
        val dialogView: View = LayoutInflater.from(view.context)
            .inflate(R.layout.dialog_detail, null)

        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPANESE)

        // 所在地・位置情報
        dialogView.findViewById<TextView>(R.id.tvDetailAddress).text =
            "所在地: ${photo.address.ifEmpty { "住所不明" }}"
        dialogView.findViewById<TextView>(R.id.tvDetailLatLng).text =
            "緯度: %.6f　経度: %.6f".format(photo.latitude, photo.longitude)
        dialogView.findViewById<TextView>(R.id.tvDetailAltitude).text =
            "標高: %.1f m".format(photo.altitude)
        dialogView.findViewById<TextView>(R.id.tvDetailDate).text =
            "撮影日時: ${sdf.format(Date(photo.timestamp))}"

        // 土地情報
        val landLines = mutableListOf<String>()
        if (photo.area.isNotEmpty()) landLines.add("土地面積: ${photo.area}㎡")
        if (photo.landCategory.isNotEmpty() && photo.landCategory != "（未選択）") landLines.add("地目: ${photo.landCategory}")
        if (photo.frontage.isNotEmpty()) landLines.add("間口: ${photo.frontage}m")
        if (photo.roadWidth.isNotEmpty()) landLines.add("道路幅: ${photo.roadWidth}m")
        if (photo.roadDirection.isNotEmpty() && photo.roadDirection != "（未選択）") landLines.add("道路向き: ${photo.roadDirection}")
        dialogView.findViewById<TextView>(R.id.tvDetailLand).text =
            if (landLines.isNotEmpty()) landLines.joinToString("\n") else "未入力"

        // 建物情報（床面積を1番目に）
        val buildingLines = mutableListOf<String>()
        if (photo.floorArea1.isNotEmpty()) buildingLines.add("床面積1階: ${photo.floorArea1}㎡")
        if (photo.floorArea2.isNotEmpty()) buildingLines.add("床面積2階: ${photo.floorArea2}㎡")
        if (photo.floorArea3.isNotEmpty()) buildingLines.add("床面積3階: ${photo.floorArea3}㎡")
        if (photo.floorAreaTotal.isNotEmpty()) buildingLines.add("床面積合計: ${photo.floorAreaTotal}㎡")
        if (photo.structure.isNotEmpty() && photo.structure != "（未選択）") buildingLines.add("構造: ${photo.structure}")
        if (photo.builtYear.isNotEmpty() && photo.builtYear != "（未選択）") buildingLines.add("築年: ${photo.builtYear}")
        if (photo.floors.isNotEmpty() && photo.floors != "（未選択）") buildingLines.add("階数: ${photo.floors}")
        if (photo.layout.isNotEmpty() && photo.layout != "（未選択）") buildingLines.add("間取り: ${photo.layout}")
        if (photo.parking.isNotEmpty() && photo.parking != "（未選択）") buildingLines.add("駐車: ${photo.parking}")
        if (photo.waterSupply.isNotEmpty() && photo.waterSupply != "（未選択）") buildingLines.add("上水道: ${photo.waterSupply}")
        if (photo.sewage.isNotEmpty() && photo.sewage != "（未選択）") buildingLines.add("下水道: ${photo.sewage}")
        dialogView.findViewById<TextView>(R.id.tvDetailBuilding).text =
            if (buildingLines.isNotEmpty()) buildingLines.joinToString("\n") else "未入力"

        // メモ
        dialogView.findViewById<TextView>(R.id.tvDetailMemo).text =
            photo.memo.ifEmpty { "未入力" }

        AlertDialog.Builder(view.context)
            .setTitle("物件詳細")
            .setView(dialogView)
            .setPositiveButton("閉じる", null)
            .show()
    }

    class DiffCallback : DiffUtil.ItemCallback<Photo>() {
        override fun areItemsTheSame(oldItem: Photo, newItem: Photo) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Photo, newItem: Photo) = oldItem == newItem
    }
}
