package com.example.flocator.main.views

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.DisplayMetrics
import com.example.flocator.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import kotlin.math.roundToInt

class MapFriendView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    bitmap: Bitmap? = null
) : ShapeableImageView(context, attrs, defStyleAttr) {
    private val size = dpToPx(48)
    var mBitmap: Bitmap? = bitmap
        set(value) {
            if (value != null) {
                setImageBitmap(value)
            }
        }

    init {
        this.shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCorners(RoundedCornerTreatment())
            .setAllCornerSizes(size.toFloat() / 2)
            .build()

        scaleType = ScaleType.CENTER_CROP

        if (bitmap == null) {
            setBackgroundResource(R.drawable.circle_bg)
        } else {
            setImageBitmap(bitmap)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(size, size)
    }

    private fun dpToPx(dp: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }
}