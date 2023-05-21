package com.example.flocator.main.ui.marks_list.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.flocator.R
import com.example.flocator.common.cache.runtime.PhotoState
import com.example.flocator.common.utils.TimePresentationUtils
import com.example.flocator.databinding.MarksListItemBinding
import com.example.flocator.main.data.Photo
import com.example.flocator.main.ui.marks_list.data.ListMarkDto
import io.reactivex.disposables.CompositeDisposable

class MarksListRecyclerViewAdapter(
    private var marksList: List<ListMarkDto>,
    private val requestPhotoLoadingCallback: (uri: String) -> Unit,
    private val requestAuthorNameLoadingCallback: (userId: Long) -> Unit,
    private val openMarkFragmentCallback: (markId: Long) -> Unit
) : Adapter<MarksListRecyclerViewAdapter.MarksListViewHolder>() {
    private val compositeDisposable = CompositeDisposable()

    inner class MarksListViewHolder(private val view: View) : ViewHolder(view) {
        private val binding: MarksListItemBinding = MarksListItemBinding.bind(view)

        fun bind(position: Int) {
            val listMarkDto = marksList[position]

            binding.root.setOnClickListener {
                openMarkFragmentCallback.invoke(listMarkDto.mark.markId)
            }

            binding.distanceToMark.text = listMarkDto.stringifiedDistanceToMark
            binding.markAddress.text = listMarkDto.mark.place
            binding.whenCreated.text =
                TimePresentationUtils.timestampToHumanPresentation(listMarkDto.mark.createdAt)
            binding.photosCount.text =
                view.resources.getString(R.string.photo_count, listMarkDto.photoCount)

            adjustThumbnail(listMarkDto.photo)
            adjustAuthorName(listMarkDto.authorName, listMarkDto.mark.authorId)
        }

        private fun adjustThumbnail(photo: Photo) {
            val photoState = photo.photoState
            val uri = photo.uri
            if (photoState is PhotoState.Loaded) {
                binding.markThumbnail.setImageBitmap(photoState.bitmap)
                binding.markThumbnailSkeleton.showOriginal()
            } else {
                requestPhotoLoadingCallback.invoke(uri)
                binding.markThumbnailSkeleton.showSkeleton()
            }
        }

        private fun adjustAuthorName(authorName: String?, authorId: Long) {
            if (authorName != null) {
                binding.authorName.text = authorName
                binding.authorNameSkeleton.showOriginal()
            } else {
                requestAuthorNameLoadingCallback.invoke(authorId)
                binding.authorNameSkeleton.showSkeleton()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarksListViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.marks_list_item, parent, false)
        return MarksListViewHolder(view)
    }

    override fun getItemCount(): Int = marksList.size

    override fun onBindViewHolder(holder: MarksListViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        compositeDisposable.dispose()
    }

    fun updateMarks(updatedMarksList: List<ListMarkDto>) {
        // Since marks always have the same size
        // it's easier to iterate over each element and check it
        val diffResult = DiffUtil.calculateDiff(
            MarksListDiffCallback(
                marksList,
                updatedMarksList
            )
        )
        marksList = updatedMarksList
        diffResult.dispatchUpdatesTo(this)
    }

    companion object {
        const val TAG = "Marks List Recycler View Adapter"
    }
}