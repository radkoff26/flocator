package ru.flocator.feature_auth.api.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.core_api.api.AppRepository
import ru.flocator.core_controller.NavController
import ru.flocator.core_controller.findNavController
import ru.flocator.core_data_store.user.data.UserCredentials
import ru.flocator.core_dependency.findDependencies
import ru.flocator.core_dto.auth.UserCredentialsDto
import ru.flocator.core_sections.AuthenticationSection
import ru.flocator.core_utils.LocationUtils
import ru.flocator.feature_auth.databinding.FragmentAuthBinding
import ru.flocator.feature_auth.internal.di.DaggerAuthComponent
import ru.flocator.feature_auth.internal.repository.AuthRepository
import ru.flocator.feature_auth.internal.ui.RegFirstFragment
import ru.flocator.feature_auth.internal.view_models.RegistrationViewModel
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
    internal lateinit var authRepository: AuthRepository

    @Inject
    internal lateinit var appRepository: AppRepository

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
            val email = binding.emailLoginFieldEdit.text.toString()
            val password = binding.passwordLoginFieldEdit.text.toString()
            if (validateFields(email, password)) {
                login(email, password)
            } else {
                showErrorMessage("Поля не должны быть пустыми!")
            }
        }

        binding.registrationBtn.setOnClickListener {
            navController
                .toFragment(RegFirstFragment())
                .commit()
        }

        return binding.root
    }

    private fun login(login: String, password: String) {
        val userCredentials = UserCredentialsDto(login = login, password = password)
        compositeDisposable.add(
            authRepository.loginUser(userCredentials)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ userId ->
                    appRepository.userCredentialsCache.updateUserCredentials(
                        UserCredentials(
                            userId!!,
                            login,
                            password
                        )
                    )
                    if (LocationUtils.hasLocationPermission(requireContext())) {
                        navController
                            .toMain()
                            .clearAll()
                            .commit()
                    } else {
                        navController
                            .toFragment(LocationRequestFragment())
                            .clearAll()
                            .commit()
                    }
                }, { error ->
                    showErrorMessage("Неверный логин или пароль")
                    Log.e(TAG, "Ошибка входа", error)
                })
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
        _binding = null
    }

    private fun showErrorMessage(text: String) {
        binding.loginErrorMessageText.visibility = View.VISIBLE
        binding.loginErrorMessageText.text = text
    }

    private fun validateFields(email: String, password: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty()
    }

    companion object {
        private const val TAG = "Auth fragment"
    }
}
