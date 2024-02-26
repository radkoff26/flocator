package ru.flocator.feature_auth.internal.ui.fragments

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.core.dependencies.findDependencies
import ru.flocator.core.navigation.NavController
import ru.flocator.core.navigation.findNavController
import ru.flocator.core.section.AuthenticationSection
import ru.flocator.core.utils.LocationUtils
import ru.flocator.data.models.auth.UserCredentialsDto
import ru.flocator.data.models.auth.UserRegistrationDto
import ru.flocator.design.SnackbarComposer
import ru.flocator.feature_auth.R
import ru.flocator.feature_auth.databinding.FragmentRegistrationBinding
import ru.flocator.feature_auth.internal.core.di.DaggerAuthComponent
import ru.flocator.feature_auth.internal.domain.LoginUserAndSaveTokensUseCase
import ru.flocator.feature_auth.internal.ui.view_models.RegistrationViewModel
import javax.inject.Inject

internal class RegThirdFragment : Fragment(),
    AuthenticationSection {
    private var _binding: FragmentRegistrationBinding? = null
    private val binding: FragmentRegistrationBinding
        get() = _binding!!

    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var registrationViewModel: RegistrationViewModel

    @Inject
    internal lateinit var controller: NavController

    @Inject
    internal lateinit var loginUserAndSaveTokens: LoginUserAndSaveTokensUseCase

    private val lastName by lazy {
        requireArguments().getString(Contraction.LAST_NAME)!!
    }

    private val firstName by lazy {
        requireArguments().getString(Contraction.FIRST_NAME)!!
    }

    private val login by lazy {
        requireArguments().getString(Contraction.LOGIN)!!
    }

    private val email by lazy {
        requireArguments().getString(Contraction.EMAIL)!!
    }

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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        val color = ContextCompat.getColor(requireContext(), ru.flocator.design.R.color.font)
        binding.firstInputEditField.contentDescription =
            resources.getString(R.string.password)
        binding.secondInputEditField.contentDescription =
            resources.getString(R.string.repeat_password)
        binding.submitBtn.contentDescription =
            resources.getString(R.string.register)
        binding.firstInputField.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        binding.secondInputField.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        binding.firstInputField.setEndIconTintList(ColorStateList.valueOf(color))
        binding.secondInputField.setEndIconTintList(ColorStateList.valueOf(color))

        binding.submitBtn.setOnClickListener {
            val firstPassword: String
            val secondPassword: String
            var passwordTransformationMethod = binding.firstInputEditField.transformationMethod
            firstPassword = if (passwordTransformationMethod is PasswordTransformationMethod) {
                binding.firstInputEditField.editableText.toString()
            } else {
                binding.firstInputEditField.text.toString()
            }
            passwordTransformationMethod = binding.secondInputEditField.transformationMethod
            secondPassword = if (passwordTransformationMethod is PasswordTransformationMethod) {
                binding.secondInputEditField.editableText.toString()
            } else {
                binding.secondInputEditField.text.toString()
            }
            if (firstPassword.isNotEmpty() && secondPassword.isNotEmpty() && comparePasswords(
                    firstPassword,
                    secondPassword
                )
            ) {
                createAccount()
            } else {
                if (firstPassword.isEmpty()) {
                    binding.firstInputField.error =
                        resources.getString(R.string.field_mustnt_be_empty)
                    binding.firstInputField.isErrorEnabled = true
                }
                if (secondPassword.isEmpty()) {
                    binding.secondInputField.error =
                        resources.getString(R.string.field_mustnt_be_empty)
                    binding.secondInputField.isErrorEnabled = true
                }
                println(
                    "СРАВНЕНИЕ" + comparePasswords(
                        firstPassword,
                        secondPassword
                    ) + " " + firstPassword + " " + secondPassword
                )
                if (!comparePasswords(
                        firstPassword,
                        secondPassword
                    ) && firstPassword.isNotEmpty() && secondPassword.isNotEmpty()
                ) {
                    binding.secondInputField.error =
                        resources.getString(R.string.passwords_dont_match)
                    binding.secondInputField.isErrorEnabled = true
                    binding.firstInputField.error =
                        resources.getString(R.string.passwords_dont_match)
                    binding.firstInputField.isErrorEnabled = true
                }
            }
        }

        binding.firstInputEditField.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.firstInputField.error = null
                    binding.firstInputField.isErrorEnabled = false
                }
            }

        binding.secondInputEditField.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.secondInputField.error = null
                    binding.secondInputField.isErrorEnabled = false
                }
            }

        binding.firstInputEditField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.firstInputField.isErrorEnabled = false
                binding.firstInputField.error = null
            }
        })

        binding.secondInputEditField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.secondInputField.isErrorEnabled = false
                binding.secondInputField.error = null
            }
        })

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(ru.flocator.design.R.drawable.back)
        }

        binding.toolbar.setNavigationOnClickListener {
            controller.back()
        }

        binding.alreadyRegisteredText.setOnClickListener {
            controller.toAuth()
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
        binding.firstInputField.hint =
            resources.getString(R.string.password)
        binding.secondInputField.hint =
            resources.getString(R.string.repeat_password)
        binding.submitBtn.text = resources.getString(R.string.register)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
        _binding = null
    }

    private fun createAccount() {
        val password = binding.firstInputEditField.text.toString()
        val userRegistrationDto = UserRegistrationDto(
            lastName = lastName,
            firstName = firstName,
            login = login,
            email = email,
            password = password
        )
        compositeDisposable.add(
            registrationViewModel.registerUser(userRegistrationDto)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    loginAndRedirectToAccount(login, password)
                }, { error ->
                    showErrorMessage()
                    Log.e(TAG, "Error while registration", error)
                })
        )
    }

    private fun loginAndRedirectToAccount(login: String, password: String) {
        compositeDisposable.add(
            loginUserAndSaveTokens(
                UserCredentialsDto(
                    login,
                    password
                )
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (LocationUtils.hasLocationPermission(requireContext())) {
                            controller.toMain()
                        } else {
                            controller.toLocationDialog()
                        }
                    }, {
                        view?.let {
                            SnackbarComposer.composeDesignedSnackbar(it, "Error!")
                        }
                    }
                )
        )
    }

    private fun comparePasswords(firstPassword: String, secondPassword: String): Boolean {
        return firstPassword == secondPassword
    }


    private fun showErrorMessage() {
        binding.registrationErrorMessageText.visibility = View.VISIBLE
        binding.registrationErrorMessageText.text =
            resources.getString(R.string.registration_error)
    }

    private object Contraction {
        const val LAST_NAME = "lastname"
        const val FIRST_NAME = "firstname"
        const val LOGIN = "login"
        const val EMAIL = "email"
    }

    companion object {
        const val TAG = "RegThirdFragment_TAG"

        fun newInstance(
            lastName: String,
            firstName: String,
            login: String,
            email: String
        ): RegThirdFragment =
            RegThirdFragment().apply {
                arguments = Bundle().apply {
                    putString(Contraction.LAST_NAME, lastName)
                    putString(Contraction.FIRST_NAME, firstName)
                    putString(Contraction.LOGIN, login)
                    putString(Contraction.EMAIL, email)
                }
            }
    }
}