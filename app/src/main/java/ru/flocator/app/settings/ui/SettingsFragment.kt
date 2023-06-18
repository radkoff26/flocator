package ru.flocator.app.settings.ui

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import ru.flocator.core_design.R
import ru.flocator.core_api.api.MainRepository
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.core_utils.FragmentNavigationUtils
import java.sql.Timestamp
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment: Fragment(), ru.flocator.core_sections.SettingsSection {
    private val compositeDisposable = CompositeDisposable()

    private lateinit var fragmentView: View

    @Inject lateinit var mainRepository: MainRepository
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val photoChangeLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
            if (result != null) {
                changeAvatar(result)
                compositeDisposable.add(
                    io.reactivex.Completable.create { emitter ->
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

                        emitter.onComplete()
                        stream.close()
                    }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError { Log.e("Sending image", "error", it) }
                        .subscribe()
                )
            }
        }
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(ru.flocator.app.R.layout.fragment_settings, container, false)
        val exitLinearLayout = fragmentView.findViewById<LinearLayout>(ru.flocator.app.R.id.exit_account_line)
        val birthDateLinearLayout = fragmentView.findViewById<LinearLayout>(ru.flocator.app.R.id.date_of_birth_line)
        val birthDateField = fragmentView.findViewById<TextView>(ru.flocator.app.R.id.date_of_birth_field)
        val blacklistLine = fragmentView.findViewById<LinearLayout>(ru.flocator.app.R.id.blacklist_line)
        val privacyLine = fragmentView.findViewById<LinearLayout>(ru.flocator.app.R.id.privacy_line)
        val changePasswordLine = fragmentView.findViewById<LinearLayout>(ru.flocator.app.R.id.change_password_line)
        val deleteAccountLine = fragmentView.findViewById<LinearLayout>(ru.flocator.app.R.id.delete_account_line)
        val nameField = fragmentView.findViewById<EditText>(ru.flocator.app.R.id.name_field)
        val avatar = fragmentView.findViewById<CircleImageView>(ru.flocator.app.R.id.avatar)
        val blacklistCnt = fragmentView.findViewById<TextView>(ru.flocator.app.R.id.blacklist_cnt)
        val toolbar = fragmentView.findViewById<Toolbar>(ru.flocator.app.R.id.toolbar)

        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(ru.flocator.app.R.drawable.back)
        }
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        compositeDisposable.add(
            io.reactivex.Observable.create {
                    emitter ->
                compositeDisposable.add(
                    mainRepository.userInfoCache.getUserInfo()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            emitter.onNext(it)
                        }, {
                            Log.e("Getting UserInfo cache error", it.stackTraceToString(), it)
                        })
                )
                compositeDisposable.add(
                    mainRepository.restApi.getCurrentUserInfo()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            emitter.onNext(it)
                        }, {
                            Log.e("Getting UserInfo network data error", it.stackTraceToString(), it)
                        })
                )
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { userInfo ->
                        if (userInfo == UserInfo.DEFAULT) {
                            return@subscribe
                        }
                        mainRepository.userInfoCache.updateUserInfo(userInfo)
                        nameField.setText(userInfo.firstName + " " + userInfo.lastName)
                        val cal = Calendar.getInstance()
                        if (userInfo.birthDate != null) {
                            cal.timeInMillis = userInfo.birthDate!!.time
                            birthDateField.text = getString(
                                ru.flocator.app.R.string.date_with_placeholders,
                                cal.get(Calendar.DAY_OF_MONTH),
                                cal.get(Calendar.MONTH) + 1,
                                cal.get(Calendar.YEAR),
                            )
                        }



                        if (userInfo.avatarUri != null) {
                            compositeDisposable.add(
                                mainRepository.photoLoader.getPhoto(userInfo.avatarUri!!)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                        { avatar.setImageBitmap(it) },
                                        { Log.e("Loading ava", "error", it) })
                            )
                        }

                    },
                    {
                        Log.e("Loading user info", it.stackTraceToString(), it)
                    }
                )
        )

        compositeDisposable.add(
            mainRepository.restApi.getCurrentUserBlocked()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        blacklistCnt.text = it.size.toString()
                    },
                    {
                        Log.e("Getting blacklist size error", it.stackTraceToString(), it)
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
                        ru.flocator.app.R.string.date_with_placeholders,
                        resDay,
                        resMonth + 1,
                        resYear)
                    val stamp = Calendar.getInstance()
                    stamp.set(resYear, resMonth, resDay)
                    compositeDisposable.add(
                        mainRepository.restApi.changeCurrentUserBirthdate(
                            Timestamp(stamp.timeInMillis)
                        )
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
                    secondName += word
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
                PrivacySettingsFragment()
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

    override fun onDestroyView() {
        compositeDisposable.dispose()
        super.onDestroyView()
    }

    private fun changeAvatar(uri: Uri) {
        val avatar = fragmentView.findViewById<CircleImageView>(ru.flocator.app.R.id.avatar)
        avatar.setImageURI(uri)
    }

}