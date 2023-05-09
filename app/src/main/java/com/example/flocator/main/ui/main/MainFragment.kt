package com.example.flocator.main.ui.main

import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.flocator.common.cache.runtime.PhotoState
import com.example.flocator.common.config.Actions
import com.example.flocator.common.receivers.NetworkReceiver
import com.example.flocator.common.storage.db.entities.User
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.example.flocator.community.fragments.ProfileFragment
import com.example.flocator.databinding.FragmentMainBinding
import com.example.flocator.main.MainSection
import com.example.flocator.main.config.BundleArgumentsContraction
import com.example.flocator.main.handlers.UserLocationHandler
import com.example.flocator.main.models.CameraStatus
import com.example.flocator.main.models.CameraStatusType
import com.example.flocator.main.ui.MainViewModelFactory
import com.example.flocator.main.ui.add_mark.AddMarkFragment
import com.example.flocator.main.ui.main.data.*
import com.example.flocator.main.ui.main.views.FriendMapView
import com.example.flocator.main.ui.main.views.MarkGroupMapView
import com.example.flocator.main.ui.main.views.MarkMapView
import com.example.flocator.main.ui.mark.MarkFragment
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
import com.yandex.runtime.ui_view.ViewProvider
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import java.lang.Float.max
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment(), MainSection {
    // Binding
    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() = _binding!!

    // Network Receiver
    private val networkReceiver = NetworkReceiver()

    // ViewModel
    @Inject
    lateinit var viewModelFactory: MainFragmentViewModel.Factory
    private val viewModel: MainFragmentViewModel by viewModels {
        MainViewModelFactory(this) {
            viewModelFactory.build(networkReceiver.networkState)
        }
    }

    // Rx
    private val compositeDisposable = CompositeDisposable()

    // Map store
    private val usersViewState = ConcurrentHashMap<Long, FriendViewDto>()
    private val marksViewState = ConcurrentHashMap<Long, MarkViewDto>()
    private val markGroupsViewState = CopyOnWriteArrayList<MarkGroupViewDto>()
    private val usersClickListeners = ConcurrentHashMap<Long, MapObjectTapListener>()
    private val markClickListeners = ConcurrentHashMap<Long, MapObjectTapListener>()

    // Map collections
    private lateinit var usersCollection: MapObjectCollection
    private lateinit var marksCollection: MapObjectCollection
    private lateinit var markGroupsCollection: MapObjectCollection

    // Handlers
    private lateinit var userLocationHandler: UserLocationHandler

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

        usersCollection = binding.mapView.map.addMapObjectLayer("users")
        marksCollection = binding.mapView.map.addMapObjectLayer("marks")
        markGroupsCollection = binding.mapView.map.addMapObjectLayer("mark groups")

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

        viewModel.requestInitialLoading()

        userLocationHandler = UserLocationHandler(requireContext(), viewLifecycleOwner) {
            viewModel.updateUserLocation(
                Point(
                    it.latitude,
                    it.longitude
                )
            )
            viewModel.postLocation()
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val cameraPosition: CameraPosition? =
                savedInstanceState.getSerializable(
                    "CAMERA_POSITION",
                    CameraPositionDto::class.java
                )?.toCameraPosition()
            if (cameraPosition != null) {
                binding.mapView.map.move(cameraPosition)
                viewModel.updateVisibleRegion(binding.mapView.map.visibleRegion)
                Log.d(TAG, "onViewStateRestored: $cameraPosition")
            }
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
    }

    override fun onResume() {
        super.onResume()
        viewModel.startPolling()
        requireActivity().registerReceiver(networkReceiver, IntentFilter(Actions.CONNECTIVITY_CHANGE))
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopPolling()
        requireActivity().unregisterReceiver(networkReceiver)
        networkReceiver.stop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val cameraPosition = CameraPositionDto.fromCameraPosition(binding.mapView.map.cameraPosition)
        outState.putSerializable("CAMERA_POSITION", cameraPosition)
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
        if (point == null || viewModel.userInfo == null) {
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
        val userInfo = viewModel.userInfo!!
        if (usersViewState[userInfo.userId] == null) {
            val friendMapView = FriendMapView(requireContext())
            friendMapView.setUserName("${userInfo.firstName} ${userInfo.lastName}")
            val viewProvider = ViewProvider(friendMapView)
            usersViewState[userInfo.userId] = FriendViewDto(
                usersCollection.addPlacemark(
                    point,
                    viewProvider
                ),
                friendMapView,
                null
            )
            if (userInfo.avatarUri != null) {
                viewModel.loadPhoto(userInfo.avatarUri)
            }
        } else {
            usersViewState[userInfo.userId]!!.placemark.geometry = point
        }
    }

    private fun onFriendsStateChanged(value: kotlin.collections.Map<Long, User>) {
        for (userEntry in value) {
            val id = userEntry.key
            val user = userEntry.value
            if (usersViewState[id] == null) {
                val friendView = FriendMapView(requireContext())
                friendView.setUserName("${user.firstName} ${user.lastName}")
                val viewProvider = ViewProvider(friendView)
                usersViewState[id] = FriendViewDto(
                    usersCollection.addPlacemark(user.location, viewProvider),
                    friendView,
                    null
                )
                usersClickListeners[id] = MapObjectTapListener { _, _ ->
                    viewModel.setCameraFollowOnFriendMark(id)
                    viewModel.cameraStatusLiveData.observeForever(this::onCameraStatusChanged)
                    true
                }
                usersViewState[id]!!.placemark.addTapListener(usersClickListeners[id]!!)
                if (user.avatarUrl == null) {
                    friendView.setPlaceHolder()
                    viewProvider.snapshot()
                    usersViewState[id]!!.placemark.setView(viewProvider)
                    usersViewState[id]!!.avatarUri = null
                } else {
                    viewModel.loadPhoto(user.avatarUrl)
                }
            } else {
                usersViewState[id]!!.placemark.geometry = user.location
            }
        }
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
        marksViewState.clear()
        marksCollection.clear()
        markGroupsViewState.clear()
        markGroupsCollection.clear()
        markClickListeners.clear()
        val found = BooleanArray(value.size) { false }
        for (groupIndex in value.indices) {
            if (!found[groupIndex]) {
                val group = value[groupIndex]
                if (group.marks.size == 1) {
                    val mark = group.marks[0].mark
                    val photos = group.marks[0].photos
                    val id = mark.markId
                    val markMapView = MarkMapView(requireContext())
                    val viewProvider = ViewProvider(markMapView)
                    marksViewState[id] = MarkViewDto(
                        marksCollection.addPlacemark(mark.location, viewProvider),
                        markMapView,
                        null,
                        null
                    )
                    markClickListeners[id] = MapObjectTapListener { _, _ ->
                        val markFragment = MarkFragment().apply {
                            arguments = Bundle().apply {
                                putLong(
                                    BundleArgumentsContraction.MarkFragmentArguments.MARK_ID,
                                    id
                                )
                                putLong(
                                    BundleArgumentsContraction.MarkFragmentArguments.USER_ID,
                                    viewModel.userInfo!!.userId
                                )
                            }
                        }
                        markFragment.show(requireActivity().supportFragmentManager, TAG)
                        true
                    }
                    marksViewState[id]!!.placemark.addTapListener(markClickListeners[id]!!)
                    if (photos.isEmpty()) {
                        markMapView.setMarkBitmapPlaceHolder()
                        viewProvider.snapshot()
                        marksViewState[id]!!.placemark.setView(viewProvider)
                        marksViewState[id]!!.thumbnailUri = null
                    } else {
                        viewModel.loadPhoto(photos[0].uri)
                    }
                    val avatar = marksViewState[id]!!.avatarUrl
                    if (avatar != null && !marksViewState[id]!!.markMapView.hasAvatar) {
                        viewModel.loadPhoto(avatar)
                    }
                } else {
                    val markGroupMapView = MarkGroupMapView(requireContext())
                    markGroupMapView.setCount(group.marks.size)
                    val viewProvider = ViewProvider(markGroupMapView)
                    markGroupsViewState.add(
                        MarkGroupViewDto(
                            markGroupsCollection.addPlacemark(group.center, viewProvider),
                            markGroupMapView,
                            group.marks
                        )
                    )
                }
            }
        }
    }

    private fun countMarksDiff(value: List<MarkGroup>): BooleanArray {
        val found = BooleanArray(value.size) { false }
        val markViews = marksViewState.entries
        for (markView in markViews) {
            synchronized(markView) {
                val foundElementIndex = value.indexOfFirst {
                    it.marks.size == 1
                            &&
                            it.marks[0].mark.markId == markView.key
                            &&
                            it.marks[0].mark.location == markView.value.placemark.geometry
                }
                if (foundElementIndex == -1) {
                    if (markView.value.placemark.isValid) {
                        marksCollection.remove(markView.value.placemark)
                    }
                    marksViewState.remove(markView.key)
                } else {
                    found[foundElementIndex] = true
                }
            }
        }
        var i = 0
        while (i < markGroupsViewState.size) {
            val item = markGroupsViewState[i]
            synchronized(item) {
                val foundElementIndex = value.indexOfFirst {
                    it.marks == item.marks
                }
                if (foundElementIndex == -1) {
                    if (item.placemark.isValid) {
                        marksCollection.remove(item.placemark)
                    }
                    markGroupsViewState.removeAt(i)
                } else {
                    found[foundElementIndex] = true
                    i++
                }
            }
        }
        return found
    }

    private fun onPhotoLoaded(value: LruCache<String, PhotoState>) {
        // User case
        if (viewModel.userInfo != null) {
            val userId = viewModel.userInfo!!.userId
            if (usersViewState[userId] != null) {
                val user = usersViewState[userId]!!
                val userInfo = viewModel.userInfo!!
                if (user.avatarUri != userInfo.avatarUri && value[userInfo.avatarUri] != null) {
                    val photo = value[userInfo.avatarUri]
                    if (photo is PhotoState.Loaded) {
                        user.friendMapView.setBitmap(photo.bitmap)
                        user.avatarUri = userInfo.avatarUri
                        updateUserView(user)
                    } else if (photo is PhotoState.Failed) {
                        viewModel.loadPhoto(user.avatarUri!!)
                    }
                }
            }
        }

        // Watch mark images change
        for (mark in marksViewState) {
            // State of current mark
            val liveMark = viewModel.marks[mark.key]!!.mark
            val photos = viewModel.marks[mark.key]!!.photos

            // Mark thumbnail case
            if (photos.isEmpty()) {
                // If photo has already been set, then this photo is no longer present
                if (mark.value.thumbnailUri != null) {
                    mark.value.markMapView.setMarkBitmapPlaceHolder()
                    mark.value.thumbnailUri = null
                    updateMarkView(mark.value)
                }
            } else {
                // Non-nullable image uri
                val thumbnailUri = photos[0].uri
                // If photo is present and not yet assigned, then it will be set
                if (value[thumbnailUri] != null && mark.value.thumbnailUri != thumbnailUri) {
                    val photo = value[thumbnailUri]!!
                    if (photo is PhotoState.Loaded) {
                        mark.value.markMapView.setMarkBitmapImage(photo.bitmap)
                        mark.value.thumbnailUri = thumbnailUri
                        updateMarkView(mark.value)
                    } else if (photo is PhotoState.Failed) {
                        mark.value.markMapView.setMarkBitmapPlaceHolder()
                        viewModel.loadPhoto(thumbnailUri)
                    }
                }
            }

            // Author avatar case
            // If author user is not still loaded
            if (viewModel.friendsLiveData.value!![liveMark.authorId] == null) {
                continue
            }
            val url =
                viewModel.friendsLiveData.value!![liveMark.authorId]!!.avatarUrl
            // If user doesn't have an avatar image
            if (url == null && mark.value.avatarUrl != null) {
                // Then there goes a placeholder
                mark.value.markMapView.setFriendBitmapPlaceHolder()
                mark.value.avatarUrl = null
                updateMarkView(mark.value)
            } else {
                if (url != null && value[url] != null && url != mark.value.avatarUrl) {
                    val photo = value[url]!!
                    if (photo is PhotoState.Loaded) {
                        mark.value.markMapView.setFriendBitmapImage(photo.bitmap)
                        mark.value.avatarUrl = url
                        updateMarkView(mark.value)
                    } else if (photo is PhotoState.Failed) {
                        viewModel.loadPhoto(url)
                    }
                }
            }
        }

        // Watch friends images change
        for (friend in usersViewState) {
            if (friend.key == viewModel.userInfo?.userId) {
                continue
            }
            val url = viewModel.friendsLiveData.value!![friend.key]!!.avatarUrl
            if (url == null) {
                friend.value.friendMapView.setPlaceHolder()
                friend.value.avatarUri = null
                updateUserView(friend.value)
            } else {
                if (url != friend.value.avatarUri && value[url] != null) {
                    val photo = value[url]!!
                    if (photo is PhotoState.Loaded) {
                        friend.value.friendMapView.setBitmap(photo.bitmap)
                        friend.value.avatarUri = url
                        updateUserView(friend.value)
                    } else {
                        viewModel.loadPhoto(url)
                    }
                }
            }
        }
    }

    private fun updateMarkView(mark: MarkViewDto) {
        val viewProvider = ViewProvider(mark.markMapView)
        viewProvider.snapshot()
        mark.placemark.setView(viewProvider)
    }

    private fun updateUserView(user: FriendViewDto) {
        val viewProvider = ViewProvider(user.friendMapView)
        viewProvider.snapshot()
        user.placemark.setView(viewProvider)
    }

    companion object {
        const val TAG = "Main Fragment"
        const val MIN_ZOOM_SCALE = 15f
    }
}
