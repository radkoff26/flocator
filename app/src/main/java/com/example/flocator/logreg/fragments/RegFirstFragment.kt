package com.example.flocator.logreg.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.flocator.databinding.FragmentRegistrationBinding
import com.example.flocator.logreg.FragmentUtil

class RegFirstFragment : Fragment() {
    private lateinit var binding: FragmentRegistrationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        binding.submitBtn.setOnClickListener {
//            val lastName = lastNameInput.editText?.text.toString().trim();
//            val firstName = firstNameInput.editText?.text.toString().trim();
//
//            createAccount(lastName, firstName)

            //мок
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            FragmentUtil.replaceFragment(transaction, RegSecondFragment())
        }

        binding.alreadyRegisteredText.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            FragmentUtil.replaceFragment(transaction, AuthFragment())
        }

        binding.backBtn.visibility = View.INVISIBLE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.firstInputField.hint = "Фамилия"
        binding.secondInputField.hint = "Имя"
        binding.submitBtn.text = "Далее"
    }

    private fun createAccount(lastName: String, firstName: String) {
        TODO("Not yet implemented")
    }
}