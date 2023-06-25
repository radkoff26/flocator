package ru.flocator.feature_settings.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import ru.flocator.core_design.fragments.ResponsiveBottomSheetDialogFragment
import ru.flocator.core_api.api.MainRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.feature_auth.api.ui.AuthFragment
import javax.inject.Inject

@AndroidEntryPoint
class DeleteAccountFragment : ResponsiveBottomSheetDialogFragment(
    ChangePasswordFragment.BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO,
    ChangePasswordFragment.BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO
), ru.flocator.core_sections.SettingsSection {
    private lateinit var fragmentView: View

    @Inject
    lateinit var mainRepository: MainRepository
    private val compositeDisposable = CompositeDisposable()
    override fun getCoordinatorLayout(): CoordinatorLayout {
        return fragmentView.findViewById(ru.flocator.app.R.id.coordinator)
    }

    override fun getBottomSheetScrollView(): NestedScrollView {
        return fragmentView.findViewById(ru.flocator.app.R.id.bs)
    }

    override fun getInnerLayout(): ViewGroup {
        return fragmentView.findViewById(ru.flocator.app.R.id.content)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        fragmentView =
            inflater.inflate(ru.flocator.app.R.layout.fragment_delete_account, container, false)
        val closeButton =
            fragmentView.findViewById<ImageView>(ru.flocator.app.R.id.delete_account_close_button)
        val passwordField =
            fragmentView.findViewById<TextInputEditText>(ru.flocator.app.R.id.delete_account_pass_field)
        val confirmButton =
            fragmentView.findViewById<MaterialButton>(ru.flocator.app.R.id.delete_account_confirm_button)
        val message =
            fragmentView.findViewById<TextView>(ru.flocator.app.R.id.delete_account_message)
        closeButton.setOnClickListener {
            dismiss()
        }

        confirmButton.setOnClickListener {
            val pass = passwordField.text
            if (pass == null || pass.isEmpty()) {
                message.text = getString(ru.flocator.app.R.string.password_cannot_be_empty)
                message.visibility = View.VISIBLE
            } else {
                compositeDisposable.add(
                    mainRepository.restApi.deleteCurrentAccount(pass.toString())
                        .observeOn(Schedulers.io())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .doOnError {
                            Log.e("Deleting account error", it.stackTraceToString(), it)
                            message.text = getString(ru.flocator.app.R.string.incorrect_pass)
                            message.visibility = View.VISIBLE
                        }
                        .subscribe {
                            compositeDisposable.add(
                                mainRepository.clearAllCache()
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
        ru.flocator.core_utils.FragmentNavigationUtils.clearAllAndOpenFragment(
            requireActivity().supportFragmentManager,
            AuthFragment()
        )
    }

    companion object {
        const val TAG = "Delete account fragment"
    }
}