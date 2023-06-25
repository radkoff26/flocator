package ru.flocator.feature_auth.api.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.flocator.core_sections.AuthenticationSection
import ru.flocator.feature_auth.databinding.FragmentAuthBinding
import ru.flocator.feature_auth.internal.view_models.RegistrationViewModel
import androidx.fragment.app.activityViewModels
import ru.flocator.core_dto.auth.UserCredentialsDto
import ru.flocator.core_api.api.MainRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.core_controller.NavController
import ru.flocator.core_data_store.user.data.UserCredentials
import ru.flocator.core_utils.LocationUtils
import ru.flocator.feature_auth.internal.ui.directory.RegFirstFragment
import javax.inject.Inject


class AuthFragment : Fragment(), AuthenticationSection {
    private var _binding: FragmentAuthBinding? = null
    private val binding: FragmentAuthBinding
        get() = _binding!!
    private val compositeDisposable = CompositeDisposable()
    private val registrationViewModel: RegistrationViewModel by activityViewModels()

    @Inject
    lateinit var repository: MainRepository

    @Inject
    lateinit var controller: NavController

    companion object {
        private const val TAG = "Auth fragment"
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
            controller
                .toFragment(RegFirstFragment())
                .commit()
        }

        return binding.root
    }

    private fun login(login: String, password: String) {
        val userCredentials = UserCredentialsDto(login = login, password = password)
        compositeDisposable.add(
            repository.restApi.loginUser(userCredentials)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ userId ->
                    repository.userCredentialsCache.updateUserCredentials(
                        UserCredentials(
                            userId!!,
                            login,
                            password
                        )
                    )
                    if (LocationUtils.hasLocationPermission(requireContext())) {
                        controller
                            .toMain()
                            .clearAll()
                            .commit()
                    } else {
                        controller
                            .toFragment(LocationRequestFragment())
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
        _binding = null
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
        return email.isNotEmpty() && password.isNotEmpty()
    }
}
