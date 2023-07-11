package ru.flocator.feature_settings.api.ui

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.flocator.cache.storage.SettingsStorage
import ru.flocator.cache.storage.domain.Language
import ru.flocator.core_api.api.AppRepository
import ru.flocator.core_controller.NavController
import ru.flocator.core_controller.findNavController
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.core_dependency.findDependencies
import ru.flocator.core_extensions.findDrawable
import ru.flocator.core_sections.SettingsSection
import ru.flocator.feature_settings.R
import ru.flocator.feature_settings.databinding.FragmentSettingsBinding
import ru.flocator.feature_settings.internal.di.DaggerSettingsComponent
import ru.flocator.feature_settings.internal.repository.SettingsRepository
import ru.flocator.feature_settings.internal.ui.*
import ru.flocator.feature_settings.internal.view_models.SettingsFragmentViewModel
import java.sql.Timestamp
import java.util.*
import javax.inject.Inject

class SettingsFragment : Fragment(), SettingsSection {
    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding
        get() = _binding!!

    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var appRepository: AppRepository

    @Inject
    lateinit var controller: NavController

    @Inject
    internal lateinit var settingsRepository: SettingsRepository

    @Inject
    internal lateinit var settingsStorage: SettingsStorage

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var settingsViewModel: SettingsFragmentViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerSettingsComponent.factory()
            .create(
                findDependencies(),
                findNavController()
            )
            .inject(this)

        settingsViewModel =
            ViewModelProvider(this, viewModelFactory)[SettingsFragmentViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val photoChangeLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
                if (result != null) {
                    compositeDisposable.add(
                        io.reactivex.Completable.create { emitter ->
                            val stream = context?.contentResolver?.openInputStream(result)!!
                            compositeDisposable.add(
                                settingsRepository.changeCurrentUserAvatar(
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

        selectCurrentLanguage()

        binding.ru.setOnClickListener {
            setLanguageAndRefreshIfNecessary(Language.RU)
        }

        binding.en.setOnClickListener {
            setLanguageAndRefreshIfNecessary(Language.EN)
        }

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(ru.flocator.core_design.R.drawable.back)
        }

        binding.toolbar.setNavigationOnClickListener {
            controller.back()
        }

        drawUserInfo(null)
        drawBlacklistCount(null)

        settingsViewModel.requestUserInfoData()
        settingsViewModel.requestBlacklistCount()

        settingsViewModel.userInfoLiveData.observe(viewLifecycleOwner, this::drawUserInfo)
        settingsViewModel.blackListCountLiveData.observe(
            viewLifecycleOwner,
            this::drawBlacklistCount
        )
        settingsViewModel.userAvatarLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                updateUserAvatar(it)
            }
        }

        binding.avatar.setOnClickListener {
            photoChangeLauncher.launch("image/*")
        }

        binding.dateOfBirthLine.setOnClickListener {
            pickBirthDateAndUpdate()
        }

//        binding.nameField.setOnFocusChangeListener { _, hasFocus ->
//            if (!hasFocus) {
//                val words = binding.nameField.text.split(" ")
//                val firstName = words[0]
//                var secondName = ""
//                for (word in words.listIterator(1)) {
//                    secondName += word
//                }
//                compositeDisposable.add(
//                    settingsRepository.changeCurrentUserName(firstName, secondName)
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe({}, { Log.e("Change name", "error", it) })
//                )
//            }
//        }

        binding.privacyLine.setOnClickListener {
            controller.toFragment(PrivacySettingsFragment())
        }

        binding.blacklistLine.setOnClickListener {
            controller.toFragment(BlackListFragment())
        }

        binding.changePasswordLine.setOnClickListener {
            val changePasswordFragment = ChangePasswordFragment()
            changePasswordFragment.show(
                requireActivity().supportFragmentManager,
                ChangePasswordFragment.TAG
            )
        }

        binding.exitAccountLine.setOnClickListener {
            val exitAccountFragment = ExitAccountFragment()
            exitAccountFragment.show(
                requireActivity().supportFragmentManager,
                ExitAccountFragment.TAG
            )
        }

        binding.deleteAccountLine.setOnClickListener {
            val deleteAccountFragment = DeleteAccountFragment()
            deleteAccountFragment.show(
                requireActivity().supportFragmentManager,
                DeleteAccountFragment.TAG
            )
        }
        return fragmentView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
        _binding = null
    }

    private fun updateUserAvatar(bitmap: Bitmap) {
        binding.avatar.setImageBitmap(bitmap)
        binding.avatarSkeleton.showOriginal()
    }

    private fun drawBlacklistCount(count: Int?) {
        if (count == null) {
            binding.blacklistCntSkeleton.showSkeleton()
        } else {
            binding.blacklistCnt.text = count.toString()
            binding.blacklistCntSkeleton.showOriginal()
        }
    }

    private fun drawUserInfo(userInfo: UserInfo?) {
        if (userInfo == null) {
            binding.dateOfBirthFieldSkeleton.showSkeleton()
            binding.fullNameLayoutSkeleton.showSkeleton()
            binding.avatarSkeleton.showSkeleton()
        } else {
            // Setting birth date
            val cal = Calendar.getInstance()
            if (userInfo.birthDate != null) {
                cal.timeInMillis = userInfo.birthDate!!.time
                binding.dateOfBirthField.text = getString(
                    R.string.date_with_placeholders,
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.YEAR),
                )
            } else {
                binding.dateOfBirthField.text = "-"
            }
            binding.dateOfBirthFieldSkeleton.showOriginal()
            // Setting name
            binding.fullName.text =
                resources.getString(R.string.name_surname, userInfo.firstName, userInfo.lastName)
            binding.fullNameLayoutSkeleton.showOriginal()
            // Setting avatar
            val avatarUri = userInfo.avatarUri
            if (avatarUri != null) {
                settingsViewModel.loadUserAvatar(avatarUri)
            } else {
                binding.avatar.setImageDrawable(
                    resources.findDrawable(ru.flocator.core_design.R.drawable.base_avatar_image)
                )
                binding.avatarSkeleton.showOriginal()
            }
        }
    }

    private fun pickBirthDateAndUpdate() {
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

    private fun setLanguageAndRefreshIfNecessary(language: Language) {
        val prevLanguage = settingsStorage.getLanguage()
        if (prevLanguage == language) {
            return
        }
        settingsStorage.setLanguage(language)
        requireActivity().recreate()
    }

    private fun selectCurrentLanguage() {
        val currentLanguage = settingsStorage.getLanguage()
        if (currentLanguage == null || currentLanguage == Language.EN) {
            binding.en.isChecked = true
        } else {
            binding.ru.isChecked = true
        }
    }
}