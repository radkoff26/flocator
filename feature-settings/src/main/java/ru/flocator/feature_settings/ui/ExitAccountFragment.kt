package ru.flocator.feature_settings.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import ru.flocator.core_design.fragments.ResponsiveBottomSheetDialogFragment
import ru.flocator.core_api.api.MainRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.flocator.core_controller.NavController
import ru.flocator.feature_settings.R
import ru.flocator.feature_settings.databinding.FragmentExitAccountBinding
import javax.inject.Inject

class ExitAccountFragment : ResponsiveBottomSheetDialogFragment(
    BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO,
    BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO
), ru.flocator.core_sections.SettingsSection {
    private var _binding: FragmentExitAccountBinding? = null
    private val binding: FragmentExitAccountBinding
        get() = _binding!!

    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var controller: NavController

    @Inject
    lateinit var repository: MainRepository

    override fun getCoordinatorLayout(): CoordinatorLayout {
        return binding.coordinator
    }

    override fun getBottomSheetScrollView(): NestedScrollView {
        return binding.bs
    }

    override fun getInnerLayout(): ViewGroup {
        return binding.content
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
                repository.clearAllCache()
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
        compositeDisposable.dispose()
        super.onDestroyView()
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