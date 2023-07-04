package ru.flocator.feature_main.api.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import ru.flocator.cache.storage.SettingsStorage
import ru.flocator.core_connection.live_data.ConnectionLiveData
import ru.flocator.core_controller.NavController
import ru.flocator.core_controller.findNavController
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.core_database.entities.MarkPhoto
import ru.flocator.core_database.entities.MarkWithPhotos
import ru.flocator.core_database.entities.User
import ru.flocator.core_dependency.findDependencies
import ru.flocator.core_map.api.FLocatorMap
import ru.flocator.core_map.api.entity.Mark
import ru.flocator.core_polling.TimeoutPoller
import ru.flocator.core_sections.MainSection
import ru.flocator.core_utils.LocationUtils
import ru.flocator.feature_main.databinding.FragmentMainBinding
import ru.flocator.feature_main.internal.contractions.AddMarkContractions
import ru.flocator.feature_main.internal.contractions.MarkContractions
import ru.flocator.feature_main.internal.contractions.MarksListContractions
import ru.flocator.feature_main.internal.di.DaggerMainComponent
import ru.flocator.feature_main.internal.domain.location.LatLngDto
import ru.flocator.feature_main.internal.domain.mark.MarkDto
import ru.flocator.feature_main.internal.ui.AddMarkFragment
import ru.flocator.feature_main.internal.ui.MarkFragment
import ru.flocator.feature_main.internal.ui.MarksListFragment
import ru.flocator.feature_main.internal.view_models.MainFragmentViewModel
import java.util.concurrent.atomic.AtomicBoolean
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

    // Connection
    @Inject
    internal lateinit var connectionLiveData: ConnectionLiveData

    // Controller
    @Inject
    internal lateinit var controller: NavController

    // Settings storage
    @Inject
    internal lateinit var settingsStorage: SettingsStorage

    // Rx
    private val compositeDisposable = CompositeDisposable()

    // Handlers
    private lateinit var userLocationTimeoutPoller: TimeoutPoller
    private lateinit var userInfoTimeoutPoller: TimeoutPoller
    private lateinit var marksFetchingTimeoutPoller: TimeoutPoller
    private lateinit var friendsFetchingTimeoutPoller: TimeoutPoller

    // Locations
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val isInitializedCamera = AtomicBoolean(false)

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
    }

    // Fragment lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        map =
            childFragmentManager.findFragmentById(ru.flocator.feature_main.R.id.map_fragment) as FLocatorMap

        map.initialize(
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
                                        LatLngDto(
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
                                LatLngDto(
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

        locateMapToUser()

        binding.openAddMarkFragment.setOnClickListener {
            if (mainFragmentViewModel.userLocationLiveData.value == null) {
                Snackbar.make(it, "Получение геолокации...", Snackbar.LENGTH_SHORT).show()
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
            controller.toProfile().commit()
        }

        binding.settingsBtn.setOnClickListener {
            controller.toSettings().commit()
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

        initializeMapConfiguration()

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
        connectionLiveData.observe(
            viewLifecycleOwner,
            this::onConnectionChanged
        )

        mainFragmentViewModel.requestInitialLoading()

        userLocationTimeoutPoller = TimeoutPoller(
            viewLifecycleOwner,
            TIMEOUT_TO_POLL_LOCATION_POST,
            { emitter ->
                LocationUtils.getCurrentLocation(requireContext(), fusedLocationProviderClient) {
                    if (it != null) {
                        mainFragmentViewModel.updateUserLocation(
                            LatLng(
                                it.latitude,
                                it.longitude
                            )
                        )
                        mainFragmentViewModel.postLocation()
                    }
                    emitter.emit()
                }
            }
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

    private fun initializeMapConfiguration() {
        val mapConfiguration = settingsStorage.getMapConfiguration()

        binding.filters.setActiveConfiguration(mapConfiguration)
        map.changeConfiguration(mapConfiguration)

        binding.filters.setToggleFilterLayoutListener {
            settingsStorage.setMapConfiguration(it)
            map.changeConfiguration(it)
        }
    }

    private fun locateMapToUser() {
        val userLocation = mainFragmentViewModel.userLocationLiveData.value

        if (userLocation != null) {
            map.moveCameraTo(userLocation)
        }
    }

    // TODO: move to MainActivity
    private fun onConnectionChanged(value: Boolean) {
        if (value) {
            // Has connection
            Snackbar.make(
                binding.root,
                resources.getString(ru.flocator.feature_main.R.string.return_to_connection),
                Snackbar.LENGTH_LONG
            ).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
            mainFragmentViewModel.goOnlineAsUser()
        } else {
            // No connection
            Snackbar.make(
                binding.root,
                resources.getString(ru.flocator.feature_main.R.string.no_connection),
                Snackbar.LENGTH_LONG
            ).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
        }
    }

    // TODO: move to MainActivity
    override fun onStart() {
        super.onStart()
        mainFragmentViewModel.goOnlineAsUser()
    }

    // TODO: move to MainActivity
    override fun onPause() {
        super.onPause()
        mainFragmentViewModel.goOfflineAsUser()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
        _binding = null
    }

    // Observer callbacks
    private fun onUserLocationChanged(latLng: LatLng?) {
        if (latLng == null || mainFragmentViewModel.userInfoLiveData.value == null) {
            return
        }
        if (!isInitializedCamera.get()) {
            map.moveCameraTo(latLng)
            isInitializedCamera.set(true)
        }
        map.updateUserLocation(latLng)
    }

    private fun onUserInfoChanged(value: UserInfo?) {
        if (mainFragmentViewModel.userLocationLiveData.value == null || value == null) {
            return
        }
        val location = mainFragmentViewModel.userLocationLiveData.value!!
        map.submitUser(
            ru.flocator.core_map.api.entity.User(
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
                ru.flocator.core_map.api.entity.User(
                    it.id,
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

                    return@map Mark(
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
        const val TIMEOUT_TO_POLL_LOCATION_POST = 3000L
        const val TIMEOUT_TO_FETCH_FRIENDS = 5000L
        const val TIMEOUT_TO_FETCH_MARKS = 10000L
        const val TIMEOUT_TO_FETCH_USER_INFO = 10000L
    }
}
