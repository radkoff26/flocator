package com.example.flocator.main.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
    private lateinit var carouselAdapter: CarouselRecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var placeholder: FrameLayout
    private lateinit var photoAddLauncher: ActivityResultLauncher<String>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setContentView(R.layout.fragment_add_mark)

        dialog.setOnShowListener {
            val view = (it as BottomSheetDialog).findViewById<LinearLayout>(R.id.bs)
                ?: return@setOnShowListener

            addPhotoBtn = view.findViewById(R.id.add_photo_btn)
            removePhotoBtn = view.findViewById(R.id.remove_photo_btn)
            recyclerView = view.findViewById(R.id.photo_carousel)
            placeholder = view.findViewById(R.id.camera_placeholder)


            addPhotoBtn.setOnClickListener {
                photoAddLauncher.launch("image/\\*")
            }

            placeholder.setOnClickListener {
                photoAddLauncher.launch("image/\\*")
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

            expandBottomSheet(view)
            adjustRecyclerView(view)
        }

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        photoAddLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { result ->
            addMarkFragmentViewModel.updateLiveData(result)
        }

        return inflater.inflate(R.layout.fragment_add_mark, container, false)
    }

    private fun expandBottomSheet(bottomSheetView: View) {
        val behavior = BottomSheetBehavior.from(bottomSheetView)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun adjustRecyclerView(view: View) {
        val recyclerView = view.findViewById(R.id.photo_carousel) as RecyclerView

        // Assign adapter to RecyclerView
        carouselAdapter = CarouselRecyclerViewAdapter { uri, b -> addMarkFragmentViewModel.toggleItem(uri, b)}
        recyclerView.adapter = carouselAdapter

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
        addMarkFragmentViewModel.liveData.observe(this, this)
    }

    override fun onChanged(t: AddMarkFragmentData?) {
        if (t == null) {
            return
        }
        val isEmpty = t.stateList.isEmpty()
        toggleOnStateEmptinessChange(isEmpty)
        if (isEmpty) {
            if (removePhotoBtn.visibility != GONE) {
                animateRemovePhotoButtonOut()
            }
            recyclerView.visibility = GONE
        } else {
            if (removePhotoBtn.visibility == GONE) {
                animateRemovePhotoButtonIn()
            }
            if (addMarkFragmentViewModel.isAnyTaken()) {
                enableRemovePhotoButton()
            } else {
                disableRemovePhotoButton()
            }
        }
        carouselAdapter.updateData(t.stateList)
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

    private fun toggleOnStateEmptinessChange(isEmpty: Boolean) {
        if (isEmpty) {
            placeholder.visibility = VISIBLE
            recyclerView.visibility = GONE
        } else {
            placeholder.visibility = GONE
            recyclerView.visibility = VISIBLE
        }
    }

    companion object {
        const val TAG = "Add Mark Fragment"
    }
}