package ru.flocator.feature_auth.internal.ui

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

        registrationViewModel =
            ViewModelProvider(this, viewModelFactory)[RegistrationViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        val color = ContextCompat.getColor(requireContext(), R.color.font)
        binding.firstInputEditField.contentDescription = PASSWORD
        binding.secondInputEditField.contentDescription = REPEAT_PASSWORD
        binding.firstInputField.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        binding.secondInputField.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        binding.firstInputField.setEndIconTintList(ColorStateList.valueOf(color))
        binding.secondInputField.setEndIconTintList(ColorStateList.valueOf(color))
        binding.submitBtn.contentDescription = REGISTER

        binding.submitBtn.setOnClickListener {
            var firstPassword = ""
            var secondPassword = ""
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
            if(firstPassword.isNotEmpty() && secondPassword.isNotEmpty() && comparePasswords(firstPassword, secondPassword)){
                createAccount()
            } else {
                if(firstPassword.isEmpty()){
                    binding.firstInputField.error = "Поле не должно быть пустым"
                    binding.firstInputField.isErrorEnabled = true
                }
                if(secondPassword.isEmpty()){
                    binding.secondInputField.error = "Поле не должно быть пустым"
                    binding.secondInputField.isErrorEnabled = true
                }
                println("СРАВНЕНИЕ" + comparePasswords(firstPassword, secondPassword) + " " + firstPassword + " " + secondPassword)
                if(!comparePasswords(firstPassword, secondPassword) && firstPassword.isNotEmpty() && secondPassword.isNotEmpty()){
                    binding.secondInputField.error = "Пароли не совпадают"
                    binding.secondInputField.isErrorEnabled = true
                    binding.firstInputField.error = "Пароли не совпадают"
                    binding.firstInputField.isErrorEnabled = true
                }
            }
        }

        binding.firstInputEditField.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                binding.firstInputField.error = null
                binding.firstInputField.isErrorEnabled = false
            }
        }

        binding.secondInputEditField.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
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
        val bundle = arguments
        val lastName = bundle?.getString("lastname")
        val firstName = bundle?.getString("firstname")
        val login = bundle?.getString("login")
        val email = bundle?.getString("email")
        println(lastName)
        println(login)
        val userRegistrationDto = UserRegistrationDto(
            lastName = lastName!!,
            firstName = firstName!!,
            login = login!!,
            email = email!!,
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

    private fun comparePasswords(firstPassword: String, secondPassword: String): Boolean {
        return firstPassword == secondPassword
    }


    private fun showErrorMessage() {
        binding.registrationErrorMessageText.visibility = View.VISIBLE
        binding.registrationErrorMessageText.text = ERROR_MESSAGE
    }
}