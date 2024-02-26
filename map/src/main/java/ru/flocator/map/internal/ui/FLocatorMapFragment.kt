package ru.flocator.map.internal.ui

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.animation.doOnEnd
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import ru.flocator.core.utils.ViewUtils
import ru.flocator.data.database.entities.User
import ru.flocator.data.models.location.Coordinates
import ru.flocator.map.R
import ru.flocator.map.api.*
import ru.flocator.map.api.configuration.MapConfiguration
import ru.flocator.map.api.entity.MapMark
import ru.flocator.map.internal.data.comparing.MapItemsCompareCallbacks
import ru.flocator.map.internal.data.difference.Difference
import ru.flocator.map.internal.data.difference.MapItemsDifferenceCalculator
import ru.flocator.map.internal.data.entity.MarkGroup
import ru.flocator.map.internal.data.holder.*
import ru.flocator.map.internal.data.marker.MarkerStore
import ru.flocator.map.internal.data.view.ViewPool
import ru.flocator.map.internal.extensions.toCoordinates
import ru.flocator.map.internal.extensions.toLatLng
import ru.flocator.map.internal.ui.views.MarkGroupView
import ru.flocator.map.internal.ui.views.MarkView
import ru.flocator.map.internal.ui.views.UserView
import ru.flocator.map.internal.utils.DisposableMapItemsUtils
import ru.flocator.map.internal.view_models.FLocatorMapFragmentViewModel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.thread
import kotlin.math.max

internal class FLocatorMapFragment : SupportMapFragment(), FLocatorMap {
    private var targetUserState: UserHolder? = null
    private val usersState: MutableMap<Long, UserHolder> = ConcurrentHashMap()
    private val marksState: MutableMap<Long, MarkHolder> = ConcurrentHashMap()
    private val markGroupsState: MutableList<MarkGroupHolder> = CopyOnWriteArrayList()

    private var loadPhotoCallback: LoadPhotoCallback? = null
    private var onFriendViewClickCallback: OnFriendViewClickCallback? = null
    private var onMarkViewClickCallback: OnMarkViewClickCallback? = null
    private var onMarkGroupViewClickCallback: OnMarkGroupViewClickCallback? = null

    private val viewModel: FLocatorMapFragmentViewModel by viewModels()

    private lateinit var userViewPool: ViewPool<UserView>
    private lateinit var markViewPool: ViewPool<MarkView>
    private lateinit var markGroupViewPool: ViewPool<MarkGroupView>

    private val markerStore: MarkerStore = MarkerStore()

    private val marksDispatchReadWriteLock: ReadWriteLock = ReentrantReadWriteLock(true)
    private val usersDispatchReadWriteLock: ReadWriteLock = ReentrantReadWriteLock(true)

    private val marksDispatchWriteLock = marksDispatchReadWriteLock.writeLock()
    private val usersDispatchWriteLock = usersDispatchReadWriteLock.writeLock()

    private val marksDispatchReadLock = marksDispatchReadWriteLock.readLock()
    private val usersDispatchReadLock = usersDispatchReadWriteLock.readLock()

    @Volatile
    private var marksDispatchCount = 0

    @Volatile
    private var usersDispatchCount = 0

    // Lifecycle
    @Suppress("DEPRECATION")
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

        getMapAsync {
            markerStore.setMap(it)
            markerStore.setLifecycleOwner(viewLifecycleOwner)

            // Invalidating objects on map if ViewModel has data for it
            if (viewModel.hasTargetUser()) {
                invalidateTargetUserOnMap(viewModel.targetUserLiveData.value)
            }

            if (viewModel.hasFriends()) {
                invalidateFriendsOnMap(viewModel.visibleUsersLiveData.value!!)
            }

            if (viewModel.hasMarks()) {
                invalidateMarksOnMap(viewModel.visibleMarksLiveData.value!!)
            }

            it.setOnCameraMoveStartedListener { reason ->
                if (reason == OnCameraMoveStartedListener.REASON_GESTURE) {
                    viewModel.fixCamera()
                }
            }
            it.setOnCameraMoveListener {
                viewModel.updateVisibleRegion(it.projection.visibleRegion)
            }
        }

        userViewPool = ViewPool(
            viewLifecycleOwner,
            UserView.Factory(context)
        )

        markViewPool = ViewPool(
            viewLifecycleOwner,
            MarkView.Factory(context)
        )

