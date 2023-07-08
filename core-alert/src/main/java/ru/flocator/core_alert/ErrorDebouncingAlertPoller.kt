package ru.flocator.core_alert

import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.snackbar.BaseTransientBottomBar.BaseCallback
import com.google.android.material.snackbar.Snackbar
import ru.flocator.core_polling.PollingEmitter
import ru.flocator.core_polling.TimeoutPoller
import java.util.LinkedList
import java.util.Queue

internal typealias AlertTask = (emitter: PollingEmitter) -> Unit

class ErrorDebouncingAlertPoller(
    @Volatile
    private var activity: FragmentActivity?
) : DefaultLifecycleObserver {
    private val queue: Queue<AlertTask> = LinkedList()
    private var isActive = true

    init {
        activity!!.lifecycle.addObserver(this)

        TimeoutPoller(activity!!, DEBOUNCING_TIME, {
            if (queue.isNotEmpty()) {
                queue.poll()!!.invoke(it)
            } else {
                it.emit()
            }
        })
    }

    override fun onDestroy(owner: LifecycleOwner) {
        isActive = false
        activity = null
    }

    fun postError(view: View, errorText: String) {
        queue.offer(createCallbackToPost(view, errorText))
    }

    private fun createCallbackToPost(view: View, errorText: String): AlertTask {
        return { emitter ->
            activity?.also {
                it.runOnUiThread {
                    SnackbarComposer.composeDesignedSnackbar(
                        view,
                        errorText,
                        Snackbar.LENGTH_LONG
                    ).addCallback(
                        object : BaseCallback<Snackbar>() {
                            override fun onDismissed(
                                transientBottomBar: Snackbar?,
                                event: Int
                            ) {
                                emitter.emit()
                            }
                        }
                    ).show()
                }
            }
        }
    }

    companion object {
        const val DEBOUNCING_TIME = 5000L
    }
}