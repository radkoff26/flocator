package com.example.flocator.common.fragments

import android.content.res.Configuration
import android.os.Bundle
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

abstract class ResponsiveBottomSheetDialogFragment : BottomSheetDialogFragment() {

    abstract fun getCoordinatorLayout(): CoordinatorLayout

    abstract fun getBottomSheetScrollView(): NestedScrollView

    abstract fun getInnerLayout(): ViewGroup

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        expandBottomSheet()
    }

    protected fun layoutBottomSheet() {
        val coordinator = getCoordinatorLayout()
        val bottomSheet = getBottomSheetScrollView()
        val innerLayout = getInnerLayout()
        val height = requireActivity().getSize().y
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
            coordinator.width,
            coordinator.measuredHeight
        )
        bottomSheet.layoutParams = CoordinatorLayout.LayoutParams(
            bottomSheet.width,
            bottomSheet.measuredHeight
        )
        innerLayout.layoutParams = FrameLayout.LayoutParams(
            innerLayout.width,
            innerLayout.measuredHeight
        )
        coordinator.requestLayout()
        val behavior = (dialog as BottomSheetDialog).behavior
        if (isPortrait()) {
            behavior.state =
                BottomSheetBehavior.STATE_EXPANDED
        } else {
            behavior.state =
                BottomSheetBehavior.STATE_HALF_EXPANDED
        }
    }

    private fun isPortrait(): Boolean {
        return requireContext().resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    private fun expandBottomSheet() {
        val coordinator = getCoordinatorLayout()
        (dialog as BottomSheetDialog).behavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if ((dialog as BottomSheetDialog).behavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                    (dialog as BottomSheetDialog).behavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Skipped
            }
        })
        val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val behavior = (dialog as BottomSheetDialog).behavior
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