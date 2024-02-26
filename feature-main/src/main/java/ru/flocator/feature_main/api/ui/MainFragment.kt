package ru.flocator.feature_main.api.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import ru.flocator.core.alert.ErrorDebouncingAlertPoller
import ru.flocator.core.dependencies.findDependencies
import ru.flocator.core.exceptions.LostConnectionException
import ru.flocator.core.location.LocationLiveData
import ru.flocator.core.navigation.NavController
import ru.flocator.core.navigation.findNavController
import ru.flocator.core.polling.TimeoutPoller
import ru.flocator.core.section.MainSection
import ru.flocator.data.data_store.info.UserInfo
import ru.flocator.data.database.entities.MarkPhoto
import ru.flocator.data.database.entities.MarkWithPhotos
import ru.flocator.data.database.entities.User
import ru.flocator.data.models.location.Coordinates
import ru.flocator.design.SnackbarComposer
import ru.flocator.feature_main.R
import ru.flocator.feature_main.databinding.FragmentMainBinding
import ru.flocator.feature_main.internal.core.contractions.AddMarkContractions
import ru.flocator.feature_main.internal.core.contractions.MarksListContractions
import ru.flocator.feature_main.internal.data.model.mark.MarkDto
import ru.flocator.feature_main.internal.core.di.DaggerMainComponent
import ru.flocator.feature_main.internal.ui.fragments.AddMarkFragment
import ru.flocator.feature_main.internal.ui.fragments.MarkFragment
import ru.flocator.feature_main.internal.ui.fragments.MarksListFragment
import ru.flocator.feature_main.internal.ui.view_models.MainFragmentViewModel
import ru.flocator.map.api.FLocatorMap
import ru.flocator.map.api.entity.MapMark
import javax.inject.Inject

class MainFragment : Fragment(), MainSection {
    // Binding
    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() = _binding!!

    // ViewModel
    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: MainFragmentViewModel

    // Controller
    @Inject
    internal lateinit var controller: NavController

    // Handlers
    private lateinit var userInfoTimeoutPoller: TimeoutPoller
    private lateinit var marksFetchingTimeoutPoller: TimeoutPoller
    private lateinit var friendsFetchingTimeoutPoller: TimeoutPoller

    // Locations
    private lateinit var locationLiveData: LocationLiveData

    // Alert
    private lateinit var alertExecutor: ErrorDebouncingAlertPoller

    // Map
    private var _map: FLocatorMap? = null
    private val map: FLocatorMap
        get() = _map!!

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerMainComponent.builder()
            .mainDependencies(findDependencies())
            .navController(findNavController())
            .build()
            .inject(this)

        viewModel =
            ViewModelProvider(this, viewModelFactory)[MainFragmentViewModel::class.java]

