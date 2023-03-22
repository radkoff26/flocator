package com.example.flocator.main.fragments

import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.AttributeSet
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import com.example.flocator.R
import com.example.flocator.main.utils.MapUtils
import com.example.flocator.main.view_models.MainFragmentViewModel
import com.example.flocator.main.views.MapFriendView
import com.google.android.material.button.MaterialButton
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.ui_view.ViewProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.net.URI
import java.net.URL
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.sqrt

const val SPEED = 0.00000056

class MainFragment : Fragment(), Observer<Point> {
    private lateinit var mapView: MapView
    private lateinit var objectTapListener: MapObjectTapListener
    private val list = listOf(
        Point(59.985873, 30.348584),
        Point(59.985943, 30.348972),
        Point(59.985970, 30.349223),
        Point(59.985623, 30.349373)
    )
    private val marks = HashMap<Int, PlacemarkMapObject>()
    private val viewModel = MainFragmentViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragment = inflater.inflate(R.layout.fragment_main, container, false)

        mapView = fragment.findViewById(R.id.map_view)
        mapView.map.move(
            CameraPosition(Point(59.945933, 30.320045), 20.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 0f),
            null
        )
        val friendView = MapFriendView(requireContext())
        val viewProvider = ViewProvider(friendView)
        marks[1] = MapUtils.addViewToMap(mapView, viewProvider, Point(59.945933, 30.320045))
        viewModel.userCoordinatesLiveData.observe(viewLifecycleOwner, this)

        val observable = Observable.create {
            var i = 0
            var point = list[i]
            Thread.sleep(1000)
            it.onNext(point)
            i++
            while (i < list.size) {
                var current = point
                while (current != list[i]) {
                    current = moveWithSpeed(current, list[i], SPEED)
                    Thread.sleep(16)
                    it.onNext(current)
                }
                point = list[i]
                i++
            }
            it.onComplete()
        }

        val disposable = observable
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                viewModel.updateUserCoordinates(it)
            }

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

    override fun onChanged(t: Point?) {
        marks[1]!!.geometry = t!!
        mapView.map.move(
            CameraPosition(t, 20.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 0.008f),
            null
        )
    }

    private fun moveWithSpeed(from: Point, to: Point, speed: Double): Point {
        val a = abs(from.latitude - to.latitude)
        val b = abs(from.longitude - to.longitude)
        if (sqrt(a * a + b * b) < speed) {
            return to
        }
        val c = b / a
        var latitude = sqrt(speed * speed / (c * c + 1))
        var longitude = latitude * c
        if (to.latitude < from.latitude) {
            latitude = -latitude
        }
        if (to.longitude < from.longitude) {
            longitude = -longitude
        }
        return Point(from.latitude + latitude, from.longitude + longitude)
    }
}