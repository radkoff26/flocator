package ru.flocator.core_design.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout

class LoadingLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val loaderImageView = LoaderImageView(context)
    private val retryImageButton = RetryImageButton(context)

    var state: LoadingState = LoadingState.HIDDEN
        private set

    enum class LoadingState {
        HIDDEN,
        LOADING,
        FAILED
    }

    init {
        visibility = GONE

        loaderImageView.measure(
            MeasureSpec.makeMeasureSpec(
                0,
                MeasureSpec.UNSPECIFIED
            ),
            MeasureSpec.makeMeasureSpec(
                0,
                MeasureSpec.UNSPECIFIED
            )
        )
        loaderImageView.layoutParams =
            LayoutParams(loaderImageView.measuredWidth, loaderImageView.measuredHeight).apply {
                gravity = Gravity.CENTER
            }

        retryImageButton.measure(
            MeasureSpec.makeMeasureSpec(
                0,
                MeasureSpec.UNSPECIFIED
            ),
            MeasureSpec.makeMeasureSpec(
                0,
                MeasureSpec.UNSPECIFIED
            )
        )
        retryImageButton.layoutParams =
            LayoutParams(retryImageButton.measuredWidth, retryImageButton.measuredHeight).apply {
                gravity = Gravity.CENTER
            }

        addView(loaderImageView)
        addView(retryImageButton)
    }

    fun setOnRetryCallback(callback: () -> Unit) {
        retryImageButton.setOnRetryCallback(callback)
    }

    fun hide() {
        state = LoadingState.HIDDEN

        visibility = GONE

        loaderImageView.stopAnimation()
    }

    fun load() {
        state = LoadingState.LOADING

        visibility = VISIBLE
        loaderImageView.visibility = VISIBLE
        retryImageButton.visibility = GONE

        loaderImageView.startAnimation()
    }

    fun fail() {
        state = LoadingState.FAILED

        visibility = VISIBLE
        retryImageButton.visibility = VISIBLE
        loaderImageView.visibility = GONE

        loaderImageView.stopAnimation()
    }
}