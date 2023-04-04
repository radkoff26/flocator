package com.example.flocator.authreg.fragments

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.flocator.databinding.FragmentAuthBinding
import com.example.flocator.utils.FragmentNavigationUtils


class AuthFragment : Fragment(), AuthRegSection {
    private lateinit var binding: FragmentAuthBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAuthBinding.inflate(inflater, container, false)

        binding.entranceBtn.setOnClickListener {
            FragmentNavigationUtils.openFragment(requireActivity().supportFragmentManager, LocationRequestFragment())
        }

        binding.registrationBtn.setOnClickListener {
            FragmentNavigationUtils.openFragment(requireActivity().supportFragmentManager, RegFirstFragment())
        }

        binding.forgotPasswordText.setOnClickListener {
            TODO("Not yet implemented")
        }

        binding.emailLoginFieldEdit.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        return binding.root
    }

    private fun login(email: String, password: String) {
        TODO("Not yet implemented")
    }

    private fun validateFields(email: String, password: String): Boolean {
        TODO("Not yet implemented")
    }
}