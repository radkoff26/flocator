package com.example.flocator.logreg.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.flocator.R
import com.example.flocator.databinding.FragmentAuthBinding
import com.example.flocator.logreg.FragmentUtil
import com.example.flocator.main.fragments.MainFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

class AuthFragment : Fragment() {
    private lateinit var binding: FragmentAuthBinding;
    private lateinit var emailInput: TextInputLayout
    private lateinit var passwordInput: TextInputLayout
    private lateinit var entranceButton: MaterialButton
    private lateinit var registrationButton: MaterialButton
    private lateinit var forgotPasswordText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAuthBinding.inflate(inflater, container, false)

        emailInput = binding.emailLoginField
        passwordInput = binding.passwordLoginField
        entranceButton = binding.entranceBtn
        registrationButton = binding.registrationBtn
        forgotPasswordText = binding.forgotPasswordText

        entranceButton.setOnClickListener {
//            val email = emailInput.editText?.text.toString().trim();
//            val password = passwordInput.editText?.text.toString().trim();
//
//            if (validateFields(email, password)) {
//                login(email, password)
//            }

            //Мок
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            FragmentUtil.openFragment(transaction, MainFragment())
        }



        registrationButton.setOnClickListener{
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            FragmentUtil.openFragment(transaction, RegNameFragment())
        }

        forgotPasswordText.setOnClickListener {
            TODO("Not yet implemented")
        }

        return binding.root
    }

    private fun login(email: String, password: String) {
        TODO("Not yet implemented")
    }

    private fun validateFields(email: String, password: String): Boolean {
        TODO("Not yet implemented")
    }
}