package ru.flocator.feature_main.api.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import ru.flocator.core_connection.live_data.ConnectionLiveData
import ru.flocator.core_controller.NavController
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.core_database.entities.MarkPhoto
import ru.flocator.core_database.entities.MarkWithPhotos
import ru.flocator.core_database.entities.User
import ru.flocator.core_dto.location.LatLngDto
import ru.flocator.core_dto.mark.MarkDto
import ru.flocator.core_map.ui.FLocatorMapFragment
import ru.flocator.core_polling.TimeoutPoller
import ru.flocator.core_sections.MainSection
import ru.flocator.core_utils.LocationUtils
import ru.flocator.feature_main.databinding.FragmentMainBinding
import ru.flocator.feature_main.internal.add_mark.contractions.AddMarkContractions
import ru.flocator.feature_main.internal.add_mark.ui.AddMarkFragment
import ru.flocator.feature_main.internal.main.view_models.MainFragmentViewModel
import ru.flocator.feature_main.internal.mark.contractions.MarkContractions
import ru.flocator.feature_main.internal.mark.ui.MarkFragment
import ru.flocator.feature_main.internal.marks_list.contractions.MarksListContractions
import ru.flocator.feature_main.internal.marks_list.ui.MarksListFragment
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class MainFragment : Fragment(), MainSection {
    // Binding
    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() = _binding!!

    // ViewModel
    private val viewModel: MainFragmentViewModel by viewModels()

    // Connection
    @Inject
    internal lateinit var connectionLiveData: ConnectionLiveData

    // Controller
    @Inject
    internal lateinit var controller: NavController

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

    private lateinit var mapFragment: FLocatorMapFragment

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

        mapFragment =
            childFragmentManager.findFragmentById(ru.flocator.feature_main.R.id.map_fragment) as FLocatorMapFragment

        mapFragment.initialize(
            viewModel::loadPhoto,
            null,
            { id ->
                if (viewModel.userInfoLiveData.value != null) {
                    val markFragment = MarkFragment().apply {
                        arguments = Bundle().apply {
                            putLong(
                                MarkContractions.MARK_ID,
                                id
                            )
                            putLong(
                                MarkContractions.USER_ID,
                                viewModel.userInfoLiveData.value!!.userId
                            )
                        }
                    }
                    markFragment.show(
                        requireActivity().supportFragmentManager,
                        TAG
                    )
                }
            },
            { marks ->
                if (viewModel.userLocationLiveData.value != null) {
                    val marksListFragment = MarksListFragment().apply {
                        arguments = Bundle().apply {
                            val markDtoList = ArrayList(
                                marks.map {
                                    val mark = it.mark
                                    MarkDto(
                                        mark.markId,
                                        mark.authorId,
                                        LatLngDto(
                                            mark.location.latitude,
                                            mark.location.longitude
                                        ),
                                        mark.text,
                                        mark.isPublic,
                                        it.photos.map(MarkPhoto::uri),
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
            if (viewModel.userLocationLiveData.value == null) {
                Snackbar.make(it, "Получение геолокации...", Snackbar.LENGTH_SHORT).show()
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
            controller.toProfile().commit()
        }

        binding.settingsBtn.setOnClickListener {
            controller.toSettings().commit()
        }

        binding.targetBtn.setOnClickListener {
            if (viewModel.userInfoLiveData.value != null) {
                val userId = viewModel.userInfoLiveData.value!!.userId
                mapFragment.followUser(userId)
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
        connectionLiveData.observe(
            viewLifecycleOwner,
            this::onConnectionChanged
        )

        viewModel.requestInitialLoading()

        userLocationTimeoutPoller = TimeoutPoller(
            viewLifecycleOwner,
            TIMEOUT_TO_POLL_LOCATION_POST,
            { emitter ->
                LocationUtils.getCurrentLocation(requireContext(), fusedLocationProviderClient) {
                    if (it != null) {
                        viewModel.updateUserLocation(
                            LatLng(
                                it.latitude,
                                it.longitude
                            )
                        )
                        viewModel.postLocation()
                    }
                    emitter.emit()
                }
            }
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

    private fun locateMapToUser() {
        val userLocation = viewModel.userLocationLiveData.value

        if (userLocation != null) {
            mapFragment.moveCameraTo(userLocation)
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
            viewModel.goOnlineAsUser()
        } else {
            // No connection
            Snackbar.make(
                binding.root,
                resources.getString(ru.flocator.feature_main.R.string.no_connection),
                Snackbar.LENGTH_LONG
            ).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.goOnlineAsUser()
    }

    override fun onPause() {
        super.onPause()
        viewModel.goOfflineAsUser()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        compositeDisposable.dispose()
    }

    // Observer callbacks
    private fun onUserLocationChanged(latLng: LatLng?) {
        if (latLng == null || viewModel.userInfoLiveData.value == null) {
            return
        }
        if (!isInitializedCamera.get()) {
            mapFragment.moveCameraTo(latLng)
            isInitializedCamera.set(true)
        }
        mapFragment.updateUserLocation(latLng)
    }

    private fun onUserInfoChanged(value: UserInfo?) {
        if (viewModel.userLocationLiveData.value == null || value == null) {
            return
        }
        mapFragment.submitUser(value)
    }

    private fun onFriendsStateChanged(value: List<User>) {
        mapFragment.submitFriends(value)
    }

    private fun onMarksStateChanged(value: List<MarkWithPhotos>) {
        mapFragment.submitMarks(value)
    }

    companion object {
        const val TAG = "Main Fragment"
        const val TIMEOUT_TO_POLL_LOCATION_POST = 3000L
        const val TIMEOUT_TO_FETCH_FRIENDS = 5000L
        const val TIMEOUT_TO_FETCH_MARKS = 10000L
        const val TIMEOUT_TO_FETCH_USER_INFO = 10000L
    }
}
