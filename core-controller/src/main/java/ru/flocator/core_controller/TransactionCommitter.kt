package ru.flocator.core_controller

abstract class TransactionCommitter internal constructor() {
    protected var clearAll: Boolean = false
    protected var closeSection: Boolean = false

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
