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
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.example.flocator.R
import com.example.flocator.main.adapters.CarouselRecyclerViewAdapter
import com.example.flocator.main.data.AddMarkFragmentData
import com.example.flocator.main.view_models.AddMarkFragmentViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class AddMarkFragment : BottomSheetDialogFragment(), Observer<AddMarkFragmentData> {
    private val addMarkFragmentViewModel = AddMarkFragmentViewModel()
    private lateinit var addPhotoBtn: MaterialButton
    private lateinit var removePhotoBtn: MaterialButton

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setContentView(R.layout.fragment_add_mark)

        dialog.setOnShowListener {
            val view = (it as BottomSheetDialog).findViewById<LinearLayout>(R.id.bs)
                ?: return@setOnShowListener

            expandBottomSheet(view)
            adjustRecyclerView(view)

            addPhotoBtn = view.findViewById(R.id.add_photo_btn)
            removePhotoBtn = view.findViewById(R.id.remove_photo_btn)

            addPhotoBtn.setOnClickListener {
                registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { result ->
                    addMarkFragmentViewModel.updateLiveData(result)
                }.launch("image/\\*")
            }

            removePhotoBtn.setOnClickListener {
                addMarkFragmentViewModel.removeItems()
            }

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

    private fun expandBottomSheet(bottomSheetView: View) {
        val behavior = BottomSheetBehavior.from(bottomSheetView)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun adjustRecyclerView(view: View) {
        val recyclerView = view.findViewById(R.id.photo_carousel) as RecyclerView

        // Assign adapter to RecyclerView
        val carouselRecyclerViewAdapter = CarouselRecyclerViewAdapter(addMarkFragmentViewModel)
        recyclerView.adapter = carouselRecyclerViewAdapter

        // Add spaces between items of RecyclerView
        val itemDecoration = DividerItemDecoration(requireContext(), HORIZONTAL)
        itemDecoration.setDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.rv_divider
            )!!
        )
        recyclerView.addItemDecoration(itemDecoration)

        // Set observer to LiveData
        addMarkFragmentViewModel.liveData.observe(this, carouselRecyclerViewAdapter)
        addMarkFragmentViewModel.liveData.observe(this, this)
    }

    override fun onChanged(t: AddMarkFragmentData?) {
        if (t!!.stateList.isEmpty()) {
            if (removePhotoBtn.visibility != GONE) {
                animateRemovePhotoButtonOut()
            }
        } else {
            if (removePhotoBtn.visibility == GONE) {
                animateRemovePhotoButtonIn()
            }
        }
    }

    private fun animateRemovePhotoButtonOut() {
        removePhotoBtn.visibility = GONE
    }

    private fun animateRemovePhotoButtonIn() {
        removePhotoBtn.visibility = VISIBLE
    }

    private fun enableRemovePhotoButton() {
        removePhotoBtn.isEnabled = true
        removePhotoBtn.iconTint = ContextCompat.getColorStateList(requireContext(), R.color.white)
        removePhotoBtn.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.danger)
        removePhotoBtn.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.white
            )
        )
    }

    private fun disableRemovePhotoButton() {
        removePhotoBtn.isEnabled = false
        removePhotoBtn.iconTint = ContextCompat.getColorStateList(requireContext(), R.color.danger)
        removePhotoBtn.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.white)
        removePhotoBtn.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.danger
            )
        )
    }

    companion object {
        const val TAG = "Add Mark Fragment"
    }
}