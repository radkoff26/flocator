package com.example.flocator.community.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flocator.*
import com.example.flocator.community.App
import com.example.flocator.community.adapters.FriendAdapter
import com.example.flocator.community.adapters.PersonActionListener
import com.example.flocator.community.adapters.PersonAdapter
import com.example.flocator.community.data_classes.Person
import com.example.flocator.databinding.FragmentCommunityBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentCommunityBinding
    private lateinit var adapter: PersonAdapter
    private lateinit var adapterForYourFriends: FriendAdapter
    private val personService: PersonService
        get() = (activity?.applicationContext as App).personService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommunityBinding.inflate(inflater, container, false)

        val manager = LinearLayoutManager(activity)
        adapter = PersonAdapter(object : PersonActionListener {
            override fun onPersonGetId(person: Person) = personService.acceptPerson(person)
            override fun onPersonCancel(person: Person) = personService.cancelPerson(person)
            override fun onPersonAccept(person: Person) = personService.acceptPerson(person)
        })
        adapter.data = PersonService().getPersons()

        binding.newFriendsRecyclerView.layoutManager = manager
        binding.newFriendsRecyclerView.adapter = adapter

        adapterForYourFriends = FriendAdapter()
        adapterForYourFriends.data = PersonService().getPersons()

        binding.yourFriendsRecyclerView.layoutManager = LinearLayoutManager(activity)
        binding.yourFriendsRecyclerView.adapter = adapterForYourFriends

        binding.buttonViewAll.setOnClickListener {
            adapter.data = personService.viewAllPersons()
            binding.buttonViewAll.visibility = View.INVISIBLE
            binding.buttonNotViewAll.visibility = View.VISIBLE
        }

        binding.buttonNotViewAll.setOnClickListener {
            adapter.data = personService.rollUpPersons()
            binding.buttonViewAll.visibility = View.VISIBLE
            binding.buttonNotViewAll.visibility = View.INVISIBLE
        }
        return binding.root
    }

}