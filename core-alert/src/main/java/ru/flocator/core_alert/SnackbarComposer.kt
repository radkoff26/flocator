package ru.flocator.core_alert

import android.view.View
import com.google.android.material.behavior.SwipeDismissBehavior.SWIPE_DIRECTION_ANY
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.BaseTransientBottomBar.Behavior
import com.google.android.material.snackbar.Snackbar
import ru.flocator.core_extensions.findColor

internal object SnackbarComposer {

    fun composeDesignedSnackbar(view: View, text: String, duration: Int): Snackbar {
        return Snackbar.make(view, text, duration)
            .setBackgroundTint(
                view.resources.findColor(ru.flocator.core_design.R.color.white)
            )
            .setTextColor(
                view.resources.findColor(ru.flocator.core_design.R.color.primary)
            )
            .setAnimationMode(ANIMATION_MODE_SLIDE)
    }
}