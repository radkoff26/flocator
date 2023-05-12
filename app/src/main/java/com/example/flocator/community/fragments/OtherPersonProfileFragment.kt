package com.example.flocator.community.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flocator.R
import com.example.flocator.common.repository.MainRepository
import com.example.flocator.common.utils.LoadUtils
import com.example.flocator.community.adapters.ExternalFriendActionListener
import com.example.flocator.community.adapters.ExternalFriendAdapter
import com.example.flocator.community.adapters.FriendActionListener
import com.example.flocator.community.adapters.FriendAdapter
import com.example.flocator.community.data_classes.*
import com.example.flocator.community.view_models.OtherPersonProfileFragmentViewModel
import com.example.flocator.databinding.FragmentPersonProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.sql.Timestamp
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class OtherPersonProfileFragment() : Fragment() {
    private var _binding: FragmentPersonProfileBinding? = null
    private val binding: FragmentPersonProfileBinding
        get() = _binding!!

    @Inject
    lateinit var repository: MainRepository

    private lateinit var otherPersonProfileFragmentViewModel: OtherPersonProfileFragmentViewModel
    private var btnAddFriendIsActive = false
    private lateinit var adapterForFriends: ExternalFriendAdapter
    private var currentUserId by Delegates.notNull<Long>()
    private var currentUser: UserExternal = UserExternal(
        -1, "", "", "", false,
        Timestamp(System.currentTimeMillis()), ArrayList<UserExternalFriends>(), false, false
    )
    private var thisUserId by Delegates.notNull<Long>()

    object Constants {
        const val NAME_AND_SURNAME = "nameAndSurnamePerson"
        const val PERSON_PHOTO = "personPhoto"
        const val USER_ID = "userId"
        const val CURRENT_USER_ID = "currentUserId"
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonProfileBinding.inflate(inflater, container, false)
        otherPersonProfileFragmentViewModel = OtherPersonProfileFragmentViewModel(repository)
        val args: Bundle? = arguments
        if (args != null) {
            currentUserId = args.getLong(Constants.CURRENT_USER_ID)
            otherPersonProfileFragmentViewModel.fetchUser(currentUserId, args.getLong(Constants.USER_ID))
        }
        adapterForFriends = ExternalFriendAdapter(object : ExternalFriendActionListener {
            override fun onPersonOpenProfile(user: UserExternalFriends) {
                openPersonProfile(user)
            }
        })
        otherPersonProfileFragmentViewModel.friendsLiveData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                adapterForFriends.data = it
            }
        })
        binding.friendsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.friendsRecyclerView.adapter = adapterForFriends

        otherPersonProfileFragmentViewModel.currentUserLiveData.observe(viewLifecycleOwner, Observer {
            currentUser = it
            binding.nameAndSurname.text = currentUser.firstName + " " + currentUser.lastName
            currentUser.avatarUri?.let { it1 -> setAvatar(it1) }
            if(currentUser.isFriend == true){
                binding.addPersonToFriend.text = "Удалить из друзей"
                binding.addPersonToFriend.setBackgroundColor(resources.getColor(R.color.button_bg))
                binding.addPersonToFriend.setTextColor(resources.getColor(R.color.black))
            }
            thisUserId = currentUser.userId!!
        })

        binding.buttonBack.setOnClickListener {
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            }
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
        binding.addPersonToFriend.setOnClickListener {
            if (!btnAddFriendIsActive) {
                binding.addPersonToFriend.setBackgroundColor(resources.getColor(R.color.button_bg))
                binding.addPersonToFriend.text = "Отменить заявку"
                binding.addPersonToFriend.setTextColor(resources.getColor(R.color.black))
                btnAddFriendIsActive = true
                otherPersonProfileFragmentViewModel.addOtherUserToFriend(currentUserId, thisUserId)
            } else {
                binding.addPersonToFriend.setBackgroundColor(resources.getColor(R.color.tint))
                binding.addPersonToFriend.text = "Добавить в друзья"
                binding.addPersonToFriend.setTextColor(resources.getColor(R.color.white))
                btnAddFriendIsActive = false
            }

        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun openPersonProfile(user: UserExternalFriends) {
        val args: Bundle = Bundle()
        if ((user.userId?.toLong() ?: 0) == currentUserId) {
            val profileFragment = ProfileFragment()
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.person_profile, profileFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        } else {
            args.putLong(Constants.CURRENT_USER_ID, currentUserId)
            user.userId?.toLong()?.let { args.putLong(Constants.USER_ID, it) }
            args.putString(Constants.NAME_AND_SURNAME, user.firstName + " " + user.lastName)
            args.putString(Constants.PERSON_PHOTO, user.avatarUri)
            val profilePersonFragment: OtherPersonProfileFragment = OtherPersonProfileFragment()
            profilePersonFragment.arguments = args
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.person_profile, profilePersonFragment)
            transaction.addToBackStack(null)
            transaction.commit()
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

}