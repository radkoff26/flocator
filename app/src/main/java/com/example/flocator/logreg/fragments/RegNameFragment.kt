package com.example.flocator.logreg.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.flocator.R
import com.example.flocator.databinding.FragmentRegNameBinding
import com.example.flocator.logreg.FragmentUtil
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

class RegNameFragment : Fragment() {
    private lateinit var binding: FragmentRegNameBinding
    private lateinit var lastNameInput: TextInputLayout
    private lateinit var firstNameInput: TextInputLayout
    private lateinit var nextButton: MaterialButton
    private lateinit var alreadyRegistered: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegNameBinding.inflate(inflater, container, false)

        lastNameInput = binding.lastnameField
        firstNameInput = binding.firstnameField
        nextButton = binding.nextBtn
        alreadyRegistered = binding.alreadyRegisteredText

        nextButton.setOnClickListener {
//            val lastName = lastNameInput.editText?.text.toString().trim();
//            val firstName = firstNameInput.editText?.text.toString().trim();
//
//            createAccount(lastName, firstName)

            //мок
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            FragmentUtil.openFragment(transaction, RegLoginFragment())
        }

        alreadyRegistered.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            FragmentUtil.openFragment(transaction, AuthFragment())
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lastnameField.hint = "Фамилия"
        binding.firstnameField.hint = "Имя"
        binding.nextBtn.text = "Далее"

        val animation: Animation =
            AnimationUtils.loadAnimation(context, R.anim.slide_in_left)
        binding.root.startAnimation(animation)
    }

    private fun createAccount(lastName: String, firstName: String) {
        TODO("Not yet implemented")
    }
}