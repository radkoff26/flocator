package ru.flocator.core_map.ui

import android.animation.ValueAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.animation.doOnEnd
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.core_database.entities.MarkWithPhotos
import ru.flocator.core_database.entities.User
import ru.flocator.core_map.domain.comparing.MapItemsCompareCallbacks
import ru.flocator.core_map.domain.configuration.MapConfiguration
import ru.flocator.core_map.domain.difference.Difference
import ru.flocator.core_map.domain.difference.MapItemsDifferenceCalculator
import ru.flocator.core_map.domain.dto.MarkGroup
import ru.flocator.core_map.domain.dto.MarkGroupViewDto
import ru.flocator.core_map.domain.dto.MarkViewDto
import ru.flocator.core_map.domain.dto.UserViewDto
import ru.flocator.core_map.domain.map.*
import ru.flocator.core_map.domain.map_item.MapItem
import ru.flocator.core_map.ui.views.MarkGroupView
import ru.flocator.core_map.ui.views.MarkView
import ru.flocator.core_map.ui.views.UserView
import ru.flocator.core_map.utils.DisposableMapItemsUtils
import ru.flocator.core_map.view_models.FLocatorMapFragmentViewModel
import kotlin.math.max

class FLocatorMapFragment :
    SupportMapFragment(),
    FLocatorMap {
    private var _map: GoogleMap? = null
    private val map: GoogleMap
        get() = _map!!

    private var targetUserState: UserViewDto? = null
    private val usersState: MutableMap<Long, UserViewDto> = mutableMapOf()
    private val marksState: MutableMap<Long, MarkViewDto> = mutableMapOf()
    private val markGroupsState: MutableList<MarkGroupViewDto> = mutableListOf()

    private var loadPhotoCallback: LoadPhotoCallback? = null
    private var onFriendViewClickCallback: OnFriendViewClickCallback? = null
    private var onMarkViewClickCallback: OnMarkViewClickCallback? = null
    private var onMarkGroupViewClickCallback: OnMarkGroupViewClickCallback? = null

    private val viewModel: FLocatorMapFragmentViewModel by viewModels()

    // Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getMapAsync {
            _map = it
            map.setOnCameraMoveStartedListener { reason ->
                if (reason == OnCameraMoveStartedListener.REASON_GESTURE) {
                    viewModel.fixCamera()
                }
            }
            map.setOnCameraMoveListener {
                viewModel.updateVisibleRegion(it.projection.visibleRegion)
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(CAMERA_POSITION)) {
                val cameraPosition = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    savedInstanceState.getParcelable(CAMERA_POSITION, CameraPosition::class.java)
                } else {
                    savedInstanceState.getParcelable(CAMERA_POSITION) as CameraPosition?
                }!!
                getMapAsync {
                    it.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeWidths()

        subscribeToTargetUserInfo()
        subscribeToVisibleUsers()
        subscribeToVisibleMarks()
        subscribeToTargetUserLocation()
        subscribeToCameraStatus()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (_map != null) {
            outState.putParcelable(CAMERA_POSITION, map.cameraPosition)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _map = null
    }

    // Subscriptions
    private fun subscribeToTargetUserInfo() {
        viewModel.userInfoLiveData.observe(viewLifecycleOwner) {
            if (it == null || viewModel.userLocationLiveData.value == null) {
                return@observe
            }
            val userLocation = viewModel.userLocationLiveData.value!!
            val userViewDto = targetUserState ?: UserViewDto(
                UserView(
                    requireContext(),
                    isTargetUser = true
                ).apply {
                    setUserName(
                        resources.getString(ru.flocator.core_map.R.string.user_name_on_map, it.firstName, it.lastName)
                    )
                },
                User(
                    it.userId,
                    it.firstName,
                    it.lastName,
                    userLocation,
                    it.avatarUri
                )
            )
            val avatar = it.avatarUri
            val userView = userViewDto.userView
            if (avatar != null) {
                userViewDto.avatarRequestDisposable = loadPhotoCallback?.invoke(avatar)
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe { bitmap ->
                        userView.setAvatarBitmap(bitmap, avatar)
                        updateMapItemOnMap(userViewDto)
                    }
            } else {
                userView.setAvatarPlaceHolder()
                updateMapItemOnMap(userViewDto)
            }
            targetUserState = userViewDto
            drawMapItemOnMap(
                userViewDto,
                userView
            )
        }
    }

    private fun subscribeToVisibleUsers() {
        viewModel.visibleUsersLiveData.observe(viewLifecycleOwner) {
            CoroutineScope(Dispatchers.IO).launch {
                val usersDifference = makeUsersDifference(it)
                requireActivity().runOnUiThread {
                    usersDifference.dispatchDifferenceTo(
                        this@FLocatorMapFragment,
                        this@FLocatorMapFragment::setNewUserViewAndProvideIt
                    ) {
                        usersState.remove(it.user.id)
                    }
                }
            }
        }
    }

    private fun subscribeToVisibleMarks() {
        viewModel.visibleMarksLiveData.observe(viewLifecycleOwner) {
            CoroutineScope(Dispatchers.IO).launch {
                val singleMarksDifference = makeSingleMarksDifference(it)
                val groupMarksDifference = makeGroupMarksDifference(it)
                requireActivity().runOnUiThread {
                    singleMarksDifference?.dispatchDifferenceTo(
                        this@FLocatorMapFragment,
                        this@FLocatorMapFragment::setNewMarkViewAndProvideIt
                    ) {
                        marksState.remove(it.mark.mark.markId)
                    }
                    groupMarksDifference.dispatchDifferenceTo(
                        this@FLocatorMapFragment,
                        this@FLocatorMapFragment::setNewMarkGroupViewAndProvideIt,
                        this@FLocatorMapFragment::removeMarkGroup
                    )
                }
            }
        }
    }

    private fun subscribeToTargetUserLocation() {
        viewModel.userLocationLiveData.observe(viewLifecycleOwner) {
            if (it == null || viewModel.userInfoLiveData.value == null || targetUserState == null || targetUserState!!.marker == null) {
                return@observe
            }
            targetUserState!!.marker!!.position = it
            val userId = viewModel.userInfoLiveData.value!!.userId
            val cameraStatus = viewModel.cameraStatusLiveData.value!!
            if (!cameraStatus.isCameraFixed && cameraStatus.userId == userId) {
                moveCameraTo(it)
            }
        }
    }

    private fun subscribeToCameraStatus() {
        viewModel.cameraStatusLiveData.observe(viewLifecycleOwner) {
            if (!it.isCameraFixed) {
                moveCameraTo(it.latLng!!)
            }
        }
    }

    // Difference calculation
    private fun makeUsersDifference(users: List<User>): Difference<UserViewDto> {
        val newUsers = users.map { UserViewDto(UserView(requireContext()), it) }
        val currentUsers = usersState.filter {
            it.value.marker != null
        }.map(
            // TODO: get rid of this entity creation overhead
            Map.Entry<Long, UserViewDto>::value
        )
        return MapItemsDifferenceCalculator.calculateDifference(
            currentUsers,
            newUsers,
            MapItemsCompareCallbacks.UserCompareCallback
        )
    }

    private fun makeSingleMarksDifference(newMarks: List<MarkGroup>): Difference<MarkViewDto>? {
        if (viewModel.userInfoLiveData.value == null) {
            return null
        }
        val userInfo = viewModel.userInfoLiveData.value!!
        val userId = userInfo.userId
        val friends = viewModel.allFriends
        val singleMarks = newMarks.filter { item ->
            item.marks.size == 1
        }.map { markGroup ->
            // TODO: get rid of this entity creation overhead
            val mark = markGroup.marks[0]
            val authorId = mark.mark.authorId
            val avatar = if (authorId == userId) {
                userInfo.avatarUri
            } else {
                friends[authorId]?.avatarUri
            }
            MarkViewDto(
                MarkView(
                    requireContext(),
                    isTargetUserMark = authorId == userId
                ),
                mark,
                avatar
            )
        }
        val currentSingleMarksList = marksState
            .filter { entry ->
                entry.value.marker != null
            }.map(Map.Entry<Long, MarkViewDto>::value)
        return MapItemsDifferenceCalculator.calculateDifference(
            currentSingleMarksList,
            singleMarks,
            MapItemsCompareCallbacks.MarkCompareCallback
        )
    }

    private fun makeGroupMarksDifference(newMarks: List<MarkGroup>): Difference<MarkGroupViewDto> {
        val groupMarks = newMarks.filter { item ->
            item.marks.size > 1
        }.map { markGroup ->
            // TODO: get rid of this entity creation overhead
            MarkGroupViewDto(
                MarkGroupView(requireContext()),
                markGroup
            )
        }
        val currentMarkGroups = markGroupsState.filter { dto ->
            dto.marker != null
        }
        return MapItemsDifferenceCalculator.calculateDifference(
            currentMarkGroups,
            groupMarks,
            MapItemsCompareCallbacks.MarkGroupCompareCallback
        )
    }

    // FLocatorMap interface implementation
    override fun isMapCreated(): Boolean = _map != null

    override fun initialize(
        loadPhotoCallback: LoadPhotoCallback?,
        onFriendViewClickCallback: OnFriendViewClickCallback?,
        onMarkViewClickCallback: OnMarkViewClickCallback?,
        onMarkGroupViewClickCallback: OnMarkGroupViewClickCallback?
    ) {
        this.loadPhotoCallback = loadPhotoCallback
        this.onFriendViewClickCallback = onFriendViewClickCallback
        this.onMarkViewClickCallback = onMarkViewClickCallback
        this.onMarkGroupViewClickCallback = onMarkGroupViewClickCallback

        getMapAsync { createdMap ->
            createdMap.setOnMarkerClickListener {
                val id = it.id
                // User marker case
                usersState.forEach { entry ->
                    val userId = entry.value.user.id
                    if (entry.value.marker?.id == id && userId != viewModel.userInfoLiveData.value?.userId) {
                        followUser(userId)
                        onFriendViewClickCallback?.invoke(userId)
                        return@setOnMarkerClickListener true
                    }
                }
                // Mark marker case
                marksState.forEach { entry ->
                    if (entry.value.marker?.id == id) {
                        onMarkViewClickCallback?.invoke(entry.value.mark.mark.markId)
                        return@setOnMarkerClickListener true
                    }
                }
                // Mark group marker case
                markGroupsState.forEach { entry ->
                    if (entry.marker?.id == id) {
                        onMarkGroupViewClickCallback?.invoke(entry.markGroup.marks)
                    }
                }
                true
            }
        }
    }

    override fun submitUser(userInfo: UserInfo) {
        viewModel.setUserInfo(userInfo)
    }

    override fun submitFriends(friends: List<User>) {
        viewModel.setFriends(friends)
    }

    override fun submitMarks(marks: List<MarkWithPhotos>) {
        viewModel.setMarks(marks)
    }

    override fun updateUserLocation(location: LatLng) {
        viewModel.setUserLocation(location)
    }

    override fun moveCameraTo(latLng: LatLng) {
        getMapAsync {
            it.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition(
                        latLng,
                        max(map.cameraPosition.zoom, MIN_ZOOM_SCALE),
                        map.cameraPosition.tilt,
                        map.cameraPosition.bearing
                    )
                )
            )
        }
    }

    override fun followUser(userId: Long) {
        val user = viewModel.allFriends[userId]
        if (user != null) {
            viewModel.makeCameraFollowUser(userId, user.location)
        } else {
            if (viewModel.userInfoLiveData.value?.userId == userId) {
                val location = viewModel.userLocationLiveData.value
                if (location != null) {
                    viewModel.makeCameraFollowUser(userId, location)
                }
            }
        }
    }

    override fun changeConfiguration(mapConfiguration: MapConfiguration) {
        viewModel.changeMapConfigurationTo(mapConfiguration)
    }

    // Widths calculation
    private fun initializeWidths() {
        val mapView = requireView()
        val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewModel.setWidths(
                    mapView.width.toFloat(),
                    ru.flocator.core_utils.ViewUtils.dpToPx(56, requireContext()).toFloat()
                )
                mapView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }
        mapView.viewTreeObserver.addOnGlobalLayoutListener(listener)
    }

    // Internal methods for difference dispatching
    internal fun drawMapItemOnMap(
        mapItem: MapItem,
        bitmapCreator: BitmapCreator
    ) {
        if (_map == null) {
            return
        }
        val bitmap = bitmapCreator.createBitmap()
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
        val marker = map.addMarker(
            MarkerOptions()
                .position(mapItem.getLocation())
                .icon(bitmapDescriptor)
                .alpha(0f)
        )
        animateFadingIn(marker!!)
        mapItem.setItemMarker(marker)
    }

    internal fun updateMapItemOnMap(mapItem: MapItem) {
        if (_map == null) {
            return
        }
        val bitmap = mapItem.getBitmapCreator().createBitmap()
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
        mapItem.getItemMarker()?.setIcon(bitmapDescriptor)
        mapItem.getItemMarker()?.position = mapItem.getLocation()
    }

    internal fun <T : MapItem> removeMapItemFromMap(
        mapItem: T,
        onRemoveMapItemCallback: ((obj: T) -> Unit)? = null
    ) {
        if (_map == null) {
            return
        }
        val marker = mapItem.getItemMarker()
        if (marker != null) {
            animateFadingOut(marker) {
                marker.remove()
                mapItem.setItemMarker(null)
                onRemoveMapItemCallback?.invoke(mapItem)
            }
        }
    }

    // Animations
    private fun animateFadingIn(marker: Marker) {
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                marker.alpha = it.animatedValue as Float
            }
            duration = 250
        }
        valueAnimator.start()
    }

    private fun animateFadingOut(marker: Marker, doOnEndCallback: () -> Unit) {
        val valueAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
            addUpdateListener {
                marker.alpha = it.animatedValue as Float
            }
            duration = 250
            doOnEnd {
                doOnEndCallback.invoke()
            }
        }
        valueAnimator.start()
    }

    // State modification
    private fun setNewUserViewAndProvideIt(dto: UserViewDto): BitmapCreator {
        val userId = dto.user.id
        val userViewDto = usersState[userId] ?: dto

        DisposableMapItemsUtils.disposeItem(userViewDto)

        val userView = userViewDto.userView
        val avatar = userViewDto.user.avatarUri

        if (avatar != null) {
            userViewDto.avatarRequestDisposable = loadPhotoCallback?.invoke(avatar)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { bitmap ->
                    userView.setAvatarBitmap(bitmap, avatar)
                    updateMapItemOnMap(userViewDto)
                }
        } else {
            userView.setAvatarPlaceHolder()
        }

        userView.setUserName(
            resources.getString(
                ru.flocator.core_map.R.string.user_name_on_map,
                dto.user.firstName,
                dto.user.lastName
            )
        )

        usersState[userId] = userViewDto

        val cameraStatus = viewModel.cameraStatusLiveData.value!!

        if (!cameraStatus.isCameraFixed && cameraStatus.userId == userId) {
            viewModel.updateFollowingCameraLocation(userViewDto.getLocation())
        }

        return userView
    }

    private fun setNewMarkViewAndProvideIt(dto: MarkViewDto): BitmapCreator {
        val markId = dto.mark.mark.markId
        val markViewDto = marksState[markId] ?: dto
        val markView = markViewDto.markView
        val thumbnail = if (markViewDto.mark.photos.isEmpty()) {
            null
        } else {
            markViewDto.mark.photos[0].uri
        }
        val avatar = markViewDto.userAvatarUri

        DisposableMapItemsUtils.disposeItem(markViewDto)

        if (thumbnail != null) {
            markViewDto.thumbnailRequestDisposable = loadPhotoCallback?.invoke(thumbnail)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { bitmap ->
                    markViewDto.markView.setMarkBitmapImage(bitmap, thumbnail)
                    updateMapItemOnMap(markViewDto)
                }
        } else {
            markViewDto.markView.setMarkBitmapPlaceHolder()
        }

        if (avatar != null) {
            markViewDto.avatarRequestDisposable = loadPhotoCallback?.invoke(avatar)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { bitmap ->
                    markViewDto.markView.setAuthorBitmapImage(bitmap, avatar)
                    updateMapItemOnMap(markViewDto)
                }
        } else {
            markViewDto.markView.setAuthorBitmapPlaceHolder()
        }

        marksState[markId] = markViewDto

        return markView
    }

    private fun setNewMarkGroupViewAndProvideIt(dto: MarkGroupViewDto): BitmapCreator {
        markGroupsState.add(dto)
        dto.markGroupView.setCount(
            dto.markGroup.marks.size
        )
        return dto.markGroupView
    }

    private fun removeMarkGroup(dto: MarkGroupViewDto) {
        markGroupsState.removeIf {
            it.markGroup.center == dto.markGroup.center
        }
    }

    companion object {
        const val TAG = "FLocatorMapFragment"
        const val MIN_ZOOM_SCALE = 15f
        const val CAMERA_POSITION = "CAMERA_POSITION"
    }
}
