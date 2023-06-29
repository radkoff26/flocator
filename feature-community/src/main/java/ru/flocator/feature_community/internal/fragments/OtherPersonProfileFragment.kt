package ru.flocator.feature_community.internal.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.flocator.app.databinding.FragmentPersonProfileBinding
import ru.flocator.core_controller.NavController
import ru.flocator.core_design.R
import ru.flocator.core_dto.user.UserExternal
import ru.flocator.core_dto.user.UserExternalFriends
import ru.flocator.core_utils.LoadUtils
import ru.flocator.core_utils.TimePresentationUtils
import ru.flocator.feature_community.internal.adapters.ExternalFriendActionListener
import ru.flocator.feature_community.internal.adapters.ExternalFriendAdapter
import ru.flocator.feature_community.api.ui.ProfileFragment
import ru.flocator.feature_community.internal.view_models.OtherPersonProfileFragmentViewModel
import ru.flocator.feature_community.repository.CommunityRepository
import javax.inject.Inject
import kotlin.properties.Delegates

internal class OtherPersonProfileFragment : Fragment() {
    private var _binding: FragmentPersonProfileBinding? = null
    private val binding: FragmentPersonProfileBinding
        get() = _binding!!

    @Inject
    lateinit var repository: CommunityRepository

    @Inject
    lateinit var controller: NavController

    private lateinit var otherPersonProfileFragmentViewModel: OtherPersonProfileFragmentViewModel
    private lateinit var adapterForFriends: ExternalFriendAdapter

    private var currentUserId by Delegates.notNull<Long>()

