package com.example.flocator.main.adapters

import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.flocator.R
import com.example.flocator.main.data.AddMarkFragmentData

class CarouselRecyclerViewAdapter(private val activityResultLauncher: ActivityResultLauncher<String>) :
    RecyclerView.Adapter<CarouselRecyclerViewAdapter.CarouselViewHolder>(),
    Observer<AddMarkFragmentData> {
    private var list: List<Uri> = emptyList()

    class CarouselViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: AppCompatImageView
        val layout: FrameLayout

        init {
            imageView = view.findViewById(R.id.carousel_item_image)
            layout = view.findViewById(R.id.carousel_item_layout)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.carousel_item, parent, false)
        return CarouselViewHolder(view)
    }

    override fun getItemCount(): Int = list.size + 1

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        if (position == 0) {
            holder.imageView.setImageResource(R.drawable.camera_image)
            holder.layout.setBackgroundColor(holder.layout.context.getColor(R.color.transparent))
            holder.imageView.setOnClickListener {
                activityResultLauncher.launch("image/\\*")
            }
        } else {
            val inputStream =
                holder.imageView.context.contentResolver.openInputStream(list[position - 1])
            val bitmap = BitmapFactory.decodeStream(inputStream)
            holder.imageView.setImageBitmap(bitmap)
        }
    }

    override fun onChanged(t: AddMarkFragmentData?) {
        list = t!!.list
        notifyDataSetChanged()
    }
}