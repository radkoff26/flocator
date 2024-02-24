package ru.flocator.core.navigation

import androidx.fragment.app.Fragment

interface NavController {

    fun toAuthWithBackStackCleared()

    fun toAuth()

    fun toLocationDialog()

    fun toMain()

    fun toProfile()

    fun toSettings()

    fun toFragment(fragment: Fragment)

    fun back()
}

fun Fragment.findNavController(): NavController {
    if (activity is NavigationRoot) {
        val navigationRoot = activity as NavigationRoot
        return navigationRoot.navController
    }
    throw IllegalStateException("Activity has no navigation controller!")
}
