package com.example.flocator.main.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.flocator.R
import com.example.flocator.main.data.CarouselItemState
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.function.BiConsumer

class CarouselRecyclerViewAdapter(
    private val onToggleCallback: BiConsumer<Uri, Boolean>
) :
    RecyclerView.Adapter<CarouselRecyclerViewAdapter.CarouselViewHolder>() {
    private var list: MutableList<CarouselItemState> = ArrayList()
    private val cache = HashMap<Uri, Bitmap>()

    class CarouselViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: AppCompatImageView
        val removeImageCheckbox: CheckBox

        init {
            imageView = view.findViewById(R.id.carousel_item_image)
            removeImageCheckbox = view.findViewById(R.id.carousel_item_remove_checkbox)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.carousel_item, parent, false)
        return CarouselViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        val state = list[position]
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
        holder.removeImageCheckbox.isChecked = state.isSelected
        holder.removeImageCheckbox.setOnCheckedChangeListener { _, b ->
            onToggleCallback.accept(state.uri, b)
        }
    }

    private fun getCompressedBitmap(inputStream: InputStream): Bitmap {
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val os = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESSION_FACTOR, os)
        val bytes = os.toByteArray()
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    fun updateData(data: List<CarouselItemState>?) {
        val diffResult = if (data != null) {
            DiffUtil.calculateDiff(CarouselAdapterDiffUtilsCallback(list, data))
        } else {
            DiffUtil.calculateDiff(CarouselAdapterDiffUtilsCallback(list, list))
        }
        list = data?.toMutableList() ?: list
        diffResult.dispatchUpdatesTo(this)
    }

    companion object {
        const val COMPRESSION_FACTOR = 40
    }
}