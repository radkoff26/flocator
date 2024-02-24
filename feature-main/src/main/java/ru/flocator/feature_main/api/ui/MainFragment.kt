package ru.flocator.feature_main.api.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.BaseTransientBottomBar
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
import ru.flocator.feature_main.internal.contractions.AddMarkContractions
import ru.flocator.feature_main.internal.contractions.MarkContractions
import ru.flocator.feature_main.internal.contractions.MarksListContractions
import ru.flocator.feature_main.internal.di.DaggerMainComponent
import ru.flocator.feature_main.internal.data.mark.MarkDto
import ru.flocator.feature_main.internal.ui.AddMarkFragment
import ru.flocator.feature_main.internal.ui.MarkFragment
import ru.flocator.feature_main.internal.ui.MarksListFragment
import ru.flocator.feature_main.internal.view_models.MainFragmentViewModel
import ru.flocator.map.api.FLocatorMap
import ru.flocator.map.api.MapPreferences
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
    private lateinit var mainFragmentViewModel: MainFragmentViewModel

    // Controller
    @Inject
    internal lateinit var controller: NavController

    // Settings storage
    // TODO: move to VM
    @Inject
    internal lateinit var mapPreferences: MapPreferences

    // Handlers
    private lateinit var userInfoTimeoutPoller: TimeoutPoller
    private lateinit var marksFetchingTimeoutPoller: TimeoutPoller
    private lateinit var friendsFetchingTimeoutPoller: TimeoutPoller

    // Locations
    private lateinit var locationLiveData: LocationLiveData

    // Alert
    private lateinit var alertExecutor: ErrorDebouncingAlertPoller

    // Map
    private lateinit var map: FLocatorMap

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerMainComponent.builder()
            .mainDependencies(findDependencies())
            .navController(findNavController())
            .build()
            .inject(this)

        mainFragmentViewModel =
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

        map =
            childFragmentManager.findFragmentById(R.id.map_fragment) as FLocatorMap

        initMap()

        locationLiveData =
            LocationLiveData(requireContext())

        locationLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                mainFragmentViewModel.updateUserLocation(it)
                mainFragmentViewModel.postLocation()
            }
        }

        binding.openAddMarkFragment.setOnClickListener {
            if (mainFragmentViewModel.userLocationLiveData.value == null) {
                Snackbar.make(
                    it,
                    resources.getString(R.string.location_fetching),
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val point = mainFragmentViewModel.userLocationLiveData.value!!

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
            if (mainFragmentViewModel.userInfoLiveData.value != null) {
                val userId = mainFragmentViewModel.userInfoLiveData.value!!.userId
                map.followUser(userId)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainFragmentViewModel.userLocationLiveData.observe(
            viewLifecycleOwner,
            this::onUserLocationChanged
        )
        mainFragmentViewModel.friendsLiveData.observe(
            viewLifecycleOwner,
            this::onFriendsStateChanged
        )
        mainFragmentViewModel.marksLiveData.observe(
            viewLifecycleOwner,
            this::onMarksStateChanged
        )
        mainFragmentViewModel.userInfoLiveData.observe(
            viewLifecycleOwner,
            this::onUserInfoChanged
        )
        mainFragmentViewModel.errorLiveData.observe(
            viewLifecycleOwner,
            this::onErrorOccurred
        )

        userInfoTimeoutPoller = TimeoutPoller(
            viewLifecycleOwner,
            TIMEOUT_TO_FETCH_USER_INFO,
            mainFragmentViewModel::fetchUserInfo
        )

        friendsFetchingTimeoutPoller = TimeoutPoller(
            viewLifecycleOwner,
            TIMEOUT_TO_FETCH_FRIENDS,
            mainFragmentViewModel::fetchFriends
        )

        marksFetchingTimeoutPoller = TimeoutPoller(
            viewLifecycleOwner,
            TIMEOUT_TO_FETCH_MARKS,
            mainFragmentViewModel::fetchMarks
        )
    }

    private fun initMap() {
        val mapConfiguration = mapPreferences.getMapConfiguration()

        binding.filters.setActiveConfiguration(mapConfiguration)

        binding.filters.setToggleFilterLayoutListener {
            mapPreferences.setMapConfiguration(it)
            map.changeConfiguration(it)
        }

        map.initialize(
            mapConfiguration,
            mainFragmentViewModel::loadPhoto,
            null,
            { id ->
                if (mainFragmentViewModel.userInfoLiveData.value != null) {
                    val markFragment = MarkFragment().apply {
                        arguments = Bundle().apply {
                            putLong(
                                MarkContractions.MARK_ID,
                                id
                            )
                            putLong(
                                MarkContractions.USER_ID,
                                mainFragmentViewModel.userInfoLiveData.value!!.userId
                            )
                        }
                    }
                    markFragment.show(
                        requireActivity().supportFragmentManager,
                        TAG
                    )
                }
            },
            { markIds ->
                if (mainFragmentViewModel.userLocationLiveData.value != null) {
                    val marksListFragment = MarksListFragment().apply {
                        arguments = Bundle().apply {
                            val markDtoList = ArrayList(
                                markIds.map {
                                    val allMarks = mainFragmentViewModel.marksLiveData.value!!
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
                            val userPoint = mainFragmentViewModel.userLocationLiveData.value!!
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
        mainFragmentViewModel.requestInitialLoading()
        mainFragmentViewModel.goOnlineAsUser() // TODO: move to MainActivity
    }

    override fun onStop() {
        super.onStop()
        mainFragmentViewModel.goOfflineAsUser() // TODO: move to MainActivity
        mainFragmentViewModel.clearError()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        if (!mainFragmentViewModel.isCameraInitialized.get()) {
            map.moveCameraTo(coordinates)
            mainFragmentViewModel.isCameraInitialized.set(true)
        }
        map.updateUserLocation(coordinates)
    }

    private fun onUserInfoChanged(value: UserInfo?) {
        if (mainFragmentViewModel.userLocationLiveData.value == null || value == null) {
            return
        }
        val location = mainFragmentViewModel.userLocationLiveData.value!!
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

                val friends = mainFragmentViewModel.friendsLiveData.value!!

                val targetUserId = mainFragmentViewModel.userInfoLiveData.value?.userId
                val friend = friends[authorId]

                val isAuthorUser = authorId == targetUserId
                val isAuthorFriend = friend != null

                if (isAuthorUser || isAuthorFriend) {
                    val avatarUri = if (isAuthorUser) {
                        mainFragmentViewModel.userInfoLiveData.value!!.avatarUri
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
