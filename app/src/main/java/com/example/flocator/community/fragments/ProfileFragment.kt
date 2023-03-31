package com.example.flocator.community.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flocator.R
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
    private lateinit var factoryFriendsViewModel: FriendsViewModelFactory
    private lateinit var friendsViewModel: FriendsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommunityBinding.inflate(inflater, container, false)
        factoryFriendsViewModel = FriendsViewModelFactory(personService)
        friendsViewModel = ViewModelProvider(this, factoryFriendsViewModel)[FriendsViewModel::class.java]
        friendsViewModel.getFriends()
        friendsViewModel.friends.observe(viewLifecycleOwner) {
            adapterForYourFriends = FriendAdapter()
            adapterForYourFriends.data = PersonRepository().getPersons() as MutableList<Person>
            binding.yourFriendsRecyclerView.also {
                it.layoutManager = LinearLayoutManager(activity)
                it.setHasFixedSize(true)
                it.adapter = adapterForYourFriends
            }
            adapter = PersonAdapter(object : PersonActionListener {
                override fun onPersonGetId(person: Person){
                    openPersonProfile(person)
                }
                override fun onPersonCancel(person: Person) = personService.cancelPerson(person)
                override fun onPersonAccept(person: Person) {
                    val findingPerson = personService.acceptPerson(person)
                    adapterForYourFriends.data.add(findingPerson)
                    adapterForYourFriends.notifyDataSetChanged()
                }
            })
            personService.addListener(listener)
            adapter.data = PersonRepository().getPersons()
            binding.newFriendsRecyclerView.also {
                it.layoutManager = LinearLayoutManager(activity)
                it.setHasFixedSize(true)
                it.adapter = adapter
            }
        }

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

        binding.addFriend.setOnClickListener {
            val addFriendByLinkFragment = AddFriendByLinkFragment()
            addFriendByLinkFragment.show(parentFragmentManager, AddFriendByLinkFragment.TAG)
        }
        return binding.root
    }

    fun openPersonProfile(person: Person){
        val args: Bundle = Bundle()
        args.putString("nameAndSurnamePerson", person.nameAndSurname)
        args.putString("personPhoto",person.photo)
        val profilePersonFragment: OtherPersonProfileFragment = OtherPersonProfileFragment()
        profilePersonFragment.arguments = args
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.community_fragment, profilePersonFragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }


}