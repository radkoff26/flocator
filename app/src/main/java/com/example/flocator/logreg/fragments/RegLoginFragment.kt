package com.example.flocator.logreg.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.example.flocator.R
import com.example.flocator.databinding.FragmentRegLoginBinding
import com.example.flocator.logreg.FragmentUtil
import com.example.flocator.main.fragments.MainFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

class RegLoginFragment : Fragment() {
    private lateinit var binding: FragmentRegLoginBinding
    private lateinit var loginInput: TextInputLayout
    private lateinit var emailInput: TextInputLayout
    private lateinit var nextButton: MaterialButton
    private lateinit var alreadyRegistered: TextView
    private lateinit var backButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegLoginBinding.inflate(inflater, container, false)

        loginInput = binding.regLoginField
        emailInput = binding.regEmailField
        nextButton = binding.nextBtn
        alreadyRegistered = binding.alreadyRegisteredText
        backButton = binding.backBtn

        nextButton.setOnClickListener {
//            val login = loginInput.editText?.text.toString().trim();
//            val email = emailInput.editText?.text.toString().trim();
//
//            createAccount(login, email)

            //мок
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            FragmentUtil.openFragment(transaction, RegPasswordFragment())
        }

        backButton.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            FragmentUtil.openFragment(transaction, RegNameFragment())
        }

        alreadyRegistered.setOnClickListener {
            //Должны сохраняться введенные значения
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            FragmentUtil.openFragment(transaction, AuthFragment())
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.regLoginField.hint = "Логин"
        binding.regEmailField.hint = "Email"
        binding.nextBtn.text = "Далее"
    }

    private fun createAccount(lastName: String, firstName: String) {
        TODO("Not yet implemented")
    }
}