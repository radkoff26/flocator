package ru.flocator.core_map.api.configuration

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.google.android.material.button.MaterialButton
import ru.flocator.core_extensions.findColor
import ru.flocator.core_extensions.findDrawable
import ru.flocator.core_map.R

class FilterLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    companion object {
        // Animation settings
        const val BUTTON_ANIMATION_DURATION = 150L
        const val TRANSITION_THRESHOLD = 30

        // Toggle Button Settings
        @StringRes
        val textRes: Int = R.string.filter

        @DrawableRes
        val iconResCollapsed: Int = R.drawable.filter_image

        @DrawableRes
        val iconResExpanded: Int = R.drawable.close_image

        @ColorRes
        val backgroundColorCollapsed: Int = ru.flocator.core_design.R.color.white

        @ColorRes
        val backgroundColorExpanded: Int = ru.flocator.core_design.R.color.tint

        @ColorRes
        val textColorCollapsed: Int = ru.flocator.core_design.R.color.tint

        @ColorRes
        val textColorExpanded: Int = ru.flocator.core_design.R.color.white

        // Filter Buttons
        @ColorRes
        val nonActiveBackgroundColor: Int = ru.flocator.core_design.R.color.white

        @ColorRes
        val activeBackgroundColor: Int = ru.flocator.core_design.R.color.secondary

