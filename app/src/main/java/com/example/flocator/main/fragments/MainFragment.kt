package com.example.flocator.main.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.example.flocator.R
import com.example.flocator.main.api.MockApi
import com.example.flocator.main.models.User
import com.example.flocator.main.utils.LoadUtils
import com.example.flocator.main.utils.MapUtils
import com.example.flocator.main.view_models.MainFragmentViewModel
import com.example.flocator.main.views.MapFriendView
import com.google.android.material.button.MaterialButton
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.ui_view.ViewProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.concurrent.ConcurrentHashMap

class MainFragment : Fragment(), Observer<List<User>> {
    private val mainFragmentViewModel = MainFragmentViewModel()
    private lateinit var mapView: MapView
    private val compositeDisposable = CompositeDisposable()
    private val marks = ConcurrentHashMap<Long, PlacemarkMapObject>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragment = inflater.inflate(R.layout.fragment_main, container, false)

        mapView = fragment.findViewById(R.id.map_view)
        mapView.map.move(
            CameraPosition(Point(59.945933, 30.320045), 11.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 0f),
            null
        )

        mainFragmentViewModel.friendsLiveData.observe(viewLifecycleOwner, this)

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
                }
        )

        val addMarkBtn = fragment.findViewById(R.id.open_add_mark_fragment) as MaterialButton

        addMarkBtn.setOnClickListener {
            val addMarkFragment = AddMarkFragment()
            addMarkFragment.show(this.parentFragmentManager, AddMarkFragment.TAG)
        }

        return fragment
    }

    override fun onStart() {
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
        super.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    @SuppressLint("CheckResult")
    override fun onChanged(users: List<User>?) {
        if (users == null) {
            return
        }
        for (user in users) {
            if (marks[user.id] == null) {
                val friendView = MapFriendView(requireContext())
                val viewProvider = ViewProvider(friendView)
                marks[user.id] = MapUtils.addViewToMap(
                    mapView,
                    viewProvider,
                    user.point
                )
                LoadUtils.loadPictureFromUrl(user.avatarUrl, 40)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { bitmap ->
                        friendView.mBitmap = bitmap
                        viewProvider.snapshot()
                        marks[user.id]!!.setView(viewProvider)
                    }
            } else {
                marks[user.id]!!.geometry = user.point
            }
        }
    }
}