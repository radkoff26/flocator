package com.example.flocator.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.flocator.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import kotlin.system.exitProcess

class ExitAccountFragment : BottomSheetDialogFragment(), SettingsSection {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_exit_account, container, false)
        val exitButton = fragmentView.findViewById<FrameLayout>(R.id.exit_account_close_button)
        val confirmButton = fragmentView.findViewById<MaterialButton>(R.id.exit_account_confirm_button)

        exitButton.setOnClickListener {
            dismiss()
        }

        confirmButton.setOnClickListener {
            exitProcess(-1) // временная заглушка
        }

        return fragmentView
    }

    companion object {
        const val TAG = "Exit account fragment"
    }
}