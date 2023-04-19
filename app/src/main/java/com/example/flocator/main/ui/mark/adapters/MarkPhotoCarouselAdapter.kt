package com.example.flocator.main.ui.mark.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.flocator.R
import com.example.flocator.common.views.LoaderImageView
import com.example.flocator.common.views.RetryImageButton
import com.example.flocator.main.ui.mark.data.CarouselPhotoState

class MarkPhotoCarouselAdapter(
    private val size: Int,
    private val loadPhotoCallback: (position: Int) -> Unit,
    private val openPhotoPagerCallback: (position: Int) -> Unit
) :
    RecyclerView.Adapter<MarkPhotoCarouselAdapter.MarkPhotoCarouselViewHolder>() {
    private var photosState: MutableList<CarouselPhotoState> = MutableList(size) { CarouselPhotoState.Loading }

    inner class MarkPhotoCarouselViewHolder(view: View) : ViewHolder(view) {
        private val retryImageButton: RetryImageButton = view.findViewById(R.id.retry_image_button)
        val imageView: AppCompatImageView = view.findViewById(R.id.carousel_item_image)
        val loaderImageView: LoaderImageView = view.findViewById(R.id.loader_image_view)

        fun bind(position: Int) {
            imageView.setOnClickListener {
                openPhotoPagerCallback.invoke(position)
            }
            retryImageButton.setOnRetryCallback {
                loadPhotoCallback.invoke(position)
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
        holder.bind(position)
        val viewGroup = holder.itemView as ViewGroup
        when (val state = photosState[position]) {
            is CarouselPhotoState.Loading -> {
                showView(R.id.loader_image_view, viewGroup)
                holder.loaderImageView.startAnimation()
                holder.imageView.setBackgroundColor(
                    ResourcesCompat.getColor(
                        holder.imageView.resources,
                        R.color.tint,
                        null
                    )
                )
                loadPhotoCallback.invoke(position)
            }
            is CarouselPhotoState.Loaded -> {
                showView(R.id.carousel_item_image, viewGroup)
                holder.loaderImageView.stopAnimation()
                holder.imageView.setBackgroundColor(
                    ResourcesCompat.getColor(
                        holder.imageView.resources,
                        R.color.transparent,
                        null
                    )
                )
                holder.imageView.setImageBitmap(state.bitmap)
            }
            is CarouselPhotoState.Failed -> {
                showView(R.id.retry_image_button, viewGroup)
                holder.loaderImageView.stopAnimation()
            }
        }
    }

    private fun showView(@IdRes id: Int, viewGroup: ViewGroup) {
        for (child in viewGroup.children) {
            if (child.id == id) {
                child.visibility = View.VISIBLE
            } else {
                child.visibility = View.GONE
            }
        }
    }

    fun updatePhotos(value: List<CarouselPhotoState>) {
        val prev = photosState.toList()
        photosState = value.toMutableList()
        for (i in value.indices) {
            if (value[i] != prev[i]) {
                notifyItemChanged(i)
            }
        }
    }
}