        markGroupViewPool = ViewPool(
            viewLifecycleOwner,
            MarkGroupView.Factory(context)
        )

        initializeWidths()

        subscribeToTargetUserInfo()
        subscribeToVisibleUsers()
        subscribeToVisibleMarks()
        subscribeToCameraStatus()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        getMapAsync {
            outState.putParcelable(CAMERA_POSITION, it.cameraPosition)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        targetUserState = null
        usersState.clear()
        marksState.clear()
        markGroupsState.clear()
    }

    // Subscriptions
    private fun subscribeToTargetUserInfo() {
        viewModel.targetUserLiveData.observe(
            viewLifecycleOwner,
            this::invalidateTargetUserOnMap
        )
    }

    private fun subscribeToVisibleUsers() {
        viewModel.visibleUsersLiveData.observe(
            viewLifecycleOwner,
            this::invalidateFriendsOnMap
        )
    }

    private fun subscribeToVisibleMarks() {
        viewModel.visibleMarksLiveData.observe(
            viewLifecycleOwner,
            this::invalidateMarksOnMap
        )
    }

    private fun subscribeToCameraStatus() {
        viewModel.cameraStatusLiveData.observe(viewLifecycleOwner) {
            if (!it.isCameraFixed) {
                moveCameraTo(it.latLng!!.toCoordinates())
            }
        }
    }

    // Map objects invalidation
    private fun invalidateTargetUserOnMap(value: User?) {
        if (value == null || !markerStore.isActive()) {
            return
        }

        val userViewHolder =
            targetUserState?.apply { user = value } ?: composeUserHolderAndDrawUserOnMap(
                value,
                true
            )

        targetUserState = updateUserHolder(userViewHolder)
    }

    private fun invalidateFriendsOnMap(value: List<User>) {
        thread {
            usersDispatchWriteLock.lock()
            val currentCount = usersDispatchCount + 1
            usersDispatchCount = currentCount
            usersDispatchWriteLock.unlock()

            val usersDifference = makeUsersDifference(value)

            usersDispatchReadLock.lock()
            val newCount = usersDispatchCount
            usersDispatchReadLock.unlock()

            if (newCount != currentCount || !markerStore.isActive()) {
                return@thread
            }
            requireActivity().runOnUiThread {
                usersDifference.dispatchDifferenceTo(
                    onAddMapItemCallback =
                    {
                        val holder = composeUserHolderAndDrawUserOnMap(it, false)
                        usersState[it.userId] = holder
                    },
                    onUpdateMapItemCallback =
                    {
                        val userId = it.user.userId
                        val holder = updateUserHolder(it)
                        usersState[userId] = holder
                    },
                    onRemoveMapItemCallback =
                    {
                        val userId = it.user.userId
                        val markerId = it.markerId
                        val marker = markerStore.getMarker(markerId)
                        DisposableMapItemsUtils.disposeItem(it)
                        if (marker != null) {
                            animateFadingOut(marker) {
                                markerStore.removeMarker(markerId)
                            }
                        }
                        val view = it.userViewHolder.userView
                        usersState.remove(userId)
                        view.recycle()
                    }
                )
            }
        }
    }

