package com.example.flocator.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.flocator.R
import com.example.flocator.authentication.authorization.AuthFragment
import com.example.flocator.common.storage.SharedStorage
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
class ExitAccountFragment : BottomSheetDialogFragment(), SettingsSection {

    @Inject
    lateinit var storage: SharedStorage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_exit_account, container, false)
        val exitButton = fragmentView.findViewById<ImageView>(R.id.exit_account_close_button)
        val confirmButton = fragmentView.findViewById<MaterialButton>(R.id.exit_account_confirm_button)

        exitButton.setOnClickListener {
            dismiss()
        }

        confirmButton.setOnClickListener {
            storage.clearUserData()
            FragmentNavigationUtils.clearAllAndOpenFragment(
                requireActivity().supportFragmentManager,
                AuthFragment()
            )
        }

        return fragmentView
    }

    companion object {
        const val TAG = "Exit account fragment"
    }
}