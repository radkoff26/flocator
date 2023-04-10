package com.example.flocator.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.flocator.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import kotlin.system.exitProcess

class DeleteAccountFragment : BottomSheetDialogFragment(), SettingsSection {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_delete_account, container, false)
        val closeButton = fragmentView.findViewById<ImageView>(R.id.delete_account_close_button)
        val confirmButton = fragmentView.findViewById<MaterialButton>(R.id.delete_account_confirm_button)

        closeButton.setOnClickListener {
            dismiss()
        }
2
        confirmButton.setOnClickListener {
            exitProcess(-1) // временная заглушка
        }
        return fragmentView
    }

    companion object {
        const val TAG = "Delete account fragment"
    }
}