package ru.flocator.app.main.config

sealed class BundleArgumentsContraction {
    object AddMarkFragmentArguments: BundleArgumentsContraction() {
        const val LATITUDE = "LATITUDE"
        const val LONGITUDE = "LONGITUDE"
    }

    object MarkFragmentArguments: BundleArgumentsContraction() {
        const val MARK_ID = "MARK_ID"
        const val USER_ID = "USER_ID"
    }

    object PhotoPagerFragmentArguments: BundleArgumentsContraction() {
        const val URI_LIST = "URI_LIST"
        const val POSITION = "POSITION"
    }

    object MarksListFragmentArguments: BundleArgumentsContraction() {
        const val USER_POINT = "USER_POINT"
        const val MARKS = "MARKS"
    }
}
