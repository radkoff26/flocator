package com.example.flocator.main.ui.photo.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.flocator.R

class PhotoRecyclerViewAdapter(
    private var photos: List<Bitmap?>,
    private val requestImageCallback: (position: Int) -> Unit
) :
    RecyclerView.Adapter<PhotoRecyclerViewAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(view: View) : ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.pager_photo_item, parent, false)
        return PhotoViewHolder(view)
    }

    override fun getItemCount(): Int = photos.size

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.itemView.run {
            val photo = findViewById<AppCompatImageView>(R.id.pager_photo_image_view)
            if (photos[position] == null) {
                requestImageCallback.invoke(position)
                photo.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        photo.resources,
                        R.drawable.loader_image,
                        null
                    )
                )
            } else {
                photo.setImageBitmap(photos[position])
            }
        }
    }

    fun updatePhotos(value: List<Bitmap?>) {
        val prev = photos.toList()
        photos = value
        for (i in prev.indices) {
            if (prev[i] != photos[i]) {
                notifyItemChanged(i)
            }
        }
    }
}