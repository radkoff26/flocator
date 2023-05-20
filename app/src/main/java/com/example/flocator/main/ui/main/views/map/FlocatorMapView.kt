package com.example.flocator.main.ui.main.views.map

import android.content.Context
import android.util.AttributeSet
import android.util.LruCache
import com.example.flocator.common.cache.runtime.PhotoState
import com.example.flocator.common.storage.db.entities.MarkWithPhotos
import com.example.flocator.common.storage.db.entities.User
import com.example.flocator.common.storage.store.user.info.UserInfo
import com.example.flocator.main.ui.main.data.FriendViewDto
import com.example.flocator.main.ui.main.data.MarkGroup
import com.example.flocator.main.ui.main.data.MarkGroupViewDto
import com.example.flocator.main.ui.main.data.MarkViewDto
import com.example.flocator.main.ui.main.views.friend.UserView
import com.example.flocator.main.ui.main.views.mark.MarkView
import com.example.flocator.main.ui.main.views.mark_group.MarkGroupView
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.ui_view.ViewProvider
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

typealias LoadPhotoCallback = (uri: String) -> Unit
typealias OnFriendViewClickCallback = (id: Long) -> MapObjectTapListener
typealias OnMarkViewClickCallback = (id: Long) -> MapObjectTapListener
typealias OnMarkGroupViewClickCallback = (marks: List<MarkWithPhotos>) -> MapObjectTapListener

class FlocatorMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : MapView(context, attrs, defStyle) {

    // Map store
    private val usersViewState = ConcurrentHashMap<Long, FriendViewDto>()
    private val marksViewState = ConcurrentHashMap<Long, MarkViewDto>()
    private val markGroupsViewState = CopyOnWriteArrayList<MarkGroupViewDto>()
    private val friendClickListeners = ConcurrentHashMap<Long, MapObjectTapListener>()
    private val markClickListeners = ConcurrentHashMap<Long, MapObjectTapListener>()
    private val markGroupClickListeners = ConcurrentHashMap<Long, MapObjectTapListener>()

    // Map collections
    private val usersCollection: MapObjectCollection = map.addMapObjectLayer("users")
    private val marksCollection: MapObjectCollection = map.addMapObjectLayer("marks")
    private val markGroupsCollection: MapObjectCollection = map.addMapObjectLayer("mark groups")

    private var loadPhotoCallback: LoadPhotoCallback? = null
    private var onFriendViewClickCallback: OnFriendViewClickCallback? = null
    private var onMarkViewClickCallback: OnMarkViewClickCallback? = null
    private var onMarkGroupViewClickCallback: OnMarkGroupViewClickCallback? = null

    fun initialize(
        loadPhotoCallback: LoadPhotoCallback,
        onFriendViewClickCallback: OnFriendViewClickCallback,
        onMarkViewClickCallback: OnMarkViewClickCallback,
        onMarkGroupViewClickCallback: OnMarkGroupViewClickCallback
    ) {
        this.loadPhotoCallback = loadPhotoCallback
        this.onFriendViewClickCallback = onFriendViewClickCallback
        this.onMarkViewClickCallback = onMarkViewClickCallback
        this.onMarkGroupViewClickCallback = onMarkGroupViewClickCallback
    }

    // Listener callbacks
    fun updateUserOnMap(
        point: Point,
        userInfo: UserInfo,
        photoCache: LruCache<String, PhotoState>
    ) {
        if (usersViewState[userInfo.userId] == null) {
            val userView = UserView(context, isTargetUser = true)
            userView.setUserName("${userInfo.firstName} ${userInfo.lastName}")
            val viewProvider = ViewProvider(userView)
            usersViewState[userInfo.userId] = FriendViewDto(
                usersCollection.addPlacemark(
                    point,
                    viewProvider
                ),
                userView
            )
        } else {
            usersViewState[userInfo.userId]!!.placemark.geometry = point
        }
        val userView = usersViewState[userInfo.userId]!!.userView
        if (userInfo.avatarUri == null) {
            if (userView.avatarUri != null) {
                userView.setAvatarPlaceHolder()
                updateUserView(usersViewState[userInfo.userId]!!)
            }
        } else {
            loadPhotoCallback?.invoke(userInfo.avatarUri)
            if (photoCache[userInfo.avatarUri] != null) {
                val photo = photoCache[userInfo.avatarUri]
                if (photo is PhotoState.Loaded) {
                    userView.setAvatarBitmap(photo.bitmap, userInfo.avatarUri)
                    updateUserView(usersViewState[userInfo.userId]!!)
                }
            }
        }
    }

