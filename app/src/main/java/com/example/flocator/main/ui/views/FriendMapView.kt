package com.example.flocator.main.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import com.example.flocator.R
import com.example.flocator.databinding.FriendMapViewBinding
import com.example.flocator.main.utils.ViewUtils.Companion.dpToPx

class FriendMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: FriendMapViewBinding
    private val defaultRadius = dpToPx(48, context)
    private val padding = dpToPx(2, context)
    private val radius: Int

    init {
        LayoutInflater.from(context).inflate(R.layout.friend_map_view, this, true)
        binding = FriendMapViewBinding.bind(this)

        if (attrs != null) {
            val typedAttrs =
                context.obtainStyledAttributes(attrs, R.styleable.FriendMapView, defStyleAttr, 0)
            radius =
                typedAttrs.getDimension(R.styleable.FriendMapView_radius, defaultRadius.toFloat())
                    .toInt()
            typedAttrs.recycle()
        } else {
            radius = defaultRadius
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        binding.image.measure(
            MeasureSpec.makeMeasureSpec(radius - padding * 2, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(radius - padding * 2, MeasureSpec.EXACTLY)
        )
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(radius, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(radius, MeasureSpec.EXACTLY)
        )
    }

    fun setBitmap(bitmap: Bitmap) {
        binding.image.setImageBitmap(bitmap)
    }

    fun setPlaceHolder() {
        binding.image.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.circle_bg,
                null
            )!!
        )
    }
}