package ru.flocator.core.alert

import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.flocator.core.polling.TimeoutPoller
import java.util.*

typealias OnDismissedCallback = () -> Unit

typealias OnErrorCallback = (view: View, errorText: String, callback: OnDismissedCallback) -> Unit

internal typealias AlertTask = (emitter: () -> Unit) -> Unit

class ErrorDebouncingAlertPoller(
    lifecycleOwner: LifecycleOwner,
    private val onErrorCallback: OnErrorCallback
) : DefaultLifecycleObserver {
    private val queue: Queue<AlertTask> = LinkedList()
    private var isActive = true

    @Volatile
    private var lastPostTime: Long = 0L

    init {
        lifecycleOwner.lifecycle.addObserver(this)


        TimeoutPoller(lifecycleOwner, DEBOUNCING_TIME / 4, {
            if (queue.isNotEmpty()) {
                queue.poll()!!.invoke {
                    it.emit()
                }
            } else {
                it.emit()
            }
        })
    }

    override fun onDestroy(owner: LifecycleOwner) {
        isActive = false
    }

    fun postError(view: View, errorText: String) {
        queue.offer(createCallbackToPost(view, errorText))
    }

    private fun createCallbackToPost(view: View, errorText: String): AlertTask {
        return { emitter ->
            val elapsed = System.currentTimeMillis()
            if (elapsed - lastPostTime >= DEBOUNCING_TIME) {
                lastPostTime = System.currentTimeMillis()
                CoroutineScope(Dispatchers.Main).launch {
                    onErrorCallback.invoke(view, errorText) {
                        emitter.invoke()
                    }
                }
            }
        }
    }

    companion object {
        const val DEBOUNCING_TIME = 5000L
    }
}