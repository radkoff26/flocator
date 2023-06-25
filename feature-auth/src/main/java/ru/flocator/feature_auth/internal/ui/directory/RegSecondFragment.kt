package ru.flocator.feature_auth.internal.ui.directory

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.core_controller.NavController
import ru.flocator.core_design.R
import ru.flocator.core_sections.AuthenticationSection
import ru.flocator.feature_auth.databinding.FragmentRegistrationBinding
import ru.flocator.feature_auth.internal.view_models.RegistrationViewModel
import javax.inject.Inject

@AndroidEntryPoint
internal class RegSecondFragment : Fragment(), AuthenticationSection {
    private var _binding: FragmentRegistrationBinding? = null
    private val binding: FragmentRegistrationBinding
        get() = _binding!!
    private val compositeDisposable = CompositeDisposable()
    private val registrationViewModel: RegistrationViewModel by activityViewModels()

    @Inject
    internal lateinit var controller: NavController

    companion object {
        private const val LOGIN = "Логин"
        private const val EMAIL = "Email"
        private const val NEXT = "Далее"
        private const val TAG = "Second registration fragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        binding.firstInputEditField.contentDescription = LOGIN
        binding.secondInputEditField.contentDescription = EMAIL
        binding.submitBtn.contentDescription = NEXT

        binding.submitBtn.setOnClickListener {
            val login = binding.firstInputEditField.text.toString()
            val email = binding.secondInputEditField.text.toString()

            if (!validateEmail(email)) {
                showErrorMessage("Некорректный email")
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
                                showErrorMessage("Логин уже занят")
                                return@subscribe
                            }
                            if (!response.emailResponse) {
                                showErrorMessage("Email уже занят")
                                return@subscribe
                            }
                            registrationViewModel.updateLoginEmail(
                                Pair(
                                    binding.firstInputEditField.text.toString(),
                                    binding.secondInputEditField.text.toString()
                                )
                            )
                            controller
                                .toFragment(RegThirdFragment())
                                .commit()
                        },
                        { error ->
                            showErrorMessage("Ошибка на сервере")
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
        view.id = R.id.second_fragment_root

        binding.firstInputField.hint = LOGIN
        binding.secondInputField.hint = EMAIL
        binding.submitBtn.text = NEXT
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

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showErrorMessage(text: String) {
        binding.registrationErrorMessageText.visibility = View.VISIBLE
        binding.registrationErrorMessageText.text = text
    }

    private fun hideErrorMessage() {
        binding.registrationErrorMessageText.visibility = View.GONE
    }

    private fun validateEmail(email: String): Boolean {
        val emailRegex = Regex(pattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
        return emailRegex.matches(input = email)
    }

    private data class Response(val loginResponse: Boolean, val emailResponse: Boolean)
}