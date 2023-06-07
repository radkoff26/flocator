package com.example.flocator.main.ui.main.views.map

sealed class MapConfiguration {

    object MarksOnly: MapConfiguration()

    object UsersOnly: MapConfiguration()

    object All: MapConfiguration()

    class SpecialFilter(val mapFilters: List<MapFilter>): MapConfiguration()
}
