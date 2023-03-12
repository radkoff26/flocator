package com.example.flocator.fragments.main

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.flocator.R
import com.example.flocator.adapters.CarouselViewPagerAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class AddMarkFragment : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setContentView(R.layout.fragment_add_mark)

        dialog.setOnShowListener {
            val view = (it as BottomSheetDialog).findViewById<LinearLayout>(R.id.bs) ?: return@setOnShowListener

            val behavior = BottomSheetBehavior.from(view)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            val viewPager = view.findViewById(R.id.photo_carousel) as ViewPager2
            viewPager.adapter = CarouselViewPagerAdapter()

            val addMarkButton = view.findViewById(R.id.add_mark_btn) as MaterialButton
            val cancelMarkButton = view.findViewById(R.id.cancel_mark_btn) as MaterialButton

            addMarkButton.setOnClickListener {
                dismiss()
            }

            cancelMarkButton.setOnClickListener {
                dismiss()
            }
        }

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragment = inflater.inflate(R.layout.fragment_add_mark, container, false)

        return fragment
    }



    companion object {
        const val TAG = "ModalBottomSheet"
    }
}