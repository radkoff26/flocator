package ru.flocator.app.authentication.authorization

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.flocator.core_dto.auth.UserCredentialsDto
import ru.flocator.app.authentication.getlocation.LocationRequestFragment
import ru.flocator.app.authentication.registration.RegFirstFragment
import ru.flocator.core_api.api.MainRepository
import ru.flocator.app.databinding.FragmentAuthBinding
import ru.flocator.app.main.ui.MainFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.app.authentication.viewmodel.RegistrationViewModel
import javax.inject.Inject

@AndroidEntryPoint
class AuthFragment : Fragment(), ru.flocator.core_sections.AuthenticationSection {
    private var _binding: FragmentAuthBinding? = null
    private val binding: FragmentAuthBinding
        get() = _binding!!
    private val compositeDisposable = CompositeDisposable()
    private val registrationViewModel: RegistrationViewModel by activityViewModels()

    @Inject
    lateinit var repository: MainRepository

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
            ru.flocator.core_utils.FragmentNavigationUtils.openFragment(
                requireActivity().supportFragmentManager,
                RegFirstFragment()
            )
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
                        ru.flocator.core_data_store.user.data.UserCredentials(
                            userId!!,
                            login,
                            password
                        )
                    )
                    if (ru.flocator.core_utils.LocationUtils.hasLocationPermission(requireContext())) {
                        ru.flocator.core_utils.FragmentNavigationUtils.clearAllAndOpenFragment(
                            requireActivity().supportFragmentManager,
                            MainFragment()
                        )
                    } else {
                        ru.flocator.core_utils.FragmentNavigationUtils.clearAllAndOpenFragment(
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
