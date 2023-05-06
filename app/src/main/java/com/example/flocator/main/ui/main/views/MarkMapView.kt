package com.example.flocator.main.ui.main.views

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.example.flocator.R
import com.example.flocator.main.utils.ViewUtils.Companion.dpToPx
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel

class MarkMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val markImageSize = dpToPx(56, context)
    private val avatarFrameSize = dpToPx(24, context)
    private val angularCoefficient = 0.16f
    private val markImage: ShapeableImageView
    private val userAvatarFrameLayout: FrameLayout
    private val userAvatarImageView: ShapeableImageView
    private var _hasAvatar: Boolean = false

    val hasAvatar: Boolean
        get() = _hasAvatar

    init {
        // Mark Image
        markImage = ShapeableImageView(context)

        markImage.shapeAppearanceModel = ShapeAppearanceModel()
            .withCornerSize(markImageSize * angularCoefficient)
        markImage.scaleType = ImageView.ScaleType.CENTER_CROP
        markImage.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.primary, null))

        addView(markImage)

        // User Image
        LayoutInflater.from(context).inflate(R.layout.round_image_view, this, true)
        userAvatarFrameLayout = findViewById(R.id.layout)
        userAvatarImageView = findViewById(R.id.image)
        val frameLayoutParams = LayoutParams(userAvatarFrameLayout.layoutParams).apply {
            gravity = (Gravity.BOTTOM + Gravity.END)
        }
        userAvatarFrameLayout.layoutParams = frameLayoutParams
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        markImage.measure(
            MeasureSpec.makeMeasureSpec(markImageSize, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(markImageSize, MeasureSpec.EXACTLY)
        )
        userAvatarFrameLayout.measure(
            MeasureSpec.makeMeasureSpec(avatarFrameSize, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(avatarFrameSize, MeasureSpec.EXACTLY)
        )
        setMeasuredDimension(
            MeasureSpec.makeMeasureSpec(markImageSize, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(markImageSize, MeasureSpec.EXACTLY)
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        markImage.layout(0, 0, markImageSize, markImageSize)
        userAvatarFrameLayout.layout(markImageSize - avatarFrameSize, markImageSize - avatarFrameSize, markImageSize, markImageSize)
    }

    fun setMarkBitmapPlaceHolder() {
        markImage.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.placeholder_image,
                null
            )
        )
    }

    fun setFriendBitmapImage(bitmap: Bitmap) {
        userAvatarImageView.setImageBitmap(bitmap)
        _hasAvatar = true
    }

    fun setMarkBitmapImage(bitmap: Bitmap) {
        markImage.setImageBitmap(bitmap)
    }

    fun setFriendBitmapPlaceHolder() {
        // TODO
    }
}