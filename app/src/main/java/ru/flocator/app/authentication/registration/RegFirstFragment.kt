package ru.flocator.app.authentication.registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import ru.flocator.core_design.R
import ru.flocator.app.authentication.viewmodel.RegistrationViewModel
import ru.flocator.app.databinding.FragmentRegistrationBinding
import ru.flocator.core_sections.AuthenticationSection
import ru.flocator.app.authentication.authorization.AuthFragment
@AndroidEntryPoint
class RegFirstFragment : Fragment(), AuthenticationSection {
    private var _binding: FragmentRegistrationBinding? = null
    private val binding: FragmentRegistrationBinding
        get() = _binding!!
    private val registrationViewModel: RegistrationViewModel by viewModels()

    companion object {
        private const val LAST_NAME = "Фамилия"
        private const val FIRST_NAME = "Имя"
        private const val NEXT = "Далее"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        binding.firstInputEditField.contentDescription = FIRST_NAME
        binding.secondInputEditField.contentDescription = LAST_NAME
        binding.submitBtn.contentDescription = NEXT

        binding.submitBtn.setOnClickListener {
            val firstName = binding.firstInputEditField.text.toString()
            val lastName = binding.secondInputEditField.text.toString()
            if (validateName(firstName, lastName)) {
                hideErrorMessage()
                registrationViewModel.nameData.value = Pair(
                    binding.firstInputEditField.text.toString(),
                    binding.secondInputEditField.text.toString()
                )

                ru.flocator.core_utils.FragmentNavigationUtils.openFragment(
                    requireActivity().supportFragmentManager,
                    RegSecondFragment()
                )
            } else {
                showErrorMessage()
            }
        }

        binding.alreadyRegisteredText.setOnClickListener {
            ru.flocator.core_utils.FragmentNavigationUtils.openFragment(
                requireActivity().supportFragmentManager,
                AuthFragment()
            )
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
        view.id = R.id.first_fragment_root

        binding.firstInputField.hint = LAST_NAME
        binding.secondInputField.hint = FIRST_NAME
        binding.submitBtn.text = NEXT

        registrationViewModel.nameData.value?.let { savedData ->
            binding.firstInputEditField.setText(savedData.first)
            binding.secondInputEditField.setText(savedData.second)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun validateName(firstName: String, lastName: String): Boolean {
        return firstName.isNotEmpty() && lastName.isNotEmpty();
    }

    private fun showErrorMessage() {
        binding.registrationErrorMessageText.visibility = View.VISIBLE
        binding.registrationErrorMessageText.text = "Поля не должны быть пустыми!"
    }

    private fun hideErrorMessage() {
        binding.registrationErrorMessageText.visibility = View.GONE
    }

}