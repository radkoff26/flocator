package com.example.flocator.main.ui.map.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import com.example.flocator.R
import com.example.flocator.main.ui.map.ui.BitmapCreator
import com.example.flocator.main.utils.ViewUtils.Companion.dpToPx
import com.google.android.material.imageview.ShapeableImageView

class MarkView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    isTargetUserMark: Boolean = false
) : FrameLayout(context, attrs, defStyleAttr), BitmapCreator {
    private val markImageSize = dpToPx(56, context)
    private val avatarFrameSize = dpToPx(24, context)
    private val roundedPath = Path()
    private val angularCoefficient = 0.16f
    private val cornerSize = markImageSize * angularCoefficient
    private val markImage: AppCompatImageView
    private val userAvatarFrameLayout: FrameLayout
    private val userAvatarImageView: ShapeableImageView

    private var markImageUri: String? = null
    private var authorImageUri: String? = null

    init {
        // Mark Image
        markImage = AppCompatImageView(context)

        markImage.scaleType = ImageView.ScaleType.CENTER_CROP
        markImage.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.primary, null))

        addView(markImage)

        // User Image
        LayoutInflater.from(context).inflate(R.layout.round_image_view, this, true)
        userAvatarFrameLayout = findViewById(R.id.layout)
        userAvatarImageView = findViewById(R.id.image)

        if (isTargetUserMark) {
            userAvatarFrameLayout.background =
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.user_circle_bg,
                    null
                )
        }

        val frameLayoutParams = LayoutParams(userAvatarFrameLayout.layoutParams).apply {
            gravity = (Gravity.BOTTOM + Gravity.END)
        }
        userAvatarFrameLayout.layoutParams = frameLayoutParams

        setMarkBitmapPlaceHolder()
        setAuthorBitmapPlaceHolder()
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
        userAvatarFrameLayout.layout(
            markImageSize - avatarFrameSize,
            markImageSize - avatarFrameSize,
            markImageSize,
            markImageSize
        )
    }

    override fun onDraw(canvas: Canvas?) {
        roundedPath.addRoundRect(
            0f, 0f, width.toFloat(), height.toFloat(),
            cornerSize, cornerSize, Path.Direction.CW
        )
        canvas?.clipPath(roundedPath)
        super.onDraw(canvas)
    }

    fun setMarkBitmapImage(bitmap: Bitmap, uri: String?) {
        if (markImageUri != uri) {
            if (uri != null) {
                markImage.setImageBitmap(bitmap)
            } else {
                setMarkBitmapPlaceHolder()
            }
            markImageUri = uri
        }
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

    fun setAuthorBitmapImage(bitmap: Bitmap, uri: String?) {
        if (uri != authorImageUri) {
            if (uri == null) {
                setAuthorBitmapPlaceHolder()
            } else {
                userAvatarImageView.setImageBitmap(bitmap)
            }
            authorImageUri = uri
        }
    }

    fun setAuthorBitmapPlaceHolder() {
        userAvatarImageView.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.base_avatar_image,
                null
            )
        )
    }

    override fun createBitmap(): Bitmap {
        measure(0, 0)
        layout(0, 0, markImageSize, markImageSize)

        // Create a bitmap with the dimensions of the custom view
        val bitmap = Bitmap.createBitmap(
            measuredWidth,
            measuredHeight,
            Bitmap.Config.ARGB_8888
        )

        // Create a canvas with the bitmap
        val canvas = Canvas(bitmap)

        // Draw the custom view onto the canvas
        draw(canvas)

        return bitmap
    }
}