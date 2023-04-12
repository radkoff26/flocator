package com.example.flocator.authentication.authorization

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.flocator.authentication.client.RetrofitClient.authenticationApi
import com.example.flocator.authentication.client.dto.UserCredentialsDto
import com.example.flocator.authentication.getlocation.LocationRequestFragment
import com.example.flocator.authentication.registration.RegFirstFragment
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.example.flocator.databinding.FragmentAuthBinding
import com.example.flocator.main.ui.main.MainFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class AuthFragment : Fragment(), AuthRegSection {
    private lateinit var binding: FragmentAuthBinding
    private val errorMessageText: TextView by lazy {
        binding.loginErrorMessageText
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAuthBinding.inflate(inflater, container, false)

        binding.entranceBtn.setOnClickListener {
            val email = binding.emailLoginFieldEdit.text.toString()
            val password = binding.passwordLoginFieldEdit.text.toString()
            if (validateFields(email, password)) {
                login(email, password)
            } else {
                showErrorMessage("Поля не должны быть пустыми!")
            }
        }

        binding.registrationBtn.setOnClickListener {
            FragmentNavigationUtils.openFragment(
                requireActivity().supportFragmentManager,
                RegFirstFragment()
            )
        }

        binding.forgotPasswordText.setOnClickListener {
            TODO("Not yet implemented")
        }

        binding.emailLoginFieldEdit.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        return binding.root
    }

    private fun login(email: String, password: String) {
        val userCredentials = UserCredentialsDto(login = email, password = password)
        val disposableLogin = authenticationApi.loginUser(userCredentials)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ userId ->
                saveUserId(userId)

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
            }, { error ->
                showErrorMessage("Неверный логин или пароль")
//                throw RuntimeException("Ошибка входа: ${error.message}")
            })
    }


    private fun showErrorMessage(text: String) {
        errorMessageText.visibility = View.VISIBLE
        errorMessageText.text = text
    }

    private fun validateFields(email: String, password: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty();
    }

    private fun saveUserId(id: Long) {
        val disposableGetUserId =
            authenticationApi.getUserById(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ user ->
                    val sharedPreferences = requireActivity().getSharedPreferences(
                        "my_shared_preferences",
                        Context.MODE_PRIVATE
                    )
                    val editor = sharedPreferences.edit()
                    editor.putLong("userId", user.id)
                    editor.apply()
                }, { error ->
                    throw RuntimeException("Ошибка получения пользователя: ${error.message}")
                })
    }
}