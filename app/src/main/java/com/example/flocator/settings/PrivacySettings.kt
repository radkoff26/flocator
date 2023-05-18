package com.example.flocator.settings

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flocator.R
import com.example.flocator.common.repository.MainRepository
import com.example.flocator.settings.FriendViewUtilities.getNumOfColumns
import com.example.flocator.settings.data_models.PrivacyStates
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@AndroidEntryPoint
class PrivacySettings : Fragment(), SettingsSection {
    private lateinit var friendListAdapter: FriendListAdapter
    @Inject
    lateinit var mainRepository: MainRepository
    private val compositeDisposable = CompositeDisposable()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentView = inflater.inflate(R.layout.fragment_black_list, container, false)
        val recyclerView = fragmentView.findViewById<RecyclerView>(R.id.blacklist_recycler_view)
        val backButton = fragmentView.findViewById<FrameLayout>(R.id.blacklist_back_button)
        val selectAllButton = fragmentView.findViewById<FrameLayout>(R.id.blacklist_unselect_all_frame)

        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        recyclerView.layoutManager = GridLayoutManager(context, getNumOfColumns(context, 120.0f))

//        friendListAdapter = FriendListAdapter(getFriends())
//        recyclerView.adapter = friendListAdapter

        selectAllButton.setOnClickListener {
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
                .subscribe {
                        friends ->
                    compositeDisposable.add(
                        mainRepository.restApi.getCurrentUserPrivacy()
                            .observeOn(Schedulers.io())
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .doOnError {
                                Log.e("Getting friends privacy error", it.stackTraceToString(), it)
                            }
                            .subscribe {
                                privData ->
                                activity?.runOnUiThread {
                                    friendListAdapter.setFriendList(
                                        friends.map {
                                            Friend(
                                                it.id,
                                                it.avatarUrl,
                                                it.firstName + " " + it.lastName,
                                                privData[it.id] == PrivacyStates.FIXED
                                            )
                                        }
                                    )
                                }
                            }
                    )
                }
        )




        return fragmentView
    }
}