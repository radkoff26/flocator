package com.example.flocator.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flocator.R
import com.example.flocator.settings.FriendViewUtilities.getNumOfColumns


class PrivacySettings : Fragment(), SettingsSection {
    private lateinit var friendListAdapter: FriendListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentView = inflater.inflate(R.layout.fragment_privacy_settings, container, false)
        val recyclerView = fragmentView.findViewById<RecyclerView>(R.id.friend_list)
        val backButton = fragmentView.findViewById<FrameLayout>(R.id.privacy_back_button)
        val selectAllButton = fragmentView.findViewById<TextView>(R.id.privacy_select_all)
        val currentLocation = fragmentView.findViewById<FrameLayout>(R.id.current_loc_img_frame)
        val approxLocation = fragmentView.findViewById<FrameLayout>(R.id.approx_loc_img_frame)
        val fixedLocation = fragmentView.findViewById<FrameLayout>(R.id.no_loc_img_frame)
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