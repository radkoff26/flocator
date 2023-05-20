package com.example.flocator.common.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.example.flocator.common.extensions.getSize
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class ResponsiveBottomSheetDialogFragment(
    private val portraitWidthRatio: Double,
    private val landscapeWidthRatio: Double
) : BottomSheetDialogFragment() {

    abstract fun getCoordinatorLayout(): CoordinatorLayout

    abstract fun getBottomSheetScrollView(): NestedScrollView

    abstract fun getInnerLayout(): ViewGroup

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        expandBottomSheet()
    }

    protected open fun layoutBottomSheet() {
        val coordinator = getCoordinatorLayout()
        val bottomSheet = getBottomSheetScrollView()
        val innerLayout = getInnerLayout()
        val size = requireActivity().getSize()
        val height = size.y
        val width = size.x
        // Width MeasureSpec is set to zero as it's irrelevant
        coordinator.measure(
            0,
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST)
        )
        bottomSheet.measure(
            0,
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST)
        )
        innerLayout.measure(
            0,
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        coordinator.layoutParams = FrameLayout.LayoutParams(
            width,
            bottomSheet.measuredHeight
        )
        val widthRatio = if (isPortrait()) portraitWidthRatio else landscapeWidthRatio
        val layoutWidth = (width * widthRatio).toInt()
        bottomSheet.layoutParams = CoordinatorLayout.LayoutParams(
            layoutWidth,
            bottomSheet.measuredHeight
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        }
        innerLayout.layoutParams = FrameLayout.LayoutParams(
            layoutWidth,
            innerLayout.measuredHeight
        )
        coordinator.requestLayout()
        val behavior = (dialog as BottomSheetDialog).behavior
        if (isPortrait()) {
            behavior.state =
                BottomSheetBehavior.STATE_EXPANDED
        } else {
            behavior.state =
                BottomSheetBehavior.STATE_HALF_EXPANDED // TODO: full expand if height is big
        }
    }

    protected fun isPortrait(): Boolean {
        return requireContext().resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    private fun expandBottomSheet() {
        val coordinator = getCoordinatorLayout()
        val bottomSheetDialog = dialog as BottomSheetDialog
        bottomSheetDialog.behavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (bottomSheetDialog.behavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Skipped
            }
        })
        val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val behavior = bottomSheetDialog.behavior
                behavior.peekHeight = 0
                behavior.skipCollapsed = false
                behavior.isHideable = true
                if (isPortrait()) {
                    behavior.isFitToContents = true
                    behavior.state =
                        BottomSheetBehavior.STATE_EXPANDED
                } else {
                    behavior.isFitToContents = false
                    behavior.state =
                        BottomSheetBehavior.STATE_HALF_EXPANDED
                }
                layoutBottomSheet()
                coordinator.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }
        coordinator.viewTreeObserver.addOnGlobalLayoutListener(listener)
    }
}