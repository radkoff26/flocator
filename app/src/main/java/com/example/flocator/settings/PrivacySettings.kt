package com.example.flocator.settings

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flocator.R
import com.example.flocator.common.repository.MainRepository
import com.example.flocator.settings.FriendViewUtilities.getNumOfColumns
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
        val selectAllButton = fragmentView.findViewById<FrameLayout>(R.id.blacklist_unselect_all_frame)
        val toolbar = fragmentView.findViewById<Toolbar>(R.id.toolbar)
        val message = fragmentView.findViewById<TextView>(R.id.blacklist_msg)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = getString(R.string.privacy)
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(R.drawable.back)
        }
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }


        recyclerView.layoutManager = GridLayoutManager(context, getNumOfColumns(context, 120.0f))

        friendListAdapter = FriendListAdapter()
        recyclerView.adapter = friendListAdapter

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
                                    if (friends.isEmpty()) {
                                        message.text = getString(R.string.privacy_no_friends)
                                        message.visibility = View.VISIBLE
                                        return@runOnUiThread
                                    }
                                    message.visibility = View.GONE
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
                        mainRepository.restApi.changePrivacy(it.userId, newStatus)
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