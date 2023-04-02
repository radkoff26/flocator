package com.example.flocator.logreg.fragments

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.flocator.databinding.FragmentRegistrationBinding
import com.example.flocator.logreg.FragmentUtil
import com.example.flocator.main.ui.fragments.MainFragment

class RegThirdFragment : Fragment() {
    private lateinit var binding: FragmentRegistrationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        binding.submitBtn.setOnClickListener {
//            val login = passInput.editText?.text.toString().trim();
//            val email = passInputRepeat.editText?.text.toString().trim();
//
//            createAccount(login, email)

            //мок
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            FragmentUtil.replaceFragment(transaction, MainFragment())
        }

        binding.backBtn.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            FragmentUtil.replaceFragment(transaction, RegSecondFragment())
        }

        binding.alreadyRegisteredText.setOnClickListener {
            //Должны сохраняться введенные значения
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            FragmentUtil.replaceFragment(transaction, AuthFragment())
        }

        binding.firstInputEditField.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        binding.secondInputEditField.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.firstInputField.hint = "Пароль"
        binding.secondInputField.hint = "Повторите пароль"
        binding.submitBtn.text = "Зарегистрироваться"

    }

    private fun createAccount(lastName: String, firstName: String) {
        TODO("Not yet implemented")
    }

}