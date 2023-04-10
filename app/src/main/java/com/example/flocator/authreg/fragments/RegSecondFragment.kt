package com.example.flocator.authreg.fragments

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.flocator.databinding.FragmentRegistrationBinding
import com.example.flocator.utils.FragmentNavigationUtils

class RegSecondFragment : Fragment(), AuthRegSection {
    private lateinit var binding: FragmentRegistrationBinding

    companion object {
        private const val LOGIN = "Логин"
        private const val EMAIL = "Email"
        private const val NEXT = "Далее"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        binding.submitBtn.setOnClickListener {
            FragmentNavigationUtils.openFragment(requireActivity().supportFragmentManager, RegThirdFragment())
        }

        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.alreadyRegisteredText.setOnClickListener {
            FragmentNavigationUtils.openFragment(requireActivity().supportFragmentManager, AuthFragment())
        }

        binding.secondInputEditField.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.firstInputField.hint = LOGIN
        binding.secondInputField.hint = EMAIL
        binding.submitBtn.text = NEXT
        binding.submitBtn.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.white
            )
        )

        savedInstanceState?.let {
            it.getString(EMAIL)?.let { savedEmail ->
                binding.firstInputEditField.setText(savedEmail)
            }

            it.getString(LOGIN)?.let { savedLogin ->
                binding.secondInputEditField.setText(savedLogin)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EMAIL, binding.firstInputEditField.toString())
        outState.putString(LOGIN, binding.secondInputEditField.toString())
    }

    private fun createAccount(lastName: String, firstName: String) {
        TODO("Not yet implemented")
    }
}