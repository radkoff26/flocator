package ru.flocator.feature_settings.internal.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.core_api.api.MainRepository
import ru.flocator.core_sections.SettingsSection
import ru.flocator.feature_settings.R
import ru.flocator.feature_settings.databinding.FragmentBlackListBinding
import ru.flocator.feature_settings.internal.domain.friend.Friend
import ru.flocator.feature_settings.internal.repository.SettingsRepository
import ru.flocator.feature_settings.internal.ui.adapters.FriendListAdapter
import ru.flocator.feature_settings.internal.utils.FriendViewUtils.getNumOfColumns
import javax.inject.Inject

internal class PrivacySettingsFragment : Fragment(), SettingsSection {
    private var _binding: FragmentBlackListBinding? = null
    private val binding: FragmentBlackListBinding
        get() = _binding!!

    private lateinit var friendListAdapter: FriendListAdapter

    @Inject
    lateinit var mainRepository: MainRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val compositeDisposable = CompositeDisposable()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentView =
            inflater.inflate(R.layout.fragment_black_list, container, false)

        _binding = FragmentBlackListBinding.bind(fragmentView)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = getString(R.string.privacy)
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(ru.flocator.core_design.R.drawable.back)
        }
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }


        binding.blacklistRecyclerView.layoutManager = GridLayoutManager(context, getNumOfColumns(context, 120.0f))

        friendListAdapter = FriendListAdapter()
        binding.blacklistRecyclerView.adapter = friendListAdapter

        binding.blacklistUnselectAllFrame.setOnClickListener {
            if (friendListAdapter.all { friend -> friend.isChecked }) {
                friendListAdapter.unselectAll()
            } else {
                friendListAdapter.selectAll()
            }
        }

        compositeDisposable.add(
            mainRepository.restApi.getFriendsOfCurrentUser()
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    Log.e("Getting friends error", it.stackTraceToString(), it)
                }
                .subscribe { friends ->
                    compositeDisposable.add(
                        settingsRepository.getCurrentUserPrivacy()
                            .observeOn(Schedulers.io())
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .doOnError {
                                Log.e("Getting friends privacy error", it.stackTraceToString(), it)
                            }
                            .subscribe { privData ->
                                activity?.runOnUiThread {
                                    if (friends.isEmpty()) {
                                        binding.blacklistMsg.text =
                                            getString(R.string.privacy_no_friends)
                                        binding.blacklistMsg.visibility = View.VISIBLE
                                        return@runOnUiThread
                                    }
                                    binding.blacklistMsg.visibility = View.GONE
                                    friendListAdapter.setFriendList(
                                        friends.map {
                                            Friend(
                                                it.id,
                                                it.avatarUri,
                                                it.firstName + " " + it.lastName,
                                                privData[it.id] == "FIXED"
                                            )
                                        }
                                    )
                                }
                            }
                    )
                }
        )

        compositeDisposable.add(
            friendListAdapter.publisher
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    Log.e("Error getting privacy change info", it.stackTraceToString(), it)
                }
                .subscribe {
                    var newStatus = "PRECISE"
                    if (it.isChecked) {
                        newStatus = "FIXED"
                    }
                    compositeDisposable.add(
                        settingsRepository.changePrivacy(it.userId, newStatus)
                            .observeOn(Schedulers.io())
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .doOnError {
                                Log.e("Changing friend privacy error", it.stackTraceToString(), it)
                            }
                            .subscribe()
                    )
                }
        )

        return fragmentView
    }
}