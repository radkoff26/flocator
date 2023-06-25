package ru.flocator.feature_community.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import ru.flocator.core_design.R
import ru.flocator.core_sections.CommunitySection
import ru.flocator.feature_community.adapters.FriendActionListener
import ru.flocator.feature_community.adapters.FriendAdapter
import ru.flocator.feature_community.adapters.PersonAdapter
import ru.flocator.core_dto.user.FriendRequests
import ru.flocator.core_dto.user.Friends
import ru.flocator.core_dto.user.TargetUser
import ru.flocator.feature_community.view_models.ProfileFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.app.databinding.FragmentCommunityBinding
import ru.flocator.feature_community.adapters.UserNewFriendActionListener
import ru.flocator.core_utils.LoadUtils
import ru.flocator.feature_community.fragments.AddFriendByLinkFragment
import ru.flocator.feature_community.fragments.OtherPersonProfileFragment
import java.sql.Timestamp

@AndroidEntryPoint
class ProfileFragment : Fragment(), CommunitySection {
    private var _binding: FragmentCommunityBinding? = null
    private val binding: FragmentCommunityBinding
        get() = _binding!!
    private val profileFragmentViewModel: ProfileFragmentViewModel by viewModels()
    private lateinit var adapterForNewFriends: PersonAdapter
    private lateinit var adapterForYourFriends: FriendAdapter
    private var currentUser: TargetUser = TargetUser(1, "1", "1", "1",false,
        Timestamp(System.currentTimeMillis()),ArrayList<FriendRequests>(),ArrayList<Friends>())
    private val compositeDisposable = CompositeDisposable()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        profileFragmentViewModel.fetchUser()
        adapterForNewFriends = PersonAdapter(object :
            UserNewFriendActionListener {
            override fun onPersonOpenProfile(user: FriendRequests) {
                openPersonProfile(user)
            }

            override fun onPersonAccept(user: FriendRequests) {
                checkSizeNewFriendsList(profileFragmentViewModel.acceptPerson(user))
            }

            override fun onPersonCancel(user: FriendRequests) {
                checkSizeNewFriendsList(profileFragmentViewModel.cancelPerson(user))
            }
        })
        adapterForYourFriends = FriendAdapter(object : FriendActionListener {
            override fun onPersonOpenProfile(user: Friends) {
                openPersonProfile(user)
            }
        })

        profileFragmentViewModel.newFriendsLiveData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                adapterForNewFriends.data = it
                val size = adapterForNewFriends.data.size
                if(size != 0){
                    binding.friendRequests.text = "Заявки в друзья"
                    binding.friendRequests.setTextColor(resources.getColor(R.color.black))
                }
                if(size <= 2){
                    binding.buttonViewAll.visibility = View.GONE
                    binding.buttonNotViewAll.visibility = View.GONE
                    if(size == 0){
                        binding.friendRequests.text = "Новых заявок пока нет!"
                        binding.friendRequests.setTextColor(resources.getColor(R.color.font))
                    }
                }
                if(size > 2){
                    binding.friendRequests.text = "Заявки в друзья"
                    binding.friendRequests.setTextColor(resources.getColor(R.color.black))
                    binding.buttonViewAll.visibility = View.VISIBLE
                    binding.buttonNotViewAll.visibility = View.GONE
                }
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
            if(currentUser.avatarUri != null){
                setAvatar(currentUser.avatarUri!!)
            }
        })



        binding.newFriendsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.newFriendsRecyclerView.adapter = adapterForNewFriends

        binding.yourFriendsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.yourFriendsRecyclerView.adapter = adapterForYourFriends

        if(adapterForNewFriends.getAllCount() <= 2){
            binding.buttonViewAll.visibility = View.GONE
            binding.buttonNotViewAll.visibility = View.GONE
            if(adapterForNewFriends.getAllCount() == 0){
                binding.friendRequests.text = "Новых заявок пока нет!"
                binding.friendRequests.setTextColor(resources.getColor(R.color.font))
            }
        }
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
            val args: Bundle = Bundle()
            args.putLong("currentUserId", profileFragmentViewModel.getCurrentUserId())
            val addFriendByLinkFragment = AddFriendByLinkFragment()
            addFriendByLinkFragment.arguments = args
            addFriendByLinkFragment.show(parentFragmentManager, AddFriendByLinkFragment.TAG)
        }
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(R.drawable.back)
        }
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (parentFragmentManager.backStackEntryCount > 0) {
                        parentFragmentManager.popBackStack()
                    }
                }
            }
        )

        return binding.root
    }

    fun openPersonProfile(user: Friends) {
        val args: Bundle = Bundle()
        args.putLong("currentUserId", profileFragmentViewModel.getCurrentUserId())
        user.userId?.let { args.putLong("userId", it) }
        args.putString("nameAndSurnamePerson", user.firstName + " " + user.lastName)
        args.putString("personPhoto", user.avatarUri)
        val profilePersonFragment = OtherPersonProfileFragment()
        profilePersonFragment.arguments = args
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(ru.flocator.app.R.id.community_fragment, profilePersonFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun openPersonProfile(user: FriendRequests) {
        val args: Bundle = Bundle()
        args.putLong("currentUserId", profileFragmentViewModel.getCurrentUserId())
        user.userId?.let { args.putLong("userId", it) }
        args.putString("nameAndSurnamePerson", user.firstName + " " + user.lastName)
        args.putString("personPhoto", user.avatarUri)
        val profilePersonFragment = OtherPersonProfileFragment()
        profilePersonFragment.arguments = args
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(ru.flocator.app.R.id.community_fragment, profilePersonFragment)
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
        binding.userPhotoSkeleton.showSkeleton()
        compositeDisposable.add(
            LoadUtils.loadPictureFromUrl(uri, 100)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        binding.profileImage.setImageBitmap(it)
                        binding.userPhotoSkeleton.showOriginal()
                    },
                    {
                        Log.d("TestLog", "no")
                    }
                )
        )
    }
    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "Profile Fragment"
    }
}