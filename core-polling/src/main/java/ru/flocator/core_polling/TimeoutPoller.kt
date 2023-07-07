package ru.flocator.core_polling

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * This class represents a mechanism of auto-polling after each [timeout].
 * @param lifecycleOwner is a [LifecycleOwner] object which lifecycle [TimeoutPoller] will subscribe to.
 * @param timeout is a number of milliseconds after which [onPollCallback] will be re-invoked.
 * @param onPollCallback is a [OnPollCallback] interface implementation which function [OnPollCallback.onPoll]
 * will be called after each timeout tick.
 * @param onTimeoutCallback is a [OnTimeoutCallback] interface implementation which function
 * [OnTimeoutCallback.onTimeout] will be called when time's out and it's necessary to poll again.
 * This means that [OnTimeoutCallback.onTimeout] needs to contain all the code related to interrupting
 * the current execution.
 * @param onPauseCallback is a [OnPauseCallback] interface implementation which function
 * [OnPauseCallback.onPause] will be called as soon as lifecycle owner reaches ON_PAUSE event.
 * @author radkoff26
 * */
class TimeoutPoller(
    lifecycleOwner: LifecycleOwner,
    private val timeout: Long,
    private val onPollCallback: OnPollCallback,
    private val onTimeoutCallback: OnTimeoutCallback? = null,
    private val onPauseCallback: OnPauseCallback? = null
) : LifecycleEventObserver {
    private val lock = Any()
    private val executionScheduler = Schedulers.newThread() // TODO: make my own implementation
    private var compositeDisposable = CompositeDisposable()
    private var isRunning = false

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                synchronized(lock) {
                    isRunning = true
                }
                poll()
            }
            Lifecycle.Event.ON_PAUSE -> {
                synchronized(lock) {
                    isRunning = false
                    compositeDisposable.clear()
                }
            }
            else -> {
                return
            }
        }
    }

    private fun poll() {
        runIfRunning {
            val currentTime = System.currentTimeMillis()
            compositeDisposable.add(
                Completable.create { emitter ->
                    if (isRunning) {
                        onPollCallback.onPoll {
                            runOnExecutionScheduler {
                                emitter.onComplete()
                            }
                        }
                    }
                }
                    .timeout(
                        timeout,
                        TimeUnit.MILLISECONDS
                    ) { emitter ->
                        // If time is out, it's necessary to call appropriate callback,
                        // so that completion of current started callback interrupts
                        onTimeoutCallback?.onTimeout()
                        emitter.onComplete()
                    }
                    .doOnComplete {
                        // After completion of the callback the elapsed time is counted
                        val elapsedTime = System.currentTimeMillis()
                        val delta = elapsedTime - currentTime
                        // If time passed from the beginning of invocation is too small
                        // and it's too early to poll again, then it just waits for the time
                        // left to pass in background
                        if (delta < timeout) {
                            Thread.sleep(timeout - delta)
                        }
                        // Polls again
                        poll()
                    }
                    .doOnDispose {
                        // When lifecycle reached ON_PAUSE event, it's necessary not only to stop polling
                        // but also to interrupt the current callback execution
                        onPauseCallback?.onPause()
                    }
                    .subscribeOn(executionScheduler)
                    .observeOn(executionScheduler)
                    .subscribe()
            )
        }
    }

    private fun runIfRunning(callback: () -> Unit) {
        if (isRunning) {
            synchronized(lock) {
                if (isRunning) {
                    callback.invoke()
                }
            }
        }
    }

    private fun runOnExecutionScheduler(callback: () -> Unit) {
        Completable.create {
            callback.invoke()
            it.onComplete()
        }
            .subscribeOn(executionScheduler)
            .subscribe()
    }
}