package com.example.flocator.logreg.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.flocator.R
import com.example.flocator.main.fragments.MainFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

class AuthFragment : Fragment() {

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
        val view = inflater.inflate(R.layout.fragment_auth, container, false)

        emailInput = view.findViewById(R.id.email_text_field)
        passwordInput = view.findViewById(R.id.password_text_field)
        entranceButton = view.findViewById(R.id.entrance_btn)
        registrationButton = view.findViewById(R.id.registration_btn)
        forgotPasswordText = view.findViewById(R.id.forgot_password_text)

        entranceButton.setOnClickListener {
//            val email = emailInput.editText?.text.toString().trim();
//            val password = passwordInput.editText?.text.toString().trim();
//
//            if (validateFields(email, password)) {
//                login(email, password)
//            }

            //Мок
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, MainFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }



        registrationButton.setOnClickListener{
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, RegNameFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        forgotPasswordText.setOnClickListener {
            TODO("Not yet implemented")
        }

        return view
    }

    private fun login(email: String, password: String) {
        TODO("Not yet implemented")
    }

    private fun validateFields(email: String, password: String): Boolean {
        TODO("Not yet implemented")
    }
}