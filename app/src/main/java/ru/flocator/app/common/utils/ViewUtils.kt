package ru.flocator.app.common.utils

import android.content.Context
import android.util.DisplayMetrics
import kotlin.math.roundToInt

object ViewUtils {
    fun dpToPx(dp: Int, context: Context): Int {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }
}