package ru.flocator.app.main.ui.map.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.example.flocator.R
import ru.flocator.app.main.ui.map.ui.BitmapCreator
import ru.flocator.app.main.utils.ViewUtils.Companion.dpToPx

class MarkGroupView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), BitmapCreator {
    private val diameter = dpToPx(56, context)
    private val countTextView: TextView

    init {
        countTextView = TextView(context)

        countTextView.textAlignment = TEXT_ALIGNMENT_CENTER

        countTextView.textSize = dpToPx(8, context).toFloat()

        countTextView.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))

        countTextView.gravity = Gravity.CENTER

        addView(countTextView)

        background = ResourcesCompat.getDrawable(resources, R.drawable.friend_circle_bg, null)
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
}