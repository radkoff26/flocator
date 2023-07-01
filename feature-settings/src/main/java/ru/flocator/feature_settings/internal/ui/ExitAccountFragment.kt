package ru.flocator.feature_settings.internal.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.flocator.core_api.api.AppRepository
import ru.flocator.core_controller.NavController
import ru.flocator.core_controller.findNavController
import ru.flocator.core_dependency.findDependencies
import ru.flocator.core_design.fragments.ResponsiveBottomSheetDialogFragment
import ru.flocator.core_sections.SettingsSection
import ru.flocator.feature_settings.R
import ru.flocator.feature_settings.databinding.FragmentExitAccountBinding
import ru.flocator.feature_settings.internal.di.DaggerSettingsComponent
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
    lateinit var appRepository: AppRepository

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
            compositeDisposable.add(
                appRepository.clearAllCache()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError {
                        Log.e(TAG, "onCreateView: failed to clear cache!", it)
                        openAuthFragment()
                    }
                    .subscribe {
                        openAuthFragment()
                    }
            )
        }

        return fragmentView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
        _binding = null
    }

    private fun openAuthFragment() {
        dismiss()
        controller.toAuth()
            .clearAll()
            .commit()
    }

    companion object {
        const val TAG = "Exit account fragment"
        const val BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO = 0.95
        const val BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO = 0.9
    }
}