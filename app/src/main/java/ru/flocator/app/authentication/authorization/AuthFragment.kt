package ru.flocator.app.authentication.authorization

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.flocator.app.authentication.client.RetrofitClient.authenticationApi
import ru.flocator.app.authentication.client.dto.UserCredentialsDto
import ru.flocator.app.authentication.getlocation.LocationRequestFragment
import ru.flocator.app.authentication.registration.RegFirstFragment
import ru.flocator.app.common.repository.MainRepository
import ru.flocator.app.common.storage.store.user.data.UserCredentials
import ru.flocator.app.common.utils.FragmentNavigationUtils
import ru.flocator.app.common.utils.LocationUtils
import com.example.flocator.databinding.FragmentAuthBinding
import ru.flocator.app.main.ui.MainFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.app.common.sections.AuthenticationSection
import javax.inject.Inject

@AndroidEntryPoint
class AuthFragment : Fragment(), AuthenticationSection {
    private var _binding: FragmentAuthBinding? = null
    private val binding: FragmentAuthBinding
        get() = _binding!!
    private val compositeDisposable = CompositeDisposable()

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

        return binding.root
    }

    private fun login(login: String, password: String) {
        val userCredentials = UserCredentialsDto(login = login, password = password)
        compositeDisposable.add(
            authenticationApi.loginUser(userCredentials)
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
                        FragmentNavigationUtils.clearAllAndOpenFragment(
                            requireActivity().supportFragmentManager,
                            MainFragment()
                        )
                    } else {
                        FragmentNavigationUtils.clearAllAndOpenFragment(
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
