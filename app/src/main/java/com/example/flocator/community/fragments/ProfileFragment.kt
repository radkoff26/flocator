package com.example.flocator.community.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.flocator.Application
import com.example.flocator.R
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.example.flocator.community.adapters.FriendActionListener
import com.example.flocator.community.adapters.FriendAdapter
import com.example.flocator.community.adapters.PersonAdapter
import com.example.flocator.community.api.UserApi
import com.example.flocator.community.data_classes.Person
import com.example.flocator.community.data_classes.User
import com.example.flocator.community.view_models.ProfileFragmentViewModel
import com.example.flocator.databinding.FragmentCommunityBinding
import com.example.flocator.main.ui.main.MainFragment
import com.example.flocator.main.utils.LoadUtils
import com.google.gson.GsonBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class ProfileFragment : Fragment() {
    private var _binding: FragmentCommunityBinding? = null
    private val binding: FragmentCommunityBinding
        get() = _binding!!
    private val profileFragmentViewModel = ProfileFragmentViewModel()
    private lateinit var adapterForNewFriends: PersonAdapter
    private lateinit var adapterForYourFriends: FriendAdapter
    private val listenerNewFriends: UserNewFriendActionListener = { adapterForNewFriends.data = it }
    private val listenerFriends: FriendListener = {
        adapterForYourFriends.data =
            it as MutableList<User>
    }
    private var currentUser: User = User(1, "1", "1", "1")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        profileFragmentViewModel.fetchUser()
        profileFragmentViewModel.fetchFriends()
        profileFragmentViewModel.fetchNewFriends()
        //profileFragmentViewModel.load()
        adapterForNewFriends = PersonAdapter(object :
            com.example.flocator.community.adapters.UserNewFriendActionListener {
            override fun onPersonOpenProfile(user: User) {
                onPersonOpenProfile(user)
            }

            override fun onPersonAccept(user: User) {
                checkSizeNewFriendsList(profileFragmentViewModel.acceptPerson(user))
            }

            override fun onPersonCancel(user: User) {
                checkSizeNewFriendsList(profileFragmentViewModel.cancelPerson(user))
            }
        })
        adapterForYourFriends = FriendAdapter(object : FriendActionListener {
            override fun onPersonOpenProfile(user: User) {
                openPersonProfile(user)
            }
        })

        profileFragmentViewModel.newFriendsLiveData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                adapterForNewFriends.data = it
            }
        })
        profileFragmentViewModel.friendsLiveData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                adapterForYourFriends.data = it
            }
        })
        profileFragmentViewModel.currentUserLiveData.observe(viewLifecycleOwner, Observer {
            currentUser = it
            binding.nameAndSurname.text = currentUser.firstName + " " + currentUser.lastName
            setAvatar(currentUser.avatarUrl!!)
        })


        binding.newFriendsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.newFriendsRecyclerView.adapter = adapterForNewFriends

        binding.yourFriendsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.yourFriendsRecyclerView.adapter = adapterForYourFriends


        binding.buttonViewAll.setOnClickListener {
            adapterForNewFriends.isOpen = true
            binding.buttonViewAll.visibility = View.INVISIBLE
            binding.buttonNotViewAll.visibility = View.VISIBLE
        }

        binding.buttonNotViewAll.setOnClickListener {
            adapterForNewFriends.isOpen = false
            binding.buttonViewAll.visibility = View.VISIBLE
            binding.buttonNotViewAll.visibility = View.INVISIBLE
        }

        binding.addFriend.setOnClickListener {
            val addFriendByLinkFragment = AddFriendByLinkFragment()
            addFriendByLinkFragment.show(parentFragmentManager, AddFriendByLinkFragment.TAG)
        }
        binding.buttonBack.setOnClickListener {
            FragmentNavigationUtils.openFragment(
                requireActivity().supportFragmentManager,
                MainFragment()
            )
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    FragmentNavigationUtils.openFragment(
                        requireActivity().supportFragmentManager,
                        MainFragment()
                    )
                }
            }
        )
        return binding.root
    }

    fun openPersonProfile(user: User) {
        val args: Bundle = Bundle()
        args.putString("nameAndSurnamePerson", user.firstName + " " + user.lastName)
        args.putString("personPhoto", user.avatarUrl)
        val profilePersonFragment: OtherPersonProfileFragment = OtherPersonProfileFragment()
        profilePersonFragment.arguments = args
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.community_fragment, profilePersonFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun checkSizeNewFriendsList(size: Int) {
        if (size <= 2) {
            binding.buttonViewAll.visibility = View.GONE
            binding.buttonNotViewAll.visibility = View.GONE
            if (size == 0) {
                binding.friendRequests.text = "Новых заявок пока нет!"
                binding.friendRequests.setTextColor(resources.getColor(R.color.font))
            }
        }
    }

    private fun setAvatar(uri: String) {
        LoadUtils.loadPictureFromUrl(uri, 100)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    binding.profileImage.setImageBitmap(it)
                },
                {
                    Log.d("TestLog", "no")
                }
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