    fun updateFriendsOnMap(
        friends: Map<Long, User>,
        photoCache: LruCache<String, PhotoState>
    ) {
        for (userEntry in friends) {
            val id = userEntry.key
            val user = userEntry.value
            if (usersViewState[id] == null) {
                val userView = UserView(context)
                userView.setUserName("${user.firstName} ${user.lastName}")
                val viewProvider = ViewProvider(userView)
                usersViewState[id] = FriendViewDto(
                    usersCollection.addPlacemark(user.location, viewProvider),
                    userView
                )
                updateUserView(usersViewState[id]!!)
                if (onFriendViewClickCallback != null) {
                    friendClickListeners[id] = onFriendViewClickCallback!!.invoke(id)
                    usersViewState[id]!!.placemark.addTapListener(friendClickListeners[id]!!)
                }
            } else {
                usersViewState[id]!!.placemark.geometry = user.location
            }
            val friendView = usersViewState[id]!!.userView
            if (user.avatarUri == null) {
                if (friendView.avatarUri != null) {
                    friendView.setAvatarPlaceHolder()
                    updateUserView(usersViewState[id]!!)
                }
            } else {
                loadPhotoCallback?.invoke(user.avatarUri)
                if (photoCache[user.avatarUri] != null) {
                    val photo = photoCache[user.avatarUri]
                    if (photo is PhotoState.Loaded) {
                        friendView.setAvatarBitmap(photo.bitmap, user.avatarUri)
                        updateUserView(usersViewState[id]!!)
                    }
                }
            }
        }
    }

    fun updateVisibleMarksOnMap(
        visibleMarks: List<MarkGroup>,
        friends: Map<Long, User>,
        photoCache: LruCache<String, PhotoState>,
        userInfo: UserInfo?
    ) {
        marksViewState.clear()
        marksCollection.clear()
        markGroupsViewState.clear()
        markGroupsCollection.clear()
        markClickListeners.clear()
        val found = BooleanArray(visibleMarks.size) { false }
        for (groupIndex in visibleMarks.indices) {
            if (!found[groupIndex]) {
                val group = visibleMarks[groupIndex]
                if (group.marks.size == 1) {
                    val mark = group.marks[0].mark
                    val photos = group.marks[0].photos
                    val id = mark.markId
                    val markView = if (userInfo?.userId == mark.authorId) {
                        MarkView(context, isTargetUserMark = true)
                    } else {
                        MarkView(context)
                    }
                    val viewProvider = ViewProvider(markView)
                    marksViewState[id] = MarkViewDto(
                        marksCollection.addPlacemark(mark.location, viewProvider),
                        markView
                    )
                    if (onMarkViewClickCallback != null) {
                        markClickListeners[id] = onMarkViewClickCallback!!.invoke(id)
                        marksViewState[id]!!.placemark.addTapListener(markClickListeners[id]!!)
                    }
                    if (photos.isEmpty()) {
                        if (markView.markImageUri != null) {
                            markView.setMarkBitmapPlaceHolder()
                            updateMarkView(marksViewState[id]!!)
                        }
                    } else {
                        val uri = photos[0].uri
                        loadPhotoCallback?.invoke(uri)
                        if (photoCache[uri] != null) {
                            val photo = photoCache[uri]
                            if (photo is PhotoState.Loaded) {
                                markView.setMarkBitmapImage(photo.bitmap, uri)
                                updateMarkView(marksViewState[id]!!)
                            }
                        }
                    }
                    val avatarUri = if (mark.authorId == userInfo?.userId)
                        userInfo.avatarUri
                    else {
                        friends[mark.authorId]?.avatarUri
                    }
                    if (avatarUri == null) {
                        if (markView.authorImageUri != null) {
                            markView.setAuthorBitmapPlaceHolder()
                            updateMarkView(marksViewState[id]!!)
                        }
                    } else {
                        loadPhotoCallback?.invoke(avatarUri)
                        if (photoCache[avatarUri] != null) {
                            val photo = photoCache[avatarUri]!!
                            if (photo is PhotoState.Loaded) {
                                markView.setAuthorBitmapImage(photo.bitmap, avatarUri)
                                updateMarkView(marksViewState[id]!!)
                            }
                        }
                    }
                } else {
                    val markGroupView = MarkGroupView(context)
                    markGroupView.setCount(group.marks.size)
                    val viewProvider = ViewProvider(markGroupView)
                    val markGroup = MarkGroupViewDto(
                        markGroupsCollection.addPlacemark(group.center, viewProvider),
                        markGroupView,
                        group.marks
                    )
                    if (onMarkGroupViewClickCallback != null) {
                        markGroupClickListeners[groupIndex.toLong()] =
                            onMarkGroupViewClickCallback!!.invoke(group.marks)
                        markGroup.placemark.addTapListener(markGroupClickListeners[groupIndex.toLong()]!!)
                    }
                    markGroupsViewState.add(markGroup)
                }
            }
        }
    }

