package ru.flocator.feature_settings.internal.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import ru.flocator.core.dependencies.findDependencies
import ru.flocator.core.extensions.baseActivity
import ru.flocator.core.navigation.NavController
import ru.flocator.core.navigation.findNavController
import ru.flocator.core.section.SettingsSection
import ru.flocator.design.fragments.ResponsiveBottomSheetDialogFragment
import ru.flocator.feature_settings.R
import ru.flocator.feature_settings.databinding.FragmentDeleteAccountBinding
import ru.flocator.feature_settings.internal.data.repository.SettingsRepository
import ru.flocator.feature_settings.internal.core.di.DaggerSettingsComponent
import ru.flocator.feature_settings.internal.ui.view_models.DeleteAccountViewModel
import javax.inject.Inject

internal class DeleteAccountFragment : ResponsiveBottomSheetDialogFragment(
    BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO,
    BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO
), SettingsSection {
    private var _binding: FragmentDeleteAccountBinding? = null
    private val binding: FragmentDeleteAccountBinding
        get() = _binding!!

    @Inject
    lateinit var controller: NavController

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: DeleteAccountViewModel

    override fun getCoordinatorLayout(): CoordinatorLayout {
        return binding.coordinator
    }

    override fun getBottomSheetScrollView(): NestedScrollView {
        return binding.bs
    }

    override fun getInnerLayout(): ViewGroup {
        return binding.content
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerSettingsComponent.factory()
            .create(
                findDependencies(),
                findNavController()
            )
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val fragmentView =
            inflater.inflate(R.layout.fragment_delete_account, container, false)

        _binding = FragmentDeleteAccountBinding.bind(fragmentView)

        viewModel = ViewModelProvider(this, viewModelFactory)[DeleteAccountViewModel::class.java]

        viewModel.errorStatusLiveData.observe(viewLifecycleOwner) {
            it ?: return@observe
            when (it) {
                DeleteAccountViewModel.UiErrorStatus.WRONG_PASSWORD -> {
                    binding.deleteAccountMessage.text = getString(R.string.incorrect_pass)
                    binding.deleteAccountMessage.visibility = View.VISIBLE
                }
                DeleteAccountViewModel.UiErrorStatus.LOADING_ERROR -> {
                    baseActivity().notifyAboutError(
                        getString(R.string.failed_to_delete_account),
                        binding.root
                    )
                }
            }
        }

        binding.deleteAccountCloseButton.setOnClickListener {
            dismiss()
        }

        binding.deleteAccountPassField.addTextChangedListener {
            // When typing is performed, it's better to disable message
            binding.deleteAccountMessage.visibility = View.INVISIBLE
        }

        binding.deleteAccountConfirmButton.setOnClickListener {
            val password = binding.deleteAccountPassField.text
            if (password == null || password.isEmpty()) {
                binding.deleteAccountMessage.text = getString(R.string.password_cannot_be_empty)
                binding.deleteAccountMessage.visibility = View.VISIBLE
            } else {
                viewModel.deleteAccount(password.toString()) {
                    settingsRepository.clearCache()
                    openAuthFragment()
                }
            }
        }
        return fragmentView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun openAuthFragment() {
        dismiss()
        controller.toAuthWithBackStackCleared()
    }

    companion object {
        const val TAG = "Delete account fragment"
        const val BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO = 0.9
        const val BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO = 0.8

        fun newInstance(): DeleteAccountFragment = DeleteAccountFragment()
    }
}