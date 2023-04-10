package com.example.flocator.main.ui.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.example.flocator.community.fragments.ProfileFragment
import androidx.fragment.app.Fragment
import com.example.flocator.utils.FragmentNavigationUtils
import com.example.flocator.databinding.FragmentMainBinding
import com.example.flocator.main.MainSection
import com.example.flocator.main.models.*
import com.example.flocator.main.ui.data.MarkGroup
import com.example.flocator.main.ui.data.dto.FriendViewDto
import com.example.flocator.main.ui.data.dto.MarkGroupViewDto
import com.example.flocator.main.ui.data.dto.MarkViewDto
import com.example.flocator.main.ui.handlers.UserLocationHandler
import com.example.flocator.main.utils.LoadUtils
import com.example.flocator.main.ui.view_models.MainFragmentViewModel
import com.example.flocator.main.ui.views.FriendMapView
import com.example.flocator.main.ui.views.MarkGroupMapView
import com.example.flocator.main.ui.views.MarkMapView
import com.example.flocator.main.utils.ViewUtils.Companion.dpToPx
import com.example.flocator.settings.SettingsFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import com.yandex.runtime.ui_view.ViewProvider
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.lang.Float.max
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class MainFragment : Fragment(), MainSection {
    // Binding
    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() = _binding!!

    // ViewModel
    private val mainFragmentViewModel = MainFragmentViewModel()

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

    // Listeners
    private val inertiaMoveListener = object : InertiaMoveListener {
        override fun onStart(p0: Map, p1: CameraPosition) {
            mainFragmentViewModel.setCameraFixed()
            mainFragmentViewModel.cameraStatusLiveData.removeObserver(this@MainFragment::onCameraStatusChanged)
        }

        override fun onCancel(p0: Map, p1: CameraPosition) {

        }

        override fun onFinish(p0: Map, p1: CameraPosition) {

        }
    }
    private val cameraListener =
        CameraListener { map, cameraPosition, _, _ ->
            mainFragmentViewModel.updateVisibleRegion(
                map.visibleRegion
            )
            Log.d(TAG, "scale: ${cameraPosition.zoom}")
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

        usersCollection = binding.mapView.map.mapObjects.addCollection()
        marksCollection = binding.mapView.map.mapObjects.addCollection()
        markGroupsCollection = binding.mapView.map.mapObjects.addCollection()

        binding.mapView.map.addInertiaMoveListener(inertiaMoveListener)
        binding.mapView.map.addCameraListener(cameraListener)

        mainFragmentViewModel.updateVisibleRegion(
            binding.mapView.map.visibleRegion
        )

        binding.openAddMarkFragment.setOnClickListener {
            val addMarkFragment = AddMarkFragment()
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
            mainFragmentViewModel.setCameraFollowOnUserMark()
            mainFragmentViewModel.cameraStatusLiveData.observeForever(this::onCameraStatusChanged)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeWidths()

        mainFragmentViewModel.userLocationLiveData.observe(
            viewLifecycleOwner,
            this::onUserLocationChanged
        )
        mainFragmentViewModel.friendsLiveData.observe(
            viewLifecycleOwner,
            this::onFriendsStateChanged
        )
        mainFragmentViewModel.visibleMarksLiveData.observe(
            viewLifecycleOwner,
            this::onMarksStateChanged
        )
        mainFragmentViewModel.photoCacheLiveData.observe(viewLifecycleOwner, this::onPhotoLoaded)

        userLocationHandler = UserLocationHandler(requireContext(), viewLifecycleOwner) {
            mainFragmentViewModel.updateUserLocation(
                Point(
                    it.latitude,
                    it.longitude
                )
            )
        }
    }

    private fun initializeWidths() {
        val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mainFragmentViewModel.setWidths(
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
        mainFragmentViewModel.startPolling()
    }

    override fun onPause() {
        super.onPause()
        mainFragmentViewModel.stopPolling()
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
        if (point == null) {
            return
        }
        // STUB!
        if (usersViewState[MainFragmentViewModel.USER_ID] == null) {
            val friendMapView = FriendMapView(requireContext())
            val viewProvider = ViewProvider(friendMapView)
            usersViewState[MainFragmentViewModel.USER_ID] = FriendViewDto(
                usersCollection.addPlacemark(
                    point,
                    viewProvider
                ),
                friendMapView,
                null
            )
            compositeDisposable.add(LoadUtils.loadPictureFromUrl(
                MainFragmentViewModel.USER_AVATAR_URL,
                COMPRESSION_FACTOR
            ).observeOn(Schedulers.computation()).subscribe { image ->
                mainFragmentViewModel.setLoadedPhotoAsync(
                    MainFragmentViewModel.USER_AVATAR_URL,
                    image
                )
            })
        } else {
            usersViewState[MainFragmentViewModel.USER_ID]!!.placemark.geometry = point
        }
    }

    private fun onFriendsStateChanged(value: kotlin.collections.Map<Long, User>) {
        for (userEntry in value) {
            val id = userEntry.key
            val user = userEntry.value
            if (usersViewState[id] == null) {
                val friendView = FriendMapView(requireContext())
                val viewProvider = ViewProvider(friendView)
                usersViewState[id] = FriendViewDto(
                    usersCollection.addPlacemark(user.location, viewProvider),
                    friendView,
                    null
                )
                friendView.setUserName("${user.firstName} ${user.lastName}")
                usersClickListeners[id] = MapObjectTapListener { _, _ ->
                    mainFragmentViewModel.setCameraFollowOnFriendMark(id)
                    mainFragmentViewModel.cameraStatusLiveData.observeForever(this::onCameraStatusChanged)
                    true
                }
                usersViewState[id]!!.placemark.addTapListener(usersClickListeners[id]!!)
                if (user.avatarUrl == null) {
                    friendView.setPlaceHolder()
                    viewProvider.snapshot()
                    usersViewState[id]!!.placemark.setView(viewProvider)
                    usersViewState[id]!!.avatarUri = null
                } else {
                    compositeDisposable.add(LoadUtils.loadPictureFromUrl(
                        user.avatarUrl,
                        COMPRESSION_FACTOR
                    ).observeOn(Schedulers.computation()).subscribe { bitmap ->
                        mainFragmentViewModel.setLoadedPhotoAsync(user.avatarUrl, bitmap)
                    })
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
                Animation(Animation.Type.SMOOTH, 0.008f),
                null
            )
        }
    }

    private fun onMarksStateChanged(value: List<MarkGroup>) {
        marksCollection.clear()
        markGroupsCollection.clear()
        marksViewState.clear()
        markClickListeners.clear()
        markGroupsViewState.clear()
        for (group in value) {
            if (group.marks.size == 1) {
                val mark = group.marks[0]
                val id = mark.markId
                val markMapView = MarkMapView(requireContext())
                val viewProvider = ViewProvider(markMapView)
                marksViewState[id] = MarkViewDto(
                    marksCollection.addPlacemark(mark.location, viewProvider),
                    markMapView,
                    null,
                    null
                )
                if (mark.photos.isEmpty()) {
                    markMapView.setMarkBitmapPlaceHolder()
                    viewProvider.snapshot()
                    marksViewState[id]!!.placemark.setView(viewProvider)
                    marksViewState[id]!!.thumbnailUri = null
                } else {
                    val firstImage = mark.photos[0]
                    if (!mainFragmentViewModel.photoCacheContains(firstImage)) {
                        compositeDisposable.add(LoadUtils.loadPictureFromUrl(
                            firstImage,
                            COMPRESSION_FACTOR
                        ).observeOn(Schedulers.computation()).subscribe { image ->
                            mainFragmentViewModel.setLoadedPhotoAsync(firstImage, image)
                        })
                    } else {
                        markMapView.setMarkBitmapImage(mainFragmentViewModel.getCachedPhoto(firstImage)!!)
                        viewProvider.snapshot()
                        marksViewState[id]!!.placemark.setView(viewProvider)
                    }
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

    private fun onPhotoLoaded(value: kotlin.collections.Map<String, Bitmap>) {
        // User case
        if (usersViewState[MainFragmentViewModel.USER_ID] != null) {
            val user = usersViewState[MainFragmentViewModel.USER_ID]!!
            if (user.avatarUri != MainFragmentViewModel.USER_AVATAR_URL && value[MainFragmentViewModel.USER_AVATAR_URL] != null) {
                user.friendMapView.setBitmap(value[MainFragmentViewModel.USER_AVATAR_URL]!!)
                val viewProvider = ViewProvider(user.friendMapView)
                user.avatarUri = MainFragmentViewModel.USER_AVATAR_URL
                viewProvider.snapshot()
                user.placemark.setView(viewProvider)
                usersViewState[MainFragmentViewModel.USER_ID] = user
            }
        }

        // Watch mark images change
        for (mark in marksViewState) {
            // State of current mark
            val liveMark = mainFragmentViewModel.marks[mark.key]!!

            // Mark thumbnail case
            if (liveMark.photos.isEmpty()) {
                if (mark.value.thumbnailUri != null) {
                    mark.value.markMapView.setMarkBitmapPlaceHolder()
                    val viewProvider = ViewProvider(mark.value.markMapView)
                    mark.value.thumbnailUri = null
                    viewProvider.snapshot()
                    mark.value.placemark.setView(viewProvider)
                }
            } else {
                // Non-nullable image url
                val thumbnailUrl = liveMark.photos[0]
                if (mark.value.thumbnailUri != thumbnailUrl && value[thumbnailUrl] != null) {
                    mark.value.markMapView.setMarkBitmapImage(value[thumbnailUrl]!!)
                    val viewProvider = ViewProvider(mark.value.markMapView)
                    mark.value.thumbnailUri = thumbnailUrl
                    viewProvider.snapshot()
                    mark.value.placemark.setView(viewProvider)
                }
            }

            // Author avatar case
            // If author user is not still loaded
            if (mainFragmentViewModel.friendsLiveData.value!![liveMark.authorId] == null) {
                continue
            }
            val url =
                mainFragmentViewModel.friendsLiveData.value!![liveMark.authorId]!!.avatarUrl
            // If user doesn't have an avatar image
            if (url == null && mark.value.avatarUrl != null) {
                // Then there goes a placeholder
                mark.value.markMapView.setFriendBitmapPlaceHolder()
                val viewProvider = ViewProvider(mark.value.markMapView)
                mark.value.avatarUrl = null
                viewProvider.snapshot()
                mark.value.placemark.setView(viewProvider)
            } else {
                if (value[url] != null && url != mark.value.avatarUrl) {
                    mark.value.markMapView.setFriendBitmapImage(value[url]!!)
                    val viewProvider = ViewProvider(mark.value.markMapView)
                    mark.value.avatarUrl = url
                    viewProvider.snapshot()
                    mark.value.placemark.setView(viewProvider)
                }
            }
        }

        // Watch friends images change
        for (friend in usersViewState) {
            if (friend.key == MainFragmentViewModel.USER_ID) {
                continue
            }
            val url = mainFragmentViewModel.friendsLiveData.value!![friend.key]!!.avatarUrl
            if (url == null) {
                friend.value.friendMapView.setPlaceHolder()
                val viewProvider = ViewProvider(friend.value.friendMapView)
                viewProvider.snapshot()
                friend.value.placemark.setView(viewProvider)
                friend.value.avatarUri = null
            } else {
                if (url != friend.value.avatarUri && value[url] != null) {
                    friend.value.friendMapView.setBitmap(value[url]!!)
                    val viewProvider = ViewProvider(friend.value.friendMapView)
                    viewProvider.snapshot()
                    friend.value.placemark.setView(viewProvider)
                    friend.value.avatarUri = url
                }
            }
        }
    }

    companion object {
        const val TAG = "Main Fragment"
        const val COMPRESSION_FACTOR = 20
        const val MIN_ZOOM_SCALE = 15f
    }
}