        alertExecutor = ErrorDebouncingAlertPoller(requireActivity()) { view, errorText, callback ->
            SnackbarComposer.composeDesignedSnackbar(view, errorText, callback)
        }
    }

    // Fragment lifecycle methods
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        _map =
            childFragmentManager.findFragmentById(R.id.map_fragment) as FLocatorMap

        initMap()

        locationLiveData =
            LocationLiveData(requireContext())

        locationLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.updateUserLocation(it)
                viewModel.postLocation()
            }
        }

        binding.openAddMarkFragment.setOnClickListener {
            if (viewModel.userLocationLiveData.value == null) {
                Snackbar.make(
                    it,
                    resources.getString(R.string.location_fetching),
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val point = viewModel.userLocationLiveData.value!!

            val args = Bundle()

            args.putDouble(
                AddMarkContractions.LATITUDE,
                point.latitude
            )

            args.putDouble(
                AddMarkContractions.LONGITUDE,
                point.longitude
            )

            val addMarkFragment = AddMarkFragment()
            addMarkFragment.arguments = args
            addMarkFragment.show(requireActivity().supportFragmentManager, AddMarkFragment.TAG)
        }

        binding.communityBtn.setOnClickListener {
            controller.toProfile()
        }

        binding.settingsBtn.setOnClickListener {
            controller.toSettings()
        }

        binding.targetBtn.setOnClickListener {
            if (viewModel.userInfoLiveData.value != null) {
                val userId = viewModel.userInfoLiveData.value!!.userId
                map.followUser(userId)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userLocationLiveData.observe(
            viewLifecycleOwner,
            this::onUserLocationChanged
        )
        viewModel.friendsLiveData.observe(
            viewLifecycleOwner,
            this::onFriendsStateChanged
        )
        viewModel.marksLiveData.observe(
            viewLifecycleOwner,
            this::onMarksStateChanged
        )
        viewModel.userInfoLiveData.observe(
            viewLifecycleOwner,
            this::onUserInfoChanged
        )
        viewModel.errorLiveData.observe(
            viewLifecycleOwner,
            this::onErrorOccurred
        )

        userInfoTimeoutPoller = TimeoutPoller(
            viewLifecycleOwner,
            TIMEOUT_TO_FETCH_USER_INFO,
            viewModel::fetchUserInfo
        )

        friendsFetchingTimeoutPoller = TimeoutPoller(
            viewLifecycleOwner,
            TIMEOUT_TO_FETCH_FRIENDS,
            viewModel::fetchFriends
        )

        marksFetchingTimeoutPoller = TimeoutPoller(
            viewLifecycleOwner,
            TIMEOUT_TO_FETCH_MARKS,
            viewModel::fetchMarks
        )
    }

    private fun initMap() {
        val mapConfiguration = viewModel.getMapConfiguration()

        binding.filters.setActiveConfiguration(mapConfiguration)

        binding.filters.setToggleFilterLayoutListener {
            viewModel.setMapConfiguration(it)
            map.changeConfiguration(it)
        }

        map.initialize(
            mapConfiguration,
            viewModel::loadPhoto,
            null,
            onMarkViewClickCallback = { id ->
                if (viewModel.userInfoLiveData.value != null) {
                    val markFragment = MarkFragment.newInstance(id)
                    markFragment.show(
                        requireActivity().supportFragmentManager,
                        TAG
                    )
                }
            },
            onMarkGroupViewClickCallback = { markIds ->
                if (viewModel.userLocationLiveData.value != null) {
                    val marksListFragment = MarksListFragment().apply {
                        arguments = Bundle().apply {
                            val markDtoList = ArrayList(
                                markIds.map {
                                    val allMarks = viewModel.marksLiveData.value!!
                                    val markWithPhotos = allMarks[it]!!
                                    val mark = markWithPhotos.mark
                                    MarkDto(
                                        mark.markId,
                                        mark.authorId,
                                        Coordinates(
                                            mark.location.latitude,
                                            mark.location.longitude
                                        ),
                                        mark.text,
                                        mark.isPublic,
                                        markWithPhotos.photos.map(MarkPhoto::uri),
                                        mark.place,
                                        mark.likesCount,
                                        mark.hasUserLiked,
                                        mark.createdAt
                                    )
                                }
                            )
                            putSerializable(
                                MarksListContractions.MARKS,
                                markDtoList
                            )
                            val userPoint = viewModel.userLocationLiveData.value!!
                            putSerializable(
                                MarksListContractions.USER_POINT,
                                Coordinates(
                                    userPoint.latitude,
                                    userPoint.longitude
                                )
                            )
                        }
                    }
                    marksListFragment.show(
                        requireActivity().supportFragmentManager,
                        MarksListFragment.TAG
                    )
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()
        viewModel.requestInitialLoading()
        viewModel.goOnlineAsUser() // TODO: move to MainActivity
    }

    override fun onStop() {
        super.onStop()
        viewModel.goOfflineAsUser() // TODO: move to MainActivity
        viewModel.clearError()
    }

    override fun onDestroyView() {
        _map = null
        _binding = null
        super.onDestroyView()
    }

    // Observer callbacks
    private fun onErrorOccurred(value: Throwable?) {
        if (value == null) {
            return
        }
        when (value) {
            is LostConnectionException -> {
                alertExecutor.postError(binding.root, resources.getString(R.string.no_connection))
            }
        }
    }

    private fun onUserLocationChanged(coordinates: Coordinates?) {
        if (coordinates == null) {
            return
        }
        if (!viewModel.isCameraInitialized.get()) {
            map.moveCameraTo(coordinates)
            viewModel.isCameraInitialized.set(true)
        }
        map.updateUserLocation(coordinates)
    }

    private fun onUserInfoChanged(value: UserInfo?) {
        if (viewModel.userLocationLiveData.value == null || value == null) {
            return
        }
        val location = viewModel.userLocationLiveData.value!!
        map.submitUser(
            User(
                value.userId,
                value.firstName,
                value.lastName,
                location,
                value.avatarUri
            )
        )
    }

    private fun onFriendsStateChanged(value: Map<Long, User>) {
        map.submitFriends(
            value.values.map {
                User(
                    it.userId,
                    it.firstName,
                    it.lastName,
                    it.location,
                    it.avatarUri
                )
            }
        )
    }

    private fun onMarksStateChanged(value: Map<Long, MarkWithPhotos>) {
        map.submitMarks(
            value.values.map {
                val mark = it.mark
                val authorId = mark.authorId

                val thumbnail = if (it.photos.isEmpty()) {
                    null
                } else {
                    it.photos[0].uri
                }

                val friends = viewModel.friendsLiveData.value!!

                val targetUserId = viewModel.userInfoLiveData.value?.userId
                val friend = friends[authorId]

                val isAuthorUser = authorId == targetUserId
                val isAuthorFriend = friend != null

                if (isAuthorUser || isAuthorFriend) {
                    val avatarUri = if (isAuthorUser) {
                        viewModel.userInfoLiveData.value!!.avatarUri
                    } else {
                        friend!!.avatarUri
                    }

                    return@map MapMark(
                        mark.markId,
                        mark.authorId,
                        mark.location,
                        thumbnail,
                        avatarUri
                    )
                }

                return@map null
            }.filterNotNull()
        )
    }

    companion object {
        const val TAG = "Main Fragment"
        const val TIMEOUT_TO_FETCH_FRIENDS = 3000L
        const val TIMEOUT_TO_FETCH_MARKS = 7000L
        const val TIMEOUT_TO_FETCH_USER_INFO = 8000L
    }
}
