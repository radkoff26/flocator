package ru.flocator.core_controller

import android.os.Bundle
import androidx.fragment.app.Fragment

interface NavController {

    fun toAuth(): TransactionCommitter

    fun toMain(): TransactionCommitter

    fun toProfile(): TransactionCommitter

    fun toSettings(): TransactionCommitter

    fun toFragment(fragment: Fragment): TransactionCommitter

    fun back()
}

fun Fragment.findNavController(): NavController {
    if (activity is NavigationRoot) {
        val navigationRoot = activity as NavigationRoot
        return navigationRoot.navController
    }
    throw IllegalStateException("Activity has no navigation controller!")
}
