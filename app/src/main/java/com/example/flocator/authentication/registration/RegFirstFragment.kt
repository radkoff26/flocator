package com.example.flocator.authentication.registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.flocator.R
import com.example.flocator.authentication.authorization.AuthFragment
import com.example.flocator.authentication.authorization.AuthRegSection
import com.example.flocator.authentication.viewmodel.RegistrationViewModel
import com.example.flocator.databinding.FragmentRegistrationBinding
import com.example.flocator.common.utils.FragmentNavigationUtils

class RegFirstFragment : Fragment(), AuthRegSection {
    private lateinit var binding: FragmentRegistrationBinding
    private lateinit var registrationViewModel: RegistrationViewModel
    private val errorMessageText: TextView by lazy {
        binding.registrationErrorMessageText
    }

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

        binding.firstInputEditField.contentDescription = FIRST_NAME
        binding.secondInputEditField.contentDescription = LAST_NAME
        binding.submitBtn.contentDescription = NEXT

        binding.submitBtn.setOnClickListener {
            val firstName = binding.firstInputEditField.text.toString()
            val lastName = binding.secondInputEditField.text.toString()
            if (validateName(firstName, lastName)) {
                hideErrorMessage()
                registrationViewModel.nameData.value = Pair(
                    binding.firstInputEditField.text.toString(),
                    binding.secondInputEditField.text.toString()
                )

                FragmentNavigationUtils.openFragment(
                    requireActivity().supportFragmentManager,
                    RegSecondFragment()
                )
            } else {
                showErrorMessage()
            }
        }

        binding.alreadyRegisteredText.setOnClickListener {
            FragmentNavigationUtils.openFragment(
                requireActivity().supportFragmentManager,
                AuthFragment()
            )
        }

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(R.drawable.back)
        }

        binding.toolbar.setNavigationOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.firstInputField.hint = LAST_NAME
        binding.secondInputField.hint = FIRST_NAME
        binding.submitBtn.text = NEXT

        registrationViewModel =
            ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
        registrationViewModel.nameData.value?.let { savedData ->
            binding.firstInputEditField.setText(savedData.first)
            binding.secondInputEditField.setText(savedData.second)
        }
    }

    private fun validateName(firstName: String, lastName: String): Boolean {
        return firstName.isNotEmpty() && lastName.isNotEmpty();
    }

    private fun showErrorMessage() {
        errorMessageText.visibility = View.VISIBLE
        errorMessageText.text = "Поля не должны быть пустыми!"
    }

    private fun hideErrorMessage() {
        errorMessageText.visibility = View.GONE
    }

}