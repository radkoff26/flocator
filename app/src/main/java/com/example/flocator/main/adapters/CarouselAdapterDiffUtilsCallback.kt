package com.example.flocator.main.adapters

import androidx.recyclerview.widget.DiffUtil
import com.example.flocator.main.data.CarouselItemState

class CarouselAdapterDiffUtilsCallback(
    private val oldList: List<CarouselItemState>,
    private val newList: List<CarouselItemState>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition] === newList[newItemPosition]

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition] == newList[newItemPosition]
}
