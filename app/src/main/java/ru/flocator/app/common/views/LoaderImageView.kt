package ru.flocator.app.common.views

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import com.example.flocator.R

class LoaderImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val imageView: AppCompatImageView = AppCompatImageView(context)
    private var valueAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 720f).apply {
        repeatCount = ValueAnimator.INFINITE
        duration = 1200
        interpolator = LinearInterpolator()
        addUpdateListener {
            imageView.rotation = it.animatedValue as Float
        }
    }

    init {
        imageView.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        }

        imageView.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.loader_image, null))
        imageView.imageTintList = ResourcesCompat.getColorStateList(context.resources, R.color.tint, null)
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.scaleX = 1.2f
        imageView.scaleY = 1.2f

        addView(imageView)
    }

    fun startAnimation() {
        if (!valueAnimator.isRunning) {
            valueAnimator.start()
        }
    }

    fun stopAnimation() {
        if (valueAnimator.isRunning) {
            valueAnimator.cancel()
        }
    }
}