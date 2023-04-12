package com.example.flocator.main.config

sealed class BundleArgumentsContraction {
    object AddMarkFragmentArguments: BundleArgumentsContraction() {
        const val LATITUDE = "LATITUDE"
        const val LONGITUDE = "LONGITUDE"
    }

    object MarkFragmentArguments: BundleArgumentsContraction() {
        const val MARK_ID = "MARK_ID"
        const val USER_ID = "USER_ID"
    }

    object PhotoPagerFragment: BundleArgumentsContraction() {
        const val URI_LIST = "URI_LIST"
        const val POSITION = "POSITION"
    }
}
