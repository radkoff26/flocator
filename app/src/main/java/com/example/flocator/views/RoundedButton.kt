package com.example.flocator.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.example.flocator.R

class RoundedButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    private lateinit var rect: RectF
    private var roundCorners = 0F
    private val paint = Paint()
    private var width = 0
    private var height = 0

    init {
        context.obtainStyledAttributes(attrs, R.styleable.RoundedButton, defStyleAttr, 0).use {
            roundCorners = it.getDimension(R.styleable.RoundedButton_rb_round_corners, 0F)
            paint.color = it.getColor(R.styleable.RoundedButton_rb_bg_color, context.getColor(R.color.primary))
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        width = MeasureSpec.getSize(widthMeasureSpec)
        height = MeasureSpec.getSize(heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        width = w
        height = h
        rect = RectF(0F, 0F, w.toFloat(), h.toFloat())
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawRoundRect(rect, roundCorners, roundCorners, paint)
        super.onDraw(canvas)
    }
}