    private fun invalidateMarksOnMap(value: List<MarkGroup>) {
        // If user info is not yet assigned
        if (viewModel.targetUserLiveData.value == null) {
            // Then it's no right to init markers since it's likely to be impossible to
            // find out which mark is target user's
            return
        }
        thread {
            marksDispatchWriteLock.lock()
            val currentCount = marksDispatchCount + 1
            marksDispatchCount = currentCount
            marksDispatchWriteLock.unlock()

            val singleMarksDifference = makeSingleMarksDifference(value)
            val groupMarksDifference = makeGroupMarksDifference(value)

            marksDispatchReadLock.lock()
            val newCount = marksDispatchCount
            marksDispatchReadLock.unlock()

            if (newCount != currentCount || !markerStore.isActive()) {
                return@thread
            }
            requireActivity().runOnUiThread {
                singleMarksDifference.dispatchDifferenceTo(
                    onAddMapItemCallback =
                    {
                        if (viewModel.targetUserLiveData.value == null) {
                            return@dispatchDifferenceTo
                        }
                        val targetUserId = viewModel.targetUserLiveData.value!!.userId
                        val holder = composeMarkHolderAndDrawMarkOnMap(
                            it,
                            targetUserId == it.authorId
                        )
                        marksState[it.markId] = holder
                    },
                    onUpdateMapItemCallback =
                    {
                        val markId = it.mark.markId
                        val holder = updateMarkHolder(it)
                        marksState[markId] = holder
                    },
                    onRemoveMapItemCallback =
                    {
                        val markId = it.mark.markId
                        val markerId = it.markerId
                        val marker = markerStore.getMarker(markerId)
                        DisposableMapItemsUtils.disposeItem(it)
                        if (marker != null) {
                            animateFadingOut(marker) {
                                markerStore.removeMarker(markerId)
                            }
                        }
                        val view = it.markViewHolder.markView
                        marksState.remove(markId)
                        view.recycle()
                    }
                )
                groupMarksDifference.dispatchDifferenceTo(
                    onAddMapItemCallback =
                    {
                        val holder = composeMarkGroupHolderAndDrawMarkGroupOnMap(it)
                        markGroupsState.add(holder)
                    },
                    onUpdateMapItemCallback =
                    {
                        updateMarkGroupHolder(it)
                    },
                    onRemoveMapItemCallback =
                    {
                        val markerId = it.markerId
                        val marker = markerStore.getMarker(markerId)
                        if (marker != null) {
                            animateFadingOut(marker) {
                                markerStore.removeMarker(markerId)
                            }
                        }
                        val view = it.markGroupViewHolder.markGroupView
                        markGroupsState.removeIf { holder ->
                            it.markGroup.marks.size == holder.markGroup.marks.size
                                    && it.markGroup.center == holder.markGroup.center
                        }
                        view.recycle()
                    }
                )
            }
        }
    }

    // Holder composition methods
    private fun composeUserHolderAndDrawUserOnMap(
        user: User,
        isTargetUser: Boolean = false
    ): UserHolder {
        val userViewHolder = LocatedUserViewHolder(
            userViewPool.getView {
                it.setUserName(
                    resources.getString(
                        R.string.user_name_on_map,
                        user.firstName,
                        user.lastName
                    )
                )
                it.setTargetUser(isTargetUser)
            },
            user.location.toLatLng()
        )
        val markerId = markerStore.createMarker(
            userViewHolder,
            this::animateFadingIn
        )
        return UserHolder(
            userViewHolder,
            user,
            markerId!!
        ).apply {
            val avatar = this.user.avatarUri

            if (userViewHolder.userView.avatarUri != avatar) {
                if (avatar != null) {
                    avatarRequestDisposable = startLoadingImage(avatar) {
                        userViewHolder.userView.setAvatarBitmap(it, avatar)
                        markerStore.updateMarker(this)
                    }
                }
                userViewHolder.userView.setAvatarPlaceHolder()
            }

            markerStore.updateMarker(this)
        }
    }

    private fun composeMarkHolderAndDrawMarkOnMap(
        mark: MapMark,
        isTargetUserMark: Boolean = false
    ): MarkHolder {
        val markViewHolder = LocatedMarkViewHolder(
            markViewPool.getView {
                it.setTargetUser(isTargetUserMark)
            },
            mark.location.toLatLng()
        )
        val markerId = markerStore.createMarker(
            markViewHolder,
            this::animateFadingIn
        )
        return MarkHolder(
            markViewHolder,
            mark,
            markerId!!
        ).apply {
            val thumbnail = mark.thumbnailUri

            if (markViewHolder.markView.markImageUri != thumbnail) {
                if (thumbnail != null) {
                    thumbnailRequestDisposable = startLoadingImage(thumbnail) {
                        this.markViewHolder.markView.setMarkBitmapImage(it, thumbnail)
                        markerStore.updateMarker(this)
                    }
                }
                this.markViewHolder.markView.setMarkBitmapPlaceHolder()
            }

            val avatar = mark.authorAvatarUri

            if (markViewHolder.markView.authorImageUri != avatar) {
                if (avatar != null) {
                    avatarRequestDisposable = startLoadingImage(avatar) {
                        this.markViewHolder.markView.setAuthorBitmapImage(it, avatar)
                        markerStore.updateMarker(this)
                    }
                }
                this.markViewHolder.markView.setAuthorBitmapPlaceHolder()
            }

            markerStore.updateMarker(this)
        }
    }

