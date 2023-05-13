package com.example.flocator.settings

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flocator.R
import com.example.flocator.common.repository.MainRepository
import com.example.flocator.settings.FriendViewUtilities.getNumOfColumns
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.serialization.json.Json
import org.json.JSONArray
import javax.inject.Inject

@AndroidEntryPoint
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
        val fragmentView = inflater.inflate(R.layout.fragment_black_list, container, false)
        val recyclerView = fragmentView.findViewById<RecyclerView>(R.id.blacklist_recycler_view)
        val backButton = fragmentView.findViewById<FrameLayout>(R.id.blacklist_back_button)
        val unselectAllButton = fragmentView.findViewById<FrameLayout>(R.id.blacklist_unselect_all_frame)

        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        recyclerView.layoutManager = GridLayoutManager(context, getNumOfColumns(context, 120.0f))

        friendListAdapter = FriendListAdapter()

        if (savedInstanceState == null) {
            compositeDisposable.add(
                mainRepository.restApi.getCurrentUserBlocked()
                    .observeOn(Schedulers.io())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { userInfos ->
                            activity?.runOnUiThread {
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

        recyclerView.adapter = friendListAdapter

        unselectAllButton.setOnClickListener {
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
                            .doOnError { Log.e("Error adding to blacklist", it.stackTraceToString(), it) }
                            .subscribe()
                    } else {
                        mainRepository.restApi.unblockUser(friend.userId)
                            .observeOn(Schedulers.io())
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .doOnError { Log.e("Error removing from blacklist", it.stackTraceToString(), it) }
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
}