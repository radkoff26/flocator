package com.example.flocator.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.example.flocator.R
import com.example.flocator.authentication.authorization.AuthFragment
import com.example.flocator.common.fragments.ResponsiveBottomSheetDialogFragment
import com.example.flocator.common.repository.MainRepository
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.example.flocator.main.ui.add_mark.AddMarkFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ExitAccountFragment : ResponsiveBottomSheetDialogFragment(
    AddMarkFragment.BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO,
    AddMarkFragment.BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO
), SettingsSection {

    @Inject
    lateinit var repository: MainRepository
    lateinit var fragmentView: View
    override fun getCoordinatorLayout(): CoordinatorLayout {
        return fragmentView.findViewById(R.id.coordinator)
    }

    override fun getBottomSheetScrollView(): NestedScrollView {
        return fragmentView.findViewById(R.id.bs)
    }

    override fun getInnerLayout(): ViewGroup {
        return fragmentView.findViewById(R.id.content)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_exit_account, container, false)
        val exitButton = fragmentView.findViewById<ImageView>(R.id.exit_account_close_button)
        val confirmButton = fragmentView.findViewById<MaterialButton>(R.id.exit_account_confirm_button)

        exitButton.setOnClickListener {
            dismiss()
        }

        confirmButton.setOnClickListener {
            repository.userDataCache.clearUserData()
            repository.userInfoCache.clearUserInfo()
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