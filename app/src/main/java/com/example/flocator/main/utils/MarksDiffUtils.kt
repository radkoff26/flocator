package com.example.flocator.main.utils

import com.example.flocator.main.ui.data.MarkGroup

object MarksDiffUtils {
    data class MarksDiffUtilsResult(val removed: List<MarkGroup>, val added: List<MarkGroup>)

    fun isChanged(previous: List<MarkGroup>, current: List<MarkGroup>) = previous != current

    fun calculateDifference(previous: List<MarkGroup>, current: List<MarkGroup>): MarksDiffUtilsResult {
        val removed: MutableList<MarkGroup> = ArrayList()
        val added: MutableList<MarkGroup> = ArrayList()
        var i = 0
        while (i < previous.size) {
            val item = previous[i]
            if (item !in current) {
                removed.add(item)
            }
            i++
        }
        i = 0
        while (i < current.size) {
            val item = current[i]
            while (item !in previous) {
                added.add(item)
            }
            i++
        }
        return MarksDiffUtilsResult(removed, added)
    }
}