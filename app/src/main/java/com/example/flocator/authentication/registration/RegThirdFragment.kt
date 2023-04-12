package com.example.flocator.authentication.registration

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.flocator.R
import com.example.flocator.authentication.authorization.AuthFragment
import com.example.flocator.authentication.authorization.AuthRegSection
import com.example.flocator.authentication.client.RetrofitClient.authenticationApi
import com.example.flocator.authentication.client.dto.UserRegistrationDto
import com.example.flocator.authentication.getlocation.LocationRequestFragment
import com.example.flocator.authentication.viewmodel.RegistrationViewModel
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.example.flocator.databinding.FragmentRegistrationBinding
import com.example.flocator.main.ui.main.MainFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class RegThirdFragment : Fragment(), AuthRegSection {
    private lateinit var binding: FragmentRegistrationBinding
    private lateinit var registrationViewModel: RegistrationViewModel

    companion object {
        private const val PASSWORD = "Пароль"
        private const val REPEAT_PASSWORD = "Повторите пароль"
        private const val REGISTER = "Зарегистрироваться"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)

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

    }

    private fun createEncryptedSharedPreferences(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            "encrypted_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun getPassword(context: Context): String? {
        val encryptedSharedPreferences = createEncryptedSharedPreferences(context)
        return encryptedSharedPreferences.getString("password", null)
    }

    private fun savePassword(context: Context, password: String) {
        val encryptedSharedPreferences = createEncryptedSharedPreferences(context)
        encryptedSharedPreferences.edit().putString("password", password).apply()
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
                password = password
            )

            val disposableRegisterUser = authenticationApi.registerUser(userRegistrationDto)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ isSuccess ->
                    if (isSuccess) {
                        savePassword(requireContext(), password)

                        if (LocationRequestFragment.hasLocationPermission(requireContext())) {
                            FragmentNavigationUtils.openFragment(
                                requireActivity().supportFragmentManager,
                                MainFragment()
                            )
                        } else {
                            FragmentNavigationUtils.openFragment(
                                requireActivity().supportFragmentManager,
                                LocationRequestFragment()
                            )
                        }
                    }
                }, { error ->
//                    throw RuntimeException("Ошибка регистрации пользователя: ${error.message}")
                })
        }
    }
}