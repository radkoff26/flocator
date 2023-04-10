package com.example.flocator.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flocator.R

class BlackListFragment : Fragment(), SettingsSection {
    private lateinit var friendListAdapter: FriendListAdapter

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

        recyclerView.layoutManager = GridLayoutManager(context, 3)

        friendListAdapter = FriendListAdapter(getFriends())

        recyclerView.adapter = friendListAdapter

        unselectAllButton.setOnClickListener {
            if (friendListAdapter.all { friend -> !friend.isChecked }) {
                friendListAdapter.selectAll()
            } else {
                friendListAdapter.unselectAll()
            }
        }
        return fragmentView
    }

    fun getFriends(): ArrayList<Friend> {
        val ans = ArrayList<Friend>()
        for (i in 1..10000) {
            ans.add(
                Friend(
                    R.drawable.avatar_svgrepo_com,
                    "Тут стоит ник",
                    true
                )
            )
        }
        return ans
    }

}