package ru.flocator.feature_auth.internal.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.flocator.core.dependencies.findDependencies
import ru.flocator.core.navigation.NavController
import ru.flocator.core.navigation.findNavController
import ru.flocator.core.section.AuthenticationSection
import ru.flocator.feature_auth.databinding.FragmentRegistrationBinding
import ru.flocator.feature_auth.internal.di.DaggerAuthComponent
import ru.flocator.feature_auth.internal.view_models.RegistrationViewModel
import javax.inject.Inject

internal class RegFirstFragment : Fragment(),
    AuthenticationSection {
    private var _binding: FragmentRegistrationBinding? = null
    private val binding: FragmentRegistrationBinding
        get() = _binding!!

    @Inject
    internal lateinit var controller: NavController

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var registrationViewModel: RegistrationViewModel

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
            resources.getString(ru.flocator.feature_auth.R.string.first_name)
        binding.secondInputEditField.contentDescription =
            resources.getString(ru.flocator.feature_auth.R.string.last_name)
        binding.submitBtn.contentDescription =
            resources.getString(ru.flocator.feature_auth.R.string.next)

        binding.submitBtn.setOnClickListener {
            val firstName = binding.firstInputEditField.text.toString()
            val lastName = binding.secondInputEditField.text.toString()

            if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
                registrationViewModel.updateNameData(
                    Pair(
                        binding.firstInputEditField.text.toString(),
                        binding.secondInputEditField.text.toString()
                    )
                )
                val bundle = Bundle()
                bundle.putString("lastname", binding.firstInputEditField.text.toString())
                bundle.putString("firstname", binding.secondInputEditField.text.toString())
                val regSecondFragment = RegSecondFragment()
                regSecondFragment.arguments = bundle
                controller.toFragment(regSecondFragment)
            } else {
                if (firstName.isEmpty()) {
                    binding.firstInputField.error =
                        resources.getString(ru.flocator.feature_auth.R.string.field_mustnt_be_empty)
                    binding.firstInputField.isErrorEnabled = true
                }
                if (lastName.isEmpty()) {
                    binding.secondInputField.error =
                        resources.getString(ru.flocator.feature_auth.R.string.field_mustnt_be_empty)
                    binding.secondInputField.isErrorEnabled = true
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

        binding.alreadyRegisteredText.setOnClickListener {
            controller.toAuth()
        }

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
            resources.getString(ru.flocator.feature_auth.R.string.first_name)
        binding.secondInputField.hint =
            resources.getString(ru.flocator.feature_auth.R.string.last_name)
        binding.submitBtn.text = resources.getString(ru.flocator.feature_auth.R.string.next)

        registrationViewModel.nameData.value?.let { savedData ->
            binding.firstInputEditField.setText(savedData.first)
            binding.secondInputEditField.setText(savedData.second)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}