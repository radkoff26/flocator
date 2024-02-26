package ru.flocator.core.exceptions

class LostConnectionException @JvmOverloads constructor(
    message: String = "",
    throwable: Throwable? = null
) : Exception(message, throwable)