    private fun composeMarkGroupHolderAndDrawMarkGroupOnMap(markGroup: MarkGroup): MarkGroupHolder {
        val markGroupViewHolder = LocatedMarkGroupViewHolder(
            markGroupViewPool.getView {
                it.setCount(markGroup.marks.size)
            },
            markGroup.center
        )
        val markerId = markerStore.createMarker(
            markGroupViewHolder,
            this::animateFadingIn
        )
        return MarkGroupHolder(
            markGroupViewHolder,
            markGroup,
            markerId!!
        )
    }

    // Update holder functions
    private fun updateUserHolder(userHolder: UserHolder): UserHolder {
        val user = userHolder.user
        val userView = userHolder.userViewHolder.userView
        val avatar = user.avatarUri

        if (userView.avatarUri != avatar) {
            DisposableMapItemsUtils.disposeItem(userHolder)

            if (avatar != null) {
                userHolder.avatarRequestDisposable = startLoadingImage(avatar) {
                    userView.setAvatarBitmap(it, avatar)
                    markerStore.updateMarker(userHolder)
                }
            } else {
                userView.setAvatarPlaceHolder()
            }
        }

        markerStore.updateMarker(userHolder)

        return userHolder
    }

    private fun updateMarkHolder(markHolder: MarkHolder): MarkHolder {
        val markView = markHolder.markViewHolder.markView
        val thumbnail = markHolder.mark.thumbnailUri
        val avatar = markHolder.mark.authorAvatarUri

        DisposableMapItemsUtils.disposeItem(markHolder)

        if (thumbnail != null) {
            // If image was already loading and it's different from the current one
            if (markView.markImageUri != thumbnail
                && markHolder.thumbnailRequestDisposable != null
                && !markHolder.thumbnailRequestDisposable!!.isDisposed
            ) {
                // Then its loading gets stopped
                markHolder.thumbnailRequestDisposable!!.dispose()
            }
            markHolder.thumbnailRequestDisposable = startLoadingImage(thumbnail) {
                markView.setMarkBitmapImage(it, thumbnail)
                markerStore.updateMarker(markHolder)
            }
        } else {
            markView.setMarkBitmapPlaceHolder()
        }

        if (avatar != null) {
            // If image was already loading and it's different from the current one
            if (markView.authorImageUri != avatar
                && markHolder.avatarRequestDisposable != null
                && !markHolder.avatarRequestDisposable!!.isDisposed
            ) {
                // Then its loading gets stopped
                markHolder.avatarRequestDisposable!!.dispose()
            }
            markHolder.avatarRequestDisposable = startLoadingImage(avatar) {
                markView.setAuthorBitmapImage(it, avatar)
                markerStore.updateMarker(markHolder)
            }
        } else {
            markView.setAuthorBitmapPlaceHolder()
        }

        markerStore.updateMarker(markHolder)

        return markHolder
    }

    private fun updateMarkGroupHolder(markGroupHolder: MarkGroupHolder) {
        val markGroupView = markGroupHolder.markGroupViewHolder.markGroupView
        markGroupView.setCount(markGroupHolder.markGroup.marks.size)
        markerStore.updateMarker(markGroupHolder)
    }

    // Difference calculation
    private fun makeUsersDifference(users: List<User>): Difference<UserHolder, User> {
        val currentUsers = usersState.map(
            Map.Entry<Long, UserHolder>::value
        )
        return MapItemsDifferenceCalculator.calculateDifference(
            currentUsers,
            users,
            MapItemsCompareCallbacks.UserCompareCallback
        )
    }

    private fun makeSingleMarksDifference(newMarks: List<MarkGroup>): Difference<MarkHolder, MapMark> {
        val singleMarks = newMarks.filter { item ->
            item.marks.size == 1
        }.map { markGroup ->
            markGroup.marks[0]
        }
        val currentSingleMarksList = marksState.map(Map.Entry<Long, MarkHolder>::value)
        return MapItemsDifferenceCalculator.calculateDifference(
            currentSingleMarksList,
            singleMarks,
            MapItemsCompareCallbacks.MarkCompareCallback
        )
    }

    private fun makeGroupMarksDifference(newMarks: List<MarkGroup>): Difference<MarkGroupHolder, MarkGroup> {
        val groupMarks = newMarks.filter { item ->
            item.marks.size > 1
        }
        return MapItemsDifferenceCalculator.calculateDifference(
            markGroupsState,
            groupMarks,
            MapItemsCompareCallbacks.MarkGroupCompareCallback
        )
    }

