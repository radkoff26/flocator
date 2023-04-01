package com.example.flocator.community.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.flocator.R
import com.example.flocator.community.App
import com.example.flocator.community.adapters.FriendActionListener
import com.example.flocator.community.adapters.FriendAdapter
import com.example.flocator.community.adapters.PersonActionListener
import com.example.flocator.community.adapters.PersonAdapter
import com.example.flocator.community.data_classes.Person
import com.example.flocator.databinding.FragmentCommunityBinding
import com.example.flocator.databinding.FragmentPersonProfileBinding

class OtherPersonProfileFragment() : Fragment() {
    private lateinit var binding: FragmentPersonProfileBinding
    private lateinit var adapterFriends: FriendAdapter
    private lateinit var factoryFriendsViewModel: FriendsViewModelFactory
    private lateinit var friendsViewModel: FriendsViewModel
    private val personService: PersonRepository
        get() = (activity?.applicationContext as App).personService
    private var btnAddFriendIsActive = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPersonProfileBinding.inflate(inflater, container, false)
        factoryFriendsViewModel = FriendsViewModelFactory(personService)
        friendsViewModel = ViewModelProvider(this, factoryFriendsViewModel)[FriendsViewModel::class.java]
        val args: Bundle? = arguments
        if (args != null) {
            binding.nameAndSurname.text = args.getString("nameAndSurnamePerson")
        }

        if (args != null) {
            context?.let {
                Glide.with(it).load(args.getString("personPhoto"))
                    .circleCrop()
                    .error(R.drawable.base_avatar_image)
                    .placeholder(R.drawable.base_avatar_image).into(binding.profileImage)
            }
        }
        friendsViewModel.getFriends()
        friendsViewModel.friends.observe(viewLifecycleOwner) {
            adapterFriends = FriendAdapter(object : FriendActionListener {
                override fun onPersonOpenProfile(person: Person){
                    openPersonProfile(person)
                }
            })
            adapterFriends.data = PersonRepository().getPersons() as MutableList<Person>
            binding.yourFriendsRecyclerView.also {
                it.layoutManager = LinearLayoutManager(activity)
                it.setHasFixedSize(true)
                it.adapter = adapterFriends
            }
        }
        binding.buttonBack.setOnClickListener{
            if(parentFragmentManager.backStackEntryCount > 0){
                parentFragmentManager.popBackStack()
            }
        }
        binding.addPersonToFriend.setOnClickListener {
            if(!btnAddFriendIsActive){
                binding.addPersonToFriend.setBackgroundColor(resources.getColor(R.color.button_bg))
                binding.addPersonToFriend.text = "Отменить заявку"
                binding.addPersonToFriend.setTextColor(resources.getColor(R.color.black))
                btnAddFriendIsActive = true
            } else {
                binding.addPersonToFriend.setBackgroundColor(resources.getColor(R.color.tint))
                binding.addPersonToFriend.text = "Добавить в друзья"
                binding.addPersonToFriend.setTextColor(resources.getColor(R.color.white))
                btnAddFriendIsActive = false
            }

        }
        return binding.root
    }
    fun openPersonProfile(person: Person){
        val args: Bundle = Bundle()
        args.putString("nameAndSurnamePerson", person.nameAndSurname)
        args.putString("personPhoto",person.photo)
        val profilePersonFragment: OtherPersonProfileFragment = OtherPersonProfileFragment()
        profilePersonFragment.arguments = args
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.person_profile, profilePersonFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}