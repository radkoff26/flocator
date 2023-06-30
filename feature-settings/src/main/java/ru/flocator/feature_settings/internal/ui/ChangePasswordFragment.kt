package ru.flocator.feature_settings.internal.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.core_api.api.AppRepository
import ru.flocator.core_controller.NavController
import ru.flocator.core_controller.findNavController
import ru.flocator.core_dependency.findDependencies
import ru.flocator.core_design.fragments.ResponsiveBottomSheetDialogFragment
import ru.flocator.core_sections.SettingsSection
import ru.flocator.feature_settings.R
import ru.flocator.feature_settings.api.dependencies.SettingsDependencies
import ru.flocator.feature_settings.databinding.FragmentChangePasswordBinding
import ru.flocator.feature_settings.internal.di.DaggerSettingsComponent
import ru.flocator.feature_settings.internal.repository.SettingsRepository
import javax.inject.Inject

internal class ChangePasswordFragment : ResponsiveBottomSheetDialogFragment(
    BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO,
    BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO
), SettingsSection {

    @Inject
    lateinit var appRepository: AppRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var controller: NavController

    private val compositeDisposable = CompositeDisposable()

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding: FragmentChangePasswordBinding
        get() = _binding!!

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
            compositeDisposable.add(
                settingsRepository.changeCurrentUserPass(
                    old,
                    new
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { res ->
                            if (res) {
                                binding.changePassMessage.setTextColor(Color.parseColor("#00ee00"))
                                binding.changePassMessage.text =
                                    getString(R.string.password_changed_successfully)
                            } else {
                                binding.changePassMessage.text =
                                    getString(R.string.password_is_incorrect)
                            }
                            binding.changePassMessage.visibility = View.VISIBLE
                            appRepository.userCredentialsCache.clearUserCredentials()
                            appRepository.userInfoCache.clearUserInfo()
                            controller.toAuth()
                                .clearAll()
                                .commit()
                        },
                        {
                            Log.e("Changing password", "error", it)
                            binding.changePassMessage.text =
                                getString(R.string.password_is_incorrect)
                            binding.changePassMessage.visibility = View.VISIBLE
                        }
                    )
            )
        }
        binding.changePasswordCloseButton.setOnClickListener {
            dismiss()
        }
        return fragmentView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
        _binding = null
    }

    companion object {
        const val TAG = "Change pass fragment"
        const val BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO = 0.9
        const val BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO = 0.8
    }


}