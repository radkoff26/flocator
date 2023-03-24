package com.example.flocator.main.views

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.flocator.R
import com.example.flocator.databinding.FriendMapViewBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import kotlin.math.roundToInt

class MapFriendView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    bitmap: Bitmap? = null
) : FrameLayout(context, attrs, defStyleAttr) {
    private val size = dpToPx(48)
    private val padding = dpToPx(2)
    private val fullSize = size + padding * 2
    private var shapeableImageView: ShapeableImageView
    var mBitmap: Bitmap? = bitmap
        set(value) {
            if (value != null) {
                shapeableImageView.setImageBitmap(value)
            }
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.friend_map_view, this, true)
        shapeableImageView = FriendMapViewBinding.bind(this).image
//        shapeableImageView.shapeAppearanceModel = ShapeAppearanceModel.builder()
//            .setAllCorners(RoundedCornerTreatment())
//            .setAllCornerSizes(size.toFloat() / 2)
//            .build()
//
//        shapeableImageView.scaleType = ImageView.ScaleType.CENTER_CROP
//        shapeableImageView.strokeWidth = padding.toFloat()
//        shapeableImageView.strokeColor = resources.getColorStateList(R.color.tint, null)
//
//        setBackgroundResource(R.drawable.circle_bg)
//
//        if (bitmap != null) {
//            shapeableImageView.setImageBitmap(bitmap)
//        }
    }

    private fun dpToPx(dp: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }
}