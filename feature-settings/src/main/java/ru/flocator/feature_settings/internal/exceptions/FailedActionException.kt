package ru.flocator.feature_settings.internal.exceptions

class FailedActionException @JvmOverloads constructor(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Exception(message, cause)
