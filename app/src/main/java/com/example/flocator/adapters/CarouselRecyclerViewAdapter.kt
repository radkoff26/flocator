package com.example.flocator.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.flocator.R
import com.example.flocator.fragments.main.State
import java.util.function.Consumer

data class CarouselItemState(
    val isSelectable: Boolean,
    val isSelected: Boolean,

)

class CarouselRecyclerViewAdapter(private val activityResultLauncher: ActivityResultLauncher<String>) :
    RecyclerView.Adapter<CarouselRecyclerViewAdapter.CarouselViewHolder>(),
    Observer<State>{
    private var list: List<Uri> = emptyList()

    class CarouselViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: AppCompatImageView

        init {
            imageView = view.findViewById(R.id.carousel_item_image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.carousel_item, parent, false)
        return CarouselViewHolder(view)
    }

    override fun getItemCount(): Int = list.size + 1

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        if (position == 0) {
            holder.imageView.setImageResource(R.drawable.camera_image)
            holder.imageView.setOnClickListener {
                activityResultLauncher.launch("image/\\*")
            }
        } else {
            val inputStream = holder.imageView.context.contentResolver.openInputStream(list[position - 1])

            val bitmap = BitmapFactory.decodeStream(inputStream)

            holder.imageView.setImageBitmap(bitmap)
        }
    }

    override fun onChanged(t: State?) {
        list = t!!.list
        notifyDataSetChanged()
    }
}