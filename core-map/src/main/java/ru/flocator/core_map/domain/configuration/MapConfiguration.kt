package ru.flocator.core_map.domain.configuration

sealed class MapConfiguration {
    object MarksOnly: MapConfiguration()
    object UsersOnly: MapConfiguration()
    object All: MapConfiguration()
    class SpecialFilter(val mapFilters: List<MapFilter>): MapConfiguration()
}