        @ColorRes
        val filterButtonTextColor: Int = ru.flocator.core_design.R.color.tint
    }

    private val filterToggleButton: MaterialButton = MaterialButton(context)

    private val filterButtonHolders: List<FilterButtonHolder> = listOf(
        FilterButtonHolder(
            MaterialButton(context),
            R.string.friends_only,
            R.drawable.users_only_image,
            MapConfiguration.UsersOnly
        ),
        FilterButtonHolder(
            MaterialButton(context),
            R.string.marks_only,
            R.drawable.marks_only_image,
            MapConfiguration.MarksOnly
        ),
        FilterButtonHolder(
            MaterialButton(context),
            R.string.all,
            R.drawable.all_image,
            MapConfiguration.All
        )
    )

    private val onToggleFilterLayoutListener: OnToggleFilterLayoutListener = OnToggleFilterLayoutListener {
        if (isExpanded) {
            collapse()
        } else {
            expand()
        }
    }

    private var activeFilter: Int = 0

    private var onFilterClickListener: OnFilterClickListener? = null

    private var isExpanded: Boolean = false

    private var animator: ValueAnimator? = null

    private var isAnimated: Boolean = false

    private data class FilterButtonHolder(
        val button: MaterialButton,
        @StringRes
        val textRes: Int,
        @DrawableRes
        val iconRes: Int,
        val mapConfiguration: MapConfiguration
    )

    fun interface OnFilterClickListener {

        fun onClickFilter(mapConfiguration: MapConfiguration)
    }

    internal fun interface OnToggleFilterLayoutListener {

        fun onToggle()
    }

    init {
        // Layout settings
        orientation = VERTICAL
        gravity = Gravity.RIGHT
        measure(
            MeasureSpec.makeMeasureSpec(
                0,
                MeasureSpec.UNSPECIFIED
            ),
            MeasureSpec.makeMeasureSpec(
                0,
                MeasureSpec.UNSPECIFIED
            )
        )

        // Toggle settings
        addView(filterToggleButton)
        filterToggleButton.text = resources.getString(textRes)
        filterToggleButton.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_END
        textAlignment = MaterialButton.TEXT_ALIGNMENT_VIEW_END
        filterToggleButton.measure(
            MeasureSpec.makeMeasureSpec(
                0,
                MeasureSpec.UNSPECIFIED
            ),
            MeasureSpec.makeMeasureSpec(
                0,
                MeasureSpec.UNSPECIFIED
            )
        )
        if (isExpanded) {
            adjustToggleButtonOnExpanded()
        } else {
            adjustToggleButtonOnCollapsed()
        }
        filterToggleButton.setOnClickListener {
            if (!isAnimated) {
                onToggleFilterLayoutListener.onToggle()
            }
        }

        // Buttons settings
        filterButtonHolders.forEachIndexed { index, item ->
            item.button.apply {
                // Adding button to the layout
                addView(this)

                // Applying appropriate styles to button
                resources.apply {
                    text = getString(item.textRes)
                    icon = findDrawable(item.iconRes)
                    iconTint = ColorStateList.valueOf(
                        findColor(filterButtonTextColor)
                    )
                    setTextColor(findColor(filterButtonTextColor))
                    iconGravity = MaterialButton.ICON_GRAVITY_TEXT_END
                }

                // Setting button text to be in the very end of the View
                textAlignment = MaterialButton.TEXT_ALIGNMENT_VIEW_END

                // Setting proper background color for this button
                setBackgroundColorOnFilterButton(index)

                // Wrapping button dimensions
                measure(
                    MeasureSpec.makeMeasureSpec(
                        0,
                        MeasureSpec.UNSPECIFIED
                    ),
                    MeasureSpec.makeMeasureSpec(
                        0,
                        MeasureSpec.UNSPECIFIED
                    )
                )

                if (!isExpanded) {
                    visibility = GONE
                }

                // Setting OnClickListener to switch filter
                setOnClickListener {
                    val holder = filterButtonHolders[index]
                    setActiveConfiguration(holder.mapConfiguration, true)
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        animator = null
        isAnimated = false
        super.onDetachedFromWindow()
    }

    fun setActiveConfiguration(mapConfiguration: MapConfiguration) {
        setActiveConfiguration(mapConfiguration, false)
    }

    private fun setActiveConfiguration(mapConfiguration: MapConfiguration, shouldNotify: Boolean) {
        val index = filterButtonHolders.indexOfFirst {
            it.mapConfiguration == mapConfiguration
        }

        if (activeFilter == index) {
            // No changes provided
            return
        }

        // Changing active filter index
        val prev = activeFilter
        activeFilter = index

        // Resetting filter button background
        setBackgroundColorOnFilterButton(prev)
        setBackgroundColorOnFilterButton(index)

        // If necessary,
        if (shouldNotify) {
            // Then notifying subscriber about map configuration (filter) change
            onFilterClickListener?.onClickFilter(mapConfiguration)
        }
    }

    private fun adjustToggleButtonOnCollapsed() {
        with(resources) {
            filterToggleButton.icon = findDrawable(iconResCollapsed)
            filterToggleButton.iconTint = ColorStateList.valueOf(
                resources.findColor(textColorCollapsed)
            )
            filterToggleButton.setBackgroundColor(findColor(backgroundColorCollapsed))
            filterToggleButton.setTextColor(findColor(textColorCollapsed))
        }
    }

    private fun adjustToggleButtonOnExpanded() {
        with(resources) {
            filterToggleButton.icon = findDrawable(iconResExpanded)
            filterToggleButton.iconTint = ColorStateList.valueOf(
                resources.findColor(textColorExpanded)
            )
            filterToggleButton.setBackgroundColor(findColor(backgroundColorExpanded))
            filterToggleButton.setTextColor(findColor(textColorExpanded))
        }
    }

    private fun collapse() {
        adjustToggleButtonOnCollapsed()
        isAnimated = true
        collapseButton(filterButtonHolders.lastIndex)
        isExpanded = false
    }

    private fun collapseButton(index: Int) {
        if (index < 0) {
            isAnimated = false
            return
        }
        val button = filterButtonHolders[index].button
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = BUTTON_ANIMATION_DURATION
                doOnEnd {
                    button.visibility = GONE
                    collapseButton(index - 1)
                }
                addUpdateListener {
                    val value = it.animatedValue as Float
                    button.translationY -= value * TRANSITION_THRESHOLD
                    button.alpha = 1f - value
                }
                start()
            }
    }

    private fun expand() {
        adjustToggleButtonOnExpanded()
        isAnimated = true
        expandButton(0)
        isExpanded = true
    }

    private fun expandButton(index: Int) {
        if (index == filterButtonHolders.size) {
            isAnimated = false
            return
        }
        val button = filterButtonHolders[index].button
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = BUTTON_ANIMATION_DURATION
            doOnStart {
                button.translationY = -TRANSITION_THRESHOLD.toFloat()
                button.visibility = VISIBLE
                button.alpha = 0f
            }
            doOnEnd {
                button.translationY = 0F
                expandButton(index + 1)
            }
            addUpdateListener {
                val value = it.animatedValue as Float
                button.translationY += value * TRANSITION_THRESHOLD
                button.alpha = value
            }
            start()
        }
    }

    private fun setBackgroundColorOnFilterButton(index: Int) {
        val button = filterButtonHolders[index].button
        if (activeFilter == index) {
            button.setBackgroundColor(
                resources.findColor(activeBackgroundColor)
            )
        } else {
            button.setBackgroundColor(
                resources.findColor(nonActiveBackgroundColor)
            )
        }
    }
}