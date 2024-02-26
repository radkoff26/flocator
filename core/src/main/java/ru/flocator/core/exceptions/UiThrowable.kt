package ru.flocator.core.exceptions

import androidx.annotation.StringRes

class UiThrowable @JvmOverloads constructor(
    override val message: String? = null,
    override val cause: Throwable? = null,
    @StringRes
    val uiMessageStringRes: Int
) : Throwable(message, cause) {

    constructor(
        uiMessageStringRes: Int,
        throwable: Throwable
    ) : this(
        uiMessageStringRes = uiMessageStringRes,
        cause = throwable,
        message = throwable.message
    )
}