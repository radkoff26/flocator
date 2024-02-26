package ru.flocator.core.holder

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.function.Consumer

/**
 * Class which has its value being asynchronously loaded.
 * */
class LateInitHolder<T> {
    @Volatile
    private var isInitialized: Boolean = false

    // No need for volatile here since happens-before is guaranteed by isInitialized
    private var value: T? = null

    private val blockingQueue: BlockingQueue<Consumer<T?>> = LinkedBlockingQueue()

    suspend fun get(): T? = withContext(Dispatchers.Default) {
        if (isInitialized) value
        else coroutineScope {
            suspendCancellableCoroutine { continuation ->
                blockingQueue.put {
                    if (!continuation.isCancelled) {
                        continuation.resumeWith(Result.success(it))
                    }
                }
            }
        }
    }

    fun init(value: T?) {
        this.value = value
        this.isInitialized = true
        while (blockingQueue.isNotEmpty()) {
            blockingQueue.take().accept(value)
        }
    }
}