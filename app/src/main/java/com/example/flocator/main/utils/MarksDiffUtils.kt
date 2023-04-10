package com.example.flocator.main.utils

import com.example.flocator.main.ui.data.MarkGroup
import java.util.Arrays

object MarksDiffUtils {
    fun isChanged(previous: List<MarkGroup>, current: List<MarkGroup>): Boolean {
        return previous != current
    }
}