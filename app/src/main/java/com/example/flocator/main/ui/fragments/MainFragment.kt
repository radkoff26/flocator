package com.example.flocator.main.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.flocator.R
import com.example.flocator.community.fragments.ProfileFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.flocator.databinding.FragmentMainBinding
import com.example.flocator.main.api.MockApi
import com.example.flocator.main.models.*
import com.example.flocator.main.models.dto.FriendViewDto
import com.example.flocator.main.models.dto.MarkViewDto
import com.example.flocator.main.utils.LoadUtils
import com.example.flocator.main.utils.MapUtils
import com.example.flocator.main.ui.view_models.MainFragmentViewModel
import com.example.flocator.main.ui.views.FriendMapView
import com.example.flocator.main.ui.views.MarkMapView
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InertiaMoveListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.runtime.ui_view.ViewProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.ConcurrentHashMap

class MainFragment : Fragment(), Observer<kotlin.collections.Map<Long, User>> {
    // Binding
    private lateinit var binding: FragmentMainBinding

    // ViewModel
    private val mainFragmentViewModel = MainFragmentViewModel()

    // Disposable
    private val compositeDisposable = CompositeDisposable()

    // Map store
    private val friendsViewState = ConcurrentHashMap<Long, FriendViewDto>()
    private val marksViewState = ConcurrentHashMap<Long, MarkViewDto>()
    private val friendClickListeners = ConcurrentHashMap<Long, MapObjectTapListener>()
    private val markClickListeners = ConcurrentHashMap<Long, MapObjectTapListener>()

    // Observers
    private val marksObserver = MarksObserver()
    private val cameraStatusObserver = CameraStatusObserver()
    private val photoObserver = LoadedPhotoObserver()

    // Listeners
    private val inertiaMoveListener = object : InertiaMoveListener {
        override fun onStart(p0: Map, p1: CameraPosition) {
            mainFragmentViewModel.setCameraFixed()
            mainFragmentViewModel.cameraStatusLiveData.removeObserver(cameraStatusObserver)
        }

        override fun onCancel(p0: Map, p1: CameraPosition) {

        }

        override fun onFinish(p0: Map, p1: CameraPosition) {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        requireActivity().actionBar?.show()

        binding.mapView.map.move(
            CameraPosition(Point(59.945933, 30.320045), 11.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 0f),
            null
        )

        binding.mapView.map.addInertiaMoveListener(inertiaMoveListener)

        mainFragmentViewModel.friendsLiveData.observe(viewLifecycleOwner, this)
        mainFragmentViewModel.marksLiveData.observe(viewLifecycleOwner, marksObserver)
        mainFragmentViewModel.photoCacheLiveData.observe(viewLifecycleOwner, photoObserver)

        compositeDisposable.addAll(
            MockApi.getAllFriends()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { friends ->
                    mainFragmentViewModel.updateUsers(friends)
                },
            MockApi.watchFriends()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    mainFragmentViewModel.updateUsers(it)
                },
            MockApi.getAllMarks()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { marks ->
                    mainFragmentViewModel.updateMarks(marks)
                },
            MockApi.watchMarks()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    mainFragmentViewModel.updateMarks(it)
                }
        )

        binding.openAddMarkFragment.setOnClickListener {
            val addMarkFragment = AddMarkFragment()
            addMarkFragment.show(this.parentFragmentManager, AddMarkFragment.TAG)
        }

