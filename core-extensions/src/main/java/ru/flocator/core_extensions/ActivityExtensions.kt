package ru.flocator.core_extensions

import android.app.Activity
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics

fun Activity.getSize(): Point {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val bounds = windowManager.currentWindowMetrics.bounds
        Point(bounds.width(), bounds.height())
    } else {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        Point(metrics.widthPixels, metrics.heightPixels)
    }
}