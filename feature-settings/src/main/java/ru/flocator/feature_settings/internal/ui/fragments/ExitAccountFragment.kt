package ru.flocator.feature_settings.internal.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import io.reactivex.disposables.CompositeDisposable
import ru.flocator.core.dependencies.findDependencies
import ru.flocator.core.navigation.NavController
import ru.flocator.core.navigation.findNavController
import ru.flocator.core.section.SettingsSection
import ru.flocator.design.fragments.ResponsiveBottomSheetDialogFragment
import ru.flocator.feature_settings.R
import ru.flocator.feature_settings.databinding.FragmentExitAccountBinding
import ru.flocator.feature_settings.internal.core.di.DaggerSettingsComponent
import ru.flocator.feature_settings.internal.data.repository.SettingsRepository
import javax.inject.Inject

internal class ExitAccountFragment : ResponsiveBottomSheetDialogFragment(
    BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO,
    BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO
), SettingsSection {
    private var _binding: FragmentExitAccountBinding? = null
    private val binding: FragmentExitAccountBinding
        get() = _binding!!

    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var controller: NavController

    @Inject
    lateinit var settingsRepository: SettingsRepository

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
        val fragmentView = inflater.inflate(R.layout.fragment_exit_account, container, false)

        _binding = FragmentExitAccountBinding.bind(fragmentView)

        binding.exitAccountCloseButton.setOnClickListener {
            dismiss()
        }

        binding.exitAccountConfirmButton.setOnClickListener {
            settingsRepository.clearCache()
            openAuthFragment()
        }

        return fragmentView
    }

    override fun onDestroyView() {
        compositeDisposable.dispose()
        _binding = null
        super.onDestroyView()
    }

    private fun openAuthFragment() {
        dismiss()
        controller.toAuth()
    }

    companion object {
        const val TAG = "Exit account fragment"
        const val BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO = 0.95
        const val BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO = 0.9
    }
}