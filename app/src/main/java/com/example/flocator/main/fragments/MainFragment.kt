package com.example.flocator.main.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.flocator.R
import com.example.flocator.logreg.fragments.RegNameFragment
import com.google.android.material.button.MaterialButton
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView

data class State(
    var list: List<Uri>
)

class MainFragment : Fragment() {
    private lateinit var mapView: MapView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragment = inflater.inflate(R.layout.fragment_main, container, false)

        mapView = fragment.findViewById(R.id.map_view)

        val addMarkBtn = fragment.findViewById(R.id.open_add_mark_fragment) as MaterialButton

        addMarkBtn.setOnClickListener {
//            val addMarkFragment = AddMarkFragment()
//            addMarkFragment.show(this.parentFragmentManager, AddMarkFragment.TAG)
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, RegNameFragment())
            transaction.addToBackStack(null)
            transaction.commit()
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