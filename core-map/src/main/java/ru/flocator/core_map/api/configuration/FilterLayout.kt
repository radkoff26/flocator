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
import androidx.core.view.children
import com.google.android.material.button.MaterialButton
import ru.flocator.core_extensions.findColor
import ru.flocator.core_extensions.findDrawable
import ru.flocator.core_map.R
import ru.flocator.core_utils.ViewUtils

class FilterLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    companion object {
        // Animation settings
        const val BUTTON_ANIMATION_DURATION = 150L
        const val TRANSITION_THRESHOLD = 30

        // Dimensions
        private const val BUTTON_TEXT_SIZE: Float = 14F
        private const val BUTTON_CORNER_RADIUS = 20
        private const val MARGIN_TOP = 8
        private const val BUTTONS_LETTER_SPACING = 0.03125F

        // Toggle Button Settings
        @StringRes
        private val textRes: Int = R.string.filter

        @DrawableRes
        private val iconResCollapsed: Int = R.drawable.filter_image

        @DrawableRes
        private val iconResExpanded: Int = R.drawable.close_image

        @ColorRes
        private val backgroundColorCollapsed: Int = ru.flocator.core_design.R.color.white

        @ColorRes
        private val backgroundColorExpanded: Int = ru.flocator.core_design.R.color.tint

        @ColorRes
        private val textColorCollapsed: Int = ru.flocator.core_design.R.color.tint

        @ColorRes
        private val textColorExpanded: Int = ru.flocator.core_design.R.color.white

        // Filter Buttons
        @ColorRes
        private val nonActiveBackgroundColor: Int = ru.flocator.core_design.R.color.white

        @ColorRes
        private val activeBackgroundColor: Int = ru.flocator.core_design.R.color.secondary

        @ColorRes
        private val filterButtonTextColor: Int = ru.flocator.core_design.R.color.tint
    }

    private val buttonIconSize: Int = ViewUtils.spToPx(20F, context)

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

    private var activeFilter: Int = 0

    private var onMapConfigurationChangeListener: OnMapConfigurationChangeListener? = null

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

    fun interface OnMapConfigurationChangeListener {

        fun onChange(mapConfiguration: MapConfiguration)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // Layout settings
        orientation = VERTICAL
        gravity = Gravity.RIGHT

        // Toggle settings
        addView(filterToggleButton)
        initToggleButton()

        // Buttons settings
        filterButtonHolders.forEachIndexed { index, item ->
            item.button.apply {
                // Adding button to the layout
                addView(this)

                initFilterButton(index)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wrapContentDimension = MeasureSpec.makeMeasureSpec(
            0,
            MeasureSpec.UNSPECIFIED
        )
        filterToggleButton.measure(
            wrapContentDimension,
            wrapContentDimension
        )
        var height = filterToggleButton.measuredHeight
        var maxWidth = filterToggleButton.measuredWidth
        val iterator = children.iterator()
        iterator.next()
        while (iterator.hasNext()) {
            val view = iterator.next()
            view.measure(
                wrapContentDimension,
                wrapContentDimension
            )
            height += MARGIN_TOP + view.measuredHeight
            maxWidth = maxOf(maxWidth, view.measuredWidth)
        }
        setMeasuredDimension(
            MeasureSpec.makeMeasureSpec(
                maxWidth,
                MeasureSpec.EXACTLY
            ),
            MeasureSpec.makeMeasureSpec(
                height,
                MeasureSpec.EXACTLY
            )
        )
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        animator = null
        isAnimated = false
        super.onDetachedFromWindow()
    }

    fun setToggleFilterLayoutListener(listener: OnMapConfigurationChangeListener) {
        onMapConfigurationChangeListener = listener
    }

    fun setActiveConfiguration(mapConfiguration: MapConfiguration) {
        setActiveConfiguration(mapConfiguration, false)
    }

    private fun initToggleButton() {
        filterToggleButton.text = resources.getString(textRes)
        filterToggleButton.textSize = BUTTON_TEXT_SIZE
        filterToggleButton.iconSize = buttonIconSize
        filterToggleButton.cornerRadius = ViewUtils.dpToPx(BUTTON_CORNER_RADIUS, context)
        filterToggleButton.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_END
        filterToggleButton.textAlignment = MaterialButton.TEXT_ALIGNMENT_VIEW_END
        filterToggleButton.isAllCaps = false
        filterToggleButton.letterSpacing = BUTTONS_LETTER_SPACING
        filterToggleButton.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
        if (isExpanded) {
            adjustToggleButtonOnExpanded()
        } else {
            adjustToggleButtonOnCollapsed()
        }
        filterToggleButton.setOnClickListener {
            if (!isAnimated) {
                if (isExpanded) {
                    collapse()
                } else {
                    expand()
                }
            }
        }
    }

    private fun initFilterButton(index: Int) {
        val item = filterButtonHolders[index]
        item.button.apply {
            // Setting margin on top of the button
            (this.layoutParams as MarginLayoutParams).topMargin = MARGIN_TOP

            // Applying appropriate styles to button
            resources.apply {
                text = getString(item.textRes)
                textSize = BUTTON_TEXT_SIZE
                icon = findDrawable(item.iconRes)
                iconSize = buttonIconSize
                iconTint = ColorStateList.valueOf(
                    findColor(filterButtonTextColor)
                )
                iconGravity = MaterialButton.ICON_GRAVITY_END
                isAllCaps = false
                letterSpacing = 0.03125F
                setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
                setTextColor(findColor(filterButtonTextColor))
                cornerRadius = ViewUtils.dpToPx(BUTTON_CORNER_RADIUS, context)
            }

            // Setting button text to be in the very end of the View
            textAlignment = MaterialButton.TEXT_ALIGNMENT_TEXT_END

            // Setting proper background color for this button
            setBackgroundColorOnFilterButton(index)

            // If the layout is not expanded,
            if (!isExpanded) {
                // Then the button is not visible
                visibility = GONE
            }

            // Setting OnClickListener to switch filter
            setOnClickListener {
                val holder = filterButtonHolders[index]
                setActiveConfiguration(holder.mapConfiguration, true)
            }
        }
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
            onMapConfigurationChangeListener?.onChange(mapConfiguration)
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
                button.translationX = value * TRANSITION_THRESHOLD
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
                button.translationX = TRANSITION_THRESHOLD.toFloat()
                button.visibility = VISIBLE
                button.alpha = 0f
            }
            doOnEnd {
                button.translationX = 0F
                expandButton(index + 1)
            }
            addUpdateListener {
                val value = it.animatedValue as Float
                button.translationX = (1F - value) * TRANSITION_THRESHOLD
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