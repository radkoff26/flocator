package com.example.flocator.authreg.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.flocator.databinding.FragmentRegistrationBinding
import com.example.flocator.authreg.FragmentUtils

class RegFirstFragment : Fragment() {
    private lateinit var binding: FragmentRegistrationBinding

    companion object {
        private const val LAST_NAME = "Фамилия"
        private const val FIRST_NAME = "Имя"
        private const val NEXT = "Далее"
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        binding.submitBtn.setOnClickListener {
            FragmentUtils.replaceFragment(requireActivity().supportFragmentManager, RegSecondFragment())
        }

        binding.alreadyRegisteredText.setOnClickListener {
            FragmentUtils.replaceFragment(requireActivity().supportFragmentManager, AuthFragment())
        }

        binding.backBtn.visibility = View.INVISIBLE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.firstInputField.hint = LAST_NAME
        binding.secondInputField.hint = FIRST_NAME
        binding.submitBtn.text = NEXT

        savedInstanceState?.let {
            it.getString(LAST_NAME)?.let { savedLastName ->
                binding.firstInputEditField.setText(savedLastName)
            }

            it.getString(FIRST_NAME)?.let { savedFirstName ->
                binding.secondInputEditField.setText(savedFirstName)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(LAST_NAME, binding.firstInputEditField.toString())
        outState.putString(FIRST_NAME, binding.secondInputEditField.toString())
    }

    private fun createAccount(lastName: String, firstName: String) {
        TODO("Not yet implemented")
    }
}