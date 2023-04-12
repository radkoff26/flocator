package com.example.flocator.main.ui.mark.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.flocator.R

class MarkPhotoCarouselAdapter(
    private val size: Int,
    private val loadPhotoCallback: (position: Int) -> Unit,
    private val openPhotoPagerCallback: (position: Int) -> Unit
) :
    RecyclerView.Adapter<MarkPhotoCarouselAdapter.MarkPhotoCarouselViewHolder>() {
    private var photos: MutableList<Bitmap?> = MutableList(size) { null }

    inner class MarkPhotoCarouselViewHolder(view: View) : ViewHolder(view) {
        val imageView: AppCompatImageView = view.findViewById(R.id.carousel_item_image)

        fun bind(position: Int) {
            imageView.setOnClickListener {
                openPhotoPagerCallback.invoke(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkPhotoCarouselViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.mark_photo_carousel_item, parent, false)
        return MarkPhotoCarouselViewHolder(view)
    }

    override fun getItemCount(): Int = size

    override fun onBindViewHolder(holder: MarkPhotoCarouselViewHolder, position: Int) {
        if (photos[position] != null) {
            holder.imageView.setBackgroundColor(
                ResourcesCompat.getColor(
                    holder.imageView.resources,
                    R.color.transparent,
                    null
                )
            )
            holder.imageView.setImageBitmap(photos[position])
        } else {
            loadPhotoCallback.invoke(position)
            holder.imageView.setBackgroundColor(
                ResourcesCompat.getColor(
                    holder.imageView.resources,
                    R.color.primary,
                    null
                )
            )
        }
        holder.bind(position)
    }

    fun updatePhotos(value: List<Bitmap?>) {
        val prev = photos.toList()
        photos = value.toMutableList()
        for (i in value.indices) {
            if (value[i] != prev[i]) {
                notifyItemChanged(i)
            }
        }
    }
}