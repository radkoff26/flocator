package com.example.flocator.logreg.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.example.flocator.R
import com.example.flocator.main.fragments.MainFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

class RegLoginFragment : Fragment() {
    private lateinit var lastNameInput: TextInputLayout
    private lateinit var firstNameInput: TextInputLayout
    private lateinit var nextButton: MaterialButton
    private lateinit var alreadyRegistered: TextView
    private lateinit var backButton: ImageButton

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
        backButton = view.findViewById(R.id.back_btn)

        nextButton.setOnClickListener {
            val lastName = lastNameInput.editText?.text.toString().trim();
            val firstName = firstNameInput.editText?.text.toString().trim();

            createAccount(lastName, firstName)
        }

        backButton.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, MainFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        alreadyRegistered.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, AuthFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return view
    }

    private fun createAccount(lastName: String, firstName: String) {
        TODO("Not yet implemented")
    }
}