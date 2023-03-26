package com.example.flocator.community.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flocator.community.App
import com.example.flocator.community.adapters.FriendAdapter
import com.example.flocator.community.adapters.PersonActionListener
import com.example.flocator.community.adapters.PersonAdapter
import com.example.flocator.community.data_classes.Person
import com.example.flocator.databinding.FragmentCommunityBinding

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentCommunityBinding
    private lateinit var adapter: PersonAdapter
    private lateinit var adapterForYourFriends: FriendAdapter
    private val listener: PersonListener = {adapter.data = it}
    private val personService: PersonRepository
        get() = (activity?.applicationContext as App).personService
    private lateinit var friendsViewModel: FriendsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommunityBinding.inflate(inflater, container, false)

        val manager = LinearLayoutManager(activity)

        adapterForYourFriends = FriendAdapter()
        adapterForYourFriends.data = PersonRepository().getPersons()



        adapter = PersonAdapter(object : PersonActionListener {
            override fun onPersonGetId(person: Person) =
                Toast.makeText(activity, "Persons ID: ${person.id}", Toast.LENGTH_SHORT)
                    .show()

            override fun onPersonCancel(person: Person) = personService.cancelPerson(person)
            override fun onPersonAccept(person: Person) = personService.acceptPerson(person,
                adapterForYourFriends.data as MutableList<Person>
            ){
                adapterForYourFriends.notifyDataSetChanged()
            }
        })
        personService.addListener(listener)

        adapter.data = PersonRepository().getPersons()
        binding.newFriendsRecyclerView.layoutManager = manager
        binding.newFriendsRecyclerView.adapter = adapter



        binding.yourFriendsRecyclerView.layoutManager = LinearLayoutManager(activity)
        binding.yourFriendsRecyclerView.adapter = adapterForYourFriends

        binding.buttonViewAll.setOnClickListener {
            adapter.isOpen = true
            binding.buttonViewAll.visibility = View.INVISIBLE
            binding.buttonNotViewAll.visibility = View.VISIBLE
        }

        binding.buttonNotViewAll.setOnClickListener {
            adapter.isOpen = false
            binding.buttonViewAll.visibility = View.VISIBLE
            binding.buttonNotViewAll.visibility = View.INVISIBLE
        }
        if (adapter.data.size <= 2){
            binding.buttonViewAll.visibility = View.INVISIBLE
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}