package com.example.flocator.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flocator.R



class PrivacySettings : Fragment() {
    private lateinit var friendListAdapter: FriendListAdapter

    fun getFriends(): ArrayList<Friend> {
        val ans = ArrayList<Friend>()
        for (i in 1..10000) {
            ans.add(
                Friend(
                    R.drawable.avatar_svgrepo_com,
                    "Тут стоит ник",
                    false
                )
            )
        }
        return ans
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentView = inflater.inflate(R.layout.fragment_privacy_settings, container, false)
        val recyclerView = fragmentView.findViewById<RecyclerView>(R.id.friend_list)
        val backButton = fragmentView.findViewById<FrameLayout>(R.id.back_button)
        val selectAllButton = fragmentView.findViewById<FrameLayout>(R.id.select_all_frame)
        val currentLocation = fragmentView.findViewById<LinearLayout>(R.id.privacy_current_location)
        val approxLocation = fragmentView.findViewById<LinearLayout>(R.id.privacy_approx_location)
        val fixedLocation = fragmentView.findViewById<LinearLayout>(R.id.privacy_fix_location)
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        recyclerView.layoutManager = GridLayoutManager(context, 3)

        friendListAdapter = FriendListAdapter(getFriends())
        recyclerView.adapter = friendListAdapter

        selectAllButton.setOnClickListener {
            friendListAdapter.selectAll()
        }

        currentLocation.setOnClickListener {
            friendListAdapter.unselectAll()
        }

        approxLocation.setOnClickListener {
            friendListAdapter.unselectAll()
        }

        fixedLocation.setOnClickListener {
            friendListAdapter.unselectAll()
        }


        return fragmentView
    }
}