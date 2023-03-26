package com.example.flocator.main.fragments

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView.*
import com.example.flocator.R
import com.example.flocator.databinding.FragmentAddMarkBinding
import com.example.flocator.main.adapters.CarouselRecyclerViewAdapter
import com.example.flocator.main.data.AddMarkFragmentData
import com.example.flocator.main.view_models.AddMarkFragmentViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddMarkFragment : BottomSheetDialogFragment(), Observer<AddMarkFragmentData> {
    private lateinit var binding: FragmentAddMarkBinding
    private lateinit var carouselAdapter: CarouselRecyclerViewAdapter
    private lateinit var photoAddLauncher: ActivityResultLauncher<String>
    private val addMarkFragmentViewModel = AddMarkFragmentViewModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setContentView(R.layout.fragment_add_mark)

        dialog.setOnShowListener {
            val view = (it as BottomSheetDialog).findViewById<LinearLayout>(R.id.bs)
                ?: return@setOnShowListener

            expandBottomSheet(view)
        }

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        photoAddLauncher =
            registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { result ->
                addMarkFragmentViewModel.updateLiveData(result)
            }

        return inflater.inflate(R.layout.fragment_add_mark, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddMarkBinding.bind(view)

        binding.addPhotoBtn.setOnClickListener {
            photoAddLauncher.launch("image/\\*")
        }

        binding.cameraPlaceholder.setOnClickListener {
            photoAddLauncher.launch("image/\\*")
        }

        binding.removePhotoBtn.setOnClickListener {
            addMarkFragmentViewModel.removeItems()
        }

        binding.addMarkBtn.setOnClickListener {
            dismiss()
        }

        binding.cancelMarkBtn.setOnClickListener {
            dismiss()
        }

        adjustRecyclerView()
    }

    private fun expandBottomSheet(bottomSheetView: View) {
        val behavior = BottomSheetBehavior.from(bottomSheetView)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun adjustRecyclerView() {
        // Assign adapter to RecyclerView
        carouselAdapter =
            CarouselRecyclerViewAdapter { uri, b -> addMarkFragmentViewModel.toggleItem(uri, b) }
        binding.photoCarousel.adapter = carouselAdapter

        // Add spaces between items of RecyclerView
        val itemDecoration = DividerItemDecoration(requireContext(), HORIZONTAL)
        itemDecoration.setDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.rv_divider
            )!!
        )
        binding.photoCarousel.addItemDecoration(itemDecoration)

        // Set observer to LiveData
        addMarkFragmentViewModel.liveData.observe(this, this)
    }

    @RequiresApi(Build.VERSION_CODES.O) // TODO: the same
    override fun onChanged(value: AddMarkFragmentData) {
        val isEmpty = value.stateList.isEmpty()
        toggleOnStateEmptinessChange(isEmpty)
        if (isEmpty) {
            if (binding.removePhotoBtn.visibility != GONE) {
                animateRemovePhotoButtonOut()
            }
        } else {
            if (binding.removePhotoBtn.visibility == GONE) {
                animateRemovePhotoButtonIn()
            }
            if (addMarkFragmentViewModel.isAnyTaken()) {
                enableRemovePhotoButton()
            } else {
                disableRemovePhotoButton()
            }
        }
        carouselAdapter.updateData(value.stateList)
    }

    private fun animateRemovePhotoButtonOut() {
        (binding.removePhotoBtn.layoutParams as LinearLayout.LayoutParams).weight = 1F
        val animatorSet = getAnimator()
        animatorSet.doOnEnd {
            binding.removePhotoBtn.visibility = GONE
        }
        animatorSet.start()
    }

    @RequiresApi(Build.VERSION_CODES.O) // TODO: eliminate this
    private fun animateRemovePhotoButtonIn() {
        binding.removePhotoBtn.visibility = VISIBLE
        (binding.removePhotoBtn.layoutParams as LinearLayout.LayoutParams).weight = 0F
        val animatorSet = getAnimator()
        animatorSet.reverse()
    }

    private fun getAnimator(): AnimatorSet {
        val weightValueAnimator = ValueAnimator.ofFloat(1F, 0F)
        weightValueAnimator.addUpdateListener {
            (binding.removePhotoBtn.layoutParams as LinearLayout.LayoutParams).weight =
                it.animatedValue as Float
            (binding.addPhotoBtn.layoutParams as LinearLayout.LayoutParams).weight =
                2F - (it.animatedValue as Float)
            binding.removePhotoBtn.requestLayout()
            binding.addPhotoBtn.requestLayout()
        }
        val marginStartAnimator = ValueAnimator.ofInt(
            requireContext().resources.getDimensionPixelSize(R.dimen.margin_between_btns),
            0
        )
        marginStartAnimator.addUpdateListener {
            val layoutParams = binding.removePhotoBtn.layoutParams as LinearLayout.LayoutParams
            layoutParams.marginStart = it.animatedValue as Int
            binding.removePhotoBtn.layoutParams = layoutParams
            binding.removePhotoBtn.requestLayout()
        }
        val animationSet = AnimatorSet()
        animationSet.duration = 600
        animationSet.playTogether(weightValueAnimator, marginStartAnimator)
        return animationSet
    }

    private fun enableRemovePhotoButton() {
        binding.removePhotoBtn.isEnabled = true
        binding.removePhotoBtn.iconTint =
            ContextCompat.getColorStateList(requireContext(), R.color.white)
        binding.removePhotoBtn.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.danger)
        binding.removePhotoBtn.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.white
            )
        )
    }

    private fun disableRemovePhotoButton() {
        binding.removePhotoBtn.isEnabled = false
        binding.removePhotoBtn.iconTint =
            ContextCompat.getColorStateList(requireContext(), R.color.danger)
        binding.removePhotoBtn.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.white)
        binding.removePhotoBtn.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.danger
            )
        )
    }

    private fun toggleOnStateEmptinessChange(isEmpty: Boolean) {
        if (isEmpty) {
            binding.cameraPlaceholder.visibility = VISIBLE
            binding.photoCarousel.visibility = GONE
        } else {
            binding.cameraPlaceholder.visibility = GONE
            binding.photoCarousel.visibility = VISIBLE
        }
    }

    companion object {
        const val TAG = "Add Mark Fragment"
    }
}