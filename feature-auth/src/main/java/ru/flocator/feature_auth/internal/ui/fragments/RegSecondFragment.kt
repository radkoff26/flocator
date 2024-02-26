package ru.flocator.feature_auth.internal.ui.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.core.dependencies.findDependencies
import ru.flocator.core.navigation.NavController
import ru.flocator.core.navigation.findNavController
import ru.flocator.core.section.AuthenticationSection
import ru.flocator.feature_auth.R
import ru.flocator.feature_auth.databinding.FragmentRegistrationBinding
import ru.flocator.feature_auth.internal.core.di.DaggerAuthComponent
import ru.flocator.feature_auth.internal.ui.view_models.RegistrationViewModel
import javax.inject.Inject

internal class RegSecondFragment : Fragment(),
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

    private val lastname by lazy {
        requireArguments().getString(Contraction.LAST_NAME)!!
    }

    private val firstname by lazy {
        requireArguments().getString(Contraction.FIRST_NAME)!!
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        binding.firstInputEditField.contentDescription =
            resources.getString(R.string.login)
        binding.secondInputEditField.contentDescription =
            resources.getString(R.string.email)
        binding.submitBtn.contentDescription =
            resources.getString(R.string.next)

        binding.submitBtn.setOnClickListener {
            val login = binding.firstInputEditField.text.toString()
            val email = binding.secondInputEditField.text.toString()

            if (login.isEmpty() || email.isEmpty()) {
                if (login.isEmpty()) {
                    binding.firstInputField.error =
                        resources.getString(R.string.field_mustnt_be_empty)
                    binding.firstInputField.isErrorEnabled = true
                }
                if (email.isEmpty()) {
                    binding.secondInputField.error =
                        resources.getString(R.string.field_mustnt_be_empty)
                    binding.secondInputField.isErrorEnabled = true
                }
                return@setOnClickListener
            }
            if (!validateEmail(email) && login.isNotEmpty() && email.isNotEmpty()) {
                binding.secondInputField.error =
                    resources.getString(R.string.wrong_password)
                binding.secondInputField.isErrorEnabled = true
                return@setOnClickListener
            }

            compositeDisposable.add(
                Single.zip<Boolean, Boolean, Response>(
                    registrationViewModel.isLoginAvailable(login),
                    registrationViewModel.isEmailAvailable(email),
                ) { loginResult, emailResult ->
                    return@zip Response(loginResult, emailResult)
                }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { response ->
                            if (!response.loginResponse) {
                                binding.firstInputField.error =
                                    resources.getString(R.string.login_is_used)
                                binding.firstInputField.isErrorEnabled = true
                                return@subscribe
                            }
                            if (!response.emailResponse) {
                                binding.secondInputField.error =
                                    resources.getString(R.string.email_is_used)
                                binding.secondInputField.isErrorEnabled = true
                                return@subscribe
                            }
                            registrationViewModel.updateLoginEmail(
                                Pair(
                                    binding.firstInputEditField.text.toString(),
                                    binding.secondInputEditField.text.toString()
                                )
                            )
                            val regThirdFragment =
                                RegThirdFragment.newInstance(lastname, firstname, login, email)
                            controller.toFragment(regThirdFragment)
                        },
                        { error ->
                            showErrorMessage(resources.getString(R.string.server_error))
                            Log.e(TAG, "Ошибка проверки доступности логина и email", error)
                        }
                    )
            )
        }

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(ru.flocator.design.R.drawable.back)
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

        binding.toolbar.setNavigationOnClickListener {
            controller.back()
        }

        binding.alreadyRegisteredText.setOnClickListener {
            controller.toAuth()
        }

        binding.secondInputEditField.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
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

        binding.firstInputField.hint = resources.getString(R.string.login)
        binding.secondInputField.hint = resources.getString(R.string.email)
        binding.submitBtn.text = resources.getString(R.string.next)
        binding.submitBtn.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.white
            )
        )

        registrationViewModel.loginEmailData.value?.let { savedData ->
            binding.firstInputEditField.setText(savedData.first)
            binding.secondInputEditField.setText(savedData.second)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
        _binding = null
    }

    private fun showErrorMessage(text: String) {
        binding.registrationErrorMessageText.visibility = View.VISIBLE
        binding.registrationErrorMessageText.text = text
    }

    private fun validateEmail(email: String): Boolean {
        val emailRegex = Regex(pattern = "^[A-Za-z0-9._%+-]+@[A-Za-z\\d.-]+\\.[A-Za-z]{2,}\$")
        return emailRegex.matches(input = email)
    }

    private data class Response(val loginResponse: Boolean, val emailResponse: Boolean)

    private object Contraction {
        const val LAST_NAME = "lastname"
        const val FIRST_NAME = "firstname"
    }

    companion object {
        const val TAG = "RegSecondFragment_TAG"

        fun newInstance(firstName: String, lastName: String): RegSecondFragment =
            RegSecondFragment().apply {
                arguments = Bundle().apply {
                    putString(Contraction.LAST_NAME, lastName)
                    putString(Contraction.FIRST_NAME, firstName)
                }
            }
    }
}