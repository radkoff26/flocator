package com.example.flocator.main.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.CheckBox

class CarouselItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {
    val checkBox = CheckBox(context)

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {

    }
}