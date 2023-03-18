package com.example.flocator.logreg.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.example.flocator.R
import com.example.flocator.logreg.FragmentUtil
import com.example.flocator.main.fragments.MainFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

class RegPasswordFragment : Fragment() {
    private lateinit var passInput: TextInputLayout
    private lateinit var passInputRepeat: TextInputLayout
    private lateinit var registerBtn: MaterialButton
    private lateinit var alreadyRegistered: TextView
    private lateinit var backButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reg_password, container, false)

        passInput = view.findViewById(R.id.reg_password_field)
        passInputRepeat = view.findViewById(R.id.reg_password_repeat_field)
        registerBtn = view.findViewById(R.id.submit_register_btn)
        alreadyRegistered = view.findViewById(R.id.already_registered_text)
        backButton = view.findViewById(R.id.back_btn)

        registerBtn.setOnClickListener {
//            val login = passInput.editText?.text.toString().trim();
//            val email = passInputRepeat.editText?.text.toString().trim();
//
//            createAccount(login, email)

            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            FragmentUtil.openFragment(transaction, MainFragment())
        }

        backButton.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            FragmentUtil.openFragment(transaction, RegLoginFragment())
        }

        alreadyRegistered.setOnClickListener {
            //Должны сохраняться введенные значения
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            FragmentUtil.openFragment(transaction, AuthFragment())
        }

        return view
    }

    private fun createAccount(lastName: String, firstName: String) {
        TODO("Not yet implemented")
    }
}