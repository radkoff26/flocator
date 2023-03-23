package com.example.flocator.logreg.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.flocator.R
import com.example.flocator.logreg.FragmentUtil
import com.example.flocator.main.fragments.MainFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

class RegNameFragment : Fragment() {
    private lateinit var lastNameInput: TextInputLayout
    private lateinit var firstNameInput: TextInputLayout
    private lateinit var nextButton: MaterialButton
    private lateinit var alreadyRegistered: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reg_name, container, false)

        lastNameInput = view.findViewById(R.id.lastname_field)
        firstNameInput = view.findViewById(R.id.firstname_field)
        nextButton = view.findViewById(R.id.next_btn)
        alreadyRegistered = view.findViewById(R.id.already_registered_text)

        nextButton.setOnClickListener {
//            val lastName = lastNameInput.editText?.text.toString().trim();
//            val firstName = firstNameInput.editText?.text.toString().trim();
//
//            createAccount(lastName, firstName)

            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            FragmentUtil.openFragment(transaction, RegLoginFragment())
        }

        alreadyRegistered.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            FragmentUtil.openFragment(transaction, AuthFragment())
        }

        return view
    }

    private fun createAccount(lastName: String, firstName: String) {
        TODO("Not yet implemented")
    }
}