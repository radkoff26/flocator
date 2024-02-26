package ru.flocator.feature_community.internal.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import ru.flocator.core.base.view_model.UiState
import ru.flocator.core.dependencies.findDependencies
import ru.flocator.core.navigation.NavController
import ru.flocator.core.navigation.findNavController
import ru.flocator.core.photo.PhotoLoadingTool
import ru.flocator.core.section.CommunitySection
import ru.flocator.feature_community.R
import ru.flocator.feature_community.databinding.FragmentPersonProfileBinding
import ru.flocator.feature_community.internal.ui.adapters.ExternalFriendActionListener
import ru.flocator.feature_community.internal.ui.adapters.ExternalFriendAdapter
import ru.flocator.feature_community.internal.data.model.UserItem
import ru.flocator.feature_community.internal.data.model.UserProfile
import ru.flocator.feature_community.internal.core.di.DaggerCommunityComponent
import ru.flocator.feature_community.internal.ui.view_models.ExternalProfileViewModel
import javax.inject.Inject

internal class ExternalProfileFragment : Fragment(), CommunitySection {
    private var _binding: FragmentPersonProfileBinding? = null
    private val binding: FragmentPersonProfileBinding
        get() = _binding!!

    @Inject
    lateinit var controller: NavController

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: ExternalProfileViewModel

    private lateinit var adapterForFriends: ExternalFriendAdapter

    object Constants {
        const val USER_ID = "userId"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerCommunityComponent.builder()
            .communityDependencies(findDependencies())
            .navController(findNavController())
            .build()
            .inject(this)

        viewModel = ViewModelProvider(
            this,
            viewModelFactory
        )[ExternalProfileViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonProfileBinding.inflate(inflater, container, false)
        val args: Bundle? = arguments
        if (args != null) {
            viewModel.setExternalUserId(
                args.getLong(Constants.USER_ID)
            )
            viewModel.loadData()
        } else {
            controller.back()
            return binding.root
        }

        observeUiStateChange()

        adapterForFriends = ExternalFriendAdapter(object : ExternalFriendActionListener {
            override fun onPersonOpenProfile(user: UserItem) {
                openPersonProfile(user)
            }
        })

        binding.friendsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.friendsRecyclerView.adapter = adapterForFriends

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = ""
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
        super.onDestroyView()
        _binding = null
    }

    private fun observeUiStateChange() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when (it) {
                        is UiState.Loaded -> {
                            showUserProfileInfo(it.data.userProfile)
                            adapterForFriends.data = it.data.friends.toMutableList()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun openPersonProfile(user: UserItem) {
        lifecycleScope.launch {
            val args = Bundle()
            val currentUserId = viewModel.getCurrentUserId()
            if (user.userId == currentUserId) {
                controller.toProfile()
            } else {
                args.putLong(Constants.USER_ID, user.userId)
                val profilePersonFragment = ExternalProfileFragment()
                profilePersonFragment.arguments = args
                controller.toFragment(profilePersonFragment)
            }
        }
    }

    private fun showUserProfileInfo(user: UserProfile) {
        binding.nameAndSurname.text = resources.getString(
            R.string.name_surname,
            user.firstName,
            user.lastName
        )

        user.avatarUri?.let { avatarUri -> setAvatar(avatarUri) }
    }

    private fun setStyle(
        @StringRes addButtonTextRes: Int,
        @StringRes afterActionTextRes: Int,
        clickCallback: () -> Unit
    ) {
        binding.addPersonToFriend.text = resources.getString(addButtonTextRes)
        binding.addPersonToFriend.setBackgroundColor(resources.getColor(ru.flocator.design.R.color.button_bg))
        binding.addPersonToFriend.setTextColor(resources.getColor(ru.flocator.design.R.color.black))
        binding.addPersonToFriend.setOnClickListener {
            clickCallback.invoke()
            binding.addPersonToFriend.setBackgroundColor(resources.getColor(ru.flocator.design.R.color.tint))
            binding.addPersonToFriend.text = resources.getString(afterActionTextRes)
            binding.addPersonToFriend.setTextColor(resources.getColor(ru.flocator.design.R.color.white))
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
    }

}