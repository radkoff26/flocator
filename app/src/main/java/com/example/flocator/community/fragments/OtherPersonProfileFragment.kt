package com.example.flocator.community.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.flocator.Application
import com.example.flocator.R
import com.example.flocator.community.adapters.FriendActionListener
import com.example.flocator.community.adapters.FriendAdapter
import com.example.flocator.community.data_classes.Person
import com.example.flocator.community.data_classes.User
import com.example.flocator.databinding.FragmentPersonProfileBinding

class OtherPersonProfileFragment() : Fragment() {
    private var _binding: FragmentPersonProfileBinding? = null
    private val binding: FragmentPersonProfileBinding
        get() = _binding!!
    private lateinit var adapterFriends: FriendAdapter
    private val userRepository: UserRepository
        get() = (activity?.applicationContext as Application).userRepository
    private var btnAddFriendIsActive = false

    object Constants {
        const val NAME_AND_SURNAME = "nameAndSurnamePerson"
        const val PERSON_PHOTO = "personPhoto"
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonProfileBinding.inflate(inflater, container, false)

        val args: Bundle? = arguments
        if (args != null) {
            binding.nameAndSurname.text = args.getString(Constants.NAME_AND_SURNAME)
        }

        if (args != null) {
            context?.let {
                Glide.with(it).load(args.getString(Constants.PERSON_PHOTO))
                    .circleCrop()
                    .error(R.drawable.base_avatar_image)
                    .placeholder(R.drawable.base_avatar_image).into(binding.profileImage)
            }
        }
        binding.buttonBack.setOnClickListener {
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            }
        }
        binding.addPersonToFriend.setOnClickListener {
            if (!btnAddFriendIsActive) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun openPersonProfile(user: User) {
        val args: Bundle = Bundle()
        args.putString("nameAndSurnamePerson", user.firstName + " " + user.lastName)
        args.putString("personPhoto", user.avatarUrl)
        val profilePersonFragment: OtherPersonProfileFragment = OtherPersonProfileFragment()
        profilePersonFragment.arguments = args
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.person_profile, profilePersonFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}