package com.example.flocator.utils

import androidx.fragment.app.FragmentActivity

object FragmentNavigationUtils {
    fun closeFragment(activity: FragmentActivity) {
        val fragments = activity.supportFragmentManager.fragments
        if (fragments.size > 1) {
            val lastFragment = fragments.last()
            activity.supportFragmentManager.beginTransaction()
                .remove(lastFragment)
                .commit()
        } else {
            activity.finish()
        }
    }
}