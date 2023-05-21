package com.example.flocator.main.ui.main

import android.os.Bundle
import android.util.Log
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.flocator.R
import com.example.flocator.common.cache.runtime.PhotoState
import com.example.flocator.common.connection.live_data.ConnectionLiveData
import com.example.flocator.common.polling.TimeoutPoller
import com.example.flocator.common.storage.db.entities.MarkWithPhotos
import com.example.flocator.common.storage.db.entities.User
import com.example.flocator.common.storage.store.user.info.UserInfo
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.example.flocator.common.utils.LocationUtils
import com.example.flocator.community.fragments.ProfileFragment
import com.example.flocator.databinding.FragmentMainBinding
import com.example.flocator.main.MainSection
import com.example.flocator.main.config.BundleArgumentsContraction
import com.example.flocator.main.models.CameraStatus
import com.example.flocator.main.models.CameraStatusType
import com.example.flocator.main.ui.add_mark.AddMarkFragment
import com.example.flocator.main.ui.main.data.CameraPositionDto
import com.example.flocator.main.ui.main.data.MarkGroup
import com.example.flocator.main.ui.main.data.PointDto
import com.example.flocator.main.ui.mark.MarkFragment
import com.example.flocator.main.ui.marks_list.MarksListFragment
import com.example.flocator.main.utils.ViewUtils.Companion.dpToPx
import com.example.flocator.settings.SettingsFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import java.lang.Float.max
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment(), MainSection {
    // Binding
    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() = _binding!!

    // ViewModel
    private val viewModel: MainFragmentViewModel by viewModels()

    // Connection
    @Inject
    lateinit var connectionLiveData: ConnectionLiveData

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

    // Listeners
    // Due to weak references stored in map, these listeners should be stored separately
    private val inertiaMoveListener = object : InertiaMoveListener {
        override fun onStart(p0: Map, p1: CameraPosition) {
            viewModel.setCameraFixed()
            viewModel.cameraStatusLiveData.removeObserver(this@MainFragment::onCameraStatusChanged)
        }

        override fun onCancel(p0: Map, p1: CameraPosition) {

        }

        override fun onFinish(p0: Map, p1: CameraPosition) {

        }
    }
    private val cameraListener =
        CameraListener { map, _, _, _ ->
            viewModel.updateVisibleRegion(
                map.visibleRegion
            )
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

        binding.mapView.initialize(
            viewModel::loadPhoto,
            { id ->
                MapObjectTapListener { _, _ ->
                    viewModel.setCameraFollowOnFriendMark(id)
                    viewModel.cameraStatusLiveData.observeForever(this::onCameraStatusChanged)
                    true
                }
            },
            { id ->
                MapObjectTapListener { _, _ ->
                    if (viewModel.userInfoLiveData.value != null) {
                        val markFragment = MarkFragment().apply {
                            arguments = Bundle().apply {
                                putLong(
                                    BundleArgumentsContraction.MarkFragmentArguments.MARK_ID,
                                    id
                                )
                                putLong(
                                    BundleArgumentsContraction.MarkFragmentArguments.USER_ID,
                                    viewModel.userInfoLiveData.value!!.userId
                                )
                            }
                        }
                        markFragment.show(
                            requireActivity().supportFragmentManager,
                            TAG
                        )
                    }
                    true
                }
            },
            { marks ->
                MapObjectTapListener { _, _ ->
                    if (viewModel.userLocationLiveData.value != null) {
                        val marksListFragment = MarksListFragment().apply {
                            arguments = Bundle().apply {
                                val markDtoList = ArrayList(
                                    marks.map(MarkWithPhotos::toMarkDto)
                                )
                                putSerializable(
                                    BundleArgumentsContraction.MarksListFragmentArguments.MARKS,
                                    markDtoList
                                )
                                val userPoint = viewModel.userLocationLiveData.value!!
                                putSerializable(
                                    BundleArgumentsContraction.MarksListFragmentArguments.USER_POINT,
                                    PointDto(
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
                    true
                }
            }
        )

        binding.mapView.map.addInertiaMoveListener(inertiaMoveListener)
        binding.mapView.map.addCameraListener(cameraListener)

        binding.openAddMarkFragment.setOnClickListener {
            if (viewModel.userLocationLiveData.value == null) {
                Snackbar.make(it, "Получение геолокации...", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val point = viewModel.userLocationLiveData.value!!

            val args = Bundle()

            args.putDouble(
                BundleArgumentsContraction.AddMarkFragmentArguments.LATITUDE,
                point.latitude
            )

            args.putDouble(
                BundleArgumentsContraction.AddMarkFragmentArguments.LONGITUDE,
                point.longitude
            )

            val addMarkFragment = AddMarkFragment()
            addMarkFragment.arguments = args
            addMarkFragment.show(requireActivity().supportFragmentManager, AddMarkFragment.TAG)
        }

        binding.communityBtn.setOnClickListener {
            FragmentNavigationUtils.openFragment(
                requireActivity().supportFragmentManager,
                ProfileFragment()
            )
        }

        binding.settingsBtn.setOnClickListener {
            FragmentNavigationUtils.openFragment(
                requireActivity().supportFragmentManager,
                SettingsFragment()
            )
        }

        binding.targetBtn.setOnClickListener {
            viewModel.setCameraFollowOnUserMark()
            viewModel.cameraStatusLiveData.observeForever(this::onCameraStatusChanged)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeWidths()

        viewModel.updateVisibleRegion(
            binding.mapView.map.visibleRegion
        )

        viewModel.userLocationLiveData.observe(
            viewLifecycleOwner,
            this::onUserLocationChanged
        )
        viewModel.friendsLiveData.observe(
            viewLifecycleOwner,
            this::onFriendsStateChanged
        )
        viewModel.visibleMarksLiveData.observe(
            viewLifecycleOwner,
            this::onMarksStateChanged
        )
        viewModel.photoCacheLiveData.observe(
            viewLifecycleOwner,
            this::onPhotoLoaded
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
                            Point(
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

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            // IMPORTANT: here is deprecated method used due to error occurring in non-deprecated one
            val cameraPosition: CameraPosition? =
                (savedInstanceState.getParcelable(
                    "CAMERA_POSITION"
                ) as CameraPositionDto?)?.toCameraPosition()
            if (cameraPosition != null) {
                binding.mapView.map.move(cameraPosition)
                viewModel.updateVisibleRegion(binding.mapView.map.visibleRegion)
                isInitializedCamera.set(true)
                Log.d(TAG, "onViewStateRestored: $cameraPosition")
            }
        }
    }

    private fun onConnectionChanged(value: Boolean) {
        if (value) {
            // Has connection
            Snackbar.make(
                binding.root,
                resources.getString(R.string.return_to_connection),
                Snackbar.LENGTH_LONG
            ).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
            viewModel.goOnlineAsUser()
        } else {
            // No connection
            Snackbar.make(
                binding.root,
                resources.getString(R.string.no_connection),
                Snackbar.LENGTH_LONG
            ).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
        }
    }

    private fun initializeWidths() {
        val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewModel.setWidths(
                    binding.mapView.width.toFloat(),
                    dpToPx(56, requireContext()).toFloat()
                )
                binding.mapView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }
        binding.mapView.viewTreeObserver.addOnGlobalLayoutListener(listener)
    }

    override fun onStart() {
        binding.mapView.onStart()
        MapKitFactory.getInstance().onStart()
        super.onStart()
        viewModel.goOnlineAsUser()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val cameraPosition =
            CameraPositionDto.fromCameraPosition(binding.mapView.map.cameraPosition)
        outState.putParcelable("CAMERA_POSITION", cameraPosition)
    }

    override fun onPause() {
        super.onPause()
        viewModel.goOfflineAsUser()
    }

    override fun onStop() {
        binding.mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        compositeDisposable.dispose()
    }

    // Listener callbacks
    private fun onUserLocationChanged(point: Point?) {
        if (point == null || viewModel.userInfoLiveData.value == null) {
            return
        }
        if (!isInitializedCamera.get()) {
            binding.mapView.map.move(
                CameraPosition(
                    point,
                    MIN_ZOOM_SCALE,
                    0F,
                    0.0F
                )
            )
            isInitializedCamera.set(true)
        }
        binding.mapView.updateUserOnMap(
            point,
            viewModel.userInfoLiveData.value!!,
            viewModel.photoCacheLiveData.value!!
        )
    }

    private fun onUserInfoChanged(value: UserInfo?) {
        if (viewModel.userLocationLiveData.value == null || value == null) {
            return
        }
        binding.mapView.updateUserOnMap(
            viewModel.userLocationLiveData.value!!,
            value,
            viewModel.photoCacheLiveData.value!!
        )
    }

    private fun onFriendsStateChanged(value: kotlin.collections.Map<Long, User>) {
        binding.mapView.updateFriendsOnMap(
            value,
            viewModel.photoCacheLiveData.value!!
        )
    }

    private fun onCameraStatusChanged(value: CameraStatus) {
        if (value.cameraStatusType == CameraStatusType.FOLLOW_FRIEND || value.cameraStatusType == CameraStatusType.FOLLOW_USER) {
            binding.mapView.map.move(
                CameraPosition(
                    value.point!!,
                    max(MIN_ZOOM_SCALE, binding.mapView.map.cameraPosition.zoom),
                    0.0f,
                    0.0f
                ),
                Animation(Animation.Type.SMOOTH, 1f),
                null
            )
        }
    }

    private fun onMarksStateChanged(value: List<MarkGroup>) {
        if (viewModel.userInfoLiveData.value == null) {
            return
        }
        binding.mapView.updateVisibleMarksOnMap(
            value,
            viewModel.friendsLiveData.value!!,
            viewModel.photoCacheLiveData.value!!,
            viewModel.userInfoLiveData.value!!
        )
    }

    private fun onPhotoLoaded(value: LruCache<String, PhotoState>) {
        if (viewModel.userInfoLiveData.value == null) {
            return
        }
        binding.mapView.updateThumbnailsOnMap(
            value,
            viewModel.userInfoLiveData.value!!,
            viewModel.marks,
            viewModel.friendsLiveData.value!!
        )
    }

    companion object {
        const val TAG = "Main Fragment"
        const val MIN_ZOOM_SCALE = 15f
        const val TIMEOUT_TO_POLL_LOCATION_POST = 3000L
        const val TIMEOUT_TO_FETCH_FRIENDS = 5000L
        const val TIMEOUT_TO_FETCH_MARKS = 10000L
        const val TIMEOUT_TO_FETCH_USER_INFO = 10000L
    }
}
