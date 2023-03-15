package com.example.flocator.main.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.example.flocator.R
import com.example.flocator.main.adapters.CarouselRecyclerViewAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class AddMarkFragment : BottomSheetDialogFragment() {
    private lateinit var launcher: ActivityResultLauncher<String>
    private val stateLiveData = MutableLiveData(State(emptyList()))

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setContentView(R.layout.fragment_add_mark)

        dialog.setOnShowListener {
            val view = (it as BottomSheetDialog).findViewById<LinearLayout>(R.id.bs)
                ?: return@setOnShowListener

            expandBottomSheet(view)
            adjustRecyclerView(view)

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launcher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { result ->
            stateLiveData.value = State(result)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_mark, container, false)
    }

    private fun expandBottomSheet(bottomSheetView: View) {
        val behavior = BottomSheetBehavior.from(bottomSheetView)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun adjustRecyclerView(view: View) {
        val recyclerView = view.findViewById(R.id.photo_carousel) as RecyclerView

        // Assign adapter to RecyclerView
        val carouselRecyclerViewAdapter = CarouselRecyclerViewAdapter(launcher)
        recyclerView.adapter = carouselRecyclerViewAdapter

        // Add spaces between items of RecyclerView
        val itemDecoration = DividerItemDecoration(requireContext(), HORIZONTAL)
        itemDecoration.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.rv_divider)!!)
        recyclerView.addItemDecoration(itemDecoration)

        // Set observer to LiveData
        stateLiveData.observe(this, carouselRecyclerViewAdapter)
    }

    companion object {
        const val TAG = "Add Mark Fragment"
    }
}