package ru.flocator.feature_settings.internal.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import io.reactivex.disposables.CompositeDisposable
import ru.flocator.core.base.fragment.BaseFragment
import ru.flocator.core.dependencies.findDependencies
import ru.flocator.core.exceptions.LostConnectionException
import ru.flocator.core.navigation.NavController
import ru.flocator.core.navigation.findNavController
import ru.flocator.core.section.SettingsSection
import ru.flocator.feature_settings.R
import ru.flocator.feature_settings.databinding.FragmentBlackListBinding
import ru.flocator.feature_settings.internal.ui.adapters.blacklist.BlackListListAdapter
import ru.flocator.feature_settings.internal.data.model.friend.BlackListUser
import ru.flocator.feature_settings.internal.data.model.state.FragmentState
import ru.flocator.feature_settings.internal.core.di.DaggerSettingsComponent
import ru.flocator.feature_settings.internal.core.exceptions.FailedActionException
import ru.flocator.feature_settings.internal.ui.view_models.BlackListViewModel
import javax.inject.Inject

internal class BlackListFragment : BaseFragment(), SettingsSection {
    private var _binding: FragmentBlackListBinding? = null
    private val binding: FragmentBlackListBinding
        get() = _binding!!

    private lateinit var blackListListAdapter: BlackListListAdapter

    @Inject
    internal lateinit var controller: NavController

    private val compositeDisposable = CompositeDisposable()

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory
    private lateinit var blackListViewModel: BlackListViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerSettingsComponent.factory()
            .create(
                findDependencies(),
                findNavController()
            )
            .inject(this)

        blackListViewModel =
            ViewModelProvider(this, factory)[BlackListViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView =
            inflater.inflate(R.layout.fragment_black_list, container, false)

        _binding = FragmentBlackListBinding.bind(fragmentView)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = getString(R.string.black_list)
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(ru.flocator.design.R.drawable.back)
        }
        binding.toolbar.setNavigationOnClickListener {
            controller.back()
        }

        binding.loadingLayout.setOnRetryCallback {
            blackListViewModel.requestBlackListLoading()
        }

        blackListViewModel.requestBlackListLoading()

        blackListListAdapter = BlackListListAdapter(
            blackListViewModel::unblockUser,
            blackListViewModel::loadPhoto
        )

        blackListViewModel.blockedUsersListLiveData.observe(
            viewLifecycleOwner,
            this::processUserInfoList
        )
        blackListViewModel.fragmentStateLiveData.observe(
            viewLifecycleOwner,
            this::onFragmentStateChange
        )
        blackListViewModel.errorLiveData.observe(
            viewLifecycleOwner,
            this::onErrorOccur
        )

        blackListListAdapter = BlackListListAdapter(
            blackListViewModel::unblockUser,
            blackListViewModel::loadPhoto
        )

        binding.blacklistRecyclerView.adapter = blackListListAdapter

        return fragmentView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
        _binding = null
    }

    private fun onErrorOccur(throwable: Throwable?) {
        when (throwable) {
            is FailedActionException -> {
                activity().notifyAboutError(
                    resources.getString(R.string.failed_to_take_action),
                    binding.root
                )
            }
            is LostConnectionException -> {
                activity().notifyAboutError(
                    resources.getString(R.string.connection_error),
                    binding.root
                )
            }
        }
    }

    private fun onFragmentStateChange(state: FragmentState) {
        when (state) {
            FragmentState.LOADING -> {
                binding.blacklistRecyclerView.visibility = View.GONE
                binding.blacklistMsg.visibility = View.GONE
                binding.loadingLayout.load()
            }
            FragmentState.LOADED -> {
                if (blackListViewModel.blockedUsersListLiveData.value!!.isEmpty()) {
                    binding.blacklistRecyclerView.visibility = View.GONE
                    binding.blacklistMsg.visibility = View.VISIBLE
                } else {
                    binding.blacklistRecyclerView.visibility = View.VISIBLE
                    binding.blacklistMsg.visibility = View.GONE
                }
                binding.loadingLayout.hide()
            }
            FragmentState.FAILED -> {
                binding.blacklistRecyclerView.visibility = View.GONE
                binding.blacklistMsg.visibility = View.GONE
                binding.loadingLayout.fail()
            }
        }
    }

    private fun processUserInfoList(userInfoList: List<BlackListUser>?) {
        if (userInfoList == null) {
            return
        }
        if (userInfoList.isEmpty()) {
            binding.blacklistMsg.text =
                getString(R.string.blacklist_is_empty)
        } else {
            blackListListAdapter.setFriendList(
                userInfoList.map {
                    it.copy()
                }
            )
        }
    }
}