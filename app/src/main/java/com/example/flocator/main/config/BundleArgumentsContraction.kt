package com.example.flocator.main.config

sealed class BundleArgumentsContraction {
    object AddMarkFragmentArguments: BundleArgumentsContraction() {
        const val LATITUDE = "LATITUDE"
        const val LONGITUDE = "LONGITUDE"
    }
}