    fun updateThumbnailsOnMap(
        photoCache: LruCache<String, PhotoState>,
        userInfo: UserInfo?,
        marks: Map<Long, MarkWithPhotos>,
        friends: Map<Long, User>
    ) {
        // User case
        if (userInfo != null) {
            val userId = userInfo.userId
            if (usersViewState[userId] != null) {
                val user = usersViewState[userId]!!
                if (userInfo.avatarUri != null) {
                    loadPhotoCallback?.invoke(userInfo.avatarUri)
                }
                if (userInfo.avatarUri != null && photoCache[userInfo.avatarUri] != null) {
                    val photo = photoCache[userInfo.avatarUri]!!
                    if (photo is PhotoState.Loaded) {
                        user.userView.setAvatarBitmap(photo.bitmap, userInfo.avatarUri)
                        updateUserView(user)
                    }
                }
            }
        }

        // Watch mark images change
        for (mark in marksViewState) {
            // State of current mark
            val liveMark = marks[mark.key]!!.mark
            val photos = marks[mark.key]!!.photos

            // Mark thumbnail case
            if (photos.isEmpty()) {
                // If photo has already been set, then this photo is no longer present
                if (mark.value.markView.markImageUri != null) {
                    mark.value.markView.setMarkBitmapPlaceHolder()
                    updateMarkView(mark.value)
                }
            } else {
                // Non-nullable image uri
                val thumbnailUri = photos[0].uri
                loadPhotoCallback?.invoke(thumbnailUri)

                // If photo is present, then it will be set
                if (photoCache[thumbnailUri] != null) {
                    val photo = photoCache[thumbnailUri]!!
                    if (photo is PhotoState.Loaded) {
                        mark.value.markView.setMarkBitmapImage(photo.bitmap, thumbnailUri)
                        updateMarkView(mark.value)
                    }
                }
            }

            // Author avatar case
            // If author user is not yet loaded
            val uri = if (mark.value.markView.isTargetUserMark) {
                userInfo?.avatarUri
            } else {
                friends[liveMark.authorId]?.avatarUri
            }
            // If user doesn't have an avatar image
            if (uri == null) {
                // Then there goes a placeholder
                if (mark.value.markView.authorImageUri != null) {
                    mark.value.markView.setAuthorBitmapPlaceHolder()
                    updateMarkView(mark.value)
                }
            } else {
                loadPhotoCallback?.invoke(uri)
                if (photoCache[uri] != null) {
                    val photo = photoCache[uri]!!
                    if (photo is PhotoState.Loaded) {
                        mark.value.markView.setAuthorBitmapImage(photo.bitmap, uri)
                        updateMarkView(mark.value)
                    }
                }
            }
        }

        // Watch friends images change
        for (friend in usersViewState) {
            val uri = if (friend.value.userView.isTargetUser) {
                userInfo?.avatarUri
            } else {
                friends[friend.key]!!.avatarUri
            }
            if (uri == null) {
                if (friend.value.userView.avatarUri != null) {
                    friend.value.userView.setAvatarPlaceHolder()
                    updateUserView(friend.value)
                }
            } else {
                if (photoCache[uri] != null) {
                    loadPhotoCallback?.invoke(uri)
                    val photo = photoCache[uri]!!
                    if (photo is PhotoState.Loaded) {
                        friend.value.userView.setAvatarBitmap(photo.bitmap, uri)
                        updateUserView(friend.value)
                    }
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

    private fun updateMarkView(mark: MarkViewDto) {
        val viewProvider = ViewProvider(mark.markView)
        viewProvider.snapshot()
        mark.placemark.setView(viewProvider)
    }

    private fun updateUserView(user: FriendViewDto) {
        val viewProvider = ViewProvider(user.userView)
        viewProvider.snapshot()
        user.placemark.setView(viewProvider)
    }

    companion object {
        const val TAG = "Flocator Map View"
    }
}