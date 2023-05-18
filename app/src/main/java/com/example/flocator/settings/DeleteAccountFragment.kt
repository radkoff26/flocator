package com.example.flocator.settings

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.flocator.R
import com.example.flocator.common.repository.MainRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@AndroidEntryPoint
class DeleteAccountFragment : BottomSheetDialogFragment(), SettingsSection {
    @Inject
    lateinit var mainRepository: MainRepository
    val compositeDisposable = CompositeDisposable()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_delete_account, container, false)
        val closeButton = fragmentView.findViewById<ImageView>(R.id.delete_account_close_button)
        val passwordField = fragmentView.findViewById<TextInputEditText>(R.id.delete_account_pass_field)
        val confirmButton = fragmentView.findViewById<MaterialButton>(R.id.delete_account_confirm_button)
        val message = fragmentView.findViewById<TextView>(R.id.delete_account_message)
        closeButton.setOnClickListener {
            dismiss()
        }
2
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
                        Log.i("Got ans", it.toString())
                    }
                )
            }

        }
        return fragmentView
    }

    companion object {
        const val TAG = "Delete account fragment"
    }
}