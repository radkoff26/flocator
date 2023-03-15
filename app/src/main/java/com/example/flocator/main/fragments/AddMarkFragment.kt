package com.example.flocator.main.fragments

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.example.flocator.MainActivity
import com.example.flocator.R
import com.example.flocator.main.adapters.CarouselRecyclerViewAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class AddMarkFragment : BottomSheetDialogFragment() {
    private lateinit var carouselRecyclerViewAdapter: CarouselRecyclerViewAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setContentView(R.layout.fragment_add_mark)

        dialog.setOnShowListener {
            val view = (it as BottomSheetDialog).findViewById<LinearLayout>(R.id.bs)
                ?: return@setOnShowListener

            val behavior = BottomSheetBehavior.from(view)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            val launcher = (requireActivity() as MainActivity).launcher

            val recyclerView = view.findViewById(R.id.photo_carousel) as RecyclerView
            carouselRecyclerViewAdapter = CarouselRecyclerViewAdapter(launcher)
            recyclerView.adapter = carouselRecyclerViewAdapter

            val itemDecoration = DividerItemDecoration(requireContext(), HORIZONTAL)
            itemDecoration.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.rv_divider)!!)
            recyclerView.addItemDecoration(itemDecoration)

            (requireActivity() as MainActivity).stateLiveData.observe(this, carouselRecyclerViewAdapter)

            val addMarkButton = view.findViewById(R.id.add_mark_btn) as MaterialButton
            val cancelMarkButton = view.findViewById(R.id.cancel_mark_btn) as MaterialButton

            addMarkButton.setOnClickListener {
                dismiss()
            }

            cancelMarkButton.setOnClickListener {
                dismiss()
            }
        }

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_mark, container, false)
    }

    companion object {
        const val TAG = "Add Mark Fragment"
    }
}