        binding.communityBtn.setOnClickListener {
            val communityFragment = ProfileFragment()
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.main_fragment, communityFragment)
            transaction.disallowAddToBackStack()
            transaction.commit()
        }

        return binding.root
    }

    override fun onStart() {
        binding.mapView.onStart()
        MapKitFactory.getInstance().onStart()
        super.onStart()
    }

    override fun onStop() {
        binding.mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    @SuppressLint("CheckResult")
    override fun onChanged(value: kotlin.collections.Map<Long, User>) {
        for (userEntry in value) {
            val id = userEntry.key
            val user = userEntry.value
            if (friendsViewState[id] == null) {
                val friendView = FriendMapView(requireContext())
                val viewProvider = ViewProvider(friendView)
                friendsViewState[id] = FriendViewDto(
                    MapUtils.addViewToMap(
                        binding.mapView,
                        viewProvider,
                        user.point
                    ),
                    friendView,
                    null
                )
                friendClickListeners[id] = MapObjectTapListener { _, _ ->
                    mainFragmentViewModel.setCameraFollowOnFriendMark(id)
                    mainFragmentViewModel.cameraStatusLiveData.observeForever(cameraStatusObserver)
                    true
                }
                friendsViewState[id]!!.placemark.addTapListener(friendClickListeners[id]!!)
                if (user.avatarUrl == null) {
                    friendView.setPlaceHolder()
                    viewProvider.snapshot()
                    friendsViewState[id]!!.placemark.setView(viewProvider)
                    friendsViewState[id]!!.avatarUrl = null
                } else {
                    LoadUtils.loadPictureFromUrl(user.avatarUrl, 20)
                        .observeOn(Schedulers.computation())
                        .subscribe { bitmap ->
                            mainFragmentViewModel.setLoadedPhotoAsync(user.avatarUrl, bitmap)
                        }
                }
            } else {
                friendsViewState[id]!!.placemark.geometry = user.point
            }
        }
    }

    inner class CameraStatusObserver : Observer<CameraStatus> {
        override fun onChanged(value: CameraStatus) {
            if (value.cameraStatusType == CameraStatusType.FOLLOW) {
                binding.mapView.map.move(
                    CameraPosition(value.point!!, 20.0f, 0.0f, 0.0f),
                    Animation(Animation.Type.SMOOTH, 0.008f),
                    null
                )
            }
        }
    }

    inner class MarksObserver : Observer<kotlin.collections.Map<Long, Mark>> {
        @SuppressLint("CheckResult")
        override fun onChanged(value: kotlin.collections.Map<Long, Mark>) {
            for (entry in value) {
                val id = entry.key
                val mark = entry.value
                if (marksViewState[id] == null) {
                    val markMapView = MarkMapView(requireContext())
                    val viewProvider = ViewProvider(markMapView)
                    marksViewState[id] = MarkViewDto(
                        MapUtils.addViewToMap(
                            binding.mapView,
                            viewProvider,
                            mark.location
                        ),
                        markMapView,
                        null,
                        null
                    )
                    if (mark.imageList.isEmpty()) {
                        markMapView.setMarkBitmapPlaceHolder()
                        viewProvider.snapshot()
                        marksViewState[id]!!.placemark.setView(viewProvider)
                        marksViewState[id]!!.thumbnailUrl = null
                    } else {
                        val firstImage = mark.imageList[0]
                        if (!mainFragmentViewModel.photoCacheContains(firstImage)) {
                            LoadUtils.loadPictureFromUrl(firstImage, 20)
                                .observeOn(Schedulers.computation())
                                .subscribe { image ->
                                    mainFragmentViewModel.setLoadedPhotoAsync(firstImage, image)
                                }
                        }
                    }
                }
            }
        }
    }

    inner class LoadedPhotoObserver : Observer<kotlin.collections.Map<String, Bitmap>> {
        // TODO: come up with less iterative implementation
        override fun onChanged(value: kotlin.collections.Map<String, Bitmap>) {
            // Watch mark images change
            for (mark in marksViewState) {
                // State of current mark
                val liveMark = mainFragmentViewModel.marksLiveData.value!![mark.key]!!

                // Mark thumbnail case
                if (liveMark.imageList.isEmpty()) {
                    if (mark.value.thumbnailUrl != null) {
                        mark.value.markMapView.setMarkBitmapPlaceHolder()
                        val viewProvider = ViewProvider(mark.value.markMapView)
                        mark.value.thumbnailUrl = null
                        viewProvider.snapshot()
                        mark.value.placemark.setView(viewProvider)
                    }
                } else {
                    // Non-nullable image url
                    val thumbnailUrl = liveMark.imageList[0]
                    if (mark.value.thumbnailUrl != thumbnailUrl && value[thumbnailUrl] != null) {
                        mark.value.markMapView.setMarkBitmapImage(value[thumbnailUrl]!!)
                        val viewProvider = ViewProvider(mark.value.markMapView)
                        mark.value.thumbnailUrl = thumbnailUrl
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
            for (friend in friendsViewState) {
                val url =
                    mainFragmentViewModel.friendsLiveData.value!![friend.key]!!.avatarUrl
                if (url == null) {
                    friend.value.friendMapView.setPlaceHolder()
                    val viewProvider = ViewProvider(friend.value.friendMapView)
                    viewProvider.snapshot()
                    friend.value.placemark.setView(viewProvider)
                    friend.value.avatarUrl = null
                } else {
                    if (url != friend.value.avatarUrl && value[url] != null) {
                        friend.value.friendMapView.setBitmap(value[url]!!)
                        val viewProvider = ViewProvider(friend.value.friendMapView)
                        viewProvider.snapshot()
                        friend.value.placemark.setView(viewProvider)
                        friend.value.avatarUrl = url
                    }
                }
            }
        }
    }
}
