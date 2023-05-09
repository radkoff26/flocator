package com.example.flocator.settings

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.example.flocator.R
import com.example.flocator.common.config.Constants
import com.example.flocator.common.repository.MainRepository
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.example.flocator.main.api.ClientAPI
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.w3c.dom.Text
import java.util.*
import javax.inject.Inject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URI
import java.net.URL
import java.sql.Timestamp

@AndroidEntryPoint
class SettingsFragment: Fragment(), SettingsSection {
    private val compositeDisposable = CompositeDisposable()

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
        .build()
    lateinit var fragmentView: View

    //    val clientAPI: ClientAPI = retrofit.create(ClientAPI::class.java)
    @Inject lateinit var clientAPI: ClientAPI
    @Inject lateinit var mainRepository: MainRepository
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val photoChangeLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
            if (result != null) {
                changeAvatar(result)
                compositeDisposable.add(
                    io.reactivex.Observable.create<Boolean> { emitter ->
                        val stream = context?.contentResolver?.openInputStream(result)!!
                        compositeDisposable.add(
                            mainRepository.restApi.changeCurrentUserAva(
                                MultipartBody.Part.createFormData(
                                    "photo",
                                    result.toString(),
                                    RequestBody.create(
                                        MediaType.parse("image/*"),
                                        stream.readBytes()
                                    )
                                )
                            )
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({}, {Log.e("Loading image from storage", "error", it)})
                        )

                        emitter.onNext(true)
                        stream.close()
                    }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({}, {Log.e("Sending image", "error", it)})
                )
            }
        }
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_settings, container, false)
        val exitLinearLayout = fragmentView.findViewById<LinearLayout>(R.id.exit_account_line)
        val birthDateLinearLayout = fragmentView.findViewById<LinearLayout>(R.id.date_of_birth_line)
        val birthDateField = fragmentView.findViewById<TextView>(R.id.date_of_birth_field)
        val blacklistLine = fragmentView.findViewById<LinearLayout>(R.id.blacklist_line)
        val privacyLine = fragmentView.findViewById<LinearLayout>(R.id.privacy_line)
        val changePasswordLine = fragmentView.findViewById<LinearLayout>(R.id.change_password_line)
        val deleteAccountLine = fragmentView.findViewById<LinearLayout>(R.id.delete_account_line)
        val nameField = fragmentView.findViewById<EditText>(R.id.name_field)
        val avatar = fragmentView.findViewById<CircleImageView>(R.id.avatar)
        val blacklistCnt = fragmentView.findViewById<TextView>(R.id.blacklist_cnt)

        compositeDisposable.add(
            Single.concat(
                mainRepository.userInfoCache.getUserInfo(),
                mainRepository.restApi.getCurrentUserInfo()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { userInfo ->
                        mainRepository.userInfoCache.updateUserInfo(userInfo)
                        nameField.setText(userInfo.firstName + " " + userInfo.lastName)
                        val cal = Calendar.getInstance()
                        if (userInfo.birthDate != null) {
                            cal.timeInMillis = userInfo.birthDate.time
                            birthDateField.text = getString(
                                R.string.date_with_placeholders,
                                cal.get(Calendar.DAY_OF_MONTH),
                                cal.get(Calendar.MONTH) + 1,
                                cal.get(Calendar.YEAR),
                            )
                        }

                        blacklistCnt.text = userInfo.blockedUsers.size.toString()

                        if (userInfo.avatarUri != null) {
                            compositeDisposable.add(
                                mainRepository.photoLoader.getPhoto(userInfo.avatarUri)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                        { avatar.setImageBitmap(it) },
                                        { Log.e("Loading ava", "error", it) })
                            )
                        }

                    },
                    {
                        Log.e("Loading user info", "Error", it)
                    }
                )
        )

        avatar.setOnClickListener {
            photoChangeLauncher.launch("image/*")
        }

        birthDateLinearLayout.setOnClickListener {

            val c = Calendar.getInstance()

            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, resYear, resMonth, resDay ->
                    // on below line we are setting
                    // date to our text view.
                    birthDateField.text = getString(
                        R.string.date_with_placeholders,
                        resDay,
                        resMonth + 1,
                        resYear)
                    val stamp = Calendar.getInstance()
                    stamp.set(resYear, resMonth, resDay)
                    compositeDisposable.add(mainRepository.restApi.changeCurrentUserBirthdate(Timestamp(stamp.timeInMillis))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({}, {Log.e("Sending user birthdate", "error", it)}))
                },
                year,
                month,
                day
            )

            datePickerDialog.show()
        }

        nameField.setOnFocusChangeListener{_, hasFocus ->
            if (!hasFocus) {
                val words = nameField.text.split(" ")
                val firstName = words[0]
                var secondName = ""
                for (word in words.listIterator(1)) {
                    secondName += word;
                }
                compositeDisposable.add(
                mainRepository.restApi.changeCurrentUserName(firstName, secondName)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({}, {Log.e("Change name", "error", it)})
                )
            }
        }



        privacyLine.setOnClickListener {
            FragmentNavigationUtils.openFragment(
                requireActivity().supportFragmentManager,
                PrivacySettings()
            )
        }

        blacklistLine.setOnClickListener {
            FragmentNavigationUtils.openFragment(
                requireActivity().supportFragmentManager,
                BlackListFragment()
            )
        }

        changePasswordLine.setOnClickListener {
            val changePasswordFragment = ChangePasswordFragment()
            changePasswordFragment.show(parentFragmentManager, ChangePasswordFragment.TAG)
        }

        exitLinearLayout.setOnClickListener {
            val exitAccountFragment = ExitAccountFragment()
            exitAccountFragment.show(parentFragmentManager, ExitAccountFragment.TAG)
        }

        deleteAccountLine.setOnClickListener {
            val deleteAccountFragment = DeleteAccountFragment()
            deleteAccountFragment.show(parentFragmentManager, DeleteAccountFragment.TAG)
        }
        return fragmentView
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun changeAvatar(uri: Uri) {
        val avatar = fragmentView.findViewById<CircleImageView>(R.id.avatar)
        avatar.setImageURI(uri)
    }

}