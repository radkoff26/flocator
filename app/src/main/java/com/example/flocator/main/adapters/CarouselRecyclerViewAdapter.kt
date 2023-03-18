package com.example.flocator.main.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import com.example.flocator.R
import com.example.flocator.main.data.AddMarkFragmentData
import com.example.flocator.main.data.CarouselItemState
import com.example.flocator.main.view_models.AddMarkFragmentViewModel
import java.io.ByteArrayOutputStream
import java.io.InputStream

class CarouselRecyclerViewAdapter(
    private val addMarkFragmentViewModel: AddMarkFragmentViewModel
) :
    RecyclerView.Adapter<CarouselRecyclerViewAdapter.CarouselViewHolder>(),
    Observer<AddMarkFragmentData> {
    private var list: MutableList<CarouselItemState> = ArrayList()
    private var isInRemovingState = false
    private val cache = HashMap<Uri, Bitmap>()

    class CarouselViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: AppCompatImageView
        val layout: FrameLayout
        val removeImageCheckbox: CheckBox

        init {
            imageView = view.findViewById(R.id.carousel_item_image)
            layout = view.findViewById(R.id.carousel_item_layout)
            removeImageCheckbox = view.findViewById(R.id.carousel_item_remove_checkbox)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.carousel_item, parent, false)
        return CarouselViewHolder(view)
    }

    override fun getItemCount(): Int = list.size + 1

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        val index = position - 1
        val state = list[index]
        if (cache.containsKey(state.uri)) {
            val outputBitmap = cache[state.uri]
            holder.imageView.setImageBitmap(outputBitmap)
        } else {
            val inputStream =
                holder.imageView.context.contentResolver.openInputStream(state.uri)
            val outputBitmap = getCompressedBitmap(inputStream!!)
            cache[state.uri] = outputBitmap
            inputStream.close()
            holder.imageView.setImageBitmap(outputBitmap)
        }
        if (isInRemovingState) {
            holder.removeImageCheckbox.visibility = AppCompatImageButton.VISIBLE
            holder.removeImageCheckbox.setOnClickListener {
                if (list.size == 1) {
                    isInRemovingState = false
                }
//                cache.remove(list[index])
//                addMarkFragmentViewModel.removeItem(index)
            }
        } else {
            holder.removeImageCheckbox.visibility = AppCompatImageButton.GONE
            holder.imageView.setOnLongClickListener {
                isInRemovingState = true
                notifyDataSetChanged()
                true
            }
        }
    }

    private fun getCompressedBitmap(inputStream: InputStream): Bitmap {
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val os = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESSION_FACTOR, os)
        val bytes = os.toByteArray()
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun updateData(data: List<CarouselItemState>?) {
        val diffResult = if (data != null) {
            DiffUtil.calculateDiff(CarouselAdapterDiffUtilsCallback(list, data))
        } else {
            DiffUtil.calculateDiff(CarouselAdapterDiffUtilsCallback(list, list))
        }
        list = data?.toMutableList() ?: list
        diffResult.dispatchUpdatesTo(object: ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {
                notifyItemInserted(position)
            }

            override fun onRemoved(position: Int, count: Int) {
                notifyItemRemoved(position)
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                notifyItemMoved(fromPosition, toPosition)
            }

            override fun onChanged(position: Int, count: Int, payload: Any?) {
                notifyItemChanged(position, payload)
            }
        })
        notifyItemChanged(0)
    }

    override fun onChanged(t: AddMarkFragmentData?) {
        updateData(t!!.stateList)
    }

    companion object {
        const val COMPRESSION_FACTOR = 40
    }
}