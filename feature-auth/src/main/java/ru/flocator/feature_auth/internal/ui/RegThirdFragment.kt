package ru.flocator.feature_auth.internal.ui

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.core_controller.NavController
import ru.flocator.core_controller.findNavController
import ru.flocator.core_dependency.findDependencies
import ru.flocator.core_design.R
import ru.flocator.core_dto.auth.UserRegistrationDto
import ru.flocator.core_sections.AuthenticationSection
import ru.flocator.feature_auth.api.ui.AuthFragment
import ru.flocator.feature_auth.databinding.FragmentRegistrationBinding
import ru.flocator.feature_auth.internal.di.DaggerAuthComponent
import ru.flocator.feature_auth.internal.view_models.RegistrationViewModel
import javax.inject.Inject

internal class RegThirdFragment : Fragment(), AuthenticationSection {
    private var _binding: FragmentRegistrationBinding? = null
    private val binding: FragmentRegistrationBinding
        get() = _binding!!

    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var registrationViewModel: RegistrationViewModel

    @Inject
    internal lateinit var controller: NavController

    companion object {
        private const val PASSWORD = "Пароль"
        private const val REPEAT_PASSWORD = "Повторите пароль"
        private const val REGISTER = "Зарегистрироваться"
        private const val TAG = "Third registration fragment"
        private const val ERROR_MESSAGE = "Ошибка регистрации пользователя"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerAuthComponent.factory()
            .create(
                findDependencies(),
                findNavController()
            )
            .inject(this)

        registrationViewModel = ViewModelProvider(this, viewModelFactory)[RegistrationViewModel::class.java]
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
            controller.back()
        }

        binding.alreadyRegisteredText.setOnClickListener {
            controller
                .toAuth()
                .commit()
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

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
        _binding = null
    }

    private fun createAccount() {
        val lastName = registrationViewModel.nameData.value!!.first
        val firstName = registrationViewModel.nameData.value!!.second
        val login = registrationViewModel.loginEmailData.value!!.first
        val email = registrationViewModel.loginEmailData.value!!.second
        val userRegistrationDto = UserRegistrationDto(
            lastName = lastName,
            firstName = firstName,
            login = login,
            email = email,
            password = binding.firstInputEditField.text.toString()
        )
        compositeDisposable.add(
            registrationViewModel.registerUser(userRegistrationDto)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ isSuccess ->
                    if (isSuccess) {
                        controller.toAuth()
                            .clearAll()
                            .commit()
                    }
                }, { error ->
                    showErrorMessage()
                    Log.e(TAG, ERROR_MESSAGE, error)
                })
        )
    }


    private fun showErrorMessage() {
        binding.registrationErrorMessageText.visibility = View.VISIBLE
        binding.registrationErrorMessageText.text = ERROR_MESSAGE
    }
}