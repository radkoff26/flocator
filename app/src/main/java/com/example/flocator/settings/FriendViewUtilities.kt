package com.example.flocator.settings

import android.content.Context

object FriendViewUtilities {
    fun getNumOfColumns(context: Context?, columnWidthDp: Float): Int {
        val displayMetrics = context?.resources?.displayMetrics ?: return 3;
        val screenWidthDp = displayMetrics.widthPixels / (displayMetrics.density);
        val noOfColumns = (screenWidthDp / columnWidthDp + 0.5).toInt();
        return noOfColumns.coerceAtLeast(3);
    }

    fun getUrlForImage(uri: String): String {
        return "http://kernelpunik.ru:8080/api/photo?uri=$uri"
    }
}