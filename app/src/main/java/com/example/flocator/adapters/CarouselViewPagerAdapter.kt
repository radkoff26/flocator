package com.example.flocator.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.flocator.R

class CarouselViewPagerAdapter :
    RecyclerView.Adapter<CarouselViewPagerAdapter.CarouselViewHolder>() {
    private val list = mutableListOf<String>()

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

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        if (position == 0) {
            holder.imageView.setImageResource(R.drawable.camera_image)
        } else {
            TODO("Not yet implemented")
        }
    }
}