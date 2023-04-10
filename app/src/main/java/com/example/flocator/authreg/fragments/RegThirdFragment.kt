package com.example.flocator.authreg.fragments

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.example.flocator.databinding.FragmentRegistrationBinding

class RegThirdFragment : Fragment(), AuthRegSection {
    private lateinit var binding: FragmentRegistrationBinding

    companion object {
        private const val PASSWORD = "Пароль"
        private const val REPEAT_PASSWORD = "Повторите пароль"
        private const val REGISTER = "Зарегистрироваться"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        binding.submitBtn.setOnClickListener {
            FragmentNavigationUtils.openFragment(requireActivity().supportFragmentManager, LocationRequestFragment())
        }

        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.alreadyRegisteredText.setOnClickListener {
            //Должны сохраняться введенные значения
            FragmentNavigationUtils.openFragment(requireActivity().supportFragmentManager, AuthFragment())
        }

        binding.firstInputEditField.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        binding.secondInputEditField.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.firstInputField.hint = PASSWORD
        binding.secondInputField.hint = REPEAT_PASSWORD
        binding.submitBtn.text = REGISTER

    }

    private fun createAccount(lastName: String, firstName: String) {
        TODO("Not yet implemented")
    }

}