    private var currentUser: UserExternal? = null

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
            otherPersonProfileFragmentViewModel.fetchUser(
                currentUserId,
                args.getLong(Constants.USER_ID)
            )
        } else {
            controller.back()
            return binding.root
        }
        adapterForFriends = ExternalFriendAdapter(object : ExternalFriendActionListener {
            override fun onPersonOpenProfile(user: UserExternalFriends) {
                openPersonProfile(user)
            }
        })
        otherPersonProfileFragmentViewModel.friendsLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                adapterForFriends.data = it
            }
        }
        binding.friendsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.friendsRecyclerView.adapter = adapterForFriends

        otherPersonProfileFragmentViewModel.currentUserLiveData.observe(
            viewLifecycleOwner
        ) { user ->
            currentUser = user
            binding.nameAndSurname.text = user.firstName + " " + user.lastName
            user.avatarUri?.let { it1 -> setAvatar(it1) }

            if (user.isFriend!!) {
                binding.addPersonToFriend.text = "Удалить из друзей"
                binding.addPersonToFriend.setBackgroundColor(resources.getColor(R.color.button_bg))
                binding.addPersonToFriend.setTextColor(resources.getColor(R.color.black))
                binding.addPersonToFriend.setOnClickListener {
                    otherPersonProfileFragmentViewModel.deleteMyFriend(
                        currentUserId,
                        thisUserId
                    )
                    binding.addPersonToFriend.setBackgroundColor(resources.getColor(R.color.tint))
                    binding.addPersonToFriend.text = "Добавить в друзья"
                    binding.addPersonToFriend.setTextColor(resources.getColor(R.color.white))
                }
            } else if (user.hasUserRequestedFriendship!!) {
                binding.addPersonToFriend.setBackgroundColor(resources.getColor(R.color.button_bg))
                binding.addPersonToFriend.text = "Принять заявку"
                binding.addPersonToFriend.setTextColor(resources.getColor(R.color.black))
                binding.addPersonToFriend.setOnClickListener {
                    otherPersonProfileFragmentViewModel.acceptFriend(currentUserId, thisUserId)
                    binding.addPersonToFriend.text = "Удалить из друзей"
                    binding.addPersonToFriend.setBackgroundColor(resources.getColor(R.color.button_bg))
                    binding.addPersonToFriend.setTextColor(resources.getColor(R.color.black))
                }
            } else if (user.hasTargetUserRequestedFriendship!!) {
                binding.addPersonToFriend.setBackgroundColor(resources.getColor(R.color.button_bg))
                binding.addPersonToFriend.text = "Отменить заявку"
                binding.addPersonToFriend.setTextColor(resources.getColor(R.color.black))
                binding.addPersonToFriend.setOnClickListener {
                    otherPersonProfileFragmentViewModel.cancelFriendRequest(
                        thisUserId,
                        currentUserId
                    )
                    binding.addPersonToFriend.setBackgroundColor(resources.getColor(R.color.tint))
                    binding.addPersonToFriend.text = "Добавить в друзья"
                    binding.addPersonToFriend.setTextColor(resources.getColor(R.color.white))
                }
            } else {
                binding.addPersonToFriend.setBackgroundColor(resources.getColor(R.color.tint))
                binding.addPersonToFriend.text = "Добавить в друзья"
                binding.addPersonToFriend.setTextColor(resources.getColor(R.color.white))
                binding.addPersonToFriend.setOnClickListener {
                    otherPersonProfileFragmentViewModel.addOtherUserToFriend(
                        currentUserId,
                        thisUserId
                    )
                    binding.addPersonToFriend.setBackgroundColor(resources.getColor(R.color.button_bg))
                    binding.addPersonToFriend.text = "Отменить заявку"
                    binding.addPersonToFriend.setTextColor(resources.getColor(R.color.black))
                }
            }
            thisUserId = user.userId!!
            if (user.isOnline == true) {
                binding.wasOnline.text = "В сети"
            } else {
                binding.wasOnline.text = if (user.lastOnline != null) {
                    "Был в сети " + TimePresentationUtils.timestampToHumanPresentation(
                        user.lastOnline!!
                    )
                } else {
                    "Был в сети недавно"
                }
            }
            println("БЛОКККК    " + user.isBlockedByUser)
            if (user.isBlockedByUser!! && !user.hasBlockedUser!!) {
                collapseInfo()
                binding.friends.text = "Вы заблокировали пользователя"
                binding.friends.setTextColor(resources.getColor(R.color.font))
                binding.blockPerson.text = "Разблокировать"
                binding.blockPerson.setOnClickListener {
                    otherPersonProfileFragmentViewModel.unblock(currentUserId, thisUserId)
                    showInfo()
                    binding.friends.text = "Друзья"
                    binding.friends.setTextColor(resources.getColor(R.color.dark))
                    binding.blockPerson.text = "Заблокировать"
                }
            }

            if (!user.isBlockedByUser!! && !user.hasBlockedUser!!) {
                binding.blockPerson.setOnClickListener {
                    otherPersonProfileFragmentViewModel.deleteMyFriend(
                        currentUserId,
                        thisUserId
                    )
                    otherPersonProfileFragmentViewModel.block(currentUserId, thisUserId)
                    collapseInfo()
                    binding.friends.text = "Вы заблокировали пользователя"
                    binding.friends.setTextColor(resources.getColor(R.color.font))
                    binding.blockPerson.text = "Разблокировать"
                }
            }

            if (user.hasBlockedUser!! && !user.isBlockedByUser!!) {
                collapseInfo()
                binding.friends.text = "Пользователь вас заблокировал"
                binding.friends.setTextColor(resources.getColor(R.color.font))
                binding.blockPerson.text = "Заблокировать"
                binding.blockPerson.setOnClickListener {
                    otherPersonProfileFragmentViewModel.block(currentUserId, thisUserId)
                    binding.friends.text = "Вы заблокировали пользователя"
                    binding.friends.setTextColor(resources.getColor(R.color.font))
                    binding.blockPerson.text = "Разблокировать"
                }

            }

            if (user.hasBlockedUser!! && user.isBlockedByUser!!) {
                collapseInfo()
                binding.friends.text = "Вы заблокировали пользователя"
                binding.friends.setTextColor(resources.getColor(R.color.font))
                binding.blockPerson.text = "Разблокировать"
                binding.blockPerson.setOnClickListener {
                    otherPersonProfileFragmentViewModel.unblock(currentUserId, thisUserId)
                    binding.friends.text = "Пользователь вас заблокировал"
                    binding.friends.setTextColor(resources.getColor(R.color.font))
                    binding.blockPerson.text = "Заблокировать"
                }
            }

        }

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(R.drawable.back)
        }

        binding.toolbar.setNavigationOnClickListener {
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

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun openPersonProfile(user: UserExternalFriends) {
        val args = Bundle()
        if ((user.userId ?: 0) == currentUserId) {
            val profileFragment = ProfileFragment()
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(ru.flocator.app.R.id.person_profile, profileFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        } else {
            args.putLong(Constants.CURRENT_USER_ID, currentUserId)
            user.userId?.let { args.putLong(Constants.USER_ID, it) }
            args.putString(Constants.NAME_AND_SURNAME, user.firstName + " " + user.lastName)
            args.putString(Constants.PERSON_PHOTO, user.avatarUri)
            val profilePersonFragment: OtherPersonProfileFragment = OtherPersonProfileFragment()
            profilePersonFragment.arguments = args
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(ru.flocator.app.R.id.person_profile, profilePersonFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    private fun collapseInfo() {
        binding.friendsRecyclerView.visibility = View.GONE
        binding.addPersonToFriend.visibility = View.GONE
    }

    private fun showInfo() {
        binding.friendsRecyclerView.visibility = View.VISIBLE
        binding.addPersonToFriend.visibility = View.VISIBLE
    }

    private fun setAvatar(uri: String) {
        binding.userPhotoSkeleton.showSkeleton()
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
    }

}