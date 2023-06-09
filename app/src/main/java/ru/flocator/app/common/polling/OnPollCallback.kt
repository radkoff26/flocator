package ru.flocator.app.common.polling

fun interface OnPollCallback {
    fun onPoll(emitter: PollingEmitter)
}