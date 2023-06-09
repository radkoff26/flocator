package ru.flocator.app.mark.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import ru.flocator.app.R
import ru.flocator.app.common.cache.runtime.PhotoState
import ru.flocator.app.common.views.LoaderImageView
import ru.flocator.app.common.views.RetryImageButton

class MarkPhotoCarouselAdapter(
    private val size: Int,
    uris: List<String>,
    private val loadPhotoCallback: (uri: String) -> Unit,
    private val openPhotoPagerCallback: (position: Int) -> Unit
) :
    RecyclerView.Adapter<MarkPhotoCarouselAdapter.MarkPhotoCarouselViewHolder>() {
    private var photosState: List<Pair<String, PhotoState>> = uris.map { Pair(it, PhotoState.Loading) }

    inner class MarkPhotoCarouselViewHolder(view: View) : ViewHolder(view) {
        private val retryImageButton: RetryImageButton = view.findViewById(R.id.retry_image_button)
        val imageView: AppCompatImageView = view.findViewById(R.id.carousel_item_image)
        val loaderImageView: LoaderImageView = view.findViewById(R.id.loader_image_view)

        fun bind(position: Int, uri: String) {
            imageView.setOnClickListener {
                openPhotoPagerCallback.invoke(position)
            }
            retryImageButton.setOnRetryCallback {
                loadPhotoCallback.invoke(uri)
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
        val value = photosState[position]
        holder.bind(position, value.first)
        val viewGroup = holder.itemView as ViewGroup
        when (val state = value.second) {
            is PhotoState.Loading -> {
                showView(R.id.loader_image_view, viewGroup)
                holder.loaderImageView.startAnimation()
                holder.imageView.setBackgroundColor(
                    ResourcesCompat.getColor(
                        holder.imageView.resources,
                        R.color.tint,
                        null
                    )
                )
                loadPhotoCallback.invoke(value.first)
            }
            is PhotoState.Loaded -> {
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
            is PhotoState.Failed -> {
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

    fun updatePhotos(value: Map<String, PhotoState>) {
        val photosList = photosState.toMutableList()
        val listOfChanges = ArrayList<Int>()
        for (i in photosList.indices) {
            val uri = photosList[i].first
            val valueState = value[uri]
            if (photosList[i].second != valueState && valueState != null) {
                photosList[i] = Pair(
                    uri,
                    valueState
                )
                listOfChanges.add(i)
            }
        }
        photosState = photosList
        listOfChanges.forEach {
            notifyItemChanged(it)
        }
    }
}