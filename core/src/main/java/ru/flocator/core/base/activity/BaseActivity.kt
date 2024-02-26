package ru.flocator.core.base.activity

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ru.flocator.core.alert.ErrorDebouncingAlertPoller

abstract class BaseActivity: AppCompatActivity() {
    private val alertPoller by lazy {
        ErrorDebouncingAlertPoller(this) { view, text, onDismissed ->
            composeSnackbar(view, text, onDismissed)
        }
    }

    fun notifyAboutError(errorText: String, view: View? = null) {
        alertPoller.postError(view ?: getRoot(), errorText)
    }

    private fun getRoot(): View = findViewById(android.R.id.content)

    protected abstract fun composeSnackbar(view: View, text: String, onDismissed: () -> Unit)
}
