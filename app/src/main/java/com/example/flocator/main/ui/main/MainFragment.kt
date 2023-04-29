package com.example.flocator.main.ui.main

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.datastore.preferences.preferencesDataStore
import com.example.flocator.community.fragments.ProfileFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.flocator.authentication.authorization.AuthFragment
import com.example.flocator.common.config.SharedPreferencesContraction
import com.example.flocator.common.photo.PhotoState
import com.example.flocator.common.storage.db.entities.User
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.example.flocator.databinding.FragmentMainBinding
import com.example.flocator.main.MainSection
import com.example.flocator.main.config.BundleArgumentsContraction
import com.example.flocator.main.models.*
import com.example.flocator.main.ui.main.data.FriendViewDto
import com.example.flocator.main.ui.main.data.MarkGroupViewDto
import com.example.flocator.main.ui.main.data.MarkViewDto
import com.example.flocator.main.handlers.UserLocationHandler
import com.example.flocator.main.ui.add_mark.AddMarkFragment
import com.example.flocator.main.ui.main.data.MarkGroup
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

@AndroidEntryPoint
class MainFragment : Fragment(), MainSection {
    // Binding
    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() = _binding!!

    // ViewModel
    private val viewModel: MainFragmentViewModel by viewModels()

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

        val userId = requireActivity().getSharedPreferences(
            SharedPreferencesContraction.User.prefs_name,
            MODE_PRIVATE
        ).getLong(SharedPreferencesContraction.User.USER_ID, 0L)

        Log.d(TAG, "userId is $userId")

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

        val userId = extractUserIdFromSharedPreferences()

        if (userId == 0L) {
            FragmentNavigationUtils.openFragmentExcludingMain(
                requireActivity().supportFragmentManager,
                AuthFragment()
            )
            return
        }

        viewModel.requestUserData(userId)

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

    private fun extractUserIdFromSharedPreferences(): Long {
        val sharedPreferences = requireActivity().getSharedPreferences(
            SharedPreferencesContraction.User.prefs_name,
            MODE_PRIVATE
        )
        val userId = sharedPreferences.getLong(SharedPreferencesContraction.User.USER_ID, 0)
        if (userId == 0L) {
            Snackbar.make(binding.root, "Вы не авторизованы!", Snackbar.LENGTH_SHORT).show()
            return 0
        }
        return userId
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
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopPolling()
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
                viewModel.photoCacheLiveData.requestPhotoLoading(userInfo.avatarUri)
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
                val viewProvider = ViewProvider(friendView)
                usersViewState[id] = FriendViewDto(
                    usersCollection.addPlacemark(user.location, viewProvider),
                    friendView,
                    null
                )
                friendView.setUserName("${user.firstName} ${user.lastName}")
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
                    viewModel.photoCacheLiveData.requestPhotoLoading(user.avatarUrl)
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
                        val firstImage = photos[0].uri
                        if (!viewModel.photoCacheLiveData.containsUri(firstImage)) {
                            viewModel.photoCacheLiveData.requestPhotoLoading(firstImage)
                        } else {
                            markMapView.setMarkBitmapImage(
                                viewModel.photoCacheLiveData.getPhoto(firstImage)!!
                            )
                            viewProvider.snapshot()
                            marksViewState[id]!!.placemark.setView(viewProvider)
                        }
                    }
                    val avatar = marksViewState[id]!!.avatarUrl
                    if (avatar != null && !marksViewState[id]!!.markMapView.hasAvatar) {
                        viewModel.photoCacheLiveData.requestPhotoLoading(avatar)
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

    private fun onPhotoLoaded(value: kotlin.collections.Map<String, PhotoState>) {
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
                        val viewProvider = ViewProvider(user.friendMapView)
                        user.avatarUri = userInfo.avatarUri
                        viewProvider.snapshot()
                        user.placemark.setView(viewProvider)
                        usersViewState[userId] = user
                    } else if (photo is PhotoState.Failed) {
                        viewModel.photoCacheLiveData.requestPhotoLoading(user.avatarUri!!)
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
                if (mark.value.thumbnailUri != null) {
                    mark.value.markMapView.setMarkBitmapPlaceHolder()
                    val viewProvider = ViewProvider(mark.value.markMapView)
                    mark.value.thumbnailUri = null
                    viewProvider.snapshot()
                    mark.value.placemark.setView(viewProvider)
                }
            } else {
                // Non-nullable image url
                val thumbnailUrl = photos[0].uri
                if (value[thumbnailUrl] != null && mark.value.thumbnailUri != thumbnailUrl) {
                    val photo = value[thumbnailUrl]!!
                    if (photo is PhotoState.Loaded) {
                        mark.value.markMapView.setMarkBitmapImage(photo.bitmap)
                        val viewProvider = ViewProvider(mark.value.markMapView)
                        mark.value.thumbnailUri = thumbnailUrl
                        viewProvider.snapshot()
                        mark.value.placemark.setView(viewProvider)
                    } else if (photo is PhotoState.Failed) {
                        viewModel.photoCacheLiveData.requestPhotoLoading(thumbnailUrl)
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
                val viewProvider = ViewProvider(mark.value.markMapView)
                mark.value.avatarUrl = null
                viewProvider.snapshot()
                mark.value.placemark.setView(viewProvider)
            } else {
                if (url != null && value[url] != null && url != mark.value.avatarUrl) {
                    val photo = value[url]!!
                    if (photo is PhotoState.Loaded) {
                        mark.value.markMapView.setFriendBitmapImage(photo.bitmap)
                        val viewProvider = ViewProvider(mark.value.markMapView)
                        mark.value.avatarUrl = url
                        viewProvider.snapshot()
                        mark.value.placemark.setView(viewProvider)
                    } else if (photo is PhotoState.Failed) {
                        viewModel.photoCacheLiveData.requestPhotoLoading(url)
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
                val viewProvider = ViewProvider(friend.value.friendMapView)
                viewProvider.snapshot()
                friend.value.placemark.setView(viewProvider)
                friend.value.avatarUri = null
            } else {
                if (url != friend.value.avatarUri && value[url] != null) {
                    val photo = value[url]!!
                    if (photo is PhotoState.Loaded) {
                        friend.value.friendMapView.setBitmap(photo.bitmap)
                        val viewProvider = ViewProvider(friend.value.friendMapView)
                        viewProvider.snapshot()
                        friend.value.placemark.setView(viewProvider)
                        friend.value.avatarUri = url
                    } else {
                        viewModel.photoCacheLiveData.requestPhotoLoading(url)
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "Main Fragment"
        const val MIN_ZOOM_SCALE = 15f
    }
}
