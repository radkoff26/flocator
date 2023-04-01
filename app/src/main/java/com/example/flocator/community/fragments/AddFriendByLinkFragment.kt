package com.example.flocator.community.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.flocator.R
import com.example.flocator.databinding.FragmentAddFriendBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import kotlin.system.exitProcess

class AddFriendByLinkFragment: BottomSheetDialogFragment() {
    private lateinit var binding: FragmentAddFriendBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddFriendBinding.inflate(inflater, container, false)

        binding.addFriendCloseButton.setOnClickListener {
            dismiss()
        }

        binding.addFriendConfirmButton.setOnClickListener {
            dismiss()
        }
        return binding.root
    }

    companion object {
        const val TAG = "Add friend by link fragment"
    }



}