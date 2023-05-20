package com.example.flocator.settings

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.example.flocator.R
import com.example.flocator.common.repository.MainRepository
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.example.flocator.main.api.ClientAPI
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import com.example.flocator.authentication.authorization.AuthFragment
import com.example.flocator.common.fragments.ResponsiveBottomSheetDialogFragment
import com.example.flocator.main.ui.add_mark.AddMarkFragment

@AndroidEntryPoint
class ChangePasswordFragment : ResponsiveBottomSheetDialogFragment(
    AddMarkFragment.BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO,
    AddMarkFragment.BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO
), SettingsSection {
    @Inject
    lateinit var clientAPI: ClientAPI
    @Inject
    lateinit var mainRepository: MainRepository
    lateinit var fragmentView: View

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
        fragmentView = inflater.inflate(R.layout.fragment_change_password, container, false)
        val confirmButton = fragmentView.findViewById<MaterialButton>(R.id.change_pass_confirm_button)
        val closeButton = fragmentView.findViewById<FrameLayout>(R.id.change_password_close_button)
        val messageField = fragmentView.findViewById<TextView>(R.id.change_pass_message)
        val oldPass = fragmentView.findViewById<TextInputEditText>(R.id.change_pass_old_pass)
        val newPass = fragmentView.findViewById<TextInputEditText>(R.id.change_pass_new_pass)
        val newPassRepeat = fragmentView.findViewById<TextInputEditText>(R.id.change_pass_new_repeat)
        messageField.visibility = View.GONE
        confirmButton.setOnClickListener {
            messageField.visibility = View.GONE
            messageField.setTextColor(Color.parseColor("#ee0000"))
            val new = newPass.text.toString()
            val repeat = newPassRepeat.text.toString()
            val old = oldPass.text.toString()
            if (new == "" || repeat == "" || old == "") {
                messageField.text = getString(R.string.fields_must_not_be_empty)
                messageField.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (new != repeat) {
                messageField.text = getString(R.string.passwords_are_not_similar)
                messageField.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (new == old) {
                messageField.text = getString(R.string.new_password_is_similar)
                messageField.visibility = View.VISIBLE
                return@setOnClickListener
            }
            compositeDisposable.add(
                mainRepository.restApi.changeCurrentUserPass (
                    old,
                    new
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe (
                        { res ->
                            if (res) {
                                messageField.setTextColor(Color.parseColor("#00ee00"))
                                messageField.text = getString(R.string.password_changed_successfully)
                            } else {
                                messageField.text = getString(R.string.password_is_incorrect)
                            }
                            messageField.visibility = View.VISIBLE
                            mainRepository.userDataCache.clearUserData()
                            mainRepository.userInfoCache.clearUserInfo()
                            FragmentNavigationUtils.clearAllAndOpenFragment(
                                requireActivity().supportFragmentManager,
                                AuthFragment()
                            )
                        },
                        {
                            Log.e("Changing password", "error", it)
                            messageField.text = getString(R.string.password_is_incorrect)
                            messageField.visibility = View.VISIBLE
                        }
                    )
            )
        }
        closeButton.setOnClickListener {
            dismiss()
        }
        return fragmentView
    }

    override fun onDestroyView() {
        compositeDisposable.dispose()
        super.onDestroyView()
    }

    companion object {
        const val TAG = "Change pass fragment"
    }


}