package com.example.flocator.authentication.registration

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.flocator.R
import com.example.flocator.authentication.authorization.AuthFragment
import com.example.flocator.authentication.authorization.AuthRegSection
import com.example.flocator.authentication.client.RetrofitClient.authenticationApi
import com.example.flocator.authentication.viewmodel.RegistrationViewModel
import com.example.flocator.databinding.FragmentRegistrationBinding
import com.example.flocator.common.utils.FragmentNavigationUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class RegSecondFragment : Fragment(), AuthRegSection {
    private lateinit var binding: FragmentRegistrationBinding
    private lateinit var registrationViewModel: RegistrationViewModel
    private val errorMessageText: TextView by lazy {
        binding.registrationErrorMessageText
    }

    companion object {
        private const val LOGIN = "Логин"
        private const val EMAIL = "Email"
        private const val NEXT = "Далее"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        binding.firstInputEditField.contentDescription = LOGIN
        binding.secondInputEditField.contentDescription = EMAIL
        binding.submitBtn.contentDescription = NEXT

        binding.submitBtn.setOnClickListener {
            val lastName = binding.firstInputEditField.text.toString()
            val email = binding.secondInputEditField.text.toString()

            val disposableLoginCheck = authenticationApi.isLoginAvailable(lastName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { isLoginAvailable ->
                    if (!isLoginAvailable) {
                        showErrorMessage("Логин уже занят")
                    }
                    authenticationApi.isEmailAvailable(email)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ isEmailAvailable ->
                    if (!validateEmail(email))
                        showErrorMessage("Некорректный email")

                    if (!isEmailAvailable) {
                        showErrorMessage("Email уже занят")
                    }
                    hideErrorMessage()
                    registrationViewModel.loginEmailData.value = Pair(lastName, email)
                    FragmentNavigationUtils.openFragment(
                        requireActivity().supportFragmentManager,
                        RegThirdFragment()
                    )
                }, { error ->
                    showErrorMessage("Ошибка на сервере")
//                    throw RuntimeException("Ошибка проверки доступности логина и email: ${error.message}")
                })
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

        binding.alreadyRegisteredText.setOnClickListener {
            FragmentNavigationUtils.openFragment(
                requireActivity().supportFragmentManager,
                AuthFragment()
            )
        }

        binding.secondInputEditField.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.firstInputField.hint = LOGIN
        binding.secondInputField.hint = EMAIL
        binding.submitBtn.text = NEXT
        binding.submitBtn.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.white
            )
        )

        registrationViewModel =
            ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
        registrationViewModel.loginEmailData.value?.let { savedData ->
            binding.firstInputEditField.setText(savedData.first)
            binding.secondInputEditField.setText(savedData.second)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EMAIL, binding.firstInputEditField.toString())
        outState.putString(LOGIN, binding.secondInputEditField.toString())
    }

    private fun showErrorMessage(text: String) {
        errorMessageText.visibility = View.VISIBLE
        errorMessageText.text = text
    }

    private fun hideErrorMessage() {
        errorMessageText.visibility = View.GONE
    }

    private fun validateEmail(email: String): Boolean {
        val emailRegex = Regex(pattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
        return emailRegex.matches(input = email)
    }
}