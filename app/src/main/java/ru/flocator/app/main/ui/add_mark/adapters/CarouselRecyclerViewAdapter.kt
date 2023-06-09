package ru.flocator.app.main.ui.add_mark.adapters

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
import ru.flocator.app.main.ui.add_mark.data.CarouselItemState
import ru.flocator.app.main.utils.DiffUtilsCallback
import java.io.ByteArrayOutputStream
import java.util.function.BiConsumer

class CarouselRecyclerViewAdapter(
    private val onToggleCallback: BiConsumer<Uri, Boolean>
) :
    RecyclerView.Adapter<CarouselRecyclerViewAdapter.CarouselViewHolder>() {
    private var list: MutableList<CarouselItemState> = ArrayList()
    private var photos: MutableMap<Uri, ByteArray> = LinkedHashMap()

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
            LayoutInflater.from(parent.context).inflate(R.layout.add_mark_photo_carousel_item, parent, false)
        return CarouselViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        val state = list[position]

        if (!photos.containsKey(state.uri)) {
            val inputStream =
                holder.imageView.context.contentResolver.openInputStream(state.uri)
            val outputByteArray = inputStream!!.readBytes()
            photos[state.uri] = outputByteArray
            inputStream.close()
        }

        holder.imageView.setImageBitmap(getCompressedBitmap(photos[state.uri]!!))

        holder.removeImageCheckbox.isChecked = state.isSelected
        holder.removeImageCheckbox.setOnCheckedChangeListener { _, b ->
            onToggleCallback.accept(state.uri, b)
        }
    }

    private fun getCompressedBitmap(byteArray: ByteArray): Bitmap {
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESSION_FACTOR, outputStream)
        val outputByteArray = outputStream.toByteArray()
        return BitmapFactory.decodeByteArray(outputByteArray, 0, outputByteArray.size)
    }

    fun updateData(data: List<CarouselItemState>?) {
        val diffResult = if (data != null) {
            DiffUtil.calculateDiff(DiffUtilsCallback(list, data))
        } else {
            DiffUtil.calculateDiff(DiffUtilsCallback(list, list))
        }

        list = data?.toMutableList() ?: list

        photos = photos.filter { entry ->
            list.find { it.uri == entry.key } != null
        }.toMutableMap()

        diffResult.dispatchUpdatesTo(this)
    }

    fun getSetOfPhotos(): Set<Map.Entry<Uri, ByteArray>> = photos.entries.toSet()

    companion object {
        const val COMPRESSION_FACTOR = 40
    }
}