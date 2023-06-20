package ru.flocator.app.authentication.registration

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import dagger.hilt.android.AndroidEntryPoint
import ru.flocator.core_design.R
import ru.flocator.app.authentication.client.RetrofitClient.authenticationApi
import ru.flocator.core_dto.auth.UserRegistrationDto
import ru.flocator.app.authentication.viewmodel.RegistrationViewModel
import ru.flocator.app.databinding.FragmentRegistrationBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.app.authentication.authorization.AuthFragment


@AndroidEntryPoint
class RegThirdFragment : Fragment(), ru.flocator.core_sections.AuthenticationSection {
    private var _binding: FragmentRegistrationBinding? = null
    private val binding: FragmentRegistrationBinding
        get() = _binding!!
    private val compositeDisposable = CompositeDisposable()
    private val registrationViewModel: RegistrationViewModel by viewModels()

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
            setHomeAsUpIndicator(ru.flocator.app.R.drawable.back)
        }

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.alreadyRegisteredText.setOnClickListener {
            ru.flocator.core_utils.FragmentNavigationUtils.openFragment(
                requireActivity().supportFragmentManager,
                AuthFragment()
            )
        }

        binding.firstInputEditField.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        binding.secondInputEditField.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val marginTopPercent = 0.05
        val marginTop = (screenHeight * marginTopPercent).toInt()

        val logoImageView = binding.logoFlocator
        val layoutParams = logoImageView.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.setMargins(0, marginTop, 0, 0)
        logoImageView.layoutParams = layoutParams
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.id = R.id.third_fragment_root

        binding.firstInputField.hint = PASSWORD
        binding.secondInputField.hint = REPEAT_PASSWORD
        binding.submitBtn.text = REGISTER
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createAccount() {
        registrationViewModel.nameData.observe(viewLifecycleOwner, Observer { nameData ->
            val lastName = nameData.first
            val firstName = nameData.second
            registrationViewModel.loginEmailData.observe(
                viewLifecycleOwner,
                Observer { loginEmail ->
                    val login = loginEmail.first
                    val email = loginEmail.second
                    val userRegistrationDto = UserRegistrationDto(
                        lastName = lastName,
                        firstName = firstName,
                        login = login,
                        email = email,
                        password = binding.firstInputEditField.text.toString()
                    )
                    compositeDisposable.add(
                        authenticationApi.registerUser(userRegistrationDto)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ isSuccess ->
                                if (isSuccess) {
                                    ru.flocator.core_utils.FragmentNavigationUtils.clearAllAndOpenFragment(
                                        requireActivity().supportFragmentManager,
                                        AuthFragment()
                                    )
                                }
                            }, { error ->
                                showErrorMessage()
                                Log.e(TAG, ERROR_MESSAGE, error)
                            })
                    )
                })
        })
    }


    private fun showErrorMessage() {
        binding.registrationErrorMessageText.visibility = View.VISIBLE
        binding.registrationErrorMessageText.text = ERROR_MESSAGE
    }
}