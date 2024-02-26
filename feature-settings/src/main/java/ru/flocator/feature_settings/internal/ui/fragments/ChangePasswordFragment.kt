package ru.flocator.feature_settings.internal.ui.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import ru.flocator.core.dependencies.findDependencies
import ru.flocator.core.extensions.baseActivity
import ru.flocator.core.navigation.NavController
import ru.flocator.core.navigation.findNavController
import ru.flocator.core.section.SettingsSection
import ru.flocator.design.fragments.ResponsiveBottomSheetDialogFragment
import ru.flocator.feature_settings.R
import ru.flocator.feature_settings.databinding.FragmentChangePasswordBinding
import ru.flocator.feature_settings.internal.core.di.DaggerSettingsComponent
import ru.flocator.feature_settings.internal.ui.view_models.ChangePasswordViewModel
import javax.inject.Inject

internal class ChangePasswordFragment : ResponsiveBottomSheetDialogFragment(
    BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO,
    BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO
), SettingsSection {

    @Inject
    lateinit var controller: NavController

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding: FragmentChangePasswordBinding
        get() = _binding!!

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: ChangePasswordViewModel

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
            inflater.inflate(R.layout.fragment_change_password, container, false)

        _binding = FragmentChangePasswordBinding.bind(fragmentView)

        viewModel = ViewModelProvider(this, viewModelFactory)[ChangePasswordViewModel::class.java]
        viewModel.errorStatusLiveData.observe(viewLifecycleOwner) {
            it ?: return@observe
            when (it) {
                ChangePasswordViewModel.UiErrorStatus.LOADING_ERROR -> {
                    baseActivity().notifyAboutError(
                        getString(R.string.password_changed_successfully),
                        binding.root
                    )
                }
                ChangePasswordViewModel.UiErrorStatus.WRONG_PASSWORD -> {
                    binding.changePassMessage.text =
                        getString(R.string.password_is_incorrect)
                    binding.changePassMessage.visibility = View.VISIBLE
                }
            }
        }

        binding.changePassMessage.visibility = View.GONE
        binding.changePassConfirmButton.setOnClickListener {
            binding.changePassMessage.visibility = View.GONE
            binding.changePassMessage.setTextColor(Color.parseColor("#ee0000"))
            val new = binding.changePassNewPass.text.toString()
            val repeat = binding.changePassNewRepeat.text.toString()
            val old = binding.changePassOldPass.text.toString()
            if (new == "" || repeat == "" || old == "") {
                binding.changePassMessage.text = getString(R.string.fields_must_not_be_empty)
                binding.changePassMessage.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (new != repeat) {
                binding.changePassMessage.text = getString(R.string.passwords_are_not_similar)
                binding.changePassMessage.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (new == old) {
                binding.changePassMessage.text = getString(R.string.new_password_is_similar)
                binding.changePassMessage.visibility = View.VISIBLE
                return@setOnClickListener
            }
            viewModel.changePassword(new, onSuccess = {
                binding.changePassMessage.setTextColor(Color.parseColor("#00ee00"))
                binding.changePassMessage.text =
                    getString(R.string.password_changed_successfully)
                binding.changePassMessage.visibility = View.VISIBLE
            })
        }
        binding.changePasswordCloseButton.setOnClickListener {
            dismiss()
        }
        return fragmentView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "Change pass fragment"
        const val BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO = 0.9
        const val BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO = 0.8

        fun newInstance(): ChangePasswordFragment = ChangePasswordFragment()
    }
}