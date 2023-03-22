package com.example.flocator.main.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


class MapFriendView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {
    private val paint = Paint()
    private lateinit var rectF: RectF
    private var width: Float = 0F
    private var height: Float = 0F

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(dpToPx(24), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(dpToPx(24), MeasureSpec.EXACTLY))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        width = w.toFloat()
        height = h.toFloat()
        rectF = RectF(0F, 0F, width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawRoundRect(rectF, width, height, paint)
        super.onDraw(canvas)
    }

    private fun dpToPx(dp: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }
}