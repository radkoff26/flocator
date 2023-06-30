package ru.flocator.feature_settings.api.ui

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.feature_settings.R
import ru.flocator.feature_settings.api.dependencies.SettingsDependencies
import ru.flocator.feature_settings.databinding.FragmentSettingsBinding
import ru.flocator.feature_settings.internal.repository.SettingsRepository
import ru.flocator.feature_settings.internal.ui.ChangePasswordFragment
import ru.flocator.feature_settings.internal.ui.DeleteAccountFragment
import ru.flocator.feature_settings.internal.ui.ExitAccountFragment
import java.sql.Timestamp
import java.util.*
import javax.inject.Inject

class SettingsFragment : Fragment(), ru.flocator.core_sections.SettingsSection {
    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding
        get() = _binding!!

    private val compositeDisposable = CompositeDisposable()

    private lateinit var fragmentView: View

    @Inject
    lateinit var dependencies: SettingsDependencies

    @Inject
    internal lateinit var settingsRepository: SettingsRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val photoChangeLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
                if (result != null) {
                    changeAvatar(result)
                    compositeDisposable.add(
                        io.reactivex.Completable.create { emitter ->
                            val stream = context?.contentResolver?.openInputStream(result)!!
                            compositeDisposable.add(
                                settingsRepository.changeCurrentUserAva(
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
                                    .subscribe(
                                        {},
                                        { Log.e("Loading image from storage", "error", it) })
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
        val fragmentView =
            inflater.inflate(R.layout.fragment_settings, container, false)

        _binding = FragmentSettingsBinding.bind(fragmentView)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(ru.flocator.core_design.R.drawable.back)
        }
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        compositeDisposable.add(
            io.reactivex.Observable.create { emitter ->
                compositeDisposable.add(
                    dependencies.appRepository.userInfoCache.getUserInfo()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            emitter.onNext(it)
                        }, {
                            Log.e("Getting UserInfo cache error", it.stackTraceToString(), it)
                        })
                )
                compositeDisposable.add(
                    settingsRepository.getCurrentUserInfo()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            emitter.onNext(it)
                        }, {
                            Log.e(
                                "Getting UserInfo network data error",
                                it.stackTraceToString(),
                                it
                            )
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
                        dependencies.appRepository.userInfoCache.updateUserInfo(userInfo)
                        binding.nameField.setText(userInfo.firstName + " " + userInfo.lastName)
                        val cal = Calendar.getInstance()
                        if (userInfo.birthDate != null) {
                            cal.timeInMillis = userInfo.birthDate!!.time
                            binding.dateOfBirthField.text = getString(
                                R.string.date_with_placeholders,
                                cal.get(Calendar.DAY_OF_MONTH),
                                cal.get(Calendar.MONTH) + 1,
                                cal.get(Calendar.YEAR),
                            )
                        }



                        if (userInfo.avatarUri != null) {
                            compositeDisposable.add(
                                dependencies.appRepository.photoLoader.getPhoto(userInfo.avatarUri!!)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                        { binding.avatar.setImageBitmap(it) },
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
            settingsRepository.getCurrentUserBlocked()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        binding.blacklistCnt.text = it.size.toString()
                    },
                    {
                        Log.e("Getting blacklist size error", it.stackTraceToString(), it)
                    }
                )
        )

        binding.avatar.setOnClickListener {
            photoChangeLauncher.launch("image/*")
        }

        binding.dateOfBirthLine.setOnClickListener {

            val c = Calendar.getInstance()

            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, resYear, resMonth, resDay ->
                    // on below line we are setting
                    // date to our text view.
                    binding.dateOfBirthField.text = getString(
                        R.string.date_with_placeholders,
                        resDay,
                        resMonth + 1,
                        resYear
                    )
                    val stamp = Calendar.getInstance()
                    stamp.set(resYear, resMonth, resDay)
                    compositeDisposable.add(
                        settingsRepository.changeCurrentUserBirthdate(
                            Timestamp(stamp.timeInMillis)
                        )
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({}, { Log.e("Sending user birthdate", "error", it) })
                    )
                },
                year,
                month,
                day
            )

            datePickerDialog.show()
        }

        binding.nameField.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val words = binding.nameField.text.split(" ")
                val firstName = words[0]
                var secondName = ""
                for (word in words.listIterator(1)) {
                    secondName += word
                }
                compositeDisposable.add(
                    settingsRepository.changeCurrentUserName(firstName, secondName)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({}, { Log.e("Change name", "error", it) })
                )
            }
        }

        binding.privacyLine.setOnClickListener {
            // TODO: Navigation
        }

        binding.blacklistLine.setOnClickListener {
            // TODO: Navigation
        }

        binding.changePasswordLine.setOnClickListener {
            val changePasswordFragment = ChangePasswordFragment()
            changePasswordFragment.show(parentFragmentManager, ChangePasswordFragment.TAG)
        }

        binding.exitAccountLine.setOnClickListener {
            val exitAccountFragment = ExitAccountFragment()
            exitAccountFragment.show(parentFragmentManager, ExitAccountFragment.TAG)
        }

        binding.deleteAccountLine.setOnClickListener {
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
        val avatar = fragmentView.findViewById<CircleImageView>(R.id.avatar)
        avatar.setImageURI(uri)
    }
}