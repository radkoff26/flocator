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
import com.example.flocator.databinding.FragmentPrivacySettingsBinding



/**
 * A simple [Fragment] subclass.
 * Use the [PrivacySettings.newInstance] factory method to
 * create an instance of this fragment.
 */
class PrivacySettings : Fragment() {
    private lateinit var binding: FragmentPrivacySettingsBinding
    private lateinit var friendListAdapter: FriendListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    fun getFriend(): ArrayList<Friend> {
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
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        recyclerView.layoutManager = GridLayoutManager(context, 3)

        friendListAdapter = FriendListAdapter(getFriend())
        recyclerView.adapter = friendListAdapter

        selectAllButton.setOnClickListener {
            friendListAdapter.selectAll()
        }

        return fragmentView
    }
}