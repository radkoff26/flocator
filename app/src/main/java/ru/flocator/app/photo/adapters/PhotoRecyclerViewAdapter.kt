package ru.flocator.app.photo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.flocator.R
import ru.flocator.app.common.cache.runtime.PhotoState
import ru.flocator.app.common.views.LoaderImageView
import ru.flocator.app.common.views.RetryImageButton
import ru.flocator.app.main.domain.photo.Photo

class PhotoRecyclerViewAdapter(
    private var photoData: List<Photo>,
    private val requestImageCallback: (uri: String) -> Unit,
    private val switchToolbarCallback: () -> Unit
) : RecyclerView.Adapter<PhotoRecyclerViewAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(private val view: View) : ViewHolder(view) {
        private fun bindOnLoading() {
            val loader = view.findViewById<LoaderImageView>(R.id.loader_image_view)
            loader.startAnimation()
        }

        private fun bindOnLoaded(position: Int) {
            val image = view.findViewById<AppCompatImageView>(R.id.pager_photo_image_view)
            image.setOnClickListener {
                switchToolbarCallback.invoke()
            }
            val loadedPhoto = photoData[position].photoState as PhotoState.Loaded
            image.setImageBitmap(loadedPhoto.bitmap)
        }

        private fun bindOnFailed(position: Int) {
            val retry = view.findViewById<RetryImageButton>(R.id.retry_image_button)
            retry.setOnRetryCallback {
                requestImageCallback(photoData[position].uri)
                notifyItemChanged(position)
            }
        }

        fun bind(position: Int) {
            when (photoData[position].photoState) {
                is PhotoState.Loading -> bindOnLoading()
                is PhotoState.Loaded -> bindOnLoaded(position)
                is PhotoState.Failed -> bindOnFailed(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, @LayoutRes viewType: Int): PhotoViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(viewType, parent, false)
        return PhotoViewHolder(view)
    }

    override fun getItemCount(): Int = photoData.size

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        requestImageCallback.invoke(photoData[position].uri)
        holder.bind(position)
    }

    override fun getItemViewType(position: Int): Int {
        return when (photoData[position].photoState) {
            is PhotoState.Loading -> LOADING_TYPE
            is PhotoState.Loaded -> LOADED_TYPE
            is PhotoState.Failed -> FAILED_TYPE
        }
    }

    fun updatePhotos(value: List<Photo>) {
        val prev = photoData.toList()
        photoData = value
        for (i in prev.indices) {
            if (prev[i] != photoData[i]) {
                notifyItemChanged(i)
            }
        }
    }

    companion object {
        const val LOADING_TYPE = R.layout.loading_pager_photo_item
        const val LOADED_TYPE = R.layout.loaded_pager_photo_item
        const val FAILED_TYPE = R.layout.failed_pager_photo_item
    }
}