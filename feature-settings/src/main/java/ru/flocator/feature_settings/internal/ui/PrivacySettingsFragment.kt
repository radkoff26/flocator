package ru.flocator.feature_settings.internal.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.flocator.core_alert.ErrorDebouncingAlertPoller
import ru.flocator.core_controller.NavController
import ru.flocator.core_controller.findNavController
import ru.flocator.core_dependency.findDependencies
import ru.flocator.core_exceptions.LostConnectionException
import ru.flocator.core_sections.SettingsSection
import ru.flocator.feature_settings.R
import ru.flocator.feature_settings.databinding.FragmentPrivacySettingsBinding
import ru.flocator.feature_settings.internal.adapters.privacy.PrivacyListAdapter
import ru.flocator.feature_settings.internal.di.DaggerSettingsComponent
import ru.flocator.feature_settings.internal.domain.privacy.PrivacyUser
import ru.flocator.feature_settings.internal.domain.state.FragmentState
import ru.flocator.feature_settings.internal.exceptions.FailedActionException
import ru.flocator.feature_settings.internal.repository.SettingsRepository
import ru.flocator.feature_settings.internal.view_models.PrivacyFragmentViewModel
import javax.inject.Inject

internal class PrivacySettingsFragment : Fragment(), SettingsSection {
    private var _binding: FragmentPrivacySettingsBinding? = null
    private val binding: FragmentPrivacySettingsBinding
        get() = _binding!!

    private var _privacyListAdapter: PrivacyListAdapter? = null
    private val privacyListAdapter: PrivacyListAdapter
        get() = _privacyListAdapter!!

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var controller: NavController

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var privacyViewModel: PrivacyFragmentViewModel

    private lateinit var alertPoller: ErrorDebouncingAlertPoller

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerSettingsComponent.factory()
            .create(
                findDependencies(),
                findNavController()
            )
            .inject(this)

        privacyViewModel = ViewModelProvider(this, factory)[PrivacyFragmentViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentView =
            inflater.inflate(R.layout.fragment_privacy_settings, container, false)

        _binding = FragmentPrivacySettingsBinding.bind(fragmentView)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = getString(R.string.privacy)
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(R.drawable.back_white)
        }
        binding.toolbar.setNavigationOnClickListener {
            controller.back()
        }

        binding.privacySelectAll.setOnClickListener {
            privacyViewModel.selectOrUnselectAll()
        }

        binding.loadingLayout.setOnRetryCallback {
            privacyViewModel.loadUserFriendsWithPrivacy()
        }

        _privacyListAdapter = PrivacyListAdapter(
            privacyViewModel::toggleUserPrivacy,
            privacyViewModel::loadPhoto
        )

        binding.privacyRecyclerView.adapter = privacyListAdapter

        alertPoller = ErrorDebouncingAlertPoller(requireActivity())

        privacyViewModel.loadUserFriendsWithPrivacy()

        privacyViewModel.fragmentStateLiveData.observe(
            viewLifecycleOwner,
            this::onFragmentStateChanged
        )

        privacyViewModel.errorLiveData.observe(
            viewLifecycleOwner,
            this::onErrorOccur
        )

        privacyViewModel.privacyListLiveData.observe(
            viewLifecycleOwner,
            this::onReceivePrivacyList
        )

        return fragmentView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _privacyListAdapter = null
    }

    private fun onErrorOccur(throwable: Throwable?) {
        when (throwable) {
            is FailedActionException -> {
                alertPoller.postError(
                    binding.root,
                    resources.getString(R.string.failed_to_take_action)
                )
            }
            is LostConnectionException -> {
                alertPoller.postError(
                    binding.root,
                    resources.getString(R.string.connection_error)
                )
            }
        }
    }

    private fun onFragmentStateChanged(state: FragmentState) {
        when (state) {
            FragmentState.LOADING -> {
                binding.privacyRecyclerView.visibility = View.GONE
                binding.privacyMsg.visibility = View.GONE
                binding.privacySelectAll.visibility = View.GONE
                binding.loadingLayout.visibility = View.VISIBLE
                binding.loadingLayout.load()
            }
            FragmentState.LOADED -> {
                val list = privacyViewModel.privacyListLiveData.value!!
                if (list.isNotEmpty()) {
                    binding.privacyRecyclerView.visibility = View.VISIBLE
                    binding.privacySelectAll.visibility = View.VISIBLE
                    binding.privacyMsg.visibility = View.GONE
                } else {
                    binding.privacyRecyclerView.visibility = View.GONE
                    binding.privacySelectAll.visibility = View.GONE
                    binding.privacyMsg.visibility = View.VISIBLE
                }
                binding.loadingLayout.hide()
            }
            FragmentState.FAILED -> {
                binding.privacyRecyclerView.visibility = View.GONE
                binding.privacyMsg.visibility = View.GONE
                binding.privacySelectAll.visibility = View.GONE
                binding.loadingLayout.visibility = View.VISIBLE
                binding.loadingLayout.fail()
            }
        }
    }

    private fun onReceivePrivacyList(list: List<PrivacyUser>?) {
        if (list == null) {
            return
        }
        if (list.isEmpty()) {
            binding.privacyMsg.text =
                getString(R.string.privacy_no_friends)
            binding.privacyMsg.visibility = View.VISIBLE
            binding.privacyRecyclerView.visibility = View.GONE
        } else {
            binding.privacyMsg.visibility = View.GONE
            binding.privacyRecyclerView.visibility = View.VISIBLE
            privacyListAdapter.setFriendList(
                list.map {
                    it.copy()
                }
            )
        }
    }
}