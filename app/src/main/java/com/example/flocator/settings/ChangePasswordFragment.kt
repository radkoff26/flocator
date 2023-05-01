package com.example.flocator.settings

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.example.flocator.R
import com.example.flocator.common.storage.SharedStorage
import com.example.flocator.main.api.ClientAPI
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.w3c.dom.Text
import retrofit2.HttpException
import javax.inject.Inject

@AndroidEntryPoint
class ChangePasswordFragment : BottomSheetDialogFragment(), SettingsSection {
    @Inject
    lateinit var clientAPI: ClientAPI
    @Inject
    lateinit var sharedStorage: SharedStorage

    val compositeDisposable = CompositeDisposable()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_change_password, container, false)
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
                messageField.text = "Поля не должны быть пустыми!"
                messageField.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (new != repeat) {
                messageField.text = "Пароли не совпадают!"
                messageField.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (new == old) {
                messageField.text = "Новый пароль совпадает со старым!"
                messageField.visibility = View.VISIBLE
                return@setOnClickListener
            }
            compositeDisposable.add(
                clientAPI.changePassword(
                    sharedStorage.getUserId()!!,
                    old,
                    new
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe (
                        { res ->
                            if (res) {
                                messageField.setTextColor(Color.parseColor("#00ee00"))
                                messageField.text = "Пароль успешно изменен!"
                            } else {
                                messageField.text = "Пароль неверен!"
                            }
                            messageField.visibility = View.VISIBLE
                        },
                        {
                            Log.e("Changing password", "error", it)
                            messageField.text = "Пароль неверен!"
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

    companion object {
        const val TAG = "Change pass fragment"
    }


}