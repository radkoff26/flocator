package ru.flocator.map.internal.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.imageview.ShapeableImageView
import ru.flocator.core.extensions.findDrawable
import ru.flocator.core.utils.ViewUtils.dpToPx
import ru.flocator.map.R
import ru.flocator.map.internal.domain.bitmap_creator.BitmapCreator
import ru.flocator.map.internal.domain.view.ReusableView
import ru.flocator.map.internal.domain.view.ViewFactory

@SuppressLint("ViewConstructor")
internal class MarkView constructor(
    context: Context,
    private val onRecycle: () -> Unit
) : FrameLayout(context), BitmapCreator, ReusableView {
    private val markImageSize = dpToPx(56, context)
    private val avatarFrameSize = dpToPx(24, context)
    private val roundedPath = Path()
    private val angularCoefficient = 0.16f
    private val cornerSize = markImageSize * angularCoefficient
    private val markImage: AppCompatImageView
    private val userAvatarFrameLayout: FrameLayout
    private val userAvatarImageView: ShapeableImageView

    @Volatile
    override var isBusy: Boolean = false

    private var isTargetUserMark: Boolean = false

    var markImageUri: String? = null
        private set
    var authorImageUri: String? = null
        private set

    init {
        // Mark Image
        markImage = AppCompatImageView(context)

        markImage.scaleType = ImageView.ScaleType.CENTER_CROP
        markImage.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.primary, null))

        addView(markImage)

        // User Image
        LayoutInflater.from(context)
            .inflate(R.layout.round_image_view, this, true)
        userAvatarFrameLayout = findViewById(R.id.layout)
        userAvatarImageView = findViewById(R.id.image)

        setTargetUser(isTargetUserMark)

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

    fun setTargetUser(isTargetUserMark: Boolean) {
        this.isTargetUserMark = isTargetUserMark

        with(resources) {
            if (this@MarkView.isTargetUserMark) {
                userAvatarFrameLayout.background = findDrawable(R.drawable.user_circle_bg)
            } else {
                userAvatarFrameLayout.background = findDrawable(R.drawable.friend_circle_bg)
            }
        }
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

    override fun recycle() {
        super.recycle()
        onRecycle.invoke()
    }

    class Factory(private var context: Context?) : ViewFactory<MarkView> {

        override fun create(onRecycle: () -> Unit): MarkView {
            if (context == null) {
                throw IllegalStateException("Factory is already cleared!")
            }
            return MarkView(context!!, onRecycle)
        }

        override fun onClear() {
            context = null
        }
    }
}