package ru.flocator.core.polling

fun interface OnPollCallback {
    fun onPoll(emitter: PollingEmitter)
}