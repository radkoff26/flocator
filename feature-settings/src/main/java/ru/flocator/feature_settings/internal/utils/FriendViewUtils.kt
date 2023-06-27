package ru.flocator.feature_settings.internal.utils

import android.content.Context

internal object FriendViewUtils {
    fun getNumOfColumns(context: Context?, columnWidthDp: Float): Int {
        val displayMetrics = context?.resources?.displayMetrics ?: return 3;
        val screenWidthDp = displayMetrics.widthPixels / (displayMetrics.density);
        val noOfColumns = (screenWidthDp / columnWidthDp + 0.5).toInt();
        return noOfColumns.coerceAtLeast(3);
    }
}