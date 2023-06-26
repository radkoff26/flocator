package ru.flocator.feature_settings.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import ru.flocator.core_api.api.MainRepository
import ru.flocator.feature_settings.ui.adapters.FriendListAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.serialization.json.Json
import ru.flocator.core_sections.SettingsSection
import ru.flocator.feature_settings.R
import ru.flocator.feature_settings.databinding.FragmentBlackListBinding
import ru.flocator.feature_settings.domain.friend.Friend
import ru.flocator.feature_settings.domain.friend.FriendListSerializer
import ru.flocator.feature_settings.utils.FriendViewUtils.getNumOfColumns
import javax.inject.Inject

class BlackListFragment : Fragment(), SettingsSection {
    private lateinit var friendListAdapter: FriendListAdapter

    @Inject
    lateinit var mainRepository: MainRepository
    private val compositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView =
            inflater.inflate(R.layout.fragment_black_list, container, false)

        val binding = FragmentBlackListBinding.bind(fragmentView)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = getString(R.string.black_list)
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(ru.flocator.core_design.R.drawable.back)
        }
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.blacklistRecyclerView.layoutManager = GridLayoutManager(context, getNumOfColumns(context, 120.0f))


        friendListAdapter = FriendListAdapter()

        if (savedInstanceState == null) {
            compositeDisposable.add(
                mainRepository.restApi.getCurrentUserBlocked()
                    .observeOn(Schedulers.io())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { userInfos ->
                            activity?.runOnUiThread {
                                if (userInfos.isEmpty()) {
                                    binding.blacklistMsg.text = getString(R.string.blacklist_is_empty)
                                    binding.blacklistMsg.visibility = View.VISIBLE
                                    return@runOnUiThread
                                }
                                binding.blacklistMsg.visibility = View.GONE
                                friendListAdapter.setFriendList(
                                    userInfos.map {
                                        Friend(
                                            it.userId,
                                            it.avatarUri,
                                            it.firstName + " " + it.lastName,
                                            true
                                        )
                                    }
                                )
                            }
                        },
                        {
                            Log.e("Getting blacklist error", it.stackTraceToString(), it)
                        }
                    )
            )
        } else {
            friendListAdapter.setFriendList(
                Json.decodeFromString(
                    FriendListSerializer,
                    savedInstanceState.getString(
                        "friends",
                        "[]"
                    )
                )
            )
        }

        binding.blacklistRecyclerView.adapter = friendListAdapter

        binding.blacklistUnselectAllFrame.setOnClickListener {
            if (friendListAdapter.all { friend -> !friend.isChecked }) {
                friendListAdapter.selectAll()
            } else {
                friendListAdapter.unselectAll()
            }
        }
        compositeDisposable.add(
            friendListAdapter.publisher
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe { friend ->
                    if (friend.isChecked) {
                        mainRepository.restApi.blockUser(friend.userId)
                            .observeOn(Schedulers.io())
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .doOnError {
                                Log.e(
                                    "Error adding to blacklist",
                                    it.stackTraceToString(),
                                    it
                                )
                            }
                            .subscribe()
                    } else {
                        mainRepository.restApi.unblockUser(friend.userId)
                            .observeOn(Schedulers.io())
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .doOnError {
                                Log.e(
                                    "Error removing from blacklist",
                                    it.stackTraceToString(),
                                    it
                                )
                            }
                            .subscribe()
                    }
                }
        )
        return fragmentView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (this::friendListAdapter.isInitialized) {
            outState.putString(
                "friends",
                Json.encodeToString(FriendListSerializer, friendListAdapter.getItems())
            )
        }
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        compositeDisposable.dispose()
        super.onDestroyView()
    }
}