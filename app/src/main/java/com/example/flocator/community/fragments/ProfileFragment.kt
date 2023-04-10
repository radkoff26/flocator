package com.example.flocator.community.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.flocator.Application
import com.example.flocator.R
import com.example.flocator.community.CommunitySection
import com.example.flocator.community.adapters.FriendActionListener
import com.example.flocator.community.adapters.FriendAdapter
import com.example.flocator.community.adapters.PersonActionListener
import com.example.flocator.community.adapters.PersonAdapter
import com.example.flocator.community.api.UserApi
import com.example.flocator.community.data_classes.Person
import com.example.flocator.community.data_classes.User
import com.example.flocator.databinding.FragmentCommunityBinding
import com.example.flocator.main.ui.view_models.MainFragmentViewModel
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.google.gson.GsonBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class ProfileFragment : Fragment(), CommunitySection {
    private var _binding: FragmentCommunityBinding? = null
    private val binding: FragmentCommunityBinding
        get() = _binding!!
    private lateinit var adapter: PersonAdapter
    private lateinit var adapterForYourFriends: FriendAdapter
    private val listenerNewFriends: PersonListener = { adapter.data = it }
    private val listenerFriends: FriendListener = {
        adapterForYourFriends.data =
            it as MutableList<Person>
    }
    private val personService: PersonRepository
        get() = (activity?.applicationContext as Application).personService
    private lateinit var factoryFriendsViewModel: FriendsViewModelFactory
    private lateinit var friendsViewModel: FriendsViewModel
    private lateinit var factoryNewFriendsViewModel: FriendsViewModelFactory
    private lateinit var newFriendsViewModel: FriendsViewModel
    private val userApi: UserApi by lazy {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://kernelpunik.ru:8080/api/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        retrofit.create()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: ${requireActivity().supportFragmentManager.fragments}")
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        factoryFriendsViewModel = FriendsViewModelFactory(personService)
        friendsViewModel = ViewModelProvider(this, factoryFriendsViewModel)[FriendsViewModel::class.java]
        friendsViewModel.getFriends()
        friendsViewModel.friends.observe(viewLifecycleOwner) {
            adapterForYourFriends = FriendAdapter(object : FriendActionListener {
                override fun onPersonOpenProfile(person: Person) {
                    openPersonProfile(person)
                }
            })
            adapterForYourFriends.data = PersonRepository().getPersons() as MutableList<Person>
            binding.yourFriendsRecyclerView.also {
                it.layoutManager = LinearLayoutManager(activity)
                it.setHasFixedSize(true)
                it.adapter = adapterForYourFriends
            }
            adapter = PersonAdapter(object : PersonActionListener {
                override fun onPersonOpenProfile(person: Person){
                    openPersonProfile(person)
                }
                override fun onPersonCancel(person: Person) = personService.cancelPerson(person)
                override fun onPersonAccept(person: Person) {
                    val findingPerson = personService.acceptPerson(person)
                    adapterForYourFriends.data.add(findingPerson)
                    adapterForYourFriends.notifyDataSetChanged()
                }
            })
            personService.addListener(listenerNewFriends)
            adapter.data = PersonRepository().getPersons()
            binding.newFriendsRecyclerView.also {
                it.layoutManager = LinearLayoutManager(activity)
                it.setHasFixedSize(true)
                it.adapter = adapter
            }
        }
        /*factoryFriendsViewModel = FriendsViewModelFactory(personService)
        friendsViewModel =
            ViewModelProvider(this, factoryFriendsViewModel)[FriendsViewModel::class.java]
        friendsViewModel.getFriends()
        friendsViewModel.friends.observe(viewLifecycleOwner) {
            adapterForYourFriends = FriendAdapter(object : FriendActionListener {
                override fun onPersonOpenProfile(person: Person) {
                    openPersonProfile(person)
                }
            })
            personService.addListener(listenerFriends)
            //adapterForYourFriends.data = friendsViewModel.friends.value as MutableList<Person>
            adapterForYourFriends.data = PersonRepository().getPersons() as MutableList<Person>
            binding.yourFriendsRecyclerView.also {
                it.layoutManager = LinearLayoutManager(activity)
                it.setHasFixedSize(true)
                it.adapter = adapterForYourFriends
            }
        }

        factoryNewFriendsViewModel = FriendsViewModelFactory(personService)
        newFriendsViewModel =
            ViewModelProvider(this, factoryFriendsViewModel)[FriendsViewModel::class.java]
        newFriendsViewModel.getFriends()
        newFriendsViewModel.friends.observe(viewLifecycleOwner){
            adapter = PersonAdapter(object : PersonActionListener {
                override fun onPersonOpenProfile(person: Person) {
                    openPersonProfile(person)
                }

                override fun onPersonCancel(person: Person) = personService.cancelPerson(person)
                override fun onPersonAccept(person: Person) {
                    val findingPerson = personService.acceptPerson(person)
                    adapterForYourFriends.data.add(findingPerson)
                    //adapterForYourFriends.notifyDataSetChanged()
                }
            })
            personService.addListener(listenerNewFriends)
            //adapter.data = friendsViewModel.friends.value as MutableList<Person>
            adapter.data = PersonRepository().getPersons() as MutableList<Person>
            binding.newFriendsRecyclerView.also {
                it.layoutManager = LinearLayoutManager(activity)
                it.setHasFixedSize(true)
                it.adapter = adapter
            }
        }*/


        userApi.getUser(4)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    binding.nameAndSurname.text = it.firstName + " " + it.lastName
                    context?.let { it1 ->
                        Glide.with(it1).load(it.avatarUrl)
                            .circleCrop()
                            .error(R.drawable.base_avatar_image)
                            .placeholder(R.drawable.base_avatar_image).into(binding.profileImage)
                    }
                },
                {
                    Log.e(TAG, it.message, it)
                })


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
            addFriendByLinkFragment.show(requireActivity().supportFragmentManager, AddFriendByLinkFragment.TAG)
        }
        binding.buttonBack.setOnClickListener {
            FragmentNavigationUtils.closeLastFragment(
                requireActivity().supportFragmentManager,
                requireActivity()
            )
        }
        return binding.root
    }

    fun openPersonProfile(person: Person) {
        val args: Bundle = Bundle()
        args.putString("nameAndSurnamePerson", person.nameAndSurname)
        args.putString("personPhoto", person.photo)
        val profilePersonFragment: OtherPersonProfileFragment = OtherPersonProfileFragment()
        profilePersonFragment.arguments = args
        FragmentNavigationUtils.openFragment(
            requireActivity().supportFragmentManager,
            profilePersonFragment
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "Profile Fragment"
    }

}