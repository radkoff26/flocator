package ru.flocator.map.internal.domain.view

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

internal class ViewPool<T : ReusableView>(
    lifecycleOwner: LifecycleOwner,
    private val factory: ViewFactory<T>
) : DefaultLifecycleObserver {
    private val viewList: MutableList<T> = ArrayList()

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    fun getView(modificationCallback: (view: T) -> Unit): T {
        val view: T
        val usedCount = countUsed()
        if (viewList.size > usedCount) {
            view = viewList.find { !it.isBusy }!!
            modificationCallback.invoke(view)
        } else {
            view = factory.create(this::onRecycleView)
            modificationCallback.invoke(view)
            if (viewList.size < MAX_POOL_LENGTH) {
                viewList.add(view)
            }
        }
        view.use()
        return view
    }

    override fun onDestroy(owner: LifecycleOwner) {
        viewList.clear()
    }

    private fun onRecycleView() {
        cleanIfNecessary()
    }

    private fun cleanIfNecessary() {
        val size = viewList.size
        val half = size / 2
        val usedCount = countUsed()
        if (half > usedCount && size > MIN_POOL_LENGTH) {
            var i = 0
            while (i < viewList.size) {
                if (!viewList[i].isBusy) {
                    viewList.removeAt(i)
                } else {
                    i++
                }
                val currentSize = viewList.size
                if (currentSize / 2 <= usedCount || currentSize == MIN_POOL_LENGTH) {
                    break
                }
            }
        }
    }

    private fun countUsed(): Int = viewList.count { it.isBusy }


    companion object {
        private const val MIN_POOL_LENGTH = 6
        private const val MAX_POOL_LENGTH = 100
    }
}