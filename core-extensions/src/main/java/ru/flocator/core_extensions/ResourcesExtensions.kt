package ru.flocator.core_extensions

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat

@ColorInt
fun Resources.findColor(@ColorRes colorId: Int): Int =
    ResourcesCompat.getColor(this, colorId, null)

fun Resources.findDrawable(@DrawableRes drawableId: Int): Drawable? =
    ResourcesCompat.getDrawable(this, drawableId, null)