package ru.flocator.feature_community.api.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import ru.flocator.core.base.fragment.BaseFragment
import ru.flocator.core.base.view_model.UiState
import ru.flocator.core.dependencies.findDependencies
import ru.flocator.core.navigation.NavController
import ru.flocator.core.navigation.findNavController
import ru.flocator.core.photo.PhotoLoadingTool
import ru.flocator.core.section.CommunitySection
import ru.flocator.feature_community.R
import ru.flocator.feature_community.databinding.FragmentCommunityBinding
import ru.flocator.feature_community.internal.adapters.FriendActionListener
import ru.flocator.feature_community.internal.adapters.FriendAdapter
import ru.flocator.feature_community.internal.adapters.PersonAdapter
import ru.flocator.feature_community.internal.adapters.UserNewFriendActionListener
import ru.flocator.feature_community.internal.data.UserItem
import ru.flocator.feature_community.internal.di.DaggerCommunityComponent
import ru.flocator.feature_community.internal.ui.AddFriendByLinkFragment
import ru.flocator.feature_community.internal.ui.ExternalProfileFragment
import ru.flocator.feature_community.internal.view_models.ProfileViewModel
import javax.inject.Inject

class ProfileFragment : BaseFragment(), CommunitySection {
    private var _binding: FragmentCommunityBinding? = null
    private val binding: FragmentCommunityBinding
        get() = _binding!!

    private lateinit var adapterForNewFriends: PersonAdapter
    private lateinit var adapterForYourFriends: FriendAdapter

    private val compositeDisposable = CompositeDisposable()

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory
    internal lateinit var profileViewModel: ProfileViewModel

    @Inject
    internal lateinit var controller: NavController

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerCommunityComponent.builder()
            .communityDependencies(findDependencies())
            .navController(findNavController())
            .build()
            .inject(this)

        profileViewModel =
            ViewModelProvider(this, viewModelFactory)[ProfileViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        profileViewModel.loadData()
        subscribeToUiState()
        adapterForNewFriends = PersonAdapter(object :
            UserNewFriendActionListener {
            override fun onPersonOpenProfile(user: UserItem) {
                openPersonProfile(user)
            }

            override fun onPersonAccept(user: UserItem) {
                checkSizeNewFriendsList(profileViewModel.acceptPerson(user))
            }

            override fun onPersonCancel(user: UserItem) {
                checkSizeNewFriendsList(profileViewModel.rejectPerson(user))
            }
        })
        adapterForYourFriends = FriendAdapter(object : FriendActionListener {
            override fun onPersonOpenProfile(user: UserItem) {
                openPersonProfile(user)
            }
        })

        profileViewModel.friendRequestsLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                adapterForNewFriends.data = it
                val size = adapterForNewFriends.data.size
                if (size != 0) {
                    binding.friendRequests.text =
                        resources.getString(R.string.friend_requests)
                    binding.friendRequests.setTextColor(resources.getColor(ru.flocator.design.R.color.black))
                }
                if (size <= 2) {
                    binding.buttonViewAll.visibility = View.GONE
                    binding.buttonNotViewAll.visibility = View.GONE
                    if (size == 0) {
                        binding.friendRequests.text =
                            resources.getString(R.string.no_new_requests)
                        binding.friendRequests.setTextColor(resources.getColor(ru.flocator.design.R.color.font))
                    }
                }
                if (size > 2) {
                    binding.friendRequests.text =
                        resources.getString(R.string.friend_requests)
                    binding.friendRequests.setTextColor(resources.getColor(ru.flocator.design.R.color.black))
                    binding.buttonViewAll.visibility = View.VISIBLE
                    binding.buttonNotViewAll.visibility = View.GONE
                }
            }
        }

        binding.newFriendsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.newFriendsRecyclerView.adapter = adapterForNewFriends

        binding.yourFriendsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.yourFriendsRecyclerView.adapter = adapterForYourFriends

        if (adapterForNewFriends.getAllCount() <= 2) {
            binding.buttonViewAll.visibility = View.GONE
            binding.buttonNotViewAll.visibility = View.GONE
            if (adapterForNewFriends.getAllCount() == 0) {
                binding.friendRequests.text =
                    resources.getString(R.string.no_new_requests)
                binding.friendRequests.setTextColor(resources.getColor(ru.flocator.design.R.color.font))
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
            lifecycleScope.launch {
                val args = Bundle()
                val addFriendByLinkFragment = AddFriendByLinkFragment()
                addFriendByLinkFragment.arguments = args
                addFriendByLinkFragment.show(
                    requireActivity().supportFragmentManager,
                    AddFriendByLinkFragment.TAG
                )
            }
        }
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(ru.flocator.design.R.drawable.back)
        }
        binding.toolbar.setNavigationOnClickListener {
            controller.back()
        }

        return binding.root
    }

    override fun onDestroyView() {
        compositeDisposable.dispose()
        _binding = null
        super.onDestroyView()
    }

    private fun subscribeToUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.uiState.collect {
                    when (it) {
                        is UiState.Failed -> {
                            activity().notifyAboutError("Failed to load!")
                        }
                        is UiState.Loaded -> {
                            val userProfile = it.data.userProfile
                            val friends = it.data.friends
                            adapterForYourFriends.data = friends.toMutableList()
                            binding.nameAndSurname.text = resources.getString(
                                R.string.name_surname,
                                userProfile.firstName,
                                userProfile.lastName
                            )
                            if (userProfile.avatarUri != null) {
                                setAvatar(userProfile.avatarUri)
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun openPersonProfile(user: UserItem) {
        val args = Bundle()
        user.userId.let { args.putLong("userId", it) }
        val profilePersonFragment = ExternalProfileFragment()
        profilePersonFragment.arguments = args
        controller.toFragment(profilePersonFragment)
    }

    private fun checkSizeNewFriendsList(size: Int) {
        if (size <= 2) {
            binding.buttonViewAll.visibility = View.GONE
            binding.buttonNotViewAll.visibility = View.GONE
            if (size == 0) {
                binding.friendRequests.text =
                    resources.getString(R.string.no_new_requests)
                binding.friendRequests.setTextColor(resources.getColor(ru.flocator.design.R.color.font))
            }
        }
    }

    private fun setAvatar(uri: String) {
        binding.userPhotoSkeleton.showSkeleton()
        compositeDisposable.add(
            PhotoLoadingTool.loadPictureFromUrl(uri, 100)
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

    companion object {
        const val TAG = "Profile Fragment"
    }
}