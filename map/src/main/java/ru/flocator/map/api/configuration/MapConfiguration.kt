package ru.flocator.map.api.configuration

sealed class MapConfiguration {
    object MarksOnly: MapConfiguration()
    object UsersOnly: MapConfiguration()
    object All: MapConfiguration()
    class SpecialFilter(val mapFilters: List<MapFilter>): MapConfiguration()

    override fun toString(): String {
        return this::class.java.simpleName
    }
}
