package ru.flocator.core_map.api.configuration

sealed class MapConfiguration {
    object MarksOnly: MapConfiguration()
    object UsersOnly: MapConfiguration()
    object All: MapConfiguration()
    class SpecialFilter(val mapFilters: List<MapFilter>): MapConfiguration()
}
