package ru.flocator.app.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.ResourcesCompat
import ru.flocator.app.R

class RetryImageButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val imageButton: AppCompatImageButton = AppCompatImageButton(context)

    init {
        imageButton.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        }

        imageButton.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.restart_image, null))
        imageButton.backgroundTintList = ResourcesCompat.getColorStateList(context.resources, R.color.transparent, null)
        imageButton.imageTintList = ResourcesCompat.getColorStateList(context.resources, R.color.tint, null)
        imageButton.scaleType = ImageView.ScaleType.FIT_CENTER
        imageButton.scaleX = 1.2f
        imageButton.scaleY = 1.2f

        addView(imageButton)
    }

    fun setOnRetryCallback(callback: () -> Unit) {
        imageButton.setOnClickListener {
            callback.invoke()
        }
    }
}