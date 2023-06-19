package ru.flocator.core_controller

abstract class TransactionCommitter {
    var clearAll: Boolean = false
    var closeSection: Boolean = false

    fun clearAll(): TransactionCommitter {
        clearAll = true
        return this
    }

    fun closeSection(): TransactionCommitter {
        closeSection = true
        return this
    }

    abstract fun commit()
}
