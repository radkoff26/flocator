package ru.flocator.feature_settings.internal.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import ru.flocator.core_design.fragments.ResponsiveBottomSheetDialogFragment
import ru.flocator.core_api.api.AppRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.core_controller.NavController
import ru.flocator.feature_settings.R
import ru.flocator.feature_settings.api.dependencies.SettingsDependencies
import ru.flocator.feature_settings.databinding.FragmentDeleteAccountBinding
import ru.flocator.feature_settings.internal.repository.SettingsRepository
import javax.inject.Inject

internal class DeleteAccountFragment : ResponsiveBottomSheetDialogFragment(
    ChangePasswordFragment.BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO,
    ChangePasswordFragment.BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO
), ru.flocator.core_sections.SettingsSection {
    private var _binding: FragmentDeleteAccountBinding? = null
    private val binding: FragmentDeleteAccountBinding
        get() = _binding!!

    @Inject
    lateinit var controller: NavController

    @Inject
    lateinit var dependencies: SettingsDependencies

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val compositeDisposable = CompositeDisposable()

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
        val fragmentView =
            inflater.inflate(R.layout.fragment_delete_account, container, false)

        _binding = FragmentDeleteAccountBinding.bind(fragmentView)

        binding.deleteAccountCloseButton.setOnClickListener {
            dismiss()
        }

        binding.deleteAccountConfirmButton.setOnClickListener {
            val pass = binding.deleteAccountPassField.text
            if (pass == null || pass.isEmpty()) {
                binding.deleteAccountMessage.text = getString(R.string.password_cannot_be_empty)
                binding.deleteAccountMessage.visibility = View.VISIBLE
            } else {
                compositeDisposable.add(
                    settingsRepository.deleteCurrentAccount(pass.toString())
                        .observeOn(Schedulers.io())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .doOnError {
                            Log.e("Deleting account error", it.stackTraceToString(), it)
                            binding.deleteAccountMessage.text = getString(R.string.incorrect_pass)
                            binding.deleteAccountMessage.visibility = View.VISIBLE
                        }
                        .subscribe {
                            compositeDisposable.add(
                                dependencies.appRepository.clearAllCache()
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .doOnError {
                                        Log.e(TAG, "onCreateView: failed to clear cache!", it)
                                        openAuthFragment()
                                    }
                                    .subscribe {
                                        openAuthFragment()
                                    }
                            )
                            Log.i("Got ans", it.toString())
                        }
                )
            }

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
        const val TAG = "Delete account fragment"
    }
}