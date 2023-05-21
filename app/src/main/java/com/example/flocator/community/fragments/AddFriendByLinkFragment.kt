package com.example.flocator.community.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.example.flocator.R
import com.example.flocator.common.fragments.ResponsiveBottomSheetDialogFragment
import com.example.flocator.common.repository.MainRepository
import com.example.flocator.community.CommunitySection
import com.example.flocator.community.view_models.AddFriendByLinkFragmentViewModel
import com.example.flocator.databinding.FragmentAddFriendBinding
import com.example.flocator.main.ui.add_mark.AddMarkFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.properties.Delegates
import kotlin.system.exitProcess

@AndroidEntryPoint
class AddFriendByLinkFragment : ResponsiveBottomSheetDialogFragment(
    BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO,
    BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO
), CommunitySection {
    private var _binding: FragmentAddFriendBinding? = null
    private val binding: FragmentAddFriendBinding
        get() = _binding!!
    private var currentUserId by Delegates.notNull<Long>()

    @Inject
    lateinit var repository: MainRepository


    private lateinit var addFriendByLinkFragmentViewModel: AddFriendByLinkFragmentViewModel
    override fun getCoordinatorLayout(): CoordinatorLayout {
        return binding.coordinator
    }

    override fun getBottomSheetScrollView(): NestedScrollView {
        return binding.bs
    }

    override fun getInnerLayout(): ViewGroup {
        return binding.content
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddFriendBinding.inflate(inflater, container, false)
        addFriendByLinkFragmentViewModel = AddFriendByLinkFragmentViewModel(repository)
        val args: Bundle? = arguments
        if(args != null){
            currentUserId = args.getLong("currentUserId")
        } else{
            currentUserId = -1
        }

        binding.addFriendConfirmButton.setOnClickListener {
            if(binding.userLoginText.text.toString().isNotEmpty()){
                println(binding.userLoginText.text.toString())
                addFriendByLinkFragmentViewModel.addFriendByLogin(currentUserId, binding.userLoginText.text.toString())
                dismiss()
            }
        }

        binding.addFriendCloseButton.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "Add friend by link fragment"
        const val BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO = 0.9
        const val BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO = 0.8
    }


}