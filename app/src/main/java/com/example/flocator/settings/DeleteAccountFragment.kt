package com.example.flocator.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.example.flocator.R
import com.example.flocator.common.fragments.ResponsiveBottomSheetDialogFragment
import com.example.flocator.common.repository.MainRepository
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.example.flocator.main.ui.add_mark.AddMarkFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@AndroidEntryPoint
class DeleteAccountFragment : ResponsiveBottomSheetDialogFragment(
    AddMarkFragment.BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO,
    AddMarkFragment.BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO
), SettingsSection {
    lateinit var fragmentView: View
    @Inject
    lateinit var mainRepository: MainRepository
    val compositeDisposable = CompositeDisposable()
    override fun getCoordinatorLayout(): CoordinatorLayout {
       return fragmentView.findViewById(R.id.coordinator)
    }

    override fun getBottomSheetScrollView(): NestedScrollView {
        return fragmentView.findViewById(R.id.bs)
    }

    override fun getInnerLayout(): ViewGroup {
        return fragmentView.findViewById(R.id.content)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_delete_account, container, false)
        val closeButton = fragmentView.findViewById<ImageView>(R.id.delete_account_close_button)
        val passwordField = fragmentView.findViewById<TextInputEditText>(R.id.delete_account_pass_field)
        val confirmButton = fragmentView.findViewById<MaterialButton>(R.id.delete_account_confirm_button)
        val message = fragmentView.findViewById<TextView>(R.id.delete_account_message)
        closeButton.setOnClickListener {
            dismiss()
        }

        confirmButton.setOnClickListener {
            val pass = passwordField.text
            if (pass == null || pass.isEmpty()) {
                message.text = getString(R.string.password_cannot_be_empty)
                message.visibility = View.VISIBLE
            } else {
                compositeDisposable.add(
                    mainRepository.restApi.deleteCurrentAccount(pass.toString())
                    .observeOn(Schedulers.io())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .doOnError {
                            Log.e("Deleting account error", it.stackTraceToString(), it)
                            message.text = getString(R.string.incorrect_pass)
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
        FragmentNavigationUtils.clearAllAndOpenFragment(
            requireActivity().supportFragmentManager,
            com.example.flocator.authentication.authorization.AuthFragment()
        )
    }

    companion object {
        const val TAG = "Delete account fragment"
    }
}