package ru.flocator.core_exceptions

class LostConnectionException @JvmOverloads constructor(
    message: String = "",
    throwable: Throwable? = null
) : Exception(message, throwable) {
}