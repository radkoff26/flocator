package ru.flocator.core_map.internal.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import ru.flocator.core_design.R
import ru.flocator.core_map.internal.domain.bitmap_creator.BitmapCreator
import ru.flocator.core_map.internal.domain.view.ReusableView
import ru.flocator.core_map.internal.domain.view.ViewFactory
import ru.flocator.core_utils.ViewUtils.dpToPx

@SuppressLint("ViewConstructor")
internal class MarkGroupView constructor(
    context: Context,
    private val onRecycle: () -> Unit
) : FrameLayout(context), BitmapCreator, ReusableView {
    private val diameter = dpToPx(56, context)
    private val countTextView: TextView

    @Volatile
    override var isBusy: Boolean = false

    init {
        countTextView = TextView(context)

        countTextView.textAlignment = TEXT_ALIGNMENT_CENTER

        countTextView.textSize = dpToPx(8, context).toFloat()

        countTextView.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))

        countTextView.gravity = Gravity.CENTER

        setCount(0)

        addView(countTextView)

        background = ResourcesCompat.getDrawable(resources, ru.flocator.core_map.R.drawable.friend_circle_bg, null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        countTextView.measure(
            MeasureSpec.makeMeasureSpec(diameter, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(diameter, MeasureSpec.EXACTLY)
        )
        setMeasuredDimension(
            MeasureSpec.makeMeasureSpec(diameter, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(diameter, MeasureSpec.EXACTLY)
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        countTextView.layout(0, 0, diameter, diameter)
    }

    fun setCount(count: Int) {
        val text = if (count > 99) {
            "99+"
        } else {
            count.toString()
        }
        countTextView.text = text
    }

    override fun createBitmap(): Bitmap {
        measure(0, 0)
        layout(0, 0, diameter, diameter)

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

    class Factory(private var context: Context?): ViewFactory<MarkGroupView> {

        override fun create(onRecycle: () -> Unit): MarkGroupView {
            if (context == null) {
                throw IllegalStateException("Factory is already cleared!")
            }
            return MarkGroupView(context!!, onRecycle)
        }

        override fun onClear() {
            context = null
        }
    }
}