package ru.flocator.feature_settings.internal.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.flocator.core.alert.ErrorDebouncingAlertPoller
import ru.flocator.core.dependencies.findDependencies
import ru.flocator.core.exceptions.LostConnectionException
import ru.flocator.core.navigation.NavController
import ru.flocator.core.navigation.findNavController
import ru.flocator.core.section.SettingsSection
import ru.flocator.design.SnackbarComposer
import ru.flocator.feature_settings.R
import ru.flocator.feature_settings.databinding.FragmentPrivacySettingsBinding
import ru.flocator.feature_settings.internal.core.exceptions.FailedActionException
import ru.flocator.feature_settings.internal.data.model.privacy.PrivacyUser
import ru.flocator.feature_settings.internal.data.model.state.FragmentState
import ru.flocator.feature_settings.internal.core.di.DaggerSettingsComponent
import ru.flocator.feature_settings.internal.ui.adapters.privacy.PrivacyListAdapter
import ru.flocator.feature_settings.internal.ui.view_models.PrivacyViewModel
import javax.inject.Inject

internal class PrivacySettingsFragment : Fragment(), SettingsSection {
    private var _binding: FragmentPrivacySettingsBinding? = null
    private val binding: FragmentPrivacySettingsBinding
        get() = _binding!!

    private var _privacyListAdapter: PrivacyListAdapter? = null
    private val privacyListAdapter: PrivacyListAdapter
        get() = _privacyListAdapter!!

    @Inject
    lateinit var controller: NavController

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var privacyViewModel: PrivacyViewModel

    private lateinit var alertPoller: ErrorDebouncingAlertPoller

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerSettingsComponent.factory()
            .create(
                findDependencies(),
                findNavController()
            )
            .inject(this)

        privacyViewModel = ViewModelProvider(this, factory)[PrivacyViewModel::class.java]
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

        alertPoller = ErrorDebouncingAlertPoller(requireActivity()) { view, errorText, callback ->
            SnackbarComposer.composeDesignedSnackbar(view, errorText, callback)
        }

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

    companion object {

        fun newInstance(): PrivacySettingsFragment = PrivacySettingsFragment()
    }
}