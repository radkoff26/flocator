package com.example.flocator.main.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.example.flocator.R
import com.example.flocator.databinding.MarkMapViewBinding

class MarkMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var binding: MarkMapViewBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.mark_map_view, this, true)
        binding = MarkMapViewBinding.bind(this)
    }

    fun setMarkBitmapPlaceHolder() {
        binding.markImage.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.placeholder_image,
                null
            )
        )
        binding.markImage.imageTintList = context.getColorStateList(R.color.white)
    }

    fun setFriendBitmapImage(bitmap: Bitmap) {
        binding.friendView.setBitmap(bitmap)
    }

    fun setMarkBitmapImage(bitmap: Bitmap) {
        binding.markImage.setImageBitmap(bitmap)
    }

    fun setFriendBitmapPlaceHolder() {
        binding.friendView.setPlaceHolder()
    }
}