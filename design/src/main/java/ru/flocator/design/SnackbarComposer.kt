package ru.flocator.design

import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.BaseTransientBottomBar.BaseCallback
import com.google.android.material.snackbar.Snackbar
import ru.flocator.core.extensions.findColor

object SnackbarComposer {

    fun composeDesignedSnackbar(view: View, text: String, onDismissed: () -> Unit = {}): Snackbar {
        return Snackbar.make(view, text, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(
                view.resources.findColor(R.color.white)
            )
            .setTextColor(
                view.resources.findColor(R.color.primary)
            )
            .setAnimationMode(ANIMATION_MODE_SLIDE)
            .addCallback(
                object : BaseCallback<Snackbar>() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        onDismissed.invoke()
                    }
                }
            )
    }
}