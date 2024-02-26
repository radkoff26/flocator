package ru.flocator.feature_auth.api.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.core.dependencies.findDependencies
import ru.flocator.core.navigation.NavController
import ru.flocator.core.navigation.findNavController
import ru.flocator.core.section.AuthenticationSection
import ru.flocator.core.utils.LocationUtils
import ru.flocator.data.models.auth.UserCredentialsDto
import ru.flocator.feature_auth.R
import ru.flocator.feature_auth.databinding.FragmentAuthBinding
import ru.flocator.feature_auth.internal.core.di.DaggerAuthComponent
import ru.flocator.feature_auth.internal.data.repository.RegistrationRepository
import ru.flocator.feature_auth.internal.ui.fragments.RegFirstFragment
import ru.flocator.feature_auth.internal.domain.LoginUserAndSaveTokensUseCase
import ru.flocator.feature_auth.internal.ui.view_models.RegistrationViewModel
import javax.inject.Inject

class AuthFragment : Fragment(), AuthenticationSection {
    private var _binding: FragmentAuthBinding? = null
    private val binding: FragmentAuthBinding
        get() = _binding!!
    private val compositeDisposable = CompositeDisposable()

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var registrationViewModel: RegistrationViewModel

    @Inject
    internal lateinit var navController: NavController

    @Inject
    internal lateinit var loginUserAndSaveTokens: LoginUserAndSaveTokensUseCase

    @Inject
    internal lateinit var registrationRepository: RegistrationRepository

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerAuthComponent.factory()
            .create(
                findDependencies(),
                findNavController()
            )
            .inject(this)

        registrationViewModel =
            ViewModelProvider(this, viewModelFactory)[RegistrationViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        registrationViewModel.clear()
        binding.entranceBtn.setOnClickListener {
            val login = binding.emailLoginFieldEdit.text.toString()
            val password = binding.passwordLoginFieldEdit.text.toString()

            if (validateLoginField(login) && validatePasswordField(password)) {
                login(login, password)
            }
            if (!validateLoginField(login)) {
                binding.loginField.error = resources.getString(R.string.field_mustnt_be_empty)
                binding.loginField.isErrorEnabled = true
            }
            if (!validatePasswordField(password)) {
                binding.passwordLoginField.error =
                    resources.getString(R.string.field_mustnt_be_empty)
                binding.passwordLoginField.isErrorEnabled = true
            }
        }

        binding.emailLoginFieldEdit.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.loginField.error = null
                    binding.loginField.isErrorEnabled = false
                }
            }

        binding.passwordLoginFieldEdit.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.passwordLoginField.error = null
                    binding.passwordLoginField.isErrorEnabled = false
                }
            }

        binding.emailLoginFieldEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.loginField.isErrorEnabled = false
                binding.loginField.error = null
            }
        })

        binding.passwordLoginFieldEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.passwordLoginField.isErrorEnabled = false
                binding.passwordLoginField.error = null
            }
        })

        binding.registrationBtn.setOnClickListener {
            navController.toFragment(RegFirstFragment.newInstance())
        }

        return binding.root
    }

    private fun login(login: String, password: String) {
        val userCredentials = UserCredentialsDto(
            login = login,
            password = password
        )
        compositeDisposable.add(
            loginUserAndSaveTokens(userCredentials)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (LocationUtils.hasLocationPermission(requireContext())) {
                        navController.toMain()
                    } else {
                        navController.toLocationDialog()
                    }
                }, { error ->
                    compositeDisposable.add(
                        registrationRepository.isLoginAvailable(login)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                {
                                    if (it) {
                                        binding.loginField.error =
                                            resources.getString(R.string.login_does_not_exits)
                                        binding.loginField.isErrorEnabled = true
                                    } else {
                                        binding.passwordLoginField.error =
                                            resources.getString(R.string.wrong_password)
                                        binding.passwordLoginField.isErrorEnabled = true
                                    }
                                },
                                {
                                    Log.e(TAG, "Ошибка проверки логина", error)
                                }
                            )
                    )
                    Log.e(TAG, "Ошибка входа", error)
                })
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
        _binding = null
    }

    private fun validateLoginField(login: String): Boolean {
        return login.isNotEmpty()
    }

    private fun validatePasswordField(password: String): Boolean {
        return password.isNotEmpty()
    }

    companion object {
        private const val TAG = "AuthFragment_TAG"

        fun newInstance(): AuthFragment = AuthFragment()
    }
}
