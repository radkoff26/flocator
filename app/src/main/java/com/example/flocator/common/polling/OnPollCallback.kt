package com.example.flocator.common.polling

fun interface OnPollCallback {
    fun onPoll(emitter: PollingEmitter)
}