package com.example.flocator.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.flocator.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton


class ChangePasswordFragment : BottomSheetDialogFragment(), SettingsSection {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_change_password, container, false)
        val confirmButton = fragmentView.findViewById<MaterialButton>(R.id.change_pass_confirm_button)
        val closeButton = fragmentView.findViewById<FrameLayout>(R.id.change_password_close_button)
        confirmButton.setOnClickListener {
            dismiss()
        }
        closeButton.setOnClickListener {
            dismiss()
        }
        return fragmentView
    }

    companion object {
        const val TAG = "Change pass fragment"
    }


}