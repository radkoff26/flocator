package com.example.flocator.authentication.registration

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.flocator.R
import com.example.flocator.authentication.authorization.AuthFragment
import com.example.flocator.authentication.Authentication
import com.example.flocator.authentication.client.RetrofitClient.authenticationApi
import com.example.flocator.authentication.client.dto.UserRegistrationDto
import com.example.flocator.authentication.viewmodel.RegistrationViewModel
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.example.flocator.databinding.FragmentRegistrationBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.sql.Timestamp

class RegThirdFragment : Fragment(), Authentication {
    private var _binding: FragmentRegistrationBinding? = null
    private val binding: FragmentRegistrationBinding
        get() = _binding!!
    private val compositeDisposable = CompositeDisposable()
    private lateinit var registrationViewModel: RegistrationViewModel

    companion object {
        private const val PASSWORD = "Пароль"
        private const val REPEAT_PASSWORD = "Повторите пароль"
        private const val REGISTER = "Зарегистрироваться"
        private const val TAG = "Third registration fragment"
        private const val ERROR_MESSAGE = "Ошибка регистрации пользователя"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        binding.firstInputEditField.contentDescription = PASSWORD
        binding.secondInputEditField.contentDescription = REPEAT_PASSWORD
        binding.submitBtn.contentDescription = REGISTER

        binding.submitBtn.setOnClickListener {
            createAccount()
        }

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(R.drawable.back)
        }

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.alreadyRegisteredText.setOnClickListener {
            FragmentNavigationUtils.openFragment(
                requireActivity().supportFragmentManager,
                AuthFragment()
            )
        }

        binding.firstInputEditField.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        binding.secondInputEditField.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.firstInputField.hint = PASSWORD
        binding.secondInputField.hint = REPEAT_PASSWORD
        binding.submitBtn.text = REGISTER
        registrationViewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    private fun createAccount() {
        val lastName = registrationViewModel.nameData.value?.first
        val firstName = registrationViewModel.nameData.value?.second
        val login = registrationViewModel.loginEmailData.value?.first
        val email = registrationViewModel.loginEmailData.value?.second
        val password = binding.firstInputEditField.text.toString()

        if (lastName != null && firstName != null && login != null && email != null) {
            val userRegistrationDto = UserRegistrationDto(
                lastName = lastName,
                firstName = firstName,
                login = login,
                email = email,
                password = password,
                birthDate = Timestamp(System.currentTimeMillis()) // TODO: fix
            )

            compositeDisposable.add(
                authenticationApi.registerUser(userRegistrationDto)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ isSuccess ->
                        if (isSuccess) {
                            FragmentNavigationUtils.clearAllAndOpenFragment(
                                requireActivity().supportFragmentManager,
                                AuthFragment()
                            )
                        }
                    }, { error ->
                        showErrorMessage()
                        Log.e(TAG, ERROR_MESSAGE, error)
                    })
            )
        }
    }

    private fun showErrorMessage() {
        binding.registrationErrorMessageText.visibility = View.VISIBLE
        binding.registrationErrorMessageText.text = ERROR_MESSAGE
    }
}