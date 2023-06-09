package ru.flocator.app.authentication.registration

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
import androidx.lifecycle.ViewModelProvider
import com.example.flocator.R
import ru.flocator.app.authentication.client.RetrofitClient.authenticationApi
import ru.flocator.app.authentication.viewmodel.RegistrationViewModel
import com.example.flocator.databinding.FragmentRegistrationBinding
import ru.flocator.app.common.utils.FragmentNavigationUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.app.common.sections.AuthenticationSection
import ru.flocator.app.authentication.authorization.AuthFragment

class RegSecondFragment : Fragment(), AuthenticationSection {
    private var _binding: FragmentRegistrationBinding? = null
    private val binding: FragmentRegistrationBinding
        get() = _binding!!
    private val compositeDisposable = CompositeDisposable()
    private lateinit var registrationViewModel: RegistrationViewModel

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
            val lastName = binding.firstInputEditField.text.toString()
            val email = binding.secondInputEditField.text.toString()

            if (!validateEmail(email)) {
                showErrorMessage("Некорректный email")
                return@setOnClickListener
            }

            compositeDisposable.add(
                Single.zip<Boolean, Boolean, Response>(
                    authenticationApi.isLoginAvailable(lastName),
                    authenticationApi.isEmailAvailable(email),
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
                            registrationViewModel.loginEmailData.value = Pair(lastName, email)
                            FragmentNavigationUtils.openFragment(
                                requireActivity().supportFragmentManager,
                                RegThirdFragment()
                            )
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
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.alreadyRegisteredText.setOnClickListener {
            FragmentNavigationUtils.clearAllAndOpenFragment(
                requireActivity().supportFragmentManager,
                AuthFragment()
            )
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

        registrationViewModel =
            ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
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