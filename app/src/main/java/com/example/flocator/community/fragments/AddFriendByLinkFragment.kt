package com.example.flocator.community.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.flocator.R
import com.example.flocator.community.CommunitySection
import com.example.flocator.databinding.FragmentAddFriendBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import kotlin.system.exitProcess

class AddFriendByLinkFragment : BottomSheetDialogFragment(), CommunitySection {
    private var _binding: FragmentAddFriendBinding? = null
    private val binding: FragmentAddFriendBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddFriendBinding.inflate(inflater, container, false)

        binding.addFriendCloseButton.setOnClickListener {
            dismiss()
        }

        binding.addFriendConfirmButton.setOnClickListener {
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
    }


}