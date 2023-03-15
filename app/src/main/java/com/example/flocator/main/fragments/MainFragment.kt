package com.example.flocator.main.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.flocator.R
import com.google.android.material.button.MaterialButton
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView

class MainFragment : Fragment() {
    private lateinit var mapView: MapView

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
}