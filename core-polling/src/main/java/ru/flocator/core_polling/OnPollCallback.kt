package ru.flocator.core_polling

fun interface OnPollCallback {
    fun onPoll(emitter: PollingEmitter)
}