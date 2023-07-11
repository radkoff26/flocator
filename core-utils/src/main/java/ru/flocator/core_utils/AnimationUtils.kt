package ru.flocator.core_utils

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Build
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd

object AnimationUtils {

    fun animateColor(
        @ColorInt from: Int,
        @ColorInt to: Int,
        duration: Long,
        onValue: (color: Color) -> Unit,
        onEnd: () -> Unit
    ): ValueAnimator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val colorFrom = Color.valueOf(from)
            val colorTo = Color.valueOf(to)
            val redDiff = colorTo.red() - colorFrom.red()
            val greenDiff = colorTo.green() - colorFrom.green()
            val blueDiff = colorTo.blue() - colorFrom.blue()
            ValueAnimator.ofFloat(0F, 1F).apply {
                this.duration = duration
                addUpdateListener {
                    val value = it.animatedValue as Float
                    val red = colorFrom.red() + value * redDiff
                    val green = colorFrom.green() + value * greenDiff
                    val blue = colorFrom.blue() + value * blueDiff
                    val color = Color.valueOf(red, green, blue, 1f)
                    onValue.invoke(color)
                }
                doOnEnd {
                    onEnd.invoke()
                }
                start()
            }
        } else {
            ValueAnimator.ofFloat()
        }
    }
}