    // FLocatorMap interface implementation
    override fun initialize(
        mapConfiguration: MapConfiguration,
        loadPhotoCallback: LoadPhotoCallback?,
        onFriendViewClickCallback: OnFriendViewClickCallback?,
        onMarkViewClickCallback: OnMarkViewClickCallback?,
        onMarkGroupViewClickCallback: OnMarkGroupViewClickCallback?
    ) {
        viewModel.changeMapConfigurationTo(mapConfiguration)
        this.loadPhotoCallback = loadPhotoCallback
        this.onFriendViewClickCallback = onFriendViewClickCallback
        this.onMarkViewClickCallback = onMarkViewClickCallback
        this.onMarkGroupViewClickCallback = onMarkGroupViewClickCallback

        getMapAsync { createdMap ->
            createdMap.setOnMarkerClickListener {
                val id = it.id
                // User marker case
                usersState.forEach { entry ->
                    with(entry.value) {
                        val userId = user.userId
                        if (markerId == id && userId != viewModel.targetUserLiveData.value?.userId) {
                            followUser(userId)
                            onFriendViewClickCallback?.invoke(userId)
                            return@setOnMarkerClickListener true
                        }
                    }
                }
                // Mark marker case
                marksState.forEach { entry ->
                    with(entry.value) {
                        if (markerId == id) {
                            onMarkViewClickCallback?.invoke(mark.markId)
                            return@setOnMarkerClickListener true
                        }
                    }
                }
                // Mark group marker case
                markGroupsState.forEach { entry ->
                    with(entry) {
                        if (markerId == id) {
                            onMarkGroupViewClickCallback?.invoke(
                                markGroup.marks.map(MapMark::markId)
                            )
                        }
                    }
                }
                true
            }
        }
    }

    override fun submitUser(user: User) {
        viewModel.setUserInfo(user)
    }

    override fun submitFriends(friends: List<User>) {
        viewModel.setFriends(friends)
    }

    override fun submitMarks(marks: List<MapMark>) {
        viewModel.setMarks(marks)
    }

    override fun updateUserLocation(location: Coordinates) {
        viewModel.setUserLocation(location.toLatLng())
    }

    override fun moveCameraTo(latLng: Coordinates) {
        getMapAsync {
            it.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition(
                        latLng.toLatLng(),
                        max(it.cameraPosition.zoom, MIN_ZOOM_SCALE),
                        it.cameraPosition.tilt,
                        it.cameraPosition.bearing
                    )
                )
            )
        }
    }

    override fun followUser(userId: Long) {
        val user = viewModel.allFriends[userId]
        if (user != null) {
            viewModel.makeCameraFollowUser(userId, user.location.toLatLng())
        } else {
            val targetUser = viewModel.targetUserLiveData.value
            if (targetUser != null && targetUser.userId == userId) {
                viewModel.makeCameraFollowUser(userId, targetUser.location.toLatLng())
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
                    ViewUtils.dpToPx(
                        MARK_WIDTH_DP,
                        requireContext()
                    ).toFloat()
                )
                mapView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }
        mapView.viewTreeObserver.addOnGlobalLayoutListener(listener)
    }

    // Animations
    private fun animateFadingIn(marker: Marker) {
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                marker.alpha = it.animatedValue as Float
            }
            duration = ANIMATION_FADE_IN_DURATION
        }
        valueAnimator.start()
    }

    private fun animateFadingOut(marker: Marker, doOnEndCallback: () -> Unit) {
        val valueAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
            addUpdateListener {
                marker.alpha = it.animatedValue as Float
            }
            duration = ANIMATION_FADE_OUT_DURATION
            doOnEnd {
                doOnEndCallback.invoke()
            }
        }
        valueAnimator.start()
    }

    private fun startLoadingImage(
        avatar: String,
        onSuccess: (bitmap: Bitmap) -> Unit
    ): Disposable? {
        return loadPhotoCallback?.invoke(avatar)
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe { bitmap ->
                onSuccess.invoke(bitmap)
            }
    }

    companion object {
        const val TAG = "FLocator Map Fragment Class"
        const val MIN_ZOOM_SCALE = 15f
        const val CAMERA_POSITION = "CAMERA_POSITION"
        const val MARK_WIDTH_DP = 56
        const val ANIMATION_FADE_IN_DURATION = 300L
        const val ANIMATION_FADE_OUT_DURATION = 250L
    }
}
