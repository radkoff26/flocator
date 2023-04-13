package com.example.flocator.authentication.authorization

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.flocator.authentication.Authentication
import com.example.flocator.authentication.client.RetrofitClient.authenticationApi
import com.example.flocator.authentication.client.dto.UserCredentialsDto
import com.example.flocator.authentication.getlocation.LocationRequestFragment
import com.example.flocator.authentication.registration.RegFirstFragment
import com.example.flocator.common.config.SharedPreferencesContraction.User.USER_ID
import com.example.flocator.common.config.SharedPreferencesContraction.User.prefs_name
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.example.flocator.databinding.FragmentAuthBinding
import com.example.flocator.main.ui.main.MainFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class AuthFragment : Fragment(), Authentication {
    private var _binding: FragmentAuthBinding? = null
    private val binding: FragmentAuthBinding
        get() = _binding!!
    private val compositeDisposable = CompositeDisposable()

    companion object {
        private const val TAG = "Auth fragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAuthBinding.inflate(inflater, container, false)

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
        compositeDisposable.add(authenticationApi.loginUser(userCredentials)
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
                Log.e(TAG, "Ошибка входа", error)
            })
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    private fun showErrorMessage(text: String) {
        binding.loginErrorMessageText.visibility = View.VISIBLE
        binding.loginErrorMessageText.text = text
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
                        prefs_name,
                        Context.MODE_PRIVATE
                    )
                    val editor = sharedPreferences.edit()
                    editor.putLong(USER_ID, user.id)
                    editor.apply()
                }, { error ->
                    throw RuntimeException("Ошибка получения пользователя: ${error.message}")
                })
    }
}