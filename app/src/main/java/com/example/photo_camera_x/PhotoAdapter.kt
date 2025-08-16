package com.example.photo_camera_x

import android.graphics.Bitmap
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class PhotoAdapter : ListAdapter<Bitmap, PhotoAdapter.PhotoViewHolder>(
    object : DiffUtil.ItemCallback<Bitmap>() {
        override fun areItemsTheSame(oldItem: Bitmap, newItem: Bitmap): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: Bitmap, newItem: Bitmap): Boolean = oldItem.sameAs(newItem)
    }
) {
    class PhotoViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val iv = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 400
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            setPadding(8, 8, 8, 8)
        }
        return PhotoViewHolder(iv)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.imageView.setImageBitmap(getItem